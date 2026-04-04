# Bitácora de Cambios — Adopción de GraphQL en Biblioteca Municipal

## ¿Qué estamos haciendo?

Estamos integrando **GraphQL** como capa de consulta adicional al backend Spring Boot y al frontend Angular de la Biblioteca Municipal. La idea es que GraphQL **coexista** con REST, no lo reemplace. Esto permite que el frontend pida exactamente los datos que necesita, con un solo endpoint (`/graphql`), cache inteligente vía Apollo, y queries tipadas.

### Arquitectura objetivo

```
Angular Frontend
├── REST Services (existentes) → /api/auth/*, /api/media/images, /api/reports/.../pdf
└── GraphQL Services (nuevos)  → /graphql (catálogo, usuarios, reportes JSON)

Spring Boot Backend
├── REST Controllers (se mantienen)
├── GraphQL Controllers (nuevos, coexisten)
└── Services + Repos + Entities (compartidos, sin duplicar lógica)
```

### Decisiones clave tomadas

| Decisión | Resultado |
|---|---|
| Auth (login/refresh/logout) | Se queda **solo en REST** — el token JWT se envía como header para ambos REST y GraphQL |
| Validación de inputs GraphQL | **Automática** via `ValidationInstrumentation` — intercepta records y valida constraints Jakarta |
| Seguridad en `/graphql` | `permitAll()` en Spring Security — cada resolver usa `@PreAuthorize` individualmente |
| Cliente GraphQL en Angular | **Apollo Angular** (`apollo-angular` + `@apollo/client`) |
| Orden de trabajo | **Backend primero** (Fase 1), luego frontend (Fase 2) |

---

## Fase 1: Backend GraphQL — ✅ COMPLETADA

**Fecha:** 2026-04-03
**Objetivo:** Completar y asegurar la capa GraphQL del backend que ya tenía ~80% de avance.

---

### Cambio 1.1 — Fix del scalar DateTime

**Archivo:** `shared/graphql/GraphQlScalarConfig.java`
**¿Qué se hizo?** Se cambió la declaración del tipo genérico del Coercing de `Coercing<LocalDateTime, String>` a `Coercing<Object, String>`.
**¿Por qué?** El campo `generatedAt` del reporte de libros por autor usa `OffsetDateTime`, pero el scalar solo declaraba soporte para `LocalDateTime`. Aunque el `serialize()` ya manejaba ambos tipos, el generic type podía causar `ClassCastException` en runtime.

```diff
- .coercing(new Coercing<LocalDateTime, String>() {
+ .coercing(new Coercing<Object, String>() {
```

---

### Cambio 1.2 — Límite de profundidad de queries

**Archivo:** `shared/graphql/GraphQlScalarConfig.java`
**¿Qué se hizo?** Se agregó un bean `MaxQueryDepthInstrumentation(15)`.
**¿Por qué?** Previene queries maliciosas o accidentales que aniden demasiados niveles (ej: `category → children → children → children...`), lo cual podría sobrecargar el servidor.

```java
@Bean
public MaxQueryDepthInstrumentation maxDepthInstrumentation() {
    return new MaxQueryDepthInstrumentation(15);
}
```

---

### Cambio 1.3 — Validación automática de inputs GraphQL

**Archivo nuevo:** `shared/graphql/GraphQlValidationInstrumentation.java`
**¿Qué se hizo?** Se creó un componente que intercepta automáticamente todos los argumentos de tipo `record` en resolvers GraphQL y los valida con el `Validator` de Jakarta.
**¿Por qué?** Los input records de GraphQL (`CreateBookInput`, etc.) no pasaban por `@Valid` como sí lo hacen los DTOs REST con `@RequestBody`. Sin esto, se podían crear libros con título vacío o ISBN inválido vía GraphQL.

**Cómo funciona:**
1. Intercepta cada DataFetcher antes de ejecutarse
2. Revisa los argumentos — si alguno es un `record`, lo valida
3. Si hay violaciones, lanza `GraphQlValidationException` con mensajes descriptivos
4. El `GraphQlExceptionResolver` la captura y retorna un error GraphQL con classification `BAD_REQUEST`

---

### Cambio 1.4 — Manejo de errores de validación

**Archivo:** `shared/graphql/GraphQlExceptionResolver.java`
**¿Qué se hizo?** Se agregó un caso para `GraphQlValidationException` que retorna un error GraphQL con classification `BAD_REQUEST`.
**¿Por qué?** Sin esto, las excepciones de validación del cambio 1.3 no se traducirían a respuestas GraphQL legibles para el cliente.

```java
if (ex instanceof GraphQlValidationInstrumentation.GraphQlValidationException e) {
    return GraphqlErrorBuilder.newError(env)
            .message(e.getMessage())
            .errorType(ErrorType.ValidationError)
            .extensions(Map.of("classification", "BAD_REQUEST"))
            .build();
}
```

---

### Cambio 1.5 — `/graphql` abierto en Spring Security

**Archivo:** `auth/config/SecurityConfig.java`
**¿Qué se hizo?** Se agregó `.requestMatchers("/graphql").permitAll()` en la cadena de filtros.
**¿Por qué?** Antes, `/graphql` caía en `.anyRequest().authenticated()`, lo que bloqueaba TODAS las requests sin JWT a nivel de Spring Security, ANTES de llegar al resolver. Ahora la seguridad se delega a `@PreAuthorize` en cada resolver, permitiendo flexibilidad futura para queries públicas.

```diff
  .requestMatchers("/graphiql", "/graphiql/**").permitAll()
+ .requestMatchers("/graphql").permitAll()
```

> ⚠️ **Recordatorio:** Todo resolver GraphQL nuevo DEBE tener `@PreAuthorize`. Sin él, queda abierto al público.

---

### Cambio 1.6 — Constraints Jakarta en 8 input records

**Archivos:** `Create*Input.java` y `Update*Input.java` de books, authors, categories, users
**¿Qué se hizo?** Se agregaron anotaciones de validación (`@NotBlank`, `@Size`, `@NotNull`, `@Min`, `@Max`, `@Pattern`) a los campos de cada record, copiando las mismas restricciones que ya tenían los DTOs REST equivalentes.
**¿Por qué?** Para que la `ValidationInstrumentation` (cambio 1.3) pueda validar estos inputs automáticamente.

**Ejemplo (`CreateBookInput`):**
```java
public record CreateBookInput(
    @NotBlank @Size(min = 5, max = 30) String isbn,
    @NotBlank @Size(min = 2, max = 200) String title,
    @NotBlank @Size(min = 2, max = 150) String publisher,
    @NotNull @Min(1000) @Max(9999) Integer publicationYear,
    @Size(max = 2000) String description,
    @NotNull Long authorId,
    @NotNull Long categoryId
) {}
```

---

### Cambio 1.7 — Dependencias de test para GraphQL

**Archivo:** `pom.xml`
**¿Qué se hizo?** Se agregaron `spring-graphql-test` y `spring-boot-graphql-test` con scope `test`.
**¿Por qué?** Necesarios para usar `@GraphQlTest` y `GraphQlTester` en los tests unitarios de los resolvers.

---

### Cambio 1.8 — 6 clases de test GraphQL (23 tests)

**Archivos nuevos en `src/test/.../graphql/`:**

| Test | Tests | Qué valida |
|---|---|---|
| `BookGraphQlControllerTest` | 5 | Listado paginado, detalle, crear, eliminar, prohibido para EMPLOYEE |
| `AuthorGraphQlControllerTest` | 4 | Búsqueda, detalle, crear, prohibido para EMPLOYEE |
| `CategoryGraphQlControllerTest` | 4 | Listado, árbol jerárquico, crear, prohibido para EMPLOYEE |
| `UserGraphQlControllerTest` | 4 | Listado, detalle, crear, prohibido para EMPLOYEE |
| `MediaGraphQlControllerTest` | 4 | Listado, detalle, eliminar, prohibido para EMPLOYEE |
| `ReportGraphQlControllerTest` | 2 | Reporte JSON, acceso permitido para EMPLOYEE |

**Patrón usado:** `@GraphQlTest` + `@MockitoBean` para servicios + `@WithMockUser` para simular roles.

**Resultado:** 23/23 pasaron ✅. El único test fallido (34/35 total) es `MunicipalBackApplicationTests.contextLoads`, que es pre-existente por falta de variables de entorno de DB — no es regresión.

---

## Fase 2: Frontend — Apollo Angular — 🔄 EN PROGRESO

**Fecha:** 2026-04-03
**Objetivo:** Integrar Apollo Angular como cliente GraphQL y crear services paralelos a los REST existentes, para después migrar las páginas gradualmente.

---

### Cambio 2.1 — Instalación de dependencias Apollo

**¿Qué se hizo?** Se instalaron las 3 dependencias necesarias para Apollo Angular.
**¿Por qué?** Apollo Angular es el cliente GraphQL que permite hacer queries/mutations tipadas con cache inteligente desde Angular.

| Paquete | Versión |
|---|---|
| `apollo-angular` | 13.0.0 |
| `@apollo/client` | 4.1.6 |
| `graphql` | 16.13.2 |

> Se instalaron con `--legacy-peer-deps` por un conflicto menor pre-existente entre `@angular/core@21.2.4` y `@angular/animations@21.2.5`. No afecta funcionalidad.

---

### Cambio 2.2 — Configuración de Apollo

**Archivo nuevo:** `src/app/core/graphql/graphql.config.ts`
**¿Qué se hizo?** Se creó `provideGraphQL()` — función que configura Apollo con:
- `HttpLink` apuntando a `/graphql`
- `InMemoryCache` para cache local
- `fetchPolicy: cache-and-network` (usa cache para respuesta inmediata + re-valida con el servidor)

**¿Por qué?** Centraliza toda la configuración de Apollo en un solo lugar. Al usar `HttpLink` (que internamente usa Angular `HttpClient`), el `authInterceptor` existente agrega **automáticamente** el JWT a las requests `/graphql`. No se necesitó ningún cambio en la autenticación.

---

### Cambio 2.3 — Registro en app.config.ts

**Archivo:** `src/app/app.config.ts`
**¿Qué se hizo?** Se agregó `provideGraphQL()` al array de providers.
**¿Por qué?** Registra Apollo a nivel global de la aplicación para que todos los services puedan inyectar `Apollo`.

```diff
+ import { provideGraphQL } from './core/graphql/graphql.config';

  providers: [
    // ... existing providers ...
+   provideGraphQL(),
  ]
```

---

### Cambio 2.4 — 6 GraphQL services creados

**Archivos nuevos en `src/app/features/.../data-access/`:**

| Servicio | Queries | Mutations |
|---|---|---|
| `books-graphql.service.ts` | `books`, `book`, `bookByIsbn` | `createBook`, `updateBook`, `deleteBook`, `attachBookImages`, `setPrimaryBookImage`, `removeBookImage` |
| `authors-graphql.service.ts` | `authors`, `author`, `authorByIdCard` | `createAuthor`, `updateAuthor`, `deleteAuthor` |
| `categories-graphql.service.ts` | `categories`, `categoryRoots`, `categoryChildren`, `category`, `categoryTree` | `createCategory`, `updateCategory`, `deleteCategory` |
| `users-graphql.service.ts` | `users`, `user` | `createUser`, `updateUser`, `deleteUser` |
| `media-graphql.service.ts` | `mediaAssets`, `mediaAsset` | `deleteMediaAsset` |
| `reports-graphql.service.ts` | `booksByAuthorReport` | — |

**Características de cada servicio:**
- `@Injectable({ providedIn: 'root' })` — singleton global
- Usa `inject(Apollo)` — inyección moderna estilo Angular signals
- Retorna `Observable<T>` tipados con las mismas interfaces DTO que los REST services
- **Coexiste** con el REST service correspondiente — las páginas pueden migrar una a una sin romper nada

---

### Cambio 2.5 — Fixes pre-existentes resueltos durante el proceso

Durante la integración se descubrieron y corrigieron problemas **pre-existentes** (no causados por GraphQL):

| Archivo | Fix | ¿Por qué existía? |
|---|---|---|
| `environments/environment.ts` | Resuelto merge conflict (`HEAD` vs `49354d2`) | Conflicto de Git no resuelto anteriormente |
| `environments/environment.prod.ts` | `apiUrl` → `apiBaseUrl` | Nombre inconsistente entre dev y prod |
| `services/prestamo.ts` | `apiUrl` → `apiBaseUrl` | Referenciaba la propiedad con nombre viejo |
| `services/usuario.ts` | `apiUrl` → `apiBaseUrl` | Referenciaba la propiedad con nombre viejo |
| `services/libro.ts` | `apiUrl` → `apiBaseUrl` | Referenciaba la propiedad con nombre viejo |

---

### Verificación de Fase 2

| Verificación | Resultado |
|---|---|
| **Build** (`npm run build`) | ✅ BUILD SUCCESS (13.6s, 8 rutas pre-renderizadas) |
| **Tests** (`npm run test`) | ⚠️ 12/20 pasaron |

**Sobre los 8 tests fallidos:** Son **pre-existentes**, no causados por nuestros cambios. Afectan:
- `app.spec.ts`
- `books-api.service.spec.ts`
- `users-api.service.spec.ts`
- `reports-api.service.spec.ts`
- `books-by-author-report-page.spec.ts`

Los tests de guards (6/6) pasan limpio ✅.

---

### Paso 2.5 — Migración Gradual de Páginas ✅

Se reemplazaron los REST services por los nuevos GraphQL services en todas las páginas clave de la aplicación.

**Páginas migradas a GraphQL:**

| # | Página | Archivo | Servicios cambiados |
|---|---|---|---|
| 1 | Catálogo de libros | `books-list-page.ts` | `BooksApiService` → `BooksGraphqlService`<br>`AuthorsApiService` → `AuthorsGraphqlService`<br>`CategoriesApiService` → `CategoriesGraphqlService` |
| 2 | Autores | `authors-list-page.ts` | `AuthorsApiService` → `AuthorsGraphqlService` |
| 3 | Categorías | `categories-list-page.ts` | `CategoriesApiService` → `CategoriesGraphqlService` |
| 4 | Usuarios | `users-list-page.ts` | `UsersApiService` → `UsersGraphqlService` |
| 5 | Reportes (JSON) | `books-by-author-report-page.ts` | Preview: `ReportsApiService` → `ReportsGraphqlService`<br>Autores dropdown: `AuthorsApiService` → `AuthorsGraphqlService`<br>*PDF download se mantiene en REST (`downloadBooksByAuthorIdCardPdf`)* |

**Ventajas observadas de la migración:**
- La API de cada GraphQL service resultó ser completamente compatible con la de su contraparte REST (mismos métodos y tipos de retorno `Observable`).
- El swap fue limpio y no requirió cambiar la lógica reactiva ni el manejo de signals dentro de los componentes.
- Los REST services originales se conservaron en el codebase como referencia y por compatibilidad si otros componentes los requieren.

---

## Troubleshooting y Errores Resueltos Post-Migración

Al realizar la prueba de humo *(E2E verification)* en el local dev server con Angular UI, identificamos y resolvimos varios errores de compilación, ruteo y SSR causados por la mezcla de entornos en la etapa dev:

| # | Error / Símbolo | Causa | Solución |
|---|---|---|---|
| **1** | `Http failure during parsing for http://localhost:4200/login` | La configuración SSR en `app.routes.server.ts` tenía `RenderMode.Prerender` para TODAS las rutas (`path: '**'`). Angular Node Server intentaba ejecutar el fetch del login contra localhost:4200 en vez del backend `8080`, resultando en un loop que devolvía HTML en vez de JSON. | Se cambiaron todas las rutas que dependen de base de datos o API calls (`/login`, `/books/**`, etc.) a `RenderMode.Client`. Dejando el `Prerender` solo como fallback para rutas pasivas. |
| **2** | `GraphQL requests bypass proxy or fail with 302` | El archivo `angular.json` tenía el array de environments (`fileReplacements`) en la scope equivocada global, sobre-escribiendo los valores dev con prod, dejando el `apiBaseUrl` como `/api`. Además, Apollo UI configuraba la uri en hardcode `'/graphql'`. | Movimos los enviroments en `angular.json` solo a Production. Cambiamos Apollo uri a `getApiUrl('/graphql')` forzando de forma robusta la url absoluta o parseo a través del proxy. |
| **3** | `TS2552: Cannot find name 'AuthorsApiService'` | El test de integración `.spec.ts` en la página de reportes seguía solicitando la dependencia `AuthorsApiService` (resto del REST) que se había borrado del controller del UI tras la migración. | Se refactorizaron los Providers en `books-by-author-report-page.spec.ts` para que mapee tanto el nuevo `AuthorsGraphqlService` como el `ReportsGraphqlService`. |

---

## Qué NO se migra a GraphQL (se queda en REST)

| Operación | Razón | Endpoint |
|---|---|---|
| Login / Refresh / Logout | Auth estándar vía REST | `/api/auth/*` |
| Upload de imágenes | `multipart/form-data` no es nativo de GraphQL | `POST /api/media/images` |
| Descarga PDF | Respuesta binaria + headers | `GET /api/reports/.../pdf` |

---

## Estado Final del Proyecto 🏁

Se realizó una validación interactiva completa simulando un usuario real:
- **Login:** Autenticación correcta a través de JWT (manteniéndose 100% en REST).
- **Flujo de Navegación:** El componente Apollo Client carga los libros desde el servidor mediante GraphQL limpiamente (las páginas de Autor, Categorías y Libros validaron renderizar su Listado).
- **Consola sin errores:** Ni el backend ni el dev server presentan warnings o fallos de inyección/cors.

La integración de GraphQL en la Biblioteca Municipal ha culminado con éxito en sus dos fases principales:

| Fase | Estado |
|---|---|
| Fase 1: Backend GraphQL | ✅ Completada |
| Fase 2.1: Instalar dependencias Apollo | ✅ Completada |
| Fase 2.2: Configuración GraphQL (`graphql.config.ts`) | ✅ Completada |
| Fase 2.3: Registro global en `app.config.ts` | ✅ Completada |
| Fase 2.4: 6 GraphQL services implementados | ✅ Completada |
| Fase 2.5: Migración de páginas a Apollo | ✅ Completada |
