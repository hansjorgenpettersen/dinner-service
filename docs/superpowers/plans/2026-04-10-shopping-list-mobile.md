# Shopping List Mobile Responsiveness Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the shopping list detail page mobile-friendly by enlarging touch targets and making the add-item form responsive.

**Architecture:** CSS/Tailwind-only changes to one file. No logic, API, or backend changes. Desktop layout preserved via `sm:` breakpoint (640px). Two self-contained tasks: item rows first, then the add form.

**Tech Stack:** React, Tailwind CSS v3

---

### Task 1: Enlarge touch targets in item rows

**Files:**
- Modify: `frontend/src/pages/ShoppingListDetailPage.tsx`

- [ ] **Step 1: Update the item row container padding and checkbox size**

  In `frontend/src/pages/ShoppingListDetailPage.tsx`, find the item row `<div>` (around line 176):

  ```tsx
  <div key={item.id} className="flex items-center gap-3 px-4 py-3 border-b border-[#f5ebe0] last:border-0">
    <input
      type="checkbox"
      checked={item.checked}
      onChange={() => toggle.mutate(item.id)}
      className="w-4 h-4 accent-[#c96a2b] flex-shrink-0"
    />
  ```

  Replace with:

  ```tsx
  <div key={item.id} className="flex items-center gap-3 px-4 py-3.5 border-b border-[#f5ebe0] last:border-0">
    <input
      type="checkbox"
      checked={item.checked}
      onChange={() => toggle.mutate(item.id)}
      className="w-5 h-5 accent-[#c96a2b] flex-shrink-0"
    />
  ```

- [ ] **Step 2: Enlarge the null-count `+` button**

  Find the null-count stepper button (around line 185):

  ```tsx
  <button
    type="button"
    onClick={() => updateCount.mutate({ itemId: item.id, count: 1 })}
    className="w-5 h-5 flex items-center justify-center rounded border border-[#e8c9a0] text-[#7a5c3a] hover:bg-[#fdf0e0] text-xs leading-none flex-shrink-0"
  >+</button>
  ```

  Replace with:

  ```tsx
  <button
    type="button"
    onClick={() => updateCount.mutate({ itemId: item.id, count: 1 })}
    className="w-9 h-9 flex items-center justify-center rounded border border-[#e8c9a0] text-[#7a5c3a] hover:bg-[#fdf0e0] text-base leading-none flex-shrink-0"
  >+</button>
  ```

- [ ] **Step 3: Enlarge the stepper − and + buttons**

  Find the stepper span with the three buttons (around line 191):

  ```tsx
  <span className="flex items-center gap-1 flex-shrink-0">
    <button
      type="button"
      onClick={() => updateCount.mutate({ itemId: item.id, count: item.count! - 1 })}
      disabled={item.count <= 1}
      className="w-5 h-5 flex items-center justify-center rounded border border-[#e8c9a0] text-[#7a5c3a] hover:bg-[#fdf0e0] disabled:opacity-30 disabled:cursor-not-allowed text-xs leading-none"
    >−</button>
    <span className="min-w-[1.5rem] text-center text-xs font-medium">{item.count}</span>
    <button
      type="button"
      onClick={() => updateCount.mutate({ itemId: item.id, count: item.count! + 1 })}
      className="w-5 h-5 flex items-center justify-center rounded border border-[#e8c9a0] text-[#7a5c3a] hover:bg-[#fdf0e0] text-xs leading-none"
    >+</button>
  </span>
  ```

  Replace with:

  ```tsx
  <span className="flex items-center gap-1 flex-shrink-0">
    <button
      type="button"
      onClick={() => updateCount.mutate({ itemId: item.id, count: item.count! - 1 })}
      disabled={item.count <= 1}
      className="w-9 h-9 flex items-center justify-center rounded border border-[#e8c9a0] text-[#7a5c3a] hover:bg-[#fdf0e0] disabled:opacity-30 disabled:cursor-not-allowed text-base leading-none"
    >−</button>
    <span className="min-w-[1.5rem] text-center text-xs font-medium">{item.count}</span>
    <button
      type="button"
      onClick={() => updateCount.mutate({ itemId: item.id, count: item.count! + 1 })}
      className="w-9 h-9 flex items-center justify-center rounded border border-[#e8c9a0] text-[#7a5c3a] hover:bg-[#fdf0e0] text-base leading-none"
    >+</button>
  </span>
  ```

- [ ] **Step 4: Give the delete button a proper tap area**

  Find the delete button (around line 209):

  ```tsx
  <button onClick={() => remove.mutate(item.id)} className="text-[#c9b09a] hover:text-red-500 flex-shrink-0">
    <X className="w-3.5 h-3.5" />
  </button>
  ```

  Replace with:

  ```tsx
  <button onClick={() => remove.mutate(item.id)} className="w-9 h-9 flex items-center justify-center text-[#c9b09a] hover:text-red-500 flex-shrink-0">
    <X className="w-3.5 h-3.5" />
  </button>
  ```

- [ ] **Step 5: Verify TypeScript**

  ```bash
  cd frontend && npx tsc --noEmit 2>&1 | tail -5
  ```

  Expected: no output (clean).

- [ ] **Step 6: Commit**

  ```bash
  git add frontend/src/pages/ShoppingListDetailPage.tsx
  git commit -m "feat: enlarge touch targets in shopping list item rows"
  ```

---

### Task 2: Make the add item form responsive

**Files:**
- Modify: `frontend/src/pages/ShoppingListDetailPage.tsx`

- [ ] **Step 1: Update the form container and item name field**

  Find the add-item form (around line 120):

  ```tsx
  <form onSubmit={e => { e.preventDefault(); add.mutate() }} className="bg-white border border-[#e8c9a0] rounded-lg p-4 mb-6 flex flex-wrap gap-2 items-end">
    <div className="flex flex-col gap-1 flex-1 min-w-[140px] relative">
  ```

  Replace with:

  ```tsx
  <form onSubmit={e => { e.preventDefault(); add.mutate() }} className="bg-white border border-[#e8c9a0] rounded-lg p-4 mb-6 flex flex-col gap-2 sm:flex-row sm:flex-wrap sm:items-end">
    <div className="flex flex-col gap-1 relative sm:flex-1 sm:min-w-[140px]">
  ```

- [ ] **Step 2: Wrap Qty and Unit price in a shared mobile row**

  Find the Qty and Unit price fields (around line 151):

  ```tsx
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
  ```

  Replace with:

  ```tsx
        </div>
        <div className="flex gap-2 sm:contents">
          <div className="flex flex-col gap-1 flex-1 sm:flex-none sm:w-20">
            <label className="text-xs text-[#7a5c3a]">Qty</label>
            <Input type="number" value={count} onChange={e => setCount(e.target.value)} placeholder="0" className="border-[#e8c9a0]" />
          </div>
          <div className="flex flex-col gap-1 flex-1 sm:flex-none sm:w-24">
            <label className="text-xs text-[#7a5c3a]">Unit price</label>
            <Input type="number" value={unitPrice} onChange={e => setUnitPrice(e.target.value)} placeholder="0.00" className="border-[#e8c9a0]" />
          </div>
        </div>
        <Button type="submit" className="bg-[#c96a2b] hover:bg-[#a8571f] text-white w-full sm:w-auto sm:self-end">Add</Button>
  ```

- [ ] **Step 3: Verify TypeScript**

  ```bash
  cd frontend && npx tsc --noEmit 2>&1 | tail -5
  ```

  Expected: no output (clean).

- [ ] **Step 4: Commit**

  ```bash
  git add frontend/src/pages/ShoppingListDetailPage.tsx
  git commit -m "feat: responsive add item form on mobile"
  ```
