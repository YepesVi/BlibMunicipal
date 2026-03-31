# AGENTS.md

Operational guide for coding agents working in this repository.

## Repository Overview

- `MunicipalBack/`: Spring Boot backend (Java 21, Maven wrapper).
- `MunicipalFront/FrontMuni/`: Angular frontend (Angular 21, npm, SSR build config).
- No root monorepo task runner; run commands per subproject.

## Project Rule Files

- Cursor rules: not found (`.cursorrules` and `.cursor/rules/` are absent).
- Copilot rules: not found (`.github/copilot-instructions.md` is absent).
- If these files appear later, treat them as higher-priority project instructions.

## Environment and Secrets

- Backend expects DB/JWT/Cloudinary values from environment variables.
- `.env` is used locally by backend bootstrap; never commit secrets.
- Do not hardcode tokens, credentials, or connection strings.

## Backend Guide (Spring Boot)

Run all backend commands from `MunicipalBack/`.

### Build / Run / Test

- Compile:
  - `./mvnw clean compile`
- Run app:
  - `./mvnw spring-boot:run`
- Package jar:
  - `./mvnw clean package`
- Run all tests:
  - `./mvnw test`
- Run specific test class:
  - `./mvnw -Dtest=ReportServiceImplTest test`
- Run specific test method:
  - `./mvnw -Dtest=ReportServiceImplTest#getBooksByAuthorIdCardPdfReturnsPdfBytes test`
- Run several classes:
  - `./mvnw -Dtest=ReportControllerTest,ReportControllerSecurityTest test`
- Verify lifecycle:
  - `./mvnw clean verify`

Windows note: `./mvnw` works in Git Bash; `mvnw.cmd` works in cmd/PowerShell.

### Lint / Formatting

- No dedicated lint plugin is configured (no Checkstyle/Spotless/PMD in `pom.xml`).
- Minimum quality gate: `./mvnw test` (or `./mvnw verify`).
- Keep formatting consistent with current code style (4-space indentation, readable line wrapping).

### Backend Code Style

- **Architecture**
  - Keep feature packages (`auth`, `catalog`, `media`, `reports`, `users`, `shared`).
  - Respect layering: `controller -> service -> repository -> model`.
- **Dependency Injection**
  - Prefer constructor injection with Lombok `@RequiredArgsConstructor`.
  - Keep dependencies as `private final`.
- **DTOs and Validation**
  - Use request/response DTOs (`record` when practical).
  - Put Jakarta validation constraints in DTOs.
  - Use `@Valid` in controller request bodies.
- **Transactions**
  - Use `@Transactional(readOnly = true)` for query-focused services.
  - Mark write operations with explicit `@Transactional`.
- **Error Handling**
  - Throw domain exceptions (`ResourceNotFoundException`, `ConflictException`, etc.).
  - Let global exception handling map to API responses; avoid ad-hoc try/catch in controllers.
- **Persistence**
  - Keep JPA entities mutable and consistent with existing annotations.
  - Avoid eager fetches unless required for correctness.
- **Naming**
  - Classes/interfaces: PascalCase.
  - Methods/fields/local vars: camelCase.
  - Constants: UPPER_SNAKE_CASE.
- **Imports**
  - Prefer explicit imports, avoid wildcard imports.
  - Remove unused imports.
- **REST Conventions**
  - Keep plural resource paths and status codes consistent.
  - Use `PageResponse<T>` for paginated endpoints.

## Frontend Guide (Angular)

Run all frontend commands from `MunicipalFront/FrontMuni/`.

### Build / Run / Test

- Install dependencies:
  - `npm install`
- Start dev server:
  - `npm run start`
- Production build:
  - `npm run build`
- Watch build:
  - `npm run watch`
- Run all tests:
  - `npm run test -- --watch=false`
- Run one spec file:
  - `npm run test -- --watch=false --include src/app/features/reports/data-access/reports-api.service.spec.ts`
- Run multiple spec files:
  - `npm run test -- --watch=false --include src/app/path/a.spec.ts --include src/app/path/b.spec.ts`
- Optional test name filtering (runner-dependent):
  - `npm run test -- --watch=false --testNamePattern="should load data"`

### Lint / Formatting

- No `lint` script is currently defined in `package.json`.
- Prettier is available (`prettier` dependency, `.prettierrc`).
- Suggested checks:
  - `npx prettier --check "src/**/*.{ts,html,scss}"`
  - `npx prettier --write "src/**/*.{ts,html,scss}"`

### Frontend Code Style

- **Type Safety**
  - Respect strict TypeScript settings.
  - Avoid `any`; use explicit interfaces/types.
- **Project Structure**
  - Keep feature logic under `src/app/features/**`.
  - Keep cross-cutting core code in `src/app/core/**`.
  - Keep shared reusable utilities/UI in `src/app/shared/**`.
- **Angular Patterns**
  - Use standalone components and explicit `imports` arrays.
  - Keep data access in `data-access/*api.service.ts`.
  - Prefer signal-based state already used in pages where applicable.
- **Forms**
  - Use reactive forms for data entry flows.
  - Keep validators aligned with backend constraints.
  - Show field-level validation messages only when touched/invalid.
- **HTTP and Auth**
  - Centralize auth header behavior in `auth-interceptor`.
  - Centralize API error mapping in `http-error-interceptor`.
  - Avoid duplicating token refresh logic in page components.
- **UX Feedback**
  - Show success/error notifications after create/update/delete/report/login actions.
  - Keep user-facing error messages actionable and concise.
- **Styling**
  - Reuse theme variables in `styles.scss` and feature styles.
  - Prefer responsive layouts with simple breakpoints over hardcoded pixel-only layouts.
- **Naming**
  - Types/classes/components: PascalCase.
  - Variables/functions/properties: camelCase.
  - Files/folders: kebab-case.
- **Imports**
  - Group imports by source and remove unused imports.
  - Prefer named imports over namespace imports.

## Testing Expectations

- Add or update tests whenever behavior changes.
- Backend changes: run at least affected Maven tests.
- Frontend changes: run affected specs and a production build.
- If a command cannot run locally, report the blocker and exact command needed to unblock.

## Git and Change Hygiene

- Keep diffs focused; avoid unrelated refactors.
- Do not revert user-authored unrelated changes.
- Never commit secrets or environment files.
- Runtime logs (`.opencode-*.log`) should remain ignored.

## Quick Reference

- Backend run: `./mvnw spring-boot:run`
- Backend single test: `./mvnw -Dtest=ClassName#method test`
- Frontend run: `npm run start`
- Frontend single spec: `npm run test -- --watch=false --include src/path/to/file.spec.ts`
- Frontend build: `npm run build`
