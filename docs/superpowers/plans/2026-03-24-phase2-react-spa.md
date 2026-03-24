# Phase 2: React SPA Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a React SPA that replaces all Thymeleaf-rendered pages, communicating with the Phase 1 REST API using JWT authentication.

**Architecture:** Vite + React 18 + TypeScript in a `frontend/` directory. Axios handles API calls with a JWT interceptor. React Router v6 for navigation. JWT stored in `localStorage`. Maven frontend plugin builds the SPA into `src/main/resources/static/` so Spring Boot serves it as static files. A catch-all Spring MVC controller forwards non-API routes to `index.html`.

**Tech Stack:** Vite 5, React 18, TypeScript 5, React Router v6, Axios, React Query v5, Vitest + React Testing Library

**Spec:** `docs/superpowers/specs/2026-03-24-dinner-service-improvements-design.md`

**Prerequisite:** Phase 1 plan (`docs/superpowers/plans/2026-03-24-phase1-backend-rest-api.md`) must be complete and all tests passing.

---

## File Map

**New files — frontend:**
- `frontend/package.json`
- `frontend/vite.config.ts`
- `frontend/tsconfig.json`
- `frontend/tsconfig.node.json`
- `frontend/index.html`
- `frontend/src/main.tsx`
- `frontend/src/App.tsx`
- `frontend/src/api/client.ts` — axios instance + JWT interceptor
- `frontend/src/api/types.ts` — TypeScript interfaces for all API responses
- `frontend/src/api/auth.ts`
- `frontend/src/api/recipes.ts`
- `frontend/src/api/shoppingLists.ts`
- `frontend/src/api/products.ts`
- `frontend/src/api/user.ts`
- `frontend/src/context/AuthContext.tsx`
- `frontend/src/components/ProtectedRoute.tsx`
- `frontend/src/components/Layout.tsx`
- `frontend/src/pages/LoginPage.tsx`
- `frontend/src/pages/RegisterPage.tsx`
- `frontend/src/pages/ForgotPasswordPage.tsx`
- `frontend/src/pages/ResetPasswordPage.tsx`
- `frontend/src/pages/DashboardPage.tsx`
- `frontend/src/pages/RecipesPage.tsx`
- `frontend/src/pages/RecipeDetailPage.tsx`
- `frontend/src/pages/ShoppingListsPage.tsx`
- `frontend/src/pages/ShoppingListDetailPage.tsx`
- `frontend/src/pages/ProductsPage.tsx`
- `frontend/src/pages/UncategorizedProductsPage.tsx`
- `frontend/src/pages/UserPage.tsx`
- `frontend/src/test/setup.ts`
- `frontend/src/test/AuthContext.test.tsx`
- `frontend/src/test/LoginPage.test.tsx`

**New files — backend:**
- `src/main/kotlin/com/example/dinnerservice/SpaController.kt` — catch-all for React Router

**Modified files:**
- `pom.xml` — add frontend-maven-plugin to run `npm run build` during `mvn package`

---

### Task 1: Scaffold the frontend project

**Files:**
- Create: `frontend/package.json`
- Create: `frontend/vite.config.ts`
- Create: `frontend/tsconfig.json`
- Create: `frontend/index.html`
- Create: `frontend/src/main.tsx`

- [ ] **Step 1: Create frontend directory and package.json**

```bash
mkdir frontend
```

Create `frontend/package.json`:

```json
{
  "name": "dinner-service-spa",
  "private": true,
  "version": "0.0.1",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "tsc && vite build",
    "test": "vitest run",
    "test:watch": "vitest",
    "preview": "vite preview"
  },
  "dependencies": {
    "axios": "^1.7.2",
    "react": "^18.3.1",
    "react-dom": "^18.3.1",
    "react-router-dom": "^6.24.0",
    "@tanstack/react-query": "^5.50.0"
  },
  "devDependencies": {
    "@testing-library/jest-dom": "^6.4.6",
    "@testing-library/react": "^16.0.0",
    "@testing-library/user-event": "^14.5.2",
    "@types/react": "^18.3.3",
    "@types/react-dom": "^18.3.0",
    "@vitejs/plugin-react": "^4.3.1",
    "jsdom": "^24.1.0",
    "typescript": "^5.5.3",
    "vite": "^5.3.4",
    "vitest": "^2.0.4"
  }
}
```

- [ ] **Step 2: Create vite.config.ts**

```typescript
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      // Both /api and /api/recipe-images are covered by this single entry
      '/api': 'http://localhost:8090'
    }
  },
  build: {
    outDir: '../src/main/resources/static',
    emptyOutDir: true
  },
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: ['./src/test/setup.ts']
  }
})
```

- [ ] **Step 3: Create tsconfig.json**

```json
{
  "compilerOptions": {
    "target": "ES2020",
    "useDefineForClassFields": true,
    "lib": ["ES2020", "DOM", "DOM.Iterable"],
    "module": "ESNext",
    "skipLibCheck": true,
    "moduleResolution": "bundler",
    "allowImportingTsExtensions": true,
    "resolveJsonModule": true,
    "isolatedModules": true,
    "noEmit": true,
    "jsx": "react-jsx",
    "strict": true,
    "noUnusedLocals": true,
    "noUnusedParameters": true,
    "noFallthroughCasesInSwitch": true,
    // Provides global types for test/expect/vi so tsc --noEmit passes on test files
    "types": ["vitest/globals", "@testing-library/jest-dom"]
  },
  "include": ["src"],
  "references": [{ "path": "./tsconfig.node.json" }]
}
```

Create `frontend/tsconfig.node.json`:
```json
{
  "compilerOptions": {
    "composite": true,
    "skipLibCheck": true,
    "module": "ESNext",
    "moduleResolution": "bundler",
    "allowSyntheticDefaultImports": true
  },
  "include": ["vite.config.ts"]
}
```

- [ ] **Step 4: Create index.html**

```html
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Dinner Service</title>
  </head>
  <body>
    <div id="root"></div>
    <script type="module" src="/src/main.tsx"></script>
  </body>
</html>
```

- [ ] **Step 5: Create src/main.tsx (minimal)**

```tsx
import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App'

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
)
```

- [ ] **Step 6: Create a placeholder App.tsx**

```tsx
export default function App() {
  return <div>Dinner Service</div>
}
```

- [ ] **Step 7: Install dependencies**

```bash
cd frontend && npm install
```

- [ ] **Step 8: Verify the dev server starts**

```bash
cd frontend && npm run dev &
sleep 3
curl -s http://localhost:5173 | grep -c "Dinner Service"
# Expected: 1
pkill -f "vite"
```

- [ ] **Step 9: Commit**

```bash
cd ..
git add frontend/
git commit -m "feat: scaffold React + Vite + TypeScript frontend"
```

---

### Task 2: API client and TypeScript types

**Files:**
- Create: `frontend/src/api/client.ts`
- Create: `frontend/src/api/types.ts`
- Create: `frontend/src/api/auth.ts`
- Create: `frontend/src/api/recipes.ts`
- Create: `frontend/src/api/shoppingLists.ts`
- Create: `frontend/src/api/products.ts`
- Create: `frontend/src/api/user.ts`

- [ ] **Step 1: Create the axios client with JWT interceptor**

Create `frontend/src/api/client.ts`:

```typescript
import axios from 'axios'

export const apiClient = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' }
})

// Attach JWT to every request
apiClient.interceptors.request.use(config => {
  const token = localStorage.getItem('jwt')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Redirect to login on 401
apiClient.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('jwt')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)
```

- [ ] **Step 2: Create TypeScript interfaces (mirrors backend DTOs)**

Create `frontend/src/api/types.ts`:

```typescript
// Auth
export interface AuthResponse { token: string; email: string }
export interface LoginRequest { email: string; password: string }
export interface RegisterRequest { email: string; password: string; confirmPassword: string }
export interface ForgotPasswordRequest { email: string }
export interface ResetPasswordRequest { token: string; password: string; confirmPassword: string }

// Recipes
export interface RecipeSummary { id: number; name: string; description: string; previewImage: string | null }
export interface Ingredient { id: number; productId: number | null; productName: string | null; quantity: number | null; unit: string }
export interface RecipeImage { id: number; filename: string; originalName: string }
export interface ShoppingListSummary { id: number; name: string; ownerEmail: string | null }
export interface RecipeDetail {
  id: number; name: string; description: string
  ingredients: Ingredient[]
  images: RecipeImage[]
  shoppingLists: ShoppingListSummary[]
  selectedListId: number | null
  ingredientCounts: Record<number, number>
}

// Shopping Lists
export interface ShoppingListItem {
  id: number; name: string; count: number | null; unitPrice: number | null; totalPrice: number | null
  checked: boolean; categoryId: number | null; categoryName: string | null; categoryColor: string | null
  addedByEmail: string | null
}
export interface ShoppingListDetail {
  id: number; name: string; items: ShoppingListItem[]; totalPrice: number; isOwner: boolean
}
export interface ShoppingListsResponse { owned: ShoppingListSummary[]; shared: ShoppingListSummary[] }

// Products
export interface Product {
  id: number; name: string; price: number | null
  categoryId: number | null; categoryName: string | null; categoryColor: string | null
}
export interface Category { id: number; name: string; color: string }

// User
export interface UserProfile {
  email: string; defaultListId: number | null
  allLists: ShoppingListSummary[]; sharedLists: ShoppingListSummary[]
}

// Errors
export interface ApiError { error: string; message: string }
```

- [ ] **Step 3: Create auth API functions**

Create `frontend/src/api/auth.ts`:

```typescript
import { apiClient } from './client'
import type { AuthResponse, LoginRequest, RegisterRequest, ForgotPasswordRequest, ResetPasswordRequest } from './types'

export const login = (data: LoginRequest) =>
  apiClient.post<AuthResponse>('/auth/login', data).then(r => r.data)

export const register = (data: RegisterRequest) =>
  apiClient.post<AuthResponse>('/auth/register', data).then(r => r.data)

export const logout = () =>
  apiClient.post('/auth/logout').then(() => {})

export const forgotPassword = (data: ForgotPasswordRequest) =>
  apiClient.post('/auth/forgot-password', data).then(r => r.data)

export const resetPassword = (data: ResetPasswordRequest) =>
  apiClient.post('/auth/reset-password', data).then(r => r.data)
```

- [ ] **Step 4: Create recipes API functions**

Create `frontend/src/api/recipes.ts`:

```typescript
import { apiClient } from './client'
import type { RecipeSummary, RecipeDetail, Ingredient } from './types'

export const getRecipes = () =>
  apiClient.get<RecipeSummary[]>('/recipes').then(r => r.data)

export const getRecipe = (id: number, selectedListId?: number) =>
  apiClient.get<RecipeDetail>(`/recipes/${id}`, {
    params: selectedListId ? { selectedListId } : {}
  }).then(r => r.data)

export const createRecipe = (data: { name: string; description?: string }) =>
  apiClient.post<RecipeSummary>('/recipes', data).then(r => r.data)

export const updateRecipe = (id: number, data: { name: string; description?: string }) =>
  apiClient.put<RecipeSummary>(`/recipes/${id}`, data).then(r => r.data)

export const deleteRecipe = (id: number) =>
  apiClient.delete(`/recipes/${id}`)

export const addIngredient = (recipeId: number, data: { productId: number; quantity?: number; unit: string }) =>
  apiClient.post<Ingredient>(`/recipes/${recipeId}/ingredients`, data).then(r => r.data)

export const deleteIngredient = (recipeId: number, ingId: number) =>
  apiClient.delete(`/recipes/${recipeId}/ingredients/${ingId}`)

export const addIngredientToList = (recipeId: number, ingId: number, listId: number) =>
  apiClient.post(`/recipes/${recipeId}/ingredients/${ingId}/add-to-list`, null, { params: { listId } })

export const removeIngredientFromList = (recipeId: number, ingId: number, listId: number) =>
  apiClient.post(`/recipes/${recipeId}/ingredients/${ingId}/remove-from-list`, null, { params: { listId } })

export const uploadImages = (recipeId: number, files: FileList) => {
  const form = new FormData()
  Array.from(files).forEach(f => form.append('files', f))
  return apiClient.post(`/recipes/${recipeId}/images`, form, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export const deleteImage = (recipeId: number, imageId: number) =>
  apiClient.delete(`/recipes/${recipeId}/images/${imageId}`)
```

- [ ] **Step 5: Create shopping lists API functions**

Create `frontend/src/api/shoppingLists.ts`:

```typescript
import { apiClient } from './client'
import type { ShoppingListsResponse, ShoppingListDetail, ShoppingListSummary, ShoppingListItem } from './types'

export const getShoppingLists = () =>
  apiClient.get<ShoppingListsResponse>('/shopping-lists').then(r => r.data)

export const getShoppingList = (id: number) =>
  apiClient.get<ShoppingListDetail>(`/shopping-lists/${id}`).then(r => r.data)

export const createShoppingList = (name: string) =>
  apiClient.post<ShoppingListSummary>('/shopping-lists', { name }).then(r => r.data)

export const addItem = (listId: number, data: { name: string; count?: number; unitPrice?: number }) =>
  apiClient.post<ShoppingListItem>(`/shopping-lists/${listId}/items`, data).then(r => r.data)

export const toggleItem = (listId: number, itemId: number) =>
  apiClient.post<ShoppingListItem>(`/shopping-lists/${listId}/items/${itemId}/toggle`).then(r => r.data)

export const deleteItem = (listId: number, itemId: number) =>
  apiClient.delete(`/shopping-lists/${listId}/items/${itemId}`)

export const clearChecked = (listId: number) =>
  apiClient.post(`/shopping-lists/${listId}/items/clear-checked`)

export const shareList = (listId: number, email: string) =>
  apiClient.post(`/shopping-lists/${listId}/share`, { email })
```

- [ ] **Step 6: Create products and user API functions**

Create `frontend/src/api/products.ts`:

```typescript
import { apiClient } from './client'
import type { Product, Category } from './types'

export const getProducts = () =>
  apiClient.get<Product[]>('/products').then(r => r.data)

export const searchProducts = (q: string) =>
  apiClient.get<Product[]>('/products/search', { params: { q } }).then(r => r.data)

export const getUncategorizedProducts = () =>
  apiClient.get<Product[]>('/products/uncategorized').then(r => r.data)

export const createProduct = (data: { name: string; price?: number; categoryId?: number }) =>
  apiClient.post<Product>('/products', data).then(r => r.data)

export const updateProduct = (id: number, data: { name: string; price?: number; categoryId?: number }) =>
  apiClient.put<Product>(`/products/${id}`, data).then(r => r.data)

export const deleteProduct = (id: number) =>
  apiClient.delete(`/products/${id}`)

export const getCategories = () =>
  apiClient.get<Category[]>('/categories').then(r => r.data)

export const createCategory = (data: { name: string; color?: string }) =>
  apiClient.post<Category>('/categories', data).then(r => r.data)

export const deleteCategory = (id: number) =>
  apiClient.delete(`/categories/${id}`)
```

Create `frontend/src/api/user.ts`:

```typescript
import { apiClient } from './client'
import type { UserProfile } from './types'

export const getProfile = () =>
  apiClient.get<UserProfile>('/user').then(r => r.data)

export const setDefaultList = (listId: number | null) =>
  apiClient.post<UserProfile>('/user/set-default-list', { listId }).then(r => r.data)

export const leaveList = (listId: number) =>
  apiClient.post(`/user/leave-list/${listId}`)
```

- [ ] **Step 7: Verify TypeScript compiles**

```bash
cd frontend && npx tsc --noEmit
```
Expected: no errors

- [ ] **Step 8: Commit**

```bash
cd ..
git add frontend/src/api/
git commit -m "feat: add API client, TypeScript types, and all API functions"
```

---

### Task 3: Auth context and protected routes

**Files:**
- Create: `frontend/src/context/AuthContext.tsx`
- Create: `frontend/src/components/ProtectedRoute.tsx`
- Create: `frontend/src/test/setup.ts`
- Create: `frontend/src/test/AuthContext.test.tsx`

- [ ] **Step 1: Write the failing test**

Create `frontend/src/test/setup.ts`:

```typescript
import '@testing-library/jest-dom'
```

Create `frontend/src/test/AuthContext.test.tsx`:

```tsx
import { render, screen, act } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { AuthProvider, useAuth } from '../context/AuthContext'
import { vi } from 'vitest'

// Stub the api module
vi.mock('../api/auth', () => ({
  login: vi.fn().mockResolvedValue({ token: 'fake.jwt.token', email: 'user@test.com' }),
  logout: vi.fn().mockResolvedValue(undefined)
}))

function TestConsumer() {
  const { currentEmail, isAuthenticated, login, logout } = useAuth()
  return (
    <div>
      <span data-testid="email">{currentEmail ?? 'none'}</span>
      <span data-testid="auth">{String(isAuthenticated)}</span>
      <button onClick={() => login({ email: 'user@test.com', password: 'pw' })}>Login</button>
      <button onClick={logout}>Logout</button>
    </div>
  )
}

function renderWithProviders() {
  return render(
    <MemoryRouter>
      <AuthProvider>
        <TestConsumer />
      </AuthProvider>
    </MemoryRouter>
  )
}

test('initially not authenticated when no token in localStorage', () => {
  localStorage.clear()
  renderWithProviders()
  expect(screen.getByTestId('auth').textContent).toBe('false')
  expect(screen.getByTestId('email').textContent).toBe('none')
})

test('login sets currentEmail and stores token', async () => {
  localStorage.clear()
  renderWithProviders()
  await act(async () => {
    screen.getByRole('button', { name: 'Login' }).click()
  })
  expect(localStorage.getItem('jwt')).toBe('fake.jwt.token')
  expect(screen.getByTestId('email').textContent).toBe('user@test.com')
  expect(screen.getByTestId('auth').textContent).toBe('true')
})

test('logout clears token and email', async () => {
  // A valid-looking JWT with sub: "user@test.com" so decodeEmail returns a real value
  const payload = btoa(JSON.stringify({ sub: 'user@test.com', exp: 9999999999 }))
  const mockJwt = `header.${payload}.signature`
  localStorage.setItem('jwt', mockJwt)
  renderWithProviders()
  // Confirm we start authenticated
  expect(screen.getByTestId('auth').textContent).toBe('true')
  await act(async () => {
    screen.getByRole('button', { name: 'Logout' }).click()
  })
  expect(localStorage.getItem('jwt')).toBeNull()
  expect(screen.getByTestId('auth').textContent).toBe('false')
})
```

- [ ] **Step 2: Run to confirm the test fails**

```bash
cd frontend && npm test 2>&1 | tail -10
```
Expected: failure — AuthContext does not exist yet.

- [ ] **Step 3: Implement AuthContext**

Create `frontend/src/context/AuthContext.tsx`:

```tsx
import { createContext, useContext, useState, useEffect, ReactNode } from 'react'
import { login as apiLogin, logout as apiLogout } from '../api/auth'
import type { LoginRequest } from '../api/types'

interface AuthContextValue {
  currentEmail: string | null
  isAuthenticated: boolean
  login: (req: LoginRequest) => Promise<void>
  loginWithToken: (token: string, email: string) => void
  logout: () => Promise<void>
}

const AuthContext = createContext<AuthContextValue | null>(null)

function decodeEmail(token: string): string | null {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    return payload.sub ?? null
  } catch {
    return null
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [currentEmail, setCurrentEmail] = useState<string | null>(() => {
    const token = localStorage.getItem('jwt')
    return token ? decodeEmail(token) : null
  })

  const login = async (req: LoginRequest) => {
    const { token, email } = await apiLogin(req)
    localStorage.setItem('jwt', token)
    setCurrentEmail(email)
  }

  // Used by registration flow: token already obtained, no need for a second API call
  const loginWithToken = (token: string, email: string) => {
    localStorage.setItem('jwt', token)
    setCurrentEmail(email)
  }

  const logout = async () => {
    await apiLogout()
    localStorage.removeItem('jwt')
    setCurrentEmail(null)
  }

  return (
    <AuthContext.Provider value={{ currentEmail, isAuthenticated: currentEmail !== null, login, loginWithToken, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used inside AuthProvider')
  return ctx
}

```

- [ ] **Step 4: Implement ProtectedRoute**

Create `frontend/src/components/ProtectedRoute.tsx`:

```tsx
import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function ProtectedRoute() {
  const { isAuthenticated } = useAuth()
  return isAuthenticated ? <Outlet /> : <Navigate to="/login" replace />
}
```

- [ ] **Step 5: Run tests to confirm they pass**

```bash
cd frontend && npm test 2>&1 | tail -10
```
Expected: `Tests 3 passed`

- [ ] **Step 6: Commit**

```bash
cd ..
git add frontend/src/context/ frontend/src/components/ProtectedRoute.tsx frontend/src/test/
git commit -m "feat: add AuthContext with JWT and ProtectedRoute"
```

---

### Task 4: App routing and layout

**Files:**
- Create: `frontend/src/components/Layout.tsx`
- Modify: `frontend/src/App.tsx`

- [ ] **Step 1: Create the shared layout component**

`Layout` renders the nav bar and then an `<Outlet />` for the page content. This is the standard React Router nested layout pattern.

Create `frontend/src/components/Layout.tsx`:

```tsx
import { Link, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Layout() {
  const { currentEmail, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = async () => {
    await logout()
    navigate('/login')
  }

  return (
    <div>
      <nav>
        <Link to="/dashboard">Home</Link>
        <Link to="/recipes">Recipes</Link>
        <Link to="/shopping-lists">Shopping Lists</Link>
        <Link to="/products">Products</Link>
        <Link to="/user">{currentEmail}</Link>
        <button onClick={handleLogout}>Logout</button>
      </nav>
      <main>
        <Outlet />
      </main>
    </div>
  )
}
```

- [ ] **Step 2: Wire up all routes in App.tsx**

Replace `frontend/src/App.tsx`:

```tsx
import { BrowserRouter, Routes, Route, Navigate, Outlet } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { AuthProvider } from './context/AuthContext'
import ProtectedRoute from './components/ProtectedRoute'
import Layout from './components/Layout'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import ForgotPasswordPage from './pages/ForgotPasswordPage'
import ResetPasswordPage from './pages/ResetPasswordPage'
import DashboardPage from './pages/DashboardPage'
import RecipesPage from './pages/RecipesPage'
import RecipeDetailPage from './pages/RecipeDetailPage'
import ShoppingListsPage from './pages/ShoppingListsPage'
import ShoppingListDetailPage from './pages/ShoppingListDetailPage'
import ProductsPage from './pages/ProductsPage'
import UncategorizedProductsPage from './pages/UncategorizedProductsPage'
import UserPage from './pages/UserPage'

const queryClient = new QueryClient()

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <AuthProvider>
          <Routes>
            {/* Public routes */}
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/forgot-password" element={<ForgotPasswordPage />} />
            <Route path="/reset-password" element={<ResetPasswordPage />} />

            {/* Protected routes — wrapped in layout. Layout renders <Outlet /> internally. */}
            <Route element={<ProtectedRoute />}>
              <Route element={<Layout />}>
                <Route path="/dashboard" element={<DashboardPage />} />
                <Route path="/recipes" element={<RecipesPage />} />
                <Route path="/recipes/:id" element={<RecipeDetailPage />} />
                <Route path="/shopping-lists" element={<ShoppingListsPage />} />
                <Route path="/shopping-lists/:id" element={<ShoppingListDetailPage />} />
                <Route path="/products" element={<ProductsPage />} />
                <Route path="/products/uncategorized" element={<UncategorizedProductsPage />} />
                <Route path="/user" element={<UserPage />} />
              </Route>
            </Route>

            {/* Default redirect */}
            <Route path="/" element={<Navigate to="/dashboard" replace />} />
            <Route path="*" element={<Navigate to="/dashboard" replace />} />
          </Routes>
        </AuthProvider>
      </BrowserRouter>
    </QueryClientProvider>
  )
}
```

- [ ] **Step 3: Create stub pages (all pages as placeholder components that will be filled in Tasks 5–11)**

```bash
mkdir -p frontend/src/pages
```

Create each of these files with a placeholder. Example for all pages:

`frontend/src/pages/LoginPage.tsx`:
```tsx
export default function LoginPage() { return <div>Login</div> }
```

Repeat the same one-line stub for:
- `RegisterPage.tsx`
- `ForgotPasswordPage.tsx`
- `ResetPasswordPage.tsx`
- `DashboardPage.tsx`
- `RecipesPage.tsx`
- `RecipeDetailPage.tsx`
- `ShoppingListsPage.tsx`
- `ShoppingListDetailPage.tsx`
- `ProductsPage.tsx`
- `UncategorizedProductsPage.tsx`
- `UserPage.tsx`

- [ ] **Step 4: Verify the dev server builds without errors**

```bash
cd frontend && npm run build 2>&1 | tail -10
```
Expected: `✓ built in`

- [ ] **Step 5: Add the Spring Boot SPA catch-all controller**

Create `src/main/kotlin/com/example/dinnerservice/SpaController.kt`:

```kotlin
package com.example.dinnerservice

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class SpaController {
    /**
     * Forwards all non-API, non-static requests to index.html so React Router
     * can handle client-side routing. Excludes /api/** and /recipe-images/**.
     */
    @GetMapping("/{path:^(?!api|recipe-images).*}/**")
    fun spa(): String = "forward:/index.html"
}
```

- [ ] **Step 6: Commit**

```bash
cd ..
git add frontend/src/ src/main/kotlin/com/example/dinnerservice/SpaController.kt
git commit -m "feat: add routing, layout, stub pages, and SPA catch-all controller"
```

---

### Task 5: Login and Register pages

**Files:**
- Modify: `frontend/src/pages/LoginPage.tsx`
- Modify: `frontend/src/pages/RegisterPage.tsx`
- Create: `frontend/src/test/LoginPage.test.tsx`

- [ ] **Step 1: Write the failing login test**

Create `frontend/src/test/LoginPage.test.tsx`:

```tsx
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { QueryClientProvider, QueryClient } from '@tanstack/react-query'
import { AuthProvider } from '../context/AuthContext'
import LoginPage from '../pages/LoginPage'
import { vi } from 'vitest'

vi.mock('../api/auth', () => ({
  login: vi.fn().mockResolvedValue({ token: 'tok', email: 'user@test.com' }),
  logout: vi.fn()
}))

function renderLogin() {
  return render(
    <QueryClientProvider client={new QueryClient()}>
      <MemoryRouter initialEntries={['/login']}>
        <AuthProvider>
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/dashboard" element={<div>Dashboard</div>} />
          </Routes>
        </AuthProvider>
      </MemoryRouter>
    </QueryClientProvider>
  )
}

test('renders email and password fields', () => {
  renderLogin()
  expect(screen.getByLabelText(/email/i)).toBeInTheDocument()
  expect(screen.getByLabelText(/password/i)).toBeInTheDocument()
})

test('submitting valid credentials navigates to dashboard', async () => {
  renderLogin()
  fireEvent.change(screen.getByLabelText(/email/i), { target: { value: 'user@test.com' } })
  fireEvent.change(screen.getByLabelText(/password/i), { target: { value: 'password123' } })
  fireEvent.click(screen.getByRole('button', { name: /sign in/i }))
  await waitFor(() => {
    expect(screen.getByText('Dashboard')).toBeInTheDocument()
  })
})
```

- [ ] **Step 2: Run to confirm it fails**

```bash
cd frontend && npm test -- LoginPage 2>&1 | tail -10
```

- [ ] **Step 3: Implement LoginPage**

Replace `frontend/src/pages/LoginPage.tsx`:

```tsx
import { useState, FormEvent } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function LoginPage() {
  const { login, isAuthenticated } = useAuth()
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  if (isAuthenticated) {
    navigate('/dashboard', { replace: true })
    return null
  }

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setError(null)
    setLoading(true)
    try {
      await login({ email, password })
      navigate('/dashboard', { replace: true })
    } catch {
      setError('Invalid email or password.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div>
      <h1>Sign in</h1>
      {error && <p role="alert">{error}</p>}
      <form onSubmit={handleSubmit}>
        <label htmlFor="email">Email</label>
        <input id="email" type="email" value={email} onChange={e => setEmail(e.target.value)} required />
        <label htmlFor="password">Password</label>
        <input id="password" type="password" value={password} onChange={e => setPassword(e.target.value)} required />
        <button type="submit" disabled={loading}>Sign in</button>
      </form>
      <Link to="/register">Create account</Link>
      <Link to="/forgot-password">Forgot password?</Link>
    </div>
  )
}
```

- [ ] **Step 4: Implement RegisterPage**

Replace `frontend/src/pages/RegisterPage.tsx`:

```tsx
import { useState, FormEvent } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { register } from '../api/auth'

export default function RegisterPage() {
  const { loginWithToken } = useAuth()
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    if (password !== confirmPassword) { setError('Passwords do not match.'); return }
    setError(null)
    setLoading(true)
    try {
      // register returns a JWT directly — no second login call needed
      const { token, email: registeredEmail } = await register({ email, password, confirmPassword })
      loginWithToken(token, registeredEmail)
      navigate('/dashboard', { replace: true })
    } catch {
      setError('Registration failed. Email may already be in use.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div>
      <h1>Create account</h1>
      {error && <p role="alert">{error}</p>}
      <form onSubmit={handleSubmit}>
        <label htmlFor="email">Email</label>
        <input id="email" type="email" value={email} onChange={e => setEmail(e.target.value)} required />
        <label htmlFor="password">Password</label>
        <input id="password" type="password" value={password} onChange={e => setPassword(e.target.value)} required minLength={8} />
        <label htmlFor="confirm">Confirm password</label>
        <input id="confirm" type="password" value={confirmPassword} onChange={e => setConfirmPassword(e.target.value)} required />
        <button type="submit" disabled={loading}>Create account</button>
      </form>
      <Link to="/login">Already have an account?</Link>
    </div>
  )
}
```

- [ ] **Step 5: Implement ForgotPasswordPage and ResetPasswordPage**

Replace `frontend/src/pages/ForgotPasswordPage.tsx`:

```tsx
import { useState, FormEvent } from 'react'
import { forgotPassword } from '../api/auth'

export default function ForgotPasswordPage() {
  const [email, setEmail] = useState('')
  const [sent, setSent] = useState(false)
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setLoading(true)
    await forgotPassword({ email }).catch(() => {})
    setSent(true)
    setLoading(false)
  }

  if (sent) return <p>If that email exists, a reset link has been sent.</p>

  return (
    <div>
      <h1>Reset password</h1>
      <form onSubmit={handleSubmit}>
        <label htmlFor="email">Email</label>
        <input id="email" type="email" value={email} onChange={e => setEmail(e.target.value)} required />
        <button type="submit" disabled={loading}>Send reset link</button>
      </form>
    </div>
  )
}
```

Replace `frontend/src/pages/ResetPasswordPage.tsx`:

```tsx
import { useState, FormEvent } from 'react'
import { useSearchParams, useNavigate } from 'react-router-dom'
import { resetPassword } from '../api/auth'

export default function ResetPasswordPage() {
  const [params] = useSearchParams()
  const navigate = useNavigate()
  const token = params.get('token') ?? ''
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    if (password !== confirmPassword) { setError('Passwords do not match.'); return }
    setLoading(true)
    try {
      await resetPassword({ token, password, confirmPassword })
      navigate('/login?reset=1')
    } catch {
      setError('Reset failed. The link may have expired.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div>
      <h1>Set new password</h1>
      {error && <p role="alert">{error}</p>}
      <form onSubmit={handleSubmit}>
        <label htmlFor="password">New password</label>
        <input id="password" type="password" value={password} onChange={e => setPassword(e.target.value)} required minLength={8} />
        <label htmlFor="confirm">Confirm password</label>
        <input id="confirm" type="password" value={confirmPassword} onChange={e => setConfirmPassword(e.target.value)} required />
        <button type="submit" disabled={loading}>Set password</button>
      </form>
    </div>
  )
}
```

- [ ] **Step 6: Run tests to confirm they pass**

```bash
cd frontend && npm test 2>&1 | tail -10
```
Expected: all tests pass

- [ ] **Step 7: Commit**

```bash
cd ..
git add frontend/src/pages/
git commit -m "feat: implement auth pages (login, register, forgot/reset password)"
```

---

### Task 6: Dashboard page

**Files:**
- Modify: `frontend/src/pages/DashboardPage.tsx`

- [ ] **Step 1: Implement DashboardPage**

Replace `frontend/src/pages/DashboardPage.tsx`:

```tsx
import { useAuth } from '../context/AuthContext'
import { Link } from 'react-router-dom'

export default function DashboardPage() {
  const { currentEmail } = useAuth()
  return (
    <div>
      <h1>Welcome, {currentEmail}</h1>
      <nav>
        <Link to="/recipes">Recipes</Link>
        <Link to="/shopping-lists">Shopping Lists</Link>
        <Link to="/products">Products</Link>
      </nav>
    </div>
  )
}
```

- [ ] **Step 2: Commit**

```bash
git add frontend/src/pages/DashboardPage.tsx
git commit -m "feat: implement Dashboard page"
```

---

### Task 7: Recipes pages

**Files:**
- Modify: `frontend/src/pages/RecipesPage.tsx`
- Modify: `frontend/src/pages/RecipeDetailPage.tsx`

- [ ] **Step 1: Implement RecipesPage**

Replace `frontend/src/pages/RecipesPage.tsx`:

```tsx
import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Link, useNavigate } from 'react-router-dom'
import { getRecipes, createRecipe } from '../api/recipes'

export default function RecipesPage() {
  const qc = useQueryClient()
  const navigate = useNavigate()
  const [name, setName] = useState('')
  const { data: recipes = [], isLoading } = useQuery({ queryKey: ['recipes'], queryFn: getRecipes })
  const create = useMutation({
    mutationFn: createRecipe,
    onSuccess: (recipe) => { qc.invalidateQueries({ queryKey: ['recipes'] }); navigate(`/recipes/${recipe.id}`) }
  })

  if (isLoading) return <p>Loading...</p>

  return (
    <div>
      <h1>Recipes</h1>
      <form onSubmit={e => { e.preventDefault(); create.mutate({ name }) }}>
        <input value={name} onChange={e => setName(e.target.value)} placeholder="New recipe name" required />
        <button type="submit">Add Recipe</button>
      </form>
      <ul>
        {recipes.map(r => (
          <li key={r.id}>
            {r.previewImage && <img src={`/recipe-images/${r.previewImage}`} alt={r.name} width={60} />}
            <Link to={`/recipes/${r.id}`}>{r.name}</Link>
          </li>
        ))}
      </ul>
    </div>
  )
}
```

- [ ] **Step 2: Implement RecipeDetailPage**

Replace `frontend/src/pages/RecipeDetailPage.tsx`:

```tsx
import { useState } from 'react'
import { useParams } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getRecipe, addIngredient, deleteIngredient, addIngredientToList,
         removeIngredientFromList, uploadImages, deleteImage, deleteRecipe, updateRecipe } from '../api/recipes'
import { searchProducts } from '../api/products'
import { useNavigate } from 'react-router-dom'

const UNITS = ['pcs', 'g', 'kg', 'ml', 'dl', 'L', 'tsp', 'tbsp', 'cup']

export default function RecipeDetailPage() {
  const { id } = useParams<{ id: string }>()
  const recipeId = Number(id)
  const navigate = useNavigate()
  const qc = useQueryClient()
  const [selectedListId, setSelectedListId] = useState<number | undefined>()
  const [productSearch, setProductSearch] = useState('')
  const [selectedProductId, setSelectedProductId] = useState<number | null>(null)
  const [quantity, setQuantity] = useState('')
  const [unit, setUnit] = useState('pcs')
  const [editing, setEditing] = useState(false)
  const [editName, setEditName] = useState('')
  const [editDesc, setEditDesc] = useState('')

  const { data: recipe, isLoading } = useQuery({
    queryKey: ['recipe', recipeId, selectedListId],
    queryFn: () => getRecipe(recipeId, selectedListId)
  })

  const { data: searchResults = [] } = useQuery({
    queryKey: ['product-search', productSearch],
    queryFn: () => searchProducts(productSearch),
    enabled: productSearch.length > 1
  })

  const invalidate = () => qc.invalidateQueries({ queryKey: ['recipe', recipeId] })

  const addIng = useMutation({ mutationFn: () =>
    addIngredient(recipeId, { productId: selectedProductId!, quantity: quantity ? Number(quantity) : undefined, unit }),
    onSuccess: invalidate })

  const removeIng = useMutation({ mutationFn: (ingId: number) => deleteIngredient(recipeId, ingId), onSuccess: invalidate })

  const addToList = useMutation({ mutationFn: (ingId: number) =>
    addIngredientToList(recipeId, ingId, selectedListId!), onSuccess: invalidate })

  const removeFromList = useMutation({ mutationFn: (ingId: number) =>
    removeIngredientFromList(recipeId, ingId, selectedListId!), onSuccess: invalidate })

  const delRecipe = useMutation({ mutationFn: () => deleteRecipe(recipeId),
    onSuccess: () => navigate('/recipes') })

  const editRecipe = useMutation({ mutationFn: () =>
    updateRecipe(recipeId, { name: editName, description: editDesc }),
    onSuccess: () => { setEditing(false); invalidate() } })

  const uploadImg = useMutation({ mutationFn: (files: FileList) => uploadImages(recipeId, files), onSuccess: invalidate })
  const delImg = useMutation({ mutationFn: (imgId: number) => deleteImage(recipeId, imgId), onSuccess: invalidate })

  if (isLoading || !recipe) return <p>Loading...</p>

  return (
    <div>
      {editing ? (
        <form onSubmit={e => { e.preventDefault(); editRecipe.mutate() }}>
          <input value={editName} onChange={e => setEditName(e.target.value)} required />
          <textarea value={editDesc} onChange={e => setEditDesc(e.target.value)} />
          <button type="submit">Save</button>
          <button type="button" onClick={() => setEditing(false)}>Cancel</button>
        </form>
      ) : (
        <>
          <h1>{recipe.name}</h1>
          <p>{recipe.description}</p>
          <button onClick={() => { setEditing(true); setEditName(recipe.name); setEditDesc(recipe.description) }}>Edit</button>
          <button onClick={() => { if (confirm('Delete this recipe?')) delRecipe.mutate() }}>Delete</button>
        </>
      )}

      {/* Shopping list selector */}
      <label htmlFor="list-select">Shopping list:</label>
      <select id="list-select" value={selectedListId ?? ''} onChange={e => setSelectedListId(e.target.value ? Number(e.target.value) : undefined)}>
        <option value="">-- none --</option>
        {recipe.shoppingLists.map(l => <option key={l.id} value={l.id}>{l.name}</option>)}
      </select>

      {/* Ingredients */}
      <h2>Ingredients</h2>
      <ul>
        {recipe.ingredients.map(ing => (
          <li key={ing.id}>
            {ing.quantity} {ing.unit} {ing.productName}
            {selectedListId && (
              <>
                <button onClick={() => addToList.mutate(ing.id)}>+</button>
                <span>{recipe.ingredientCounts[ing.id] ?? 0}</span>
                <button onClick={() => removeFromList.mutate(ing.id)}>-</button>
              </>
            )}
            <button onClick={() => removeIng.mutate(ing.id)}>✕</button>
          </li>
        ))}
      </ul>

      {/* Add ingredient form */}
      <form onSubmit={e => { e.preventDefault(); addIng.mutate() }}>
        <input
          placeholder="Search product..."
          value={productSearch}
          onChange={e => { setProductSearch(e.target.value); setSelectedProductId(null) }}
        />
        {searchResults.length > 0 && !selectedProductId && (
          <ul>
            {searchResults.map(p => (
              <li key={p.id} onClick={() => { setSelectedProductId(p.id); setProductSearch(p.name) }} style={{ cursor: 'pointer' }}>
                {p.name}
              </li>
            ))}
          </ul>
        )}
        <input type="number" placeholder="Qty" value={quantity} onChange={e => setQuantity(e.target.value)} />
        <select value={unit} onChange={e => setUnit(e.target.value)}>
          {UNITS.map(u => <option key={u} value={u}>{u}</option>)}
        </select>
        <button type="submit" disabled={!selectedProductId}>Add ingredient</button>
      </form>

      {/* Images */}
      <h2>Photos</h2>
      <div>
        {recipe.images.map(img => (
          <span key={img.id}>
            <img src={`/recipe-images/${img.filename}`} alt={img.originalName} width={120} />
            <button onClick={() => delImg.mutate(img.id)}>Delete</button>
          </span>
        ))}
      </div>
      <input type="file" multiple accept="image/*"
        onChange={e => { if (e.target.files?.length) uploadImg.mutate(e.target.files) }} />
    </div>
  )
}
```

- [ ] **Step 3: Commit**

```bash
git add frontend/src/pages/RecipesPage.tsx frontend/src/pages/RecipeDetailPage.tsx
git commit -m "feat: implement Recipes and Recipe Detail pages"
```

---

### Task 8: Shopping list pages

**Files:**
- Modify: `frontend/src/pages/ShoppingListsPage.tsx`
- Modify: `frontend/src/pages/ShoppingListDetailPage.tsx`

- [ ] **Step 1: Implement ShoppingListsPage**

Replace `frontend/src/pages/ShoppingListsPage.tsx`:

```tsx
import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { getShoppingLists, createShoppingList } from '../api/shoppingLists'

export default function ShoppingListsPage() {
  const qc = useQueryClient()
  const [name, setName] = useState('')
  const { data, isLoading } = useQuery({ queryKey: ['shopping-lists'], queryFn: getShoppingLists })
  const create = useMutation({
    mutationFn: createShoppingList,
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['shopping-lists'] }); setName('') }
  })

  if (isLoading) return <p>Loading...</p>

  return (
    <div>
      <h1>Shopping Lists</h1>
      <form onSubmit={e => { e.preventDefault(); create.mutate(name) }}>
        <input value={name} onChange={e => setName(e.target.value)} placeholder="New list name" required />
        <button type="submit">Create</button>
      </form>
      <h2>My Lists</h2>
      <ul>{(data?.owned ?? []).map(l => <li key={l.id}><Link to={`/shopping-lists/${l.id}`}>{l.name}</Link></li>)}</ul>
      <h2>Shared With Me</h2>
      <ul>{(data?.shared ?? []).map(l => <li key={l.id}><Link to={`/shopping-lists/${l.id}`}>{l.name} (by {l.ownerEmail})</Link></li>)}</ul>
    </div>
  )
}
```

- [ ] **Step 2: Implement ShoppingListDetailPage**

Replace `frontend/src/pages/ShoppingListDetailPage.tsx`:

```tsx
import { useState } from 'react'
import { useParams } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getShoppingList, addItem, toggleItem, deleteItem, clearChecked, shareList } from '../api/shoppingLists'

export default function ShoppingListDetailPage() {
  const { id } = useParams<{ id: string }>()
  const listId = Number(id)
  const qc = useQueryClient()
  const [itemName, setItemName] = useState('')
  const [count, setCount] = useState('')
  const [unitPrice, setUnitPrice] = useState('')
  const [shareEmail, setShareEmail] = useState('')
  const [shareError, setShareError] = useState<string | null>(null)

  const { data: list, isLoading } = useQuery({
    queryKey: ['shopping-list', listId],
    queryFn: () => getShoppingList(listId)
  })

  const invalidate = () => qc.invalidateQueries({ queryKey: ['shopping-list', listId] })

  const add = useMutation({ mutationFn: () =>
    addItem(listId, { name: itemName, count: count ? Number(count) : undefined, unitPrice: unitPrice ? Number(unitPrice) : undefined }),
    onSuccess: () => { invalidate(); setItemName(''); setCount(''); setUnitPrice('') }
  })

  // Optimistic toggle for instant UI feedback on mobile
  const toggle = useMutation({
    mutationFn: (itemId: number) => toggleItem(listId, itemId),
    onMutate: async (itemId) => {
      await qc.cancelQueries({ queryKey: ['shopping-list', listId] })
      const prev = qc.getQueryData(['shopping-list', listId])
      qc.setQueryData(['shopping-list', listId], (old: typeof list) => old ? {
        ...old,
        items: old.items.map(i => i.id === itemId ? { ...i, checked: !i.checked } : i)
      } : old)
      return { prev }
    },
    onError: (_err, _id, ctx) => { if (ctx?.prev) qc.setQueryData(['shopping-list', listId], ctx.prev) },
    onSettled: invalidate
  })

  const remove = useMutation({ mutationFn: (itemId: number) => deleteItem(listId, itemId), onSuccess: invalidate })
  const clear = useMutation({ mutationFn: () => clearChecked(listId), onSuccess: invalidate })
  const share = useMutation({
    mutationFn: () => shareList(listId, shareEmail),
    onSuccess: () => { setShareEmail(''); setShareError(null); invalidate() },
    onError: () => setShareError('User not found.')
  })

  if (isLoading || !list) return <p>Loading...</p>

  const groupedByCategory = list.items.reduce<Record<string, typeof list.items>>((acc, item) => {
    const key = item.categoryName ?? 'Uncategorized'
    acc[key] = [...(acc[key] ?? []), item]
    return acc
  }, {})

  return (
    <div>
      <h1>{list.name}</h1>
      <p>Total: €{list.totalPrice.toFixed(2)} | {list.items.length} items</p>

      <form onSubmit={e => { e.preventDefault(); add.mutate() }}>
        <input value={itemName} onChange={e => setItemName(e.target.value)} placeholder="Item name" required />
        <input type="number" value={count} onChange={e => setCount(e.target.value)} placeholder="Qty" />
        <input type="number" value={unitPrice} onChange={e => setUnitPrice(e.target.value)} placeholder="Price" />
        <button type="submit">Add</button>
      </form>

      {Object.entries(groupedByCategory).map(([category, items]) => (
        <div key={category}>
          <h3>{category}</h3>
          <ul>
            {items.map(item => (
              <li key={item.id} style={{ textDecoration: item.checked ? 'line-through' : 'none' }}>
                <input type="checkbox" checked={item.checked} onChange={() => toggle.mutate(item.id)} />
                {item.count} × {item.name}
                {item.totalPrice != null && ` €${item.totalPrice.toFixed(2)}`}
                <button onClick={() => remove.mutate(item.id)}>✕</button>
              </li>
            ))}
          </ul>
        </div>
      ))}

      <button onClick={() => clear.mutate()}>Clear checked</button>

      {list.isOwner && (
        <div>
          <h3>Share list</h3>
          {shareError && <p role="alert">{shareError}</p>}
          <form onSubmit={e => { e.preventDefault(); share.mutate() }}>
            <input type="email" value={shareEmail} onChange={e => setShareEmail(e.target.value)} placeholder="Email to share with" required />
            <button type="submit">Share</button>
          </form>
        </div>
      )}
    </div>
  )
}
```

- [ ] **Step 3: Commit**

```bash
git add frontend/src/pages/ShoppingListsPage.tsx frontend/src/pages/ShoppingListDetailPage.tsx
git commit -m "feat: implement Shopping Lists pages with optimistic toggle"
```

---

### Task 9: Products and User pages

**Files:**
- Modify: `frontend/src/pages/ProductsPage.tsx`
- Modify: `frontend/src/pages/UserPage.tsx`

- [ ] **Step 1: Implement ProductsPage**

Replace `frontend/src/pages/ProductsPage.tsx`:

```tsx
import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getProducts, getCategories, createProduct, updateProduct, deleteProduct,
         createCategory, deleteCategory } from '../api/products'

export default function ProductsPage() {
  const qc = useQueryClient()
  const { data: products = [] } = useQuery({ queryKey: ['products'], queryFn: getProducts })
  const { data: categories = [] } = useQuery({ queryKey: ['categories'], queryFn: getCategories })

  const [newName, setNewName] = useState('')
  const [newPrice, setNewPrice] = useState('')
  const [newCatId, setNewCatId] = useState('')
  const [catName, setCatName] = useState('')
  const [catColor, setCatColor] = useState('#cccccc')
  const [editId, setEditId] = useState<number | null>(null)
  const [editName, setEditName] = useState('')
  const [editPrice, setEditPrice] = useState('')
  const [editCatId, setEditCatId] = useState('')

  const inv = (keys: string[]) => keys.forEach(k => qc.invalidateQueries({ queryKey: [k] }))

  const create = useMutation({ mutationFn: () =>
    createProduct({ name: newName, price: newPrice ? Number(newPrice) : undefined, categoryId: newCatId ? Number(newCatId) : undefined }),
    onSuccess: () => { inv(['products']); setNewName(''); setNewPrice(''); setNewCatId('') }
  })

  const edit = useMutation({ mutationFn: () =>
    updateProduct(editId!, { name: editName, price: editPrice ? Number(editPrice) : undefined, categoryId: editCatId ? Number(editCatId) : undefined }),
    onSuccess: () => { inv(['products']); setEditId(null) }
  })

  const del = useMutation({ mutationFn: (id: number) => deleteProduct(id), onSuccess: () => inv(['products']) })

  const createCat = useMutation({ mutationFn: () => createCategory({ name: catName, color: catColor }),
    onSuccess: () => { inv(['categories']); setCatName(''); setCatColor('#cccccc') } })

  const deleteCat = useMutation({ mutationFn: (id: number) => deleteCategory(id), onSuccess: () => inv(['categories']) })

  return (
    <div>
      <h1>Products</h1>

      <form onSubmit={e => { e.preventDefault(); create.mutate() }}>
        <input value={newName} onChange={e => setNewName(e.target.value)} placeholder="Product name" required />
        <input type="number" value={newPrice} onChange={e => setNewPrice(e.target.value)} placeholder="Price" />
        <select value={newCatId} onChange={e => setNewCatId(e.target.value)}>
          <option value="">No category</option>
          {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
        </select>
        <button type="submit">Add product</button>
      </form>

      <table>
        <tbody>
          {products.map(p => (
            <tr key={p.id}>
              {editId === p.id ? (
                <td colSpan={4}>
                  <form onSubmit={e => { e.preventDefault(); edit.mutate() }}>
                    <input value={editName} onChange={e => setEditName(e.target.value)} required />
                    <input type="number" value={editPrice} onChange={e => setEditPrice(e.target.value)} />
                    <select value={editCatId} onChange={e => setEditCatId(e.target.value)}>
                      <option value="">No category</option>
                      {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                    </select>
                    <button type="submit">Save</button>
                    <button type="button" onClick={() => setEditId(null)}>Cancel</button>
                  </form>
                </td>
              ) : (
                <>
                  <td>{p.categoryColor && <span style={{ background: p.categoryColor, padding: '2px 6px' }}>{p.categoryName}</span>}</td>
                  <td>{p.name}</td>
                  <td>{p.price != null ? `€${p.price.toFixed(2)}` : ''}</td>
                  <td>
                    <button onClick={() => { setEditId(p.id); setEditName(p.name); setEditPrice(p.price?.toString() ?? ''); setEditCatId(p.categoryId?.toString() ?? '') }}>Edit</button>
                    <button onClick={() => del.mutate(p.id)}>Delete</button>
                  </td>
                </>
              )}
            </tr>
          ))}
        </tbody>
      </table>

      <h2>Categories</h2>
      <form onSubmit={e => { e.preventDefault(); createCat.mutate() }}>
        <input value={catName} onChange={e => setCatName(e.target.value)} placeholder="Category name" required />
        <input type="color" value={catColor} onChange={e => setCatColor(e.target.value)} />
        <button type="submit">Add category</button>
      </form>
      <ul>
        {categories.map(c => (
          <li key={c.id} style={{ color: c.color }}>
            {c.name}
            <button onClick={() => deleteCat.mutate(c.id)}>Delete</button>
          </li>
        ))}
      </ul>
    </div>
  )
}
```

- [ ] **Step 2: Implement UncategorizedProductsPage**

Create `frontend/src/pages/UncategorizedProductsPage.tsx`:

```tsx
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getUncategorizedProducts, getCategories, updateProduct } from '../api/products'

export default function UncategorizedProductsPage() {
  const qc = useQueryClient()
  const { data: products = [] } = useQuery({ queryKey: ['products-uncategorized'], queryFn: getUncategorizedProducts })
  const { data: categories = [] } = useQuery({ queryKey: ['categories'], queryFn: getCategories })

  const assign = useMutation({
    mutationFn: ({ id, categoryId }: { id: number; categoryId: number }) =>
      updateProduct(id, { name: products.find(p => p.id === id)!.name, categoryId }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['products-uncategorized'] })
      qc.invalidateQueries({ queryKey: ['products'] })
    }
  })

  return (
    <div>
      <h1>Uncategorized Products</h1>
      {products.length === 0 && <p>All products have a category.</p>}
      <ul>
        {products.map(p => (
          <li key={p.id}>
            {p.name}
            <select
              defaultValue=""
              onChange={e => { if (e.target.value) assign.mutate({ id: p.id, categoryId: Number(e.target.value) }) }}
            >
              <option value="" disabled>Assign category…</option>
              {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
            </select>
          </li>
        ))}
      </ul>
    </div>
  )
}
```

- [ ] **Step 3: Implement UserPage**

Replace `frontend/src/pages/UserPage.tsx`:

```tsx
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { getProfile, setDefaultList, leaveList } from '../api/user'
import { useAuth } from '../context/AuthContext'

export default function UserPage() {
  const { logout } = useAuth()
  const navigate = useNavigate()
  const qc = useQueryClient()
  const { data: profile, isLoading } = useQuery({ queryKey: ['user'], queryFn: getProfile })

  const setDefault = useMutation({
    mutationFn: (listId: number | null) => setDefaultList(listId),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['user'] })
  })

  const leave = useMutation({
    mutationFn: (listId: number) => leaveList(listId),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['user'] })
  })

  const handleLogout = async () => {
    await logout()
    navigate('/login')
  }

  if (isLoading || !profile) return <p>Loading...</p>

  return (
    <div>
      <h1>Account</h1>
      <p>{profile.email}</p>
      <button onClick={handleLogout}>Logout</button>

      <h2>Default shopping list</h2>
      <select
        value={profile.defaultListId ?? ''}
        onChange={e => setDefault.mutate(e.target.value ? Number(e.target.value) : null)}
      >
        <option value="">None</option>
        {profile.allLists.map(l => <option key={l.id} value={l.id}>{l.name}</option>)}
      </select>

      <h2>Shared lists</h2>
      <ul>
        {profile.sharedLists.map(l => (
          <li key={l.id}>
            {l.name} (by {l.ownerEmail})
            <button onClick={() => leave.mutate(l.id)}>Leave</button>
          </li>
        ))}
      </ul>
    </div>
  )
}
```

- [ ] **Step 3: Commit**

```bash
git add frontend/src/pages/ProductsPage.tsx \
        frontend/src/pages/UncategorizedProductsPage.tsx \
        frontend/src/pages/UserPage.tsx
git commit -m "feat: implement Products, Uncategorized Products, and User pages"
```

---

### Task 10: Wire Maven to build the frontend

**Files:**
- Modify: `pom.xml`

- [ ] **Step 1: Add the frontend-maven-plugin to pom.xml**

Inside the `<plugins>` section of `pom.xml`, add after the kotlin-maven-plugin:

```xml
<plugin>
    <groupId>com.github.eirslett</groupId>
    <artifactId>frontend-maven-plugin</artifactId>
    <version>1.15.0</version>
    <configuration>
        <workingDirectory>frontend</workingDirectory>
        <nodeVersion>v20.14.0</nodeVersion>
        <npmVersion>10.7.0</npmVersion>
    </configuration>
    <executions>
        <execution>
            <id>install-node-and-npm</id>
            <goals><goal>install-node-and-npm</goal></goals>
        </execution>
        <execution>
            <id>npm-install</id>
            <goals><goal>npm</goal></goals>
            <configuration><arguments>install</arguments></configuration>
        </execution>
        <execution>
            <id>npm-build</id>
            <goals><goal>npm</goal></goals>
            <phase>generate-resources</phase>
            <configuration><arguments>run build</arguments></configuration>
        </execution>
    </executions>
</plugin>
```

- [ ] **Step 2: Add `src/main/resources/static/` to .gitignore**

Open (or create) `.gitignore` at the project root and add:
```
src/main/resources/static/
```
The built SPA files are generated — they should not be committed.

- [ ] **Step 3: Build the full project**

```bash
mvn package -DskipTests -q
ls src/main/resources/static/
```
Expected: `index.html`, `assets/` directory

- [ ] **Step 4: Smoke-test the full stack**

```bash
java -jar target/dinner-service-*.jar &
sleep 10
# SPA is served
curl -s -o /dev/null -w "%{http_code}" http://localhost:8090/
# Expected: 200
# API still works
curl -s -o /dev/null -w "%{http_code}" http://localhost:8090/api/auth/login \
  -H "Content-Type: application/json" -d '{"email":"x@x.com","password":"wrong"}'
# Expected: 401
pkill -f "dinner-service.*\.jar"
```

- [ ] **Step 5: Commit**

```bash
git add pom.xml .gitignore
git commit -m "feat: wire Maven frontend plugin to build React SPA"
```

---

### Task 11: Run all tests and final commit

- [ ] **Step 1: Run backend tests**

```bash
mvn test -q 2>&1 | tail -20
```
Expected: all suites pass

- [ ] **Step 2: Run frontend tests**

```bash
cd frontend && npm test 2>&1 | tail -10
```
Expected: all tests pass

- [ ] **Step 3: Full build**

```bash
cd .. && mvn package -q
```
Expected: `BUILD SUCCESS`

- [ ] **Step 4: Final commit**

```bash
git add -A
git commit -m "feat: Phase 2 complete — React SPA replacing Thymeleaf frontend"
```
