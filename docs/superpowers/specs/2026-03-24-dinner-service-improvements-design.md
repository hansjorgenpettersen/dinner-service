# Dinner Service Improvements — Design Spec
Date: 2026-03-24

## Overview

Comprehensive improvement of the dinner-service app covering architecture migration, security, reliability, and code quality. The app is a Kotlin + Spring Boot + PostgreSQL dinner planning service used by a small test group.

The work is split into two sequential phases:
- **Phase 1:** Refactor the backend to a pure REST API, fix security and reliability issues
- **Phase 2:** Build a React SPA that replaces the current Thymeleaf frontend

---

## Architecture

**Current:** Thymeleaf server-rendered MVC, session-based auth, logic spread across 5 controllers.

**Target:**
```
┌─────────────────────┐        REST/JSON        ┌─────────────────────────┐
│   React SPA         │ ◄──────────────────────► │  Spring Boot API        │
│   (Vite + TS)       │     JWT in header        │  (@RestController only) │
│   served statically │                          │  PostgreSQL + JPA       │
└─────────────────────┘                          └─────────────────────────┘
```

The React SPA is built by Vite into `src/main/resources/static/` and served by Spring Boot as static files. No separate frontend server is needed in production.

---

## Phase 1: Backend

### 1.1 Authentication — Session → JWT

- Add `spring-boot-starter-security` and `jjwt` dependencies
- `POST /api/auth/login`: validate credentials, return a signed JWT (7-day expiry)
- `POST /api/auth/register`: create user, return JWT
- `POST /api/auth/logout`: client discards token — stateless, no server-side action
- A `JwtFilter` (`OncePerRequestFilter`) reads `Authorization: Bearer <token>`, validates it, and sets `SecurityContextHolder`
- All `session.getAttribute("email")` checks removed from controllers
- Spring Security configured with **stateless session policy**
- **CSRF protection is explicitly disabled** — the JWT-in-Authorization-header pattern is not vulnerable to CSRF because cookies are not used for auth; disabling it is required for the SPA to make POST requests without CSRF tokens
- Public routes (no JWT required): `/api/auth/**`, `/api/recipe-images/**`, `/` (SPA index)

**JWT secret:**
- Configurable via `app.jwt-secret` in `application-local.properties`
- Must be at least 256 bits (32 characters) for HS256 — the app must **fail to start** if this property is absent or too short
- No refresh token or token revocation in scope for this iteration — a stolen token remains valid until its 7-day expiry; accepted for test-group scope
- Token rotation: all existing tokens are invalidated when the secret changes (acceptable; users must log in again)

**Password reset flow:**
- Logic unchanged: generate a UUID token, store it in `password_reset_tokens` table with a 1-hour expiry, email a link to the user
- The link format is `https://<host>/reset-password?token=<uuid>` — this is a client-side React route
- React reads the token from the URL and calls `POST /api/auth/reset-password`
- Error responses for invalid/expired tokens: `{ "error": "TOKEN_INVALID" }` (400) or `{ "error": "TOKEN_EXPIRED" }` (400)

### 1.2 Controller Refactor

- All controllers annotated with `@RestController`, routes prefixed with `/api`
- Return `ResponseEntity<T>` or data classes; no `Model`, no Thymeleaf template names
- Request bodies annotated with `@Valid`; field-level constraints added to request data classes

**Full route table:**

| Method | Path | Description |
|---|---|---|
| POST | `/api/auth/login` | Login, returns JWT |
| POST | `/api/auth/register` | Register, returns JWT |
| POST | `/api/auth/logout` | No-op (client discards token) |
| POST | `/api/auth/forgot-password` | Send reset email |
| POST | `/api/auth/reset-password` | Reset password with token |
| GET | `/api/recipes` | List all recipes |
| POST | `/api/recipes` | Create recipe |
| GET | `/api/recipes/{id}` | Get recipe detail |
| PUT | `/api/recipes/{id}` | Edit recipe name/description |
| DELETE | `/api/recipes/{id}` | Delete recipe |
| POST | `/api/recipes/{id}/ingredients` | Add ingredient to recipe |
| DELETE | `/api/recipes/{id}/ingredients/{ingId}` | Remove ingredient from recipe |
| POST | `/api/recipes/{id}/ingredients/{ingId}/add-to-list` | Add ingredient to shopping list |
| POST | `/api/recipes/{id}/ingredients/{ingId}/remove-from-list` | Remove ingredient from shopping list |
| POST | `/api/recipes/{id}/images` | Upload image(s) |
| DELETE | `/api/recipes/{id}/images/{imageId}` | Delete image |
| GET | `/api/recipe-images/{filename}` | Serve image file (public) |
| POST | `/api/recipes/{id}/select-list` | Set active shopping list for recipe view |
| GET | `/api/shopping-lists` | List owned + shared lists |
| POST | `/api/shopping-lists` | Create shopping list |
| GET | `/api/shopping-lists/{id}` | Get list with items |
| POST | `/api/shopping-lists/{id}/items` | Add item |
| POST | `/api/shopping-lists/{id}/items/{itemId}/toggle` | Toggle checked |
| DELETE | `/api/shopping-lists/{id}/items/{itemId}` | Delete item |
| POST | `/api/shopping-lists/{id}/items/clear-checked` | Delete all checked items |
| POST | `/api/shopping-lists/{id}/share` | Share list with user by email |
| GET | `/api/products` | List all products |
| POST | `/api/products` | Create product |
| GET | `/api/products/search?q=` | Autocomplete search (public within auth) |
| PUT | `/api/products/{id}` | Edit product |
| DELETE | `/api/products/{id}` | Delete product |
| GET | `/api/products/uncategorized` | List uncategorized products |
| GET | `/api/categories` | List all categories |
| POST | `/api/categories` | Create category |
| DELETE | `/api/categories/{id}` | Delete category |
| GET | `/api/user` | Get current user profile |
| POST | `/api/user/set-default-list` | Set default shopping list |
| POST | `/api/user/leave-list/{id}` | Leave a shared list |

### 1.3 Shared CurrentUserService

- Extract the duplicated `currentUser()` helper (copy-pasted in `RecipeController`, `ShoppingListController`, `UserController`) into a single `CurrentUserService` bean
- Reads the authenticated principal from `SecurityContextHolder` — no repository call needed per request

### 1.4 Remove Legacy Endpoints

- Delete `HomeController` entirely (`/greet`, `/guests`, `/random`)
- Remove the `Guest` entity and `GuestRepository`

### 1.5 Reliability Fixes

| Issue | Fix |
|---|---|
| `ddl-auto=update` in production | Change to `ddl-auto=validate`; schema managed by Flyway |
| `show-sql=true` | Set to `false` |
| `deploy.sh` uses `mvn spring-boot:run` | Build fat JAR with `mvn package -DskipTests`, run with `java -jar dinner-service.jar` |

**Flyway migration strategy:**
- Add `flyway-core` dependency
- V1 migration script generated from the current schema via `pg_dump --schema-only` — must match the existing tables exactly
- For the existing production database: set `flyway.baseline-on-migrate=true` and `flyway.baseline-version=1` so Flyway treats the current schema as the baseline without re-running V1

### 1.6 Product Autocomplete

`RecipeController.view()` currently loads all products into the model for the ingredient dropdown. After the refactor, the React ingredient form uses `GET /api/products/search?q=` for live autocomplete instead of loading the full product list on page load.

### 1.7 Global Exception Handler

- Add a `@RestControllerAdvice` (not `@ControllerAdvice`) global exception handler
- Returns consistent JSON error responses:
  ```json
  { "error": "NOT_FOUND", "message": "Recipe not found" }
  ```
- Handles: `EntityNotFoundException` (404), `AccessDeniedException` (403), `@Valid` constraint violations (400 with field errors), and unhandled exceptions (500)

### 1.8 CORS Configuration

- A `CorsConfigurationSource` bean allows `http://localhost:5173` (Vite dev server) during development
- In production, CORS is restricted to same-origin (SPA is served from Spring Boot itself)
- Configure via `app.cors.allowed-origins` property so dev vs. prod can differ without code changes

---

## Phase 2: React SPA

### 2.1 Project Setup

- Location: `frontend/` at the repo root
- Stack: Vite + React 18 + TypeScript
- `axios` for API calls, with an interceptor that:
  - Attaches `Authorization: Bearer <token>` to every request
  - Redirects to `/login` on 401 responses
- React Router v6 for client-side navigation
- JWT stored in `localStorage` — **deliberate tradeoff accepted for test-group scope**; acknowledged risk: XSS on any page could expose the token. Mitigation: React must never use `dangerouslySetInnerHTML` with user-supplied data (recipe names, product names, descriptions, etc.)
- No UI component framework — plain CSS to preserve existing visual style
- Maven frontend plugin runs `npm run build` as part of `mvn package`, output to `src/main/resources/static/`

**SPA catch-all routing:**
Spring Boot serves `index.html` for all non-API routes via a dedicated controller:
```kotlin
@GetMapping("/{path:^(?!api|recipe-images).*}/**")
fun spa(): String = "forward:/index.html"
```
This pattern explicitly excludes `/api/**` and `/recipe-images/**` to avoid conflicts with REST endpoints.

### 2.2 Auth

- `AuthContext` provides `currentUser`, `login()`, `logout()` to the whole app
- Protected route wrapper redirects to `/login` if no token present
- On login: store JWT in `localStorage`, decode payload to get user email for display

### 2.3 Pages

| Page | Route | Notes |
|---|---|---|
| Login | `/login` | `POST /api/auth/login` |
| Register | `/register` | `POST /api/auth/register` |
| Forgot Password | `/forgot-password` | `POST /api/auth/forgot-password` |
| Reset Password | `/reset-password?token=` | `POST /api/auth/reset-password` |
| Dashboard | `/dashboard` | Quick links to lists and recipes |
| Recipes | `/recipes` | List with thumbnails, create new |
| Recipe Detail | `/recipes/:id` | Ingredients, images, add-to-list with selected list |
| Shopping Lists | `/shopping-lists` | Owned + shared lists |
| Shopping List Detail | `/shopping-lists/:id` | Items grouped by category, toggle, clear, share |
| Products | `/products` | Product catalog + category management |
| Uncategorized Products | `/products/uncategorized` | Quick category assignment |
| User Settings | `/user` | Default list, leave shared lists |

### 2.4 Data Flow

- API responses are typed with TypeScript interfaces mirroring the backend data classes
- No global state library — React Query for server state, React Context only for auth
- Optimistic updates for toggle-checked on shopping list items (feels instant on mobile)

### 2.5 Input Validation

- Frontend validates required fields and obvious constraints before submitting
- Backend is authoritative — frontend validation is UX only, not a security boundary

---

## What Is Not Changing

- Database schema (beyond Flyway managing it)
- BCrypt password hashing
- Email-based password reset logic
- Recipe image upload and serving
- Shopping list sharing model (owner + sharedWith)
- Recipe visibility: global (all users see all recipes) — kept for test-group scope
- Deployment target (Linux server, same machine)

---

## Testing Strategy

- **Backend:** JUnit 5 + Spring Boot Test for controller integration tests against a real PostgreSQL instance via **Testcontainers** (not H2 — PostgreSQL dialect differences make H2 unreliable for catching real bugs)
- **Frontend:** Vitest + React Testing Library for component and auth flow tests
- No end-to-end tests in scope for this iteration

---

## Configuration Properties

| Property | Required | Description |
|---|---|---|
| `app.jwt-secret` | Yes (min 32 chars) | JWT signing secret; app fails to start if absent/short |
| `app.upload-dir` | Yes | Directory for recipe image uploads |
| `app.cors.allowed-origins` | No | Comma-separated origins for CORS (dev only) |
| `flyway.baseline-on-migrate` | Yes (existing DB) | Set to `true` for first Flyway run on existing database |
