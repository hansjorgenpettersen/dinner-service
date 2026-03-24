# UI Styling Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add Tailwind CSS v3 + shadcn/ui to the existing React SPA with a warm food-focused design: dark brown top nav, cream backgrounds, orange accents.

**Architecture:** Install Tailwind + shadcn/ui CLI into the existing Vite project. The CLI handles tsconfig paths, vite alias, and the cn() utility. Restyle every page in place — no new routes, no API changes. Auth pages (login/register/forgot/reset) render outside Layout so they get a standalone centered-card design.

**Tech Stack:** Tailwind CSS v3 (`tailwindcss@^3.4`), shadcn/ui, postcss, autoprefixer, lucide-react, React Router NavLink

---

### Task 1: Install Tailwind CSS and create config files

**Files:**
- Create: `frontend/tailwind.config.js`
- Create: `frontend/postcss.config.js`
- Modify: `frontend/package.json` (via npm install)

> Note: `package.json` has `"type": "module"` so both config files must use ESM `export default` syntax — do NOT use `module.exports`.

- [ ] **Step 1: Install Tailwind packages**

```bash
cd frontend
npm install -D tailwindcss@^3.4 postcss autoprefixer
```

Expected: packages added to devDependencies.

- [ ] **Step 2: Create `frontend/tailwind.config.js`**

Do NOT run `npx tailwindcss init` (it generates CJS syntax). Create the file manually:

```js
/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: { extend: {} },
  plugins: [],
}
```

- [ ] **Step 3: Create `frontend/postcss.config.js`**

```js
export default {
  plugins: {
    tailwindcss: {},
    autoprefixer: {},
  },
}
```

- [ ] **Step 4: Verify Tailwind is wired**

```bash
cd frontend
npx tailwindcss --version
```

Expected: prints `3.x.x`

- [ ] **Step 5: Commit**

```bash
git add frontend/tailwind.config.js frontend/postcss.config.js frontend/package.json frontend/package-lock.json
git commit -m "feat: install tailwind css v3"
```

---

### Task 2: Initialize shadcn/ui, set up index.css, add components

**Files:**
- Modify: `frontend/tsconfig.json` (CLI adds paths)
- Modify: `frontend/vite.config.ts` (CLI adds alias)
- Create: `frontend/src/lib/utils.ts` (CLI creates)
- Create: `frontend/src/index.css` (write content manually after CLI init)
- Modify: `frontend/src/main.tsx` (add CSS import)
- Create: `frontend/src/components/ui/button.tsx`
- Create: `frontend/src/components/ui/input.tsx`
- Create: `frontend/src/components/ui/label.tsx`
- Create: `frontend/src/components/ui/card.tsx`
- Create: `frontend/src/components/ui/badge.tsx`

- [ ] **Step 1: Run shadcn CLI init**

```bash
cd frontend
npx shadcn@latest init
```

When prompted, answer:
- Style: **Default**
- Base color: **Neutral** (we'll override colors)
- Global CSS file: **src/index.css**
- CSS variables: **Yes**
- React Server Components: **No**
- Write to `components.json`? **Yes**

The CLI will update `tsconfig.json` (adds `baseUrl` + `paths`), update `vite.config.ts` (adds `resolve.alias`), create `src/lib/utils.ts`, and create `src/index.css`.

- [ ] **Step 2: Replace `frontend/src/index.css` with warm palette**

Overwrite the entire file:

```css
@tailwind base;
@tailwind components;
@tailwind utilities;

@layer base {
  :root {
    /* shadcn requires HSL channel triplets — consumed as hsl(var(--primary)) */
    --background: 32 79% 96%;          /* #fdf6ee */
    --foreground: 26 77% 14%;          /* #3d1f08 */
    --card: 0 0% 100%;                 /* #ffffff */
    --card-foreground: 26 77% 14%;     /* #3d1f08 */
    --border: 34 61% 77%;              /* #e8c9a0 */
    --primary: 24 65% 48%;             /* #c96a2b */
    --primary-foreground: 32 100% 97%; /* #fff8f0 */
    --muted-foreground: 32 36% 35%;    /* #7a5c3a */
    --destructive: 0 72% 51%;          /* #dc2626 */
    --destructive-foreground: 0 0% 100%;
    --radius: 0.5rem;
    --input: 34 61% 77%;
    --ring: 24 65% 48%;
  }

  body {
    @apply bg-[#fdf6ee] text-[#3d1f08];
  }
}
```

- [ ] **Step 3: Add CSS import to `frontend/src/main.tsx`**

Add `import './index.css'` as the first line:

```tsx
import './index.css'
import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App'

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
)
```

- [ ] **Step 4: Add shadcn components**

```bash
cd frontend
npx shadcn@latest add button input label card badge
```

- [ ] **Step 5: Install lucide-react**

```bash
npm install lucide-react
```

- [ ] **Step 6: Verify build**

```bash
npm run build
```

Expected: BUILD successful, no TypeScript errors.

- [ ] **Step 7: Commit**

```bash
git add frontend/src/index.css frontend/src/main.tsx frontend/tsconfig.json frontend/vite.config.ts frontend/src/lib/ frontend/src/components/ui/ frontend/package.json frontend/package-lock.json frontend/components.json
git commit -m "feat: add shadcn/ui with warm color palette"
```

---

### Task 3: Restyle Layout (top navbar)

**Files:**
- Modify: `frontend/src/components/Layout.tsx`

> Current file uses plain `<Link>` tags. Replace with `<NavLink>` from react-router-dom which has an `isActive` prop, used to apply the orange pill style on the active nav item.

- [ ] **Step 1: Replace `frontend/src/components/Layout.tsx`**

```tsx
import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Layout() {
  const { currentEmail, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = async () => {
    await logout()
    navigate('/login')
  }

  const navCls = ({ isActive }: { isActive: boolean }) =>
    isActive
      ? 'bg-[#c96a2b] text-white rounded px-3 py-1 text-sm font-medium'
      : 'text-[#d4a07a] hover:text-[#f5e6d3] px-3 py-1 text-sm transition-colors'

  return (
    <div className="min-h-screen bg-[#fdf6ee]">
      <nav className="bg-[#7a3a1a] px-4 py-3 flex items-center gap-1 sticky top-0 z-10">
        <span className="text-[#f5e6d3] font-bold text-lg mr-4 select-none">🍽 Dinner Service</span>
        <NavLink to="/dashboard" className={navCls}>Dashboard</NavLink>
        <NavLink to="/recipes" className={navCls}>Recipes</NavLink>
        <NavLink to="/shopping-lists" className={navCls}>Shopping Lists</NavLink>
        <NavLink to="/products" className={navCls}>Products</NavLink>
        <div className="ml-auto flex items-center gap-3">
          <NavLink to="/user" className={navCls}>
            <span className="max-w-[160px] truncate inline-block align-bottom">{currentEmail}</span>
          </NavLink>
          <button
            onClick={handleLogout}
            className="text-[#d4a07a] hover:text-[#f5e6d3] text-sm transition-colors"
          >
            Logout
          </button>
        </div>
      </nav>
      <main>
        <Outlet />
      </main>
    </div>
  )
}
```

- [ ] **Step 2: Verify build**

```bash
cd frontend && npm run build
```

Expected: no errors.

- [ ] **Step 3: Commit**

```bash
git add frontend/src/components/Layout.tsx
git commit -m "feat: restyle top navbar with warm theme"
```

---

### Task 4: Restyle auth pages

**Files:**
- Modify: `frontend/src/pages/LoginPage.tsx`
- Modify: `frontend/src/pages/RegisterPage.tsx`
- Modify: `frontend/src/pages/ForgotPasswordPage.tsx`
- Modify: `frontend/src/pages/ResetPasswordPage.tsx`

> Auth pages render outside the Layout component (they have no navbar). Each gets a full-screen cream background with a centered Card. All logic stays the same — only the JSX structure changes.

- [ ] **Step 1: Replace `frontend/src/pages/LoginPage.tsx`**

```tsx
import { useState, FormEvent } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { Button } from '../components/ui/button'
import { Input } from '../components/ui/input'
import { Label } from '../components/ui/label'
import { Card, CardContent, CardHeader, CardTitle } from '../components/ui/card'

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
    <div className="min-h-screen bg-[#fdf6ee] flex items-center justify-center px-4">
      <Card className="w-full max-w-sm border-[#e8c9a0] shadow-sm">
        <CardHeader className="text-center pb-2">
          <div className="text-4xl mb-1">🍽</div>
          <CardTitle className="text-[#3d1f08] text-xl">Sign in</CardTitle>
        </CardHeader>
        <CardContent>
          {error && (
            <p role="alert" className="text-red-600 text-sm mb-4 bg-red-50 border border-red-200 rounded px-3 py-2">
              {error}
            </p>
          )}
          <form onSubmit={handleSubmit} className="flex flex-col gap-4">
            <div className="flex flex-col gap-1.5">
              <Label htmlFor="email" className="text-[#3d1f08]">Email</Label>
              <Input id="email" type="email" value={email} onChange={e => setEmail(e.target.value)} required className="border-[#e8c9a0] focus-visible:ring-[#c96a2b]" />
            </div>
            <div className="flex flex-col gap-1.5">
              <Label htmlFor="password" className="text-[#3d1f08]">Password</Label>
              <Input id="password" type="password" value={password} onChange={e => setPassword(e.target.value)} required className="border-[#e8c9a0] focus-visible:ring-[#c96a2b]" />
            </div>
            <Button type="submit" disabled={loading} className="bg-[#c96a2b] hover:bg-[#a8571f] text-white w-full mt-1">
              {loading ? 'Signing in…' : 'Sign in'}
            </Button>
          </form>
          <div className="flex justify-between mt-4 text-sm">
            <Link to="/register" className="text-[#7a5c3a] hover:text-[#c96a2b]">Create account</Link>
            <Link to="/forgot-password" className="text-[#7a5c3a] hover:text-[#c96a2b]">Forgot password?</Link>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
```

- [ ] **Step 2: Replace `frontend/src/pages/RegisterPage.tsx`**

```tsx
import { useState, FormEvent } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { register } from '../api/auth'
import { Button } from '../components/ui/button'
import { Input } from '../components/ui/input'
import { Label } from '../components/ui/label'
import { Card, CardContent, CardHeader, CardTitle } from '../components/ui/card'

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
    <div className="min-h-screen bg-[#fdf6ee] flex items-center justify-center px-4">
      <Card className="w-full max-w-sm border-[#e8c9a0] shadow-sm">
        <CardHeader className="text-center pb-2">
          <div className="text-4xl mb-1">🍽</div>
          <CardTitle className="text-[#3d1f08] text-xl">Create account</CardTitle>
        </CardHeader>
        <CardContent>
          {error && (
            <p role="alert" className="text-red-600 text-sm mb-4 bg-red-50 border border-red-200 rounded px-3 py-2">
              {error}
            </p>
          )}
          <form onSubmit={handleSubmit} className="flex flex-col gap-4">
            <div className="flex flex-col gap-1.5">
              <Label htmlFor="email" className="text-[#3d1f08]">Email</Label>
              <Input id="email" type="email" value={email} onChange={e => setEmail(e.target.value)} required className="border-[#e8c9a0] focus-visible:ring-[#c96a2b]" />
            </div>
            <div className="flex flex-col gap-1.5">
              <Label htmlFor="password" className="text-[#3d1f08]">Password</Label>
              <Input id="password" type="password" value={password} onChange={e => setPassword(e.target.value)} required minLength={8} className="border-[#e8c9a0] focus-visible:ring-[#c96a2b]" />
            </div>
            <div className="flex flex-col gap-1.5">
              <Label htmlFor="confirm" className="text-[#3d1f08]">Confirm password</Label>
              <Input id="confirm" type="password" value={confirmPassword} onChange={e => setConfirmPassword(e.target.value)} required className="border-[#e8c9a0] focus-visible:ring-[#c96a2b]" />
            </div>
            <Button type="submit" disabled={loading} className="bg-[#c96a2b] hover:bg-[#a8571f] text-white w-full mt-1">
              {loading ? 'Creating…' : 'Create account'}
            </Button>
          </form>
          <div className="mt-4 text-center text-sm">
            <Link to="/login" className="text-[#7a5c3a] hover:text-[#c96a2b]">Already have an account?</Link>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
```

- [ ] **Step 3: Replace `frontend/src/pages/ForgotPasswordPage.tsx`**

```tsx
import { useState, FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { forgotPassword } from '../api/auth'
import { Button } from '../components/ui/button'
import { Input } from '../components/ui/input'
import { Label } from '../components/ui/label'
import { Card, CardContent, CardHeader, CardTitle } from '../components/ui/card'

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

  return (
    <div className="min-h-screen bg-[#fdf6ee] flex items-center justify-center px-4">
      <Card className="w-full max-w-sm border-[#e8c9a0] shadow-sm">
        <CardHeader className="text-center pb-2">
          <div className="text-4xl mb-1">🍽</div>
          <CardTitle className="text-[#3d1f08] text-xl">Reset password</CardTitle>
        </CardHeader>
        <CardContent>
          {sent ? (
            <div className="text-center py-2">
              <p className="text-[#7a5c3a] mb-4">If that email exists, a reset link has been sent.</p>
              <Link to="/login" className="text-[#c96a2b] hover:underline text-sm">Back to sign in</Link>
            </div>
          ) : (
            <>
              <form onSubmit={handleSubmit} className="flex flex-col gap-4">
                <div className="flex flex-col gap-1.5">
                  <Label htmlFor="email" className="text-[#3d1f08]">Email</Label>
                  <Input id="email" type="email" value={email} onChange={e => setEmail(e.target.value)} required className="border-[#e8c9a0] focus-visible:ring-[#c96a2b]" />
                </div>
                <Button type="submit" disabled={loading} className="bg-[#c96a2b] hover:bg-[#a8571f] text-white w-full">
                  {loading ? 'Sending…' : 'Send reset link'}
                </Button>
              </form>
              <div className="mt-4 text-center text-sm">
                <Link to="/login" className="text-[#7a5c3a] hover:text-[#c96a2b]">Back to sign in</Link>
              </div>
            </>
          )}
        </CardContent>
      </Card>
    </div>
  )
}
```

- [ ] **Step 4: Replace `frontend/src/pages/ResetPasswordPage.tsx`**

```tsx
import { useState, FormEvent } from 'react'
import { useSearchParams, useNavigate } from 'react-router-dom'
import { resetPassword } from '../api/auth'
import { Button } from '../components/ui/button'
import { Input } from '../components/ui/input'
import { Label } from '../components/ui/label'
import { Card, CardContent, CardHeader, CardTitle } from '../components/ui/card'

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
    <div className="min-h-screen bg-[#fdf6ee] flex items-center justify-center px-4">
      <Card className="w-full max-w-sm border-[#e8c9a0] shadow-sm">
        <CardHeader className="text-center pb-2">
          <div className="text-4xl mb-1">🍽</div>
          <CardTitle className="text-[#3d1f08] text-xl">Set new password</CardTitle>
        </CardHeader>
        <CardContent>
          {error && (
            <p role="alert" className="text-red-600 text-sm mb-4 bg-red-50 border border-red-200 rounded px-3 py-2">
              {error}
            </p>
          )}
          <form onSubmit={handleSubmit} className="flex flex-col gap-4">
            <div className="flex flex-col gap-1.5">
              <Label htmlFor="password" className="text-[#3d1f08]">New password</Label>
              <Input id="password" type="password" value={password} onChange={e => setPassword(e.target.value)} required minLength={8} className="border-[#e8c9a0] focus-visible:ring-[#c96a2b]" />
            </div>
            <div className="flex flex-col gap-1.5">
              <Label htmlFor="confirm" className="text-[#3d1f08]">Confirm password</Label>
              <Input id="confirm" type="password" value={confirmPassword} onChange={e => setConfirmPassword(e.target.value)} required className="border-[#e8c9a0] focus-visible:ring-[#c96a2b]" />
            </div>
            <Button type="submit" disabled={loading} className="bg-[#c96a2b] hover:bg-[#a8571f] text-white w-full">
              {loading ? 'Saving…' : 'Set password'}
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  )
}
```

- [ ] **Step 5: Verify build + tests**

```bash
cd frontend && npm run build && npm test
```

Expected: build succeeds, tests pass.

- [ ] **Step 6: Commit**

```bash
git add frontend/src/pages/LoginPage.tsx frontend/src/pages/RegisterPage.tsx frontend/src/pages/ForgotPasswordPage.tsx frontend/src/pages/ResetPasswordPage.tsx
git commit -m "feat: restyle auth pages with warm card design"
```

---

### Task 5: Restyle Dashboard

**Files:**
- Modify: `frontend/src/pages/DashboardPage.tsx`

- [ ] **Step 1: Replace `frontend/src/pages/DashboardPage.tsx`**

```tsx
import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { Card, CardContent } from '../components/ui/card'

export default function DashboardPage() {
  const { currentEmail } = useAuth()

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold text-[#3d1f08] mb-2">Welcome back</h1>
      <p className="text-[#7a5c3a] mb-8">{currentEmail}</p>

      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <Link to="/recipes">
          <Card className="border-[#e8c9a0] shadow-sm hover:shadow-md transition-shadow cursor-pointer">
            <CardContent className="p-6 flex flex-col items-center gap-2">
              <span className="text-4xl">📖</span>
              <span className="font-semibold text-[#3d1f08]">Recipes</span>
              <span className="text-sm text-[#7a5c3a]">Browse your recipes</span>
            </CardContent>
          </Card>
        </Link>

        <Link to="/shopping-lists">
          <Card className="border-[#e8c9a0] shadow-sm hover:shadow-md transition-shadow cursor-pointer">
            <CardContent className="p-6 flex flex-col items-center gap-2">
              <span className="text-4xl">🛒</span>
              <span className="font-semibold text-[#3d1f08]">Shopping Lists</span>
              <span className="text-sm text-[#7a5c3a]">Manage your lists</span>
            </CardContent>
          </Card>
        </Link>

        <Link to="/products">
          <Card className="border-[#e8c9a0] shadow-sm hover:shadow-md transition-shadow cursor-pointer">
            <CardContent className="p-6 flex flex-col items-center gap-2">
              <span className="text-4xl">📦</span>
              <span className="font-semibold text-[#3d1f08]">Products</span>
              <span className="text-sm text-[#7a5c3a]">Manage products</span>
            </CardContent>
          </Card>
        </Link>
      </div>
    </div>
  )
}
```

- [ ] **Step 2: Build**

```bash
cd frontend && npm run build
```

- [ ] **Step 3: Commit**

```bash
git add frontend/src/pages/DashboardPage.tsx
git commit -m "feat: restyle dashboard with stat cards"
```

---

### Task 6: Restyle Recipes pages

**Files:**
- Modify: `frontend/src/pages/RecipesPage.tsx`
- Modify: `frontend/src/pages/RecipeDetailPage.tsx`

- [ ] **Step 1: Replace `frontend/src/pages/RecipesPage.tsx`**

```tsx
import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Link, useNavigate } from 'react-router-dom'
import { getRecipes, createRecipe } from '../api/recipes'
import { Button } from '../components/ui/button'
import { Input } from '../components/ui/input'
import { ChevronRight } from 'lucide-react'

export default function RecipesPage() {
  const qc = useQueryClient()
  const navigate = useNavigate()
  const [name, setName] = useState('')
  const [showForm, setShowForm] = useState(false)
  const { data: recipes = [], isLoading } = useQuery({ queryKey: ['recipes'], queryFn: getRecipes })
  const create = useMutation({
    mutationFn: createRecipe,
    onSuccess: (recipe) => { qc.invalidateQueries({ queryKey: ['recipes'] }); navigate(`/recipes/${recipe.id}`) }
  })

  if (isLoading) return <div className="max-w-4xl mx-auto px-4 py-8 text-[#7a5c3a]">Loading…</div>

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-[#3d1f08]">Recipes</h1>
        <Button
          onClick={() => setShowForm(v => !v)}
          className="bg-[#c96a2b] hover:bg-[#a8571f] text-white"
        >
          + New Recipe
        </Button>
      </div>

      {showForm && (
        <form
          onSubmit={e => { e.preventDefault(); create.mutate({ name }); setShowForm(false); setName('') }}
          className="flex gap-2 mb-6 bg-white border border-[#e8c9a0] rounded-lg p-4"
        >
          <Input
            value={name}
            onChange={e => setName(e.target.value)}
            placeholder="Recipe name"
            required
            className="border-[#e8c9a0] flex-1"
            autoFocus
          />
          <Button type="submit" className="bg-[#c96a2b] hover:bg-[#a8571f] text-white">Create</Button>
          <Button type="button" variant="outline" onClick={() => setShowForm(false)} className="border-[#e8c9a0] text-[#7a5c3a]">Cancel</Button>
        </form>
      )}

      {recipes.length === 0 ? (
        <p className="text-[#7a5c3a] text-center py-12">No recipes yet. Create your first one!</p>
      ) : (
        <div className="flex flex-col gap-2">
          {recipes.map(r => (
            <Link key={r.id} to={`/recipes/${r.id}`}>
              <div className="bg-white border border-[#e8c9a0] rounded-lg px-4 py-3 flex items-center gap-3 hover:border-[#c96a2b] transition-colors">
                {r.previewImage
                  ? <img src={`/recipe-images/${r.previewImage}`} alt={r.name} className="w-10 h-10 rounded object-cover flex-shrink-0" />
                  : <span className="text-2xl flex-shrink-0">🍽</span>
                }
                <span className="font-medium text-[#3d1f08] flex-1">{r.name}</span>
                <ChevronRight className="text-[#c96a2b] w-4 h-4 flex-shrink-0" />
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  )
}
```

- [ ] **Step 2: Replace `frontend/src/pages/RecipeDetailPage.tsx`**

```tsx
import { useState } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getRecipe, addIngredient, deleteIngredient, addIngredientToList,
         removeIngredientFromList, uploadImages, deleteImage, deleteRecipe, updateRecipe } from '../api/recipes'
import { searchProducts } from '../api/products'
import { Button } from '../components/ui/button'
import { Input } from '../components/ui/input'
import { Label } from '../components/ui/label'
import { ChevronLeft, Trash2, Pencil, Plus, Minus, X } from 'lucide-react'

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
    onSuccess: () => { invalidate(); setProductSearch(''); setSelectedProductId(null); setQuantity('') }
  })

  const removeIng = useMutation({ mutationFn: (ingId: number) => deleteIngredient(recipeId, ingId), onSuccess: invalidate })
  const addToList = useMutation({ mutationFn: (ingId: number) => addIngredientToList(recipeId, ingId, selectedListId!), onSuccess: invalidate })
  const removeFromList = useMutation({ mutationFn: (ingId: number) => removeIngredientFromList(recipeId, ingId, selectedListId!), onSuccess: invalidate })
  const delRecipe = useMutation({ mutationFn: () => deleteRecipe(recipeId), onSuccess: () => navigate('/recipes') })
  const editRecipe = useMutation({ mutationFn: () => updateRecipe(recipeId, { name: editName, description: editDesc }),
    onSuccess: () => { setEditing(false); invalidate() }
  })
  const uploadImg = useMutation({ mutationFn: (files: FileList) => uploadImages(recipeId, files), onSuccess: invalidate })
  const delImg = useMutation({ mutationFn: (imgId: number) => deleteImage(recipeId, imgId), onSuccess: invalidate })

  if (isLoading || !recipe) return <div className="max-w-4xl mx-auto px-4 py-8 text-[#7a5c3a]">Loading…</div>

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <Link to="/recipes" className="inline-flex items-center gap-1 text-[#7a5c3a] hover:text-[#c96a2b] text-sm mb-6">
        <ChevronLeft className="w-4 h-4" /> Back to recipes
      </Link>

      {/* Title / edit section */}
      <div className="bg-white border border-[#e8c9a0] rounded-lg p-6 mb-6">
        {editing ? (
          <form onSubmit={e => { e.preventDefault(); editRecipe.mutate() }} className="flex flex-col gap-3">
            <Input value={editName} onChange={e => setEditName(e.target.value)} required className="border-[#e8c9a0] text-lg font-bold" />
            <textarea
              value={editDesc}
              onChange={e => setEditDesc(e.target.value)}
              rows={3}
              className="w-full border border-[#e8c9a0] rounded-md px-3 py-2 text-sm resize-none focus:outline-none focus:ring-2 focus:ring-[#c96a2b]"
            />
            <div className="flex gap-2">
              <Button type="submit" className="bg-[#c96a2b] hover:bg-[#a8571f] text-white">Save</Button>
              <Button type="button" variant="outline" onClick={() => setEditing(false)} className="border-[#e8c9a0] text-[#7a5c3a]">Cancel</Button>
            </div>
          </form>
        ) : (
          <div>
            <div className="flex items-start justify-between gap-4 mb-2">
              <h1 className="text-2xl font-bold text-[#3d1f08]">{recipe.name}</h1>
              <div className="flex gap-2 flex-shrink-0">
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => { setEditing(true); setEditName(recipe.name); setEditDesc(recipe.description) }}
                  className="border-[#e8c9a0] text-[#7a5c3a] hover:text-[#3d1f08]"
                >
                  <Pencil className="w-3.5 h-3.5 mr-1" /> Edit
                </Button>
                <Button
                  variant="destructive"
                  size="sm"
                  onClick={() => { if (confirm('Delete this recipe?')) delRecipe.mutate() }}
                >
                  <Trash2 className="w-3.5 h-3.5 mr-1" /> Delete
                </Button>
              </div>
            </div>
            {recipe.description && <p className="text-[#7a5c3a] text-sm">{recipe.description}</p>}
          </div>
        )}
      </div>

      {/* Shopping list selector */}
      <div className="bg-white border border-[#e8c9a0] rounded-lg p-4 mb-6 flex items-center gap-3">
        <Label htmlFor="list-select" className="text-[#3d1f08] whitespace-nowrap">Shopping list:</Label>
        <select
          id="list-select"
          value={selectedListId ?? ''}
          onChange={e => setSelectedListId(e.target.value ? Number(e.target.value) : undefined)}
          className="border border-[#e8c9a0] rounded-md px-2 py-1.5 text-sm text-[#3d1f08] bg-white flex-1"
        >
          <option value="">— none —</option>
          {recipe.shoppingLists.map(l => <option key={l.id} value={l.id}>{l.name}</option>)}
        </select>
      </div>

      {/* Ingredients */}
      <div className="bg-white border border-[#e8c9a0] rounded-lg p-6 mb-6">
        <h2 className="text-lg font-semibold text-[#3d1f08] mb-4">Ingredients</h2>
        {recipe.ingredients.length === 0 ? (
          <p className="text-[#7a5c3a] text-sm">No ingredients yet.</p>
        ) : (
          <div className="flex flex-col gap-2 mb-4">
            {recipe.ingredients.map(ing => (
              <div key={ing.id} className="flex items-center gap-2 py-2 border-b border-[#f0e0cc] last:border-0">
                <span className="text-[#7a5c3a] text-sm flex-1">
                  {ing.quantity && `${ing.quantity} ${ing.unit} `}{ing.productName}
                </span>
                {selectedListId && (
                  <div className="flex items-center gap-1">
                    <button onClick={() => removeFromList.mutate(ing.id)} className="text-[#7a5c3a] hover:text-[#c96a2b] p-1">
                      <Minus className="w-3 h-3" />
                    </button>
                    <span className="text-sm text-[#3d1f08] w-5 text-center">{recipe.ingredientCounts[ing.id] ?? 0}</span>
                    <button onClick={() => addToList.mutate(ing.id)} className="text-[#7a5c3a] hover:text-[#c96a2b] p-1">
                      <Plus className="w-3 h-3" />
                    </button>
                  </div>
                )}
                <button onClick={() => removeIng.mutate(ing.id)} className="text-[#7a5c3a] hover:text-red-500 p-1">
                  <X className="w-3.5 h-3.5" />
                </button>
              </div>
            ))}
          </div>
        )}

        {/* Add ingredient form */}
        <form onSubmit={e => { e.preventDefault(); addIng.mutate() }} className="flex flex-wrap gap-2 items-end border-t border-[#f0e0cc] pt-4">
          <div className="flex flex-col gap-1 flex-1 min-w-[140px] relative">
            <Label className="text-[#3d1f08] text-xs">Product</Label>
            <Input
              placeholder="Search product…"
              value={productSearch}
              onChange={e => { setProductSearch(e.target.value); setSelectedProductId(null) }}
              className="border-[#e8c9a0]"
            />
            {searchResults.length > 0 && !selectedProductId && (
              <div className="absolute top-full left-0 right-0 bg-white border border-[#e8c9a0] rounded-md shadow-md z-10 mt-1">
                {searchResults.map(p => (
                  <button
                    key={p.id}
                    type="button"
                    onClick={() => { setSelectedProductId(p.id); setProductSearch(p.name) }}
                    className="w-full text-left px-3 py-2 text-sm text-[#3d1f08] hover:bg-[#fdf0e0]"
                  >
                    {p.name}
                  </button>
                ))}
              </div>
            )}
          </div>
          <div className="flex flex-col gap-1 w-20">
            <Label className="text-[#3d1f08] text-xs">Qty</Label>
            <Input type="number" placeholder="0" value={quantity} onChange={e => setQuantity(e.target.value)} className="border-[#e8c9a0]" />
          </div>
          <div className="flex flex-col gap-1 w-24">
            <Label className="text-[#3d1f08] text-xs">Unit</Label>
            <select value={unit} onChange={e => setUnit(e.target.value)} className="border border-[#e8c9a0] rounded-md px-2 py-2 text-sm bg-white">
              {UNITS.map(u => <option key={u} value={u}>{u}</option>)}
            </select>
          </div>
          <Button type="submit" disabled={!selectedProductId} className="bg-[#c96a2b] hover:bg-[#a8571f] text-white self-end">
            Add
          </Button>
        </form>
      </div>

      {/* Photos */}
      <div className="bg-white border border-[#e8c9a0] rounded-lg p-6">
        <h2 className="text-lg font-semibold text-[#3d1f08] mb-4">Photos</h2>
        {recipe.images.length > 0 && (
          <div className="flex flex-wrap gap-3 mb-4">
            {recipe.images.map(img => (
              <div key={img.id} className="relative group">
                <img src={`/recipe-images/${img.filename}`} alt={img.originalName} className="w-28 h-28 object-cover rounded-lg border border-[#e8c9a0]" />
                <button
                  onClick={() => delImg.mutate(img.id)}
                  className="absolute top-1 right-1 bg-red-600 text-white rounded-full w-5 h-5 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity"
                >
                  <X className="w-3 h-3" />
                </button>
              </div>
            ))}
          </div>
        )}
        <label className="inline-flex items-center gap-2 cursor-pointer text-sm text-[#c96a2b] hover:text-[#a8571f]">
          <span>+ Add photos</span>
          <input type="file" multiple accept="image/*" className="hidden"
            onChange={e => { if (e.target.files?.length) uploadImg.mutate(e.target.files) }} />
        </label>
      </div>
    </div>
  )
}
```

- [ ] **Step 3: Build**

```bash
cd frontend && npm run build
```

- [ ] **Step 4: Commit**

```bash
git add frontend/src/pages/RecipesPage.tsx frontend/src/pages/RecipeDetailPage.tsx
git commit -m "feat: restyle recipes pages"
```

---

### Task 7: Restyle Shopping List pages

**Files:**
- Modify: `frontend/src/pages/ShoppingListsPage.tsx`
- Modify: `frontend/src/pages/ShoppingListDetailPage.tsx`

- [ ] **Step 1: Replace `frontend/src/pages/ShoppingListsPage.tsx`**

```tsx
import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { getShoppingLists, createShoppingList } from '../api/shoppingLists'
import { Button } from '../components/ui/button'
import { Input } from '../components/ui/input'
import { Badge } from '../components/ui/badge'
import { ChevronRight } from 'lucide-react'

export default function ShoppingListsPage() {
  const qc = useQueryClient()
  const [name, setName] = useState('')
  const [showForm, setShowForm] = useState(false)
  const { data, isLoading } = useQuery({ queryKey: ['shopping-lists'], queryFn: getShoppingLists })
  const create = useMutation({
    mutationFn: createShoppingList,
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['shopping-lists'] }); setName(''); setShowForm(false) }
  })

  if (isLoading) return <div className="max-w-4xl mx-auto px-4 py-8 text-[#7a5c3a]">Loading…</div>

  const owned = data?.owned ?? []
  const shared = data?.shared ?? []

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-[#3d1f08]">Shopping Lists</h1>
        <Button onClick={() => setShowForm(v => !v)} className="bg-[#c96a2b] hover:bg-[#a8571f] text-white">
          + New List
        </Button>
      </div>

      {showForm && (
        <form
          onSubmit={e => { e.preventDefault(); create.mutate(name) }}
          className="flex gap-2 mb-6 bg-white border border-[#e8c9a0] rounded-lg p-4"
        >
          <Input value={name} onChange={e => setName(e.target.value)} placeholder="List name" required className="border-[#e8c9a0] flex-1" autoFocus />
          <Button type="submit" className="bg-[#c96a2b] hover:bg-[#a8571f] text-white">Create</Button>
          <Button type="button" variant="outline" onClick={() => setShowForm(false)} className="border-[#e8c9a0] text-[#7a5c3a]">Cancel</Button>
        </form>
      )}

      {owned.length > 0 && (
        <div className="mb-6">
          <h2 className="text-sm font-semibold text-[#7a5c3a] uppercase tracking-wide mb-2">My Lists</h2>
          <div className="flex flex-col gap-2">
            {owned.map(l => (
              <Link key={l.id} to={`/shopping-lists/${l.id}`}>
                <div className="bg-white border border-[#e8c9a0] rounded-lg px-4 py-3 flex items-center gap-3 hover:border-[#c96a2b] transition-colors">
                  <span className="text-xl">🛒</span>
                  <span className="font-medium text-[#3d1f08] flex-1">{l.name}</span>
                  <ChevronRight className="text-[#c96a2b] w-4 h-4" />
                </div>
              </Link>
            ))}
          </div>
        </div>
      )}

      {shared.length > 0 && (
        <div>
          <h2 className="text-sm font-semibold text-[#7a5c3a] uppercase tracking-wide mb-2">Shared With Me</h2>
          <div className="flex flex-col gap-2">
            {shared.map(l => (
              <Link key={l.id} to={`/shopping-lists/${l.id}`}>
                <div className="bg-white border border-[#e8c9a0] rounded-lg px-4 py-3 flex items-center gap-3 hover:border-[#c96a2b] transition-colors">
                  <span className="text-xl">🛒</span>
                  <span className="font-medium text-[#3d1f08] flex-1">{l.name}</span>
                  <Badge variant="outline" className="border-[#e8c9a0] text-[#7a5c3a] text-xs">{l.ownerEmail}</Badge>
                  <ChevronRight className="text-[#c96a2b] w-4 h-4" />
                </div>
              </Link>
            ))}
          </div>
        </div>
      )}

      {owned.length === 0 && shared.length === 0 && (
        <p className="text-[#7a5c3a] text-center py-12">No lists yet. Create your first one!</p>
      )}
    </div>
  )
}
```

- [ ] **Step 2: Replace `frontend/src/pages/ShoppingListDetailPage.tsx`**

```tsx
import { useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getShoppingList, addItem, toggleItem, deleteItem, clearChecked, shareList } from '../api/shoppingLists'
import type { ShoppingListDetail } from '../api/types'
import { Button } from '../components/ui/button'
import { Input } from '../components/ui/input'
import { ChevronLeft, X } from 'lucide-react'

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

  const toggle = useMutation({
    mutationFn: (itemId: number) => toggleItem(listId, itemId),
    onMutate: async (itemId) => {
      await qc.cancelQueries({ queryKey: ['shopping-list', listId] })
      const prev = qc.getQueryData(['shopping-list', listId])
      qc.setQueryData(['shopping-list', listId], (old: ShoppingListDetail | undefined) => old ? {
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

  if (isLoading || !list) return <div className="max-w-4xl mx-auto px-4 py-8 text-[#7a5c3a]">Loading…</div>

  const groupedByCategory = list.items.reduce<Record<string, typeof list.items>>((acc, item) => {
    const key = item.categoryName ?? 'Uncategorized'
    acc[key] = [...(acc[key] ?? []), item]
    return acc
  }, {})

  const checkedCount = list.items.filter(i => i.checked).length

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <Link to="/shopping-lists" className="inline-flex items-center gap-1 text-[#7a5c3a] hover:text-[#c96a2b] text-sm mb-6">
        <ChevronLeft className="w-4 h-4" /> Back to lists
      </Link>

      <div className="flex items-center justify-between mb-2">
        <h1 className="text-2xl font-bold text-[#3d1f08]">{list.name}</h1>
        {checkedCount > 0 && (
          <Button variant="outline" size="sm" onClick={() => clear.mutate()} className="border-[#e8c9a0] text-[#7a5c3a] hover:text-red-600">
            Clear checked ({checkedCount})
          </Button>
        )}
      </div>
      <p className="text-[#7a5c3a] text-sm mb-6">
        {list.items.length} items · Total €{list.totalPrice.toFixed(2)}
      </p>

      {/* Add item form */}
      <form onSubmit={e => { e.preventDefault(); add.mutate() }} className="bg-white border border-[#e8c9a0] rounded-lg p-4 mb-6 flex flex-wrap gap-2 items-end">
        <div className="flex flex-col gap-1 flex-1 min-w-[140px]">
          <label className="text-xs text-[#7a5c3a]">Item</label>
          <Input value={itemName} onChange={e => setItemName(e.target.value)} placeholder="Item name" required className="border-[#e8c9a0]" />
        </div>
        <div className="flex flex-col gap-1 w-20">
          <label className="text-xs text-[#7a5c3a]">Qty</label>
          <Input type="number" value={count} onChange={e => setCount(e.target.value)} placeholder="0" className="border-[#e8c9a0]" />
        </div>
        <div className="flex flex-col gap-1 w-24">
          <label className="text-xs text-[#7a5c3a]">Unit price</label>
          <Input type="number" value={unitPrice} onChange={e => setUnitPrice(e.target.value)} placeholder="0.00" className="border-[#e8c9a0]" />
        </div>
        <Button type="submit" className="bg-[#c96a2b] hover:bg-[#a8571f] text-white self-end">Add</Button>
      </form>

      {/* Items grouped by category */}
      <div className="flex flex-col gap-4">
        {Object.entries(groupedByCategory).map(([category, items]) => (
          <div key={category} className="bg-white border border-[#e8c9a0] rounded-lg overflow-hidden">
            <div className="px-4 py-2 bg-[#fdf0e0] border-b border-[#e8c9a0]">
              <h3 className="text-sm font-semibold text-[#7a5c3a] uppercase tracking-wide">{category}</h3>
            </div>
            <div>
              {items.map(item => (
                <div key={item.id} className="flex items-center gap-3 px-4 py-3 border-b border-[#f5ebe0] last:border-0">
                  <input
                    type="checkbox"
                    checked={item.checked}
                    onChange={() => toggle.mutate(item.id)}
                    className="w-4 h-4 accent-[#c96a2b] flex-shrink-0"
                  />
                  <span className={`flex-1 text-sm ${item.checked ? 'line-through text-[#b0a090]' : 'text-[#3d1f08]'}`}>
                    {item.count && `${item.count} × `}{item.name}
                    {item.totalPrice != null && <span className="text-[#7a5c3a] ml-2">€{item.totalPrice.toFixed(2)}</span>}
                  </span>
                  <button onClick={() => remove.mutate(item.id)} className="text-[#c9b09a] hover:text-red-500 flex-shrink-0">
                    <X className="w-3.5 h-3.5" />
                  </button>
                </div>
              ))}
            </div>
          </div>
        ))}
      </div>

      {/* Share section */}
      {list.isOwner && (
        <div className="mt-8 bg-white border border-[#e8c9a0] rounded-lg p-6">
          <h3 className="text-base font-semibold text-[#3d1f08] mb-3">Share list</h3>
          {shareError && <p role="alert" className="text-red-600 text-sm mb-3">{shareError}</p>}
          <form onSubmit={e => { e.preventDefault(); share.mutate() }} className="flex gap-2">
            <Input type="email" value={shareEmail} onChange={e => setShareEmail(e.target.value)} placeholder="Email to share with" required className="border-[#e8c9a0] flex-1" />
            <Button type="submit" variant="outline" className="border-[#e8c9a0] text-[#7a5c3a] hover:text-[#3d1f08]">Share</Button>
          </form>
        </div>
      )}
    </div>
  )
}
```

- [ ] **Step 3: Build**

```bash
cd frontend && npm run build
```

- [ ] **Step 4: Commit**

```bash
git add frontend/src/pages/ShoppingListsPage.tsx frontend/src/pages/ShoppingListDetailPage.tsx
git commit -m "feat: restyle shopping list pages"
```

---

### Task 8: Restyle Products, Uncategorized Products, and User pages

**Files:**
- Modify: `frontend/src/pages/ProductsPage.tsx`
- Modify: `frontend/src/pages/UncategorizedProductsPage.tsx`
- Modify: `frontend/src/pages/UserPage.tsx`

> **ProductsPage** gets a structural change: add `selectedCategoryId` state and a two-column layout (left 1/3 = categories, right 2/3 = filtered products). The existing API calls are unchanged.

- [ ] **Step 1: Replace `frontend/src/pages/ProductsPage.tsx`**

```tsx
import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getProducts, getCategories, createProduct, updateProduct, deleteProduct,
         createCategory, deleteCategory } from '../api/products'
import { Button } from '../components/ui/button'
import { Input } from '../components/ui/input'
import { Trash2, Pencil, Check, X } from 'lucide-react'

export default function ProductsPage() {
  const qc = useQueryClient()
  const { data: products = [] } = useQuery({ queryKey: ['products'], queryFn: getProducts })
  const { data: categories = [] } = useQuery({ queryKey: ['categories'], queryFn: getCategories })

  const [selectedCategoryId, setSelectedCategoryId] = useState<number | null>(null)
  const [newName, setNewName] = useState('')
  const [newPrice, setNewPrice] = useState('')
  const [newCatId, setNewCatId] = useState('')
  const [catName, setCatName] = useState('')
  const [catColor, setCatColor] = useState('#c96a2b')
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
    onSuccess: () => { inv(['categories']); setCatName(''); setCatColor('#c96a2b') }
  })

  const deleteCat = useMutation({ mutationFn: (id: number) => deleteCategory(id), onSuccess: () => inv(['categories']) })

  const visibleProducts = selectedCategoryId === null
    ? products
    : products.filter(p => p.categoryId === selectedCategoryId)

  return (
    <div className="max-w-5xl mx-auto px-4 py-8">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-[#3d1f08]">Products</h1>
        <Link to="/products/uncategorized" className="text-sm text-[#7a5c3a] hover:text-[#c96a2b]">
          Uncategorized →
        </Link>
      </div>

      <div className="flex gap-6">
        {/* Left: Categories */}
        <div className="w-1/3 flex-shrink-0">
          <h2 className="text-sm font-semibold text-[#7a5c3a] uppercase tracking-wide mb-2">Categories</h2>
          <div className="flex flex-col gap-1 mb-4">
            <button
              onClick={() => setSelectedCategoryId(null)}
              className={`text-left px-3 py-2 rounded-md text-sm font-medium transition-colors ${
                selectedCategoryId === null
                  ? 'bg-[#c96a2b] text-white'
                  : 'text-[#3d1f08] hover:bg-[#fdf0e0]'
              }`}
            >
              All products
            </button>
            {categories.map(c => (
              <div key={c.id} className="flex items-center gap-1">
                <button
                  onClick={() => setSelectedCategoryId(c.id)}
                  className={`flex-1 text-left px-3 py-2 rounded-md text-sm transition-colors flex items-center gap-2 ${
                    selectedCategoryId === c.id
                      ? 'bg-[#c96a2b] text-white'
                      : 'text-[#3d1f08] hover:bg-[#fdf0e0]'
                  }`}
                >
                  <span className="w-2.5 h-2.5 rounded-full flex-shrink-0" style={{ background: c.color }} />
                  {c.name}
                </button>
                <button
                  onClick={() => deleteCat.mutate(c.id)}
                  className="p-1.5 text-[#c9b09a] hover:text-red-500 rounded"
                >
                  <Trash2 className="w-3 h-3" />
                </button>
              </div>
            ))}
          </div>

          {/* Add category form */}
          <form onSubmit={e => { e.preventDefault(); createCat.mutate() }} className="bg-white border border-[#e8c9a0] rounded-lg p-3 flex flex-col gap-2">
            <p className="text-xs font-semibold text-[#7a5c3a]">+ Add category</p>
            <Input value={catName} onChange={e => setCatName(e.target.value)} placeholder="Category name" required className="border-[#e8c9a0] text-sm h-8" />
            <div className="flex gap-2 items-center">
              <input type="color" value={catColor} onChange={e => setCatColor(e.target.value)} className="w-8 h-8 rounded border border-[#e8c9a0] cursor-pointer" />
              <Button type="submit" size="sm" className="bg-[#c96a2b] hover:bg-[#a8571f] text-white flex-1 h-8">Add</Button>
            </div>
          </form>
        </div>

        {/* Right: Products */}
        <div className="flex-1">
          <h2 className="text-sm font-semibold text-[#7a5c3a] uppercase tracking-wide mb-2">
            {selectedCategoryId === null ? 'All Products' : (categories.find(c => c.id === selectedCategoryId)?.name ?? 'Products')}
          </h2>

          {/* Add product form */}
          <form onSubmit={e => { e.preventDefault(); create.mutate() }} className="bg-white border border-[#e8c9a0] rounded-lg p-3 mb-3 flex flex-wrap gap-2 items-end">
            <Input value={newName} onChange={e => setNewName(e.target.value)} placeholder="Product name" required className="border-[#e8c9a0] flex-1 min-w-[120px] h-8 text-sm" />
            <Input type="number" value={newPrice} onChange={e => setNewPrice(e.target.value)} placeholder="Price" className="border-[#e8c9a0] w-20 h-8 text-sm" />
            <select value={newCatId} onChange={e => setNewCatId(e.target.value)} className="border border-[#e8c9a0] rounded-md px-2 h-8 text-sm bg-white">
              <option value="">No category</option>
              {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
            </select>
            <Button type="submit" size="sm" className="bg-[#c96a2b] hover:bg-[#a8571f] text-white h-8">Add</Button>
          </form>

          {visibleProducts.length === 0 ? (
            <p className="text-[#7a5c3a] text-sm py-6 text-center">No products here yet.</p>
          ) : (
            <div className="flex flex-col gap-1">
              {visibleProducts.map(p => (
                <div key={p.id} className="bg-white border border-[#e8c9a0] rounded-lg px-3 py-2">
                  {editId === p.id ? (
                    <form onSubmit={e => { e.preventDefault(); edit.mutate() }} className="flex flex-wrap gap-2 items-center">
                      <Input value={editName} onChange={e => setEditName(e.target.value)} required className="border-[#e8c9a0] flex-1 h-7 text-sm" />
                      <Input type="number" value={editPrice} onChange={e => setEditPrice(e.target.value)} className="border-[#e8c9a0] w-20 h-7 text-sm" />
                      <select value={editCatId} onChange={e => setEditCatId(e.target.value)} className="border border-[#e8c9a0] rounded px-2 h-7 text-sm bg-white">
                        <option value="">No category</option>
                        {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                      </select>
                      <button type="submit" className="text-green-600 hover:text-green-800 p-1"><Check className="w-4 h-4" /></button>
                      <button type="button" onClick={() => setEditId(null)} className="text-[#7a5c3a] hover:text-[#3d1f08] p-1"><X className="w-4 h-4" /></button>
                    </form>
                  ) : (
                    <div className="flex items-center gap-2">
                      {p.categoryColor && (
                        <span className="w-2 h-2 rounded-full flex-shrink-0" style={{ background: p.categoryColor }} />
                      )}
                      <span className="flex-1 text-sm text-[#3d1f08]">{p.name}</span>
                      {p.price != null && <span className="text-sm text-[#7a5c3a]">€{p.price.toFixed(2)}</span>}
                      <button
                        onClick={() => { setEditId(p.id); setEditName(p.name); setEditPrice(p.price?.toString() ?? ''); setEditCatId(p.categoryId?.toString() ?? '') }}
                        className="text-[#c9b09a] hover:text-[#c96a2b] p-1"
                      >
                        <Pencil className="w-3 h-3" />
                      </button>
                      <button onClick={() => del.mutate(p.id)} className="text-[#c9b09a] hover:text-red-500 p-1">
                        <Trash2 className="w-3 h-3" />
                      </button>
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
```

- [ ] **Step 2: Replace `frontend/src/pages/UncategorizedProductsPage.tsx`**

```tsx
import { Link } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getUncategorizedProducts, getCategories, updateProduct } from '../api/products'
import { ChevronLeft } from 'lucide-react'

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
    <div className="max-w-4xl mx-auto px-4 py-8">
      <Link to="/products" className="inline-flex items-center gap-1 text-[#7a5c3a] hover:text-[#c96a2b] text-sm mb-6">
        <ChevronLeft className="w-4 h-4" /> Back to products
      </Link>
      <h1 className="text-2xl font-bold text-[#3d1f08] mb-6">Uncategorized Products</h1>
      {products.length === 0 ? (
        <p className="text-[#7a5c3a] text-center py-12">All products have a category. 🎉</p>
      ) : (
        <div className="flex flex-col gap-2">
          {products.map(p => (
            <div key={p.id} className="bg-white border border-[#e8c9a0] rounded-lg px-4 py-3 flex items-center gap-3">
              <span className="font-medium text-[#3d1f08] flex-1">{p.name}</span>
              <select
                defaultValue=""
                onChange={e => { if (e.target.value) assign.mutate({ id: p.id, categoryId: Number(e.target.value) }) }}
                className="border border-[#e8c9a0] rounded-md px-2 py-1.5 text-sm bg-white text-[#3d1f08]"
              >
                <option value="" disabled>Assign category…</option>
                {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
              </select>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
```

- [ ] **Step 3: Replace `frontend/src/pages/UserPage.tsx`**

```tsx
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { getProfile, setDefaultList, leaveList } from '../api/user'
import { useAuth } from '../context/AuthContext'
import { Button } from '../components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '../components/ui/card'

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

  if (isLoading || !profile) return <div className="max-w-4xl mx-auto px-4 py-8 text-[#7a5c3a]">Loading…</div>

  return (
    <div className="max-w-2xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold text-[#3d1f08] mb-6">Account</h1>

      <Card className="border-[#e8c9a0] shadow-sm mb-6">
        <CardHeader className="pb-2">
          <CardTitle className="text-base text-[#3d1f08]">Profile</CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-[#7a5c3a]">{profile.email}</p>
        </CardContent>
      </Card>

      <Card className="border-[#e8c9a0] shadow-sm mb-6">
        <CardHeader className="pb-2">
          <CardTitle className="text-base text-[#3d1f08]">Default Shopping List</CardTitle>
        </CardHeader>
        <CardContent>
          <select
            value={profile.defaultListId ?? ''}
            onChange={e => setDefault.mutate(e.target.value ? Number(e.target.value) : null)}
            className="border border-[#e8c9a0] rounded-md px-3 py-2 text-sm bg-white text-[#3d1f08] w-full"
          >
            <option value="">None</option>
            {profile.allLists.map(l => <option key={l.id} value={l.id}>{l.name}</option>)}
          </select>
        </CardContent>
      </Card>

      {profile.sharedLists.length > 0 && (
        <Card className="border-[#e8c9a0] shadow-sm mb-6">
          <CardHeader className="pb-2">
            <CardTitle className="text-base text-[#3d1f08]">Shared Lists</CardTitle>
          </CardHeader>
          <CardContent className="flex flex-col gap-2 p-0">
            {profile.sharedLists.map(l => (
              <div key={l.id} className="flex items-center gap-3 px-6 py-3 border-b border-[#f5ebe0] last:border-0">
                <div className="flex-1">
                  <p className="text-sm font-medium text-[#3d1f08]">{l.name}</p>
                  <p className="text-xs text-[#7a5c3a]">by {l.ownerEmail}</p>
                </div>
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => leave.mutate(l.id)}
                  className="border-[#e8c9a0] text-[#7a5c3a] hover:border-red-300 hover:text-red-600"
                >
                  Leave
                </Button>
              </div>
            ))}
          </CardContent>
        </Card>
      )}

      <Button
        variant="destructive"
        onClick={handleLogout}
        className="w-full"
      >
        Sign out
      </Button>
    </div>
  )
}
```

- [ ] **Step 4: Build + tests**

```bash
cd frontend && npm run build && npm test
```

Expected: build succeeds, all tests pass.

- [ ] **Step 5: Commit**

```bash
git add frontend/src/pages/ProductsPage.tsx frontend/src/pages/UncategorizedProductsPage.tsx frontend/src/pages/UserPage.tsx
git commit -m "feat: restyle products and user pages"
```

---

### Task 9: Final build, Maven package, and deploy

**Files:** none (verification only)

- [ ] **Step 1: Run full frontend build + tests**

```bash
cd frontend && npm run build && npm test
```

Expected: BUILD successful, all tests pass.

- [ ] **Step 2: Maven package (builds frontend + Spring Boot JAR)**

```bash
cd .. && mvn clean package -Dmaven.test.skip=true
```

Expected: BUILD SUCCESS, JAR created at `target/dinner-service-0.0.1-SNAPSHOT.jar`

- [ ] **Step 3: Commit any uncommitted changes, then push**

```bash
git add -A
git status  # check nothing sensitive is staged
git push origin master
```

- [ ] **Step 4: Deploy on server**

SSH to the server and run:

```bash
cd /projects/dinner-service
sudo systemctl stop dinner-service
git pull origin master
mvn clean package -Dmaven.test.skip=true
sudo systemctl start dinner-service
sleep 8
curl -s -o /dev/null -w "%{http_code}" http://localhost:8090/
```

Expected: `200`
