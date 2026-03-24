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

The React SPA is built by Vite into `src/main/resources/static/` and served by Spring Boot as static files. No separate frontend server is needed.

---

## Phase 1: Backend

### 1.1 Authentication — Session → JWT

- Add `spring-boot-starter-security` and `jjwt` dependencies
- `POST /api/auth/login`: validate credentials, return a signed JWT (7-day expiry)
- `POST /api/auth/register`: create user, return JWT
- `POST /api/auth/logout`: client discards token (stateless — no server-side action needed)
- Password reset flow unchanged in logic; responses changed from HTTP redirects to JSON
- A `JwtFilter` (`OncePerRequestFilter`) reads `Authorization: Bearer <token>`, validates it, and sets `SecurityContextHolder`
- All `session.getAttribute("email")` checks removed from controllers
- Spring Security configured with a stateless session policy; public routes: `/api/auth/**`, `/api/recipe-images/**`

### 1.2 Controller Refactor

- All controllers annotated with `@RestController`, routes prefixed with `/api`
- Return `ResponseEntity<T>` or data classes; no `Model`, no Thymeleaf template names
- Full route map:

| Resource | Endpoints |
|---|---|
| Auth | `POST /api/auth/login`, `/register`, `/logout`, `/forgot-password`, `/reset-password` |
| Recipes | `GET/POST /api/recipes`, `GET/POST/DELETE /api/recipes/{id}`, ingredients CRUD, images CRUD, add/remove ingredient to list |
| Shopping Lists | `GET/POST /api/shopping-lists`, `GET /api/shopping-lists/{id}`, items CRUD, toggle, clear-checked, share |
| Products | `GET/POST /api/products`, `GET /api/products/search`, `PUT/DELETE /api/products/{id}`, categories CRUD |
| User | `GET /api/user`, `POST /api/user/set-default-list`, `POST /api/user/leave-list/{id}` |

### 1.3 Shared CurrentUserService

- Extract the duplicated `currentUser()` helper (currently copy-pasted in `RecipeController`, `ShoppingListController`, `UserController`) into a single `CurrentUserService` bean
- Reads the authenticated principal from `SecurityContextHolder` — no repository call needed per request

### 1.4 Remove Legacy Endpoints

- Delete `HomeController` entirely (`/greet`, `/guests`, `/random`)
- Remove the legacy `Guest` entity and `GuestRepository`

### 1.5 Reliability Fixes

| Issue | Fix |
|---|---|
| `ddl-auto=update` in production | Change to `ddl-auto=validate`; add Flyway for schema migrations |
| `show-sql=true` | Set to `false` |
| `deploy.sh` uses `mvn spring-boot:run` | Build fat JAR with `mvn package -DskipTests`, run with `java -jar dinner-service.jar` |
| No Flyway | Add `flyway-core` dependency + initial migration from current schema |

### 1.6 Product Autocomplete

- `RecipeController.view()` currently loads all products into the model for the ingredient dropdown
- After refactor: the ingredient form uses the existing `GET /api/products/search?q=` endpoint for autocomplete instead of a full product list

### 1.7 Error Handling

- Add a `@ControllerAdvice` global exception handler returning consistent JSON error responses:
  ```json
  { "error": "NOT_FOUND", "message": "Recipe not found" }
  ```
- Handle `EntityNotFoundException`, `AccessDeniedException`, and validation errors uniformly

---

## Phase 2: React SPA

### 2.1 Project Setup

- Location: `frontend/` at the repo root
- Stack: Vite + React 18 + TypeScript
- `axios` for API calls, with an interceptor that:
  - Attaches `Authorization: Bearer <token>` to every request
  - Redirects to `/login` on 401 responses
- React Router v6 for client-side navigation
- JWT stored in `localStorage`
- No UI component framework — plain CSS to preserve existing visual style
- Maven frontend plugin runs `npm run build` as part of `mvn package`, output to `src/main/resources/static/`
- Spring Boot `GET /**` catch-all serves `index.html` for React Router

### 2.2 Auth

- `AuthContext` provides `currentUser`, `login()`, `logout()` to the whole app
- Protected route wrapper redirects to `/login` if no token present
- On login: store JWT in `localStorage`, decode to get user email for display

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
- No global state library — React Query (or plain `useEffect`) for server state, React Context only for auth
- Optimistic updates for toggle-checked on shopping list items (feels instant on mobile)

---

## What Is Not Changing

- Database schema (beyond Flyway managing it)
- BCrypt password hashing
- Email-based password reset logic
- Recipe image upload and serving
- Shopping list sharing model (owner + sharedWith)
- Deployment target (Linux server, same machine)

---

## Testing Strategy

- **Backend:** JUnit 5 + Spring Boot Test for controller integration tests (real DB via Testcontainers or H2)
- **Frontend:** Vitest + React Testing Library for component and auth flow tests
- No end-to-end tests in scope for this iteration

---

## Open Questions

- Should the JWT secret be configurable via `application-local.properties`? (Yes — add `app.jwt-secret` property)
- Should recipe visibility be per-user or remain global (all users see all recipes)? (Keep global for now — small test group)
