# Shopping List Quantity Stepper

**Date:** 2026-04-10
**Status:** Approved

## Problem

Users cannot edit the quantity of a shopping list item after adding it. The only workaround is to delete and re-add the item.

## Solution

Add `−` / `+` stepper buttons to each item row so users can adjust quantity in place with a single tap.

## UI Design

Each item row currently displays `{count} × {name}`. Replace the count text with a stepper control:

```
[ − ]  2  [ + ]  × Milk
```

- Minimum value: 1. The `−` button is disabled when count is 1.
- Step size: 1 (integers only).
- Items with a null count show only a `[ + ]` button; clicking it sets count to 1.
- Updates are applied immediately on each click (no Save button).
- Optimistic UI: update local state first, revert on API error.

## Backend

### New endpoint

```
PATCH /api/shopping-lists/{id}/items/{itemId}
```

**Request body:**
```json
{ "count": 3 }
```

**Authorization:** item creator or list owner only (same rule as DELETE).

**Response:** updated `ShoppingListItem` DTO.

**Validation:** `count` must be a positive integer (≥ 1).

### Data model

`ShoppingListItem.count` stays `Double?` in the database. The endpoint accepts and stores integer values; fractional values are not exposed via this feature.

## Frontend

### API client (`shoppingLists.ts`)

Add `updateItemCount(listId, itemId, count)` calling `PATCH /api/shopping-lists/{listId}/items/{itemId}`.

### Component (`ShoppingListDetailPage.tsx`)

Replace the `{item.count && '${item.count} × '}` span with a `<QuantityStepper>` component (or inline JSX) that renders the `−` / `+` controls and the current count.

On `−` or `+` click:
1. Compute new count (clamp to min 1).
2. Optimistically update local state.
3. Call `updateItemCount`.
4. On error, revert to previous count.

## Error Handling

- Network errors: revert optimistic update, show a toast or inline error.
- 403 Forbidden: revert, inform user they don't have permission.

## Out of Scope

- Fractional quantities
- Editing unit price inline
- Keyboard input (no text field, buttons only)
