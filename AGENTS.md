# AGENTS.md

Guidance for coding agents working in this repository.

## Repository Layout

- `MunicipalBack/`: Spring Boot backend (Java 21, Maven wrapper included).
- `MunicipalFront/FrontMuni/`: Angular frontend (Angular 21, npm).
- This repo currently has no root-level monorepo task runner; run commands per subproject.

## Rule Files

- Cursor rules: none found (`.cursorrules` and `.cursor/rules/` not present).
- Copilot rules: none found (`.github/copilot-instructions.md` not present).
- If these files are added later, follow them as higher-priority project rules.

## Environment and Secrets

- Backend reads DB/JWT/Cloudinary settings from env vars (see `MunicipalBack/src/main/resources/application.properties`).
- Backend bootstraps `.env` values in `MunicipalBack/src/main/java/com/Biblioteca/MunicipalBack/MunicipalBackApplication.java`.
- Never hardcode secrets in source files or tests.
- Do not commit `.env` or credential files.

## Backend (Spring Boot / Maven)

Run from `MunicipalBack/`.

### Build / Run / Test Commands

- Install/resolve and compile:
  - Unix: `./mvnw clean compile`
  - Windows: `mvnw.cmd clean compile`
- Run app locally:
  - Unix: `./mvnw spring-boot:run`
  - Windows: `mvnw.cmd spring-boot:run`
- Full package:
  - Unix: `./mvnw clean package`
  - Windows: `mvnw.cmd clean package`
- Run all tests:
  - Unix: `./mvnw test`
  - Windows: `mvnw.cmd test`
- Run a single test class:
  - Unix: `./mvnw -Dtest=MunicipalBackApplicationTests test`
  - Windows: `mvnw.cmd -Dtest=MunicipalBackApplicationTests test`
- Run a single test method:
  - Unix: `./mvnw -Dtest=MunicipalBackApplicationTests#contextLoads test`
  - Windows: `mvnw.cmd -Dtest=MunicipalBackApplicationTests#contextLoads test`
- Verify lifecycle (includes tests):
  - Unix: `./mvnw clean verify`
  - Windows: `mvnw.cmd clean verify`

### Lint / Formatting Status

- No dedicated Java lint/format plugin is configured in `pom.xml` (no Checkstyle/Spotless/PMD found).
- Treat `mvn test` (or `mvn verify`) as the minimum quality gate.
- Keep formatting consistent with existing code (4-space indentation, readable wrapping).

### Backend Code Style

- **Architecture**
  - Keep domain modules under feature packages (`auth`, `catalog`, `media`, `users`, `shared`).
  - Typical layering: `controller` -> `service` -> `repository` -> `model`.
- **Dependency Injection**
  - Prefer constructor injection via Lombok `@RequiredArgsConstructor`.
  - Keep injected fields `private final`.
- **DTOs**
  - Use Java `record` for request/response DTOs where possible.
  - Put validation annotations on request DTO fields.
- **Validation**
  - Use `@Valid` in controllers for request bodies.
  - Use Jakarta validation annotations (`@NotBlank`, `@Size`, `@Min`, etc.).
- **Transactions**
  - Class-level `@Transactional(readOnly = true)` in services; mark mutating methods with `@Transactional`.
- **Error Handling**
  - Throw domain exceptions (`ResourceNotFoundException`, `ConflictException`, etc.).
  - Let `GlobalExceptionHandler` produce `ApiErrorResponse`; avoid ad-hoc controller try/catch.
- **Normalization**
  - Trim inbound strings in services before persistence.
  - Convert optional blank fields to `null` when that is existing behavior.
- **Naming**
  - Classes/interfaces: PascalCase.
  - Methods/fields/locals: camelCase.
  - Constants: UPPER_SNAKE_CASE.
  - Packages: keep existing package structure conventions in this repo.
- **Persistence**
  - Entities are mutable classes with JPA annotations and Lombok getters/setters.
  - Keep table/column naming explicit where existing code does so.
- **Imports**
  - Prefer explicit imports over wildcard imports in new/edited code.
  - Remove unused imports.
- **REST Conventions**
  - Use plural resource paths and HTTP verbs consistently.
  - Use `@ResponseStatus` for non-default status codes.
  - Keep paginated endpoints returning `PageResponse<T>` when applicable.

## Frontend (Angular)

Run from `MunicipalFront/FrontMuni/`.

### Build / Run / Test Commands

- Install dependencies: `npm install`
- Dev server: `npm run start`
- Production build: `npm run build`
- Dev build watch: `npm run watch`
- Run all unit tests: `npm run test`
- Run a single spec file (Angular test builder forwards include pattern):
  - `npm run test -- --include src/app/services/usuario.spec.ts`
- Optional single test name filtering (if supported in local builder/version):
  - `npm run test -- --testNamePattern="should be created"`

### Lint / Formatting Status

- No `lint` script is defined in `package.json`.
- No ESLint config is present.
- Prettier is configured via `.prettierrc`; use it as the formatter source of truth.
- Suggested formatting check:
  - `npx prettier --check "src/**/*.{ts,html,scss}"`

### Frontend Code Style

- **TypeScript Strictness**
  - Project uses strict TS (`strict: true`, `noImplicitReturns`, etc. in `tsconfig.json`).
  - Avoid `any`; prefer precise interfaces/types/models.
- **Angular Style**
  - Standalone components are used (`imports` in `@Component` metadata).
  - Keep component/service/interceptor tests in `*.spec.ts`.
- **Naming**
  - Classes/types/interfaces: PascalCase.
  - Variables/functions/properties: camelCase.
  - File and folder names: kebab-case (existing convention).
- **Formatting**
  - 2-space indentation (`.editorconfig`).
  - Single quotes in TS (`.prettierrc` and `.editorconfig`).
  - Keep line width near Prettier `printWidth: 100`.
- **Imports**
  - Group imports by source; remove unused imports.
  - Prefer explicit named imports.
- **Error Handling**
  - Centralize HTTP error handling in interceptors when possible.
  - Avoid swallowing errors; return typed fallbacks or rethrow appropriately.
- **Scoping**
  - Keep feature code in module/feature folders under `src/app/modules`.
  - Keep shared logic in `src/app/services`, models in `src/app/models`, interceptors in `src/app/core/interceptors`.

## Agent Workflow Expectations

- Prefer minimal, focused diffs over broad refactors.
- Preserve public API behavior unless task explicitly requires API changes.
- Add/adjust tests when behavior changes.
- Before finishing:
  - Backend changes: run relevant Maven tests (at least targeted tests).
  - Frontend changes: run relevant Angular tests (at least targeted specs).
- If a requested command is unavailable (e.g., missing node_modules), report clearly and provide exact next command to unblock.

## Quick Command Reference

- Backend run: `./mvnw spring-boot:run`
- Backend all tests: `./mvnw test`
- Backend single test method: `./mvnw -Dtest=ClassName#method test`
- Frontend dev: `npm run start`
- Frontend all tests: `npm run test`
- Frontend single spec: `npm run test -- --include src/path/to/file.spec.ts`
