# UI Styling Design

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add Tailwind CSS + shadcn/ui to the existing React SPA with a warm, food-focused visual design.

**Architecture:** Install Tailwind CSS v3 and shadcn/ui into the existing Vite/React frontend. Configure a custom warm color palette. Restyle all existing pages in place — no new routes, no new API calls.

**Tech Stack:** Tailwind CSS v3, shadcn/ui, postcss, autoprefixer, Vite

---

## Color Palette

| Token (CSS variable) | Value | Used for |
|---|---|---|
| `--primary` | `#c96a2b` | Buttons, active nav item, links |
| `--primary-foreground` | `#fff8f0` | Text on primary bg |
| `--background` | `#fdf6ee` | Page background |
| `--nav-bg` | `#7a3a1a` | Top navbar background |
| `--nav-text` | `#f5e6d3` | Navbar text/icons |
| `--card` | `#ffffff` | Card backgrounds |
| `--border` | `#e8c9a0` | Card and input borders |
| `--foreground` | `#3d1f08` | Headings and primary text |
| `--muted-foreground` | `#7a5c3a` | Secondary text |
| `--destructive` | `#dc2626` | Delete/error actions |

---

## Files Changed

**New files:**
- `frontend/tailwind.config.ts` — Tailwind config with custom warm color tokens
- `frontend/postcss.config.js` — PostCSS config required by Tailwind

**Modified files:**
- `frontend/package.json` — add tailwindcss, postcss, autoprefixer, @radix-ui primitives, class-variance-authority, clsx, tailwind-merge, lucide-react
- `frontend/src/index.css` — replace content with Tailwind directives + shadcn CSS variable declarations
- `frontend/src/components/Layout.tsx` — restyle top navbar
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

**New component files (shadcn/ui copies):**
- `frontend/src/components/ui/button.tsx`
- `frontend/src/components/ui/input.tsx`
- `frontend/src/components/ui/label.tsx`
- `frontend/src/components/ui/card.tsx`
- `frontend/src/components/ui/badge.tsx`
- `frontend/src/components/ui/dialog.tsx`

---

## Layout Structure

### Top Navbar (`Layout.tsx`)
- Full-width dark brown bar (`#7a3a1a`)
- Left: app name "🍽 Dinner Service" in cream
- Center: nav links — Dashboard, Recipes, Shopping Lists, Products — in muted cream, active link highlighted with orange pill (`#c96a2b`)
- Right: user icon / email, logout
- Below navbar: page content area on warm cream background (`#fdf6ee`)

### Page Content Pattern
Every page follows the same structure:
```
<main class="max-w-4xl mx-auto px-4 py-8">
  <div class="flex items-center justify-between mb-6">
    <h1 class="text-2xl font-bold text-[#3d1f08]">Page Title</h1>
    <Button>+ Add New</Button>   {/* if applicable */}
  </div>
  {/* content */}
</main>
```

---

## Component Patterns

### List Items (Recipes, Shopping Lists, Products)
White card with warm border, row layout:
```
[emoji/icon]  [Name]  [secondary info]  [→ or action button]
```

### Forms (Login, Register, Add items)
- `Label` + `Input` stacked vertically, full width
- Primary `Button` for submit
- Muted text for secondary actions / links

### Cards
White background, `#e8c9a0` border, 8px border-radius, subtle shadow

### Buttons
- **Primary:** `bg-[#c96a2b] text-white hover:bg-[#a8571f]`
- **Outline:** `border-[#e8c9a0] text-[#7a5c3a] hover:bg-[#fdf0e0]`
- **Destructive:** `bg-destructive text-white`

---

## shadcn/ui Components Used

These components are copied into `src/components/ui/` (not imported from a package):

| Component | Used in |
|---|---|
| `Button` | All pages |
| `Input` | Login, Register, ForgotPassword, ResetPassword, forms in detail pages |
| `Label` | All form pages |
| `Card`, `CardHeader`, `CardContent` | Dashboard, list items |
| `Badge` | Recipe tags, shopping list categories |
| `Dialog`, `DialogContent` | Confirmation dialogs (delete recipe, leave shared list) |

---

## Page-by-Page Styling Summary

| Page | Key styling notes |
|---|---|
| Login / Register / Forgot / Reset | Centered card on warm bg, max-w-sm, logo at top |
| Dashboard | 2-column stat cards (recipes count, shopping lists count) |
| Recipes | List of rows with recipe name + arrow; "+ New Recipe" button |
| Recipe Detail | Title, description, ingredients list, image thumbnail, action buttons |
| Shopping Lists | List rows with list name + owner badge + arrow |
| Shopping List Detail | Grouped by category, checkbox per item, strikethrough when checked |
| Products | Two-panel: categories list + products list |
| Uncategorized Products | Simple list with category dropdown per item |
| User | Profile info card + default list selector + logout button |

---

## Out of Scope

- Dark mode toggle
- Animations / transitions beyond Tailwind defaults
- Mobile-specific bottom nav or hamburger menu
- Image-heavy recipe card grid (list layout chosen)
- Custom icon set (use lucide-react icons)
- i18n / localization
