# Shopping List Mobile Responsiveness Design

**Date:** 2026-04-10
**Status:** Approved

## Problem

The shopping list detail page has three mobile usability issues:
1. Stepper (−/+) and delete (✕) buttons are 20px — well below the 44px touch target minimum
2. The add item form is cramped on narrow screens — qty and price fields crowd the item name field
3. Item rows have too little breathing room with everything on one line

## Solution

Responsive polish — CSS/Tailwind only changes to `ShoppingListDetailPage.tsx`. No logic, API, or layout structure changes. Desktop layout is preserved using the `sm:` breakpoint (640px).

## Changes

### Add item form

**Current:** all fields in one `flex-wrap` row.

**New:** on mobile, stack vertically in three rows:
- Row 1: item name (full width)
- Row 2: qty + unit price side-by-side (`flex` with equal widths)
- Row 3: Add button (full width)

On desktop (`sm:` and above), restore the current horizontal single-row layout.

```
Mobile:                    Desktop (sm:):
┌─────────────────────┐   ┌──────────┬────┬──────────┬──────┐
│ Item name           │   │ Item     │Qty │Unit price│ Add  │
├──────────┬──────────┤   └──────────┴────┴──────────┴──────┘
│ Qty      │Unit price│
├──────────┴──────────┤
│       Add           │
└─────────────────────┘
```

### Item rows

| Element | Before | After |
|---------|--------|-------|
| Stepper − and + buttons | `w-5 h-5` (20px), `text-xs` | `w-9 h-9` (36px), `text-base` |
| Delete button | bare icon, no tap area | wrapped in `w-9 h-9` flex container |
| Checkbox | `w-4 h-4` | `w-5 h-5` |
| Row padding | `py-3` | `py-3.5` |

### Scope

- Only `frontend/src/pages/ShoppingListDetailPage.tsx`
- No backend changes
- No changes to `ShoppingListsPage.tsx`
- No changes to the sharing section
- No logic or behaviour changes
