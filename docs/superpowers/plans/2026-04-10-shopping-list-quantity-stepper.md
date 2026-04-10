# Shopping List Quantity Stepper Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add `−` / `+` stepper buttons to shopping list items so users can adjust quantity in place without deleting and re-adding.

**Architecture:** Backend gets a new `PATCH /api/shopping-lists/{id}/items/{itemId}` endpoint that accepts `{ count: Int }` and persists the updated value. The frontend replaces the static count text with inline stepper buttons that call this endpoint with optimistic UI updates.

**Tech Stack:** Kotlin/Spring Boot (backend), React + TypeScript + TanStack Query (frontend), TestRestTemplate integration tests (backend tests)

---

### Task 1: Write failing backend tests for PATCH endpoint

**Files:**
- Modify: `src/test/kotlin/com/example/dinnerservice/ShoppingListControllerTest.kt`

- [ ] **Step 1: Add three failing tests to `ShoppingListControllerTest`**

  Open `src/test/kotlin/com/example/dinnerservice/ShoppingListControllerTest.kt` and add these three tests at the end of the class (before the closing `}`):

  ```kotlin
  @Test
  fun `PATCH item updates count`() {
      val user = userRepository.findByEmail("user@test.com").get()
      val list = shoppingListRepository.save(ShoppingList(name = "List", owner = user))
      val item = shoppingListItemRepository.save(ShoppingListItem(name = "Milk", count = 1.0, shoppingList = list, addedBy = user))

      val response = restTemplate.exchange(
          "/api/shopping-lists/${list.id}/items/${item.id}",
          HttpMethod.PATCH,
          authEntity(token, mapOf("count" to 3)),
          ShoppingListItemDto::class.java
      )
      assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
      assertThat(response.body?.count).isEqualTo(3.0)
  }

  @Test
  fun `PATCH item returns 403 when user is not owner or creator`() {
      val other = userRepository.save(User(email = "other@test.com"))
      val list = shoppingListRepository.save(ShoppingList(name = "List", owner = other))
      val item = shoppingListItemRepository.save(ShoppingListItem(name = "Milk", count = 1.0, shoppingList = list, addedBy = other))

      val response = restTemplate.exchange(
          "/api/shopping-lists/${list.id}/items/${item.id}",
          HttpMethod.PATCH,
          authEntity(token, mapOf("count" to 3)),
          Void::class.java
      )
      assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
  }

  @Test
  fun `PATCH item returns 400 for count less than 1`() {
      val user = userRepository.findByEmail("user@test.com").get()
      val list = shoppingListRepository.save(ShoppingList(name = "List", owner = user))
      val item = shoppingListItemRepository.save(ShoppingListItem(name = "Milk", count = 2.0, shoppingList = list, addedBy = user))

      val response = restTemplate.exchange(
          "/api/shopping-lists/${list.id}/items/${item.id}",
          HttpMethod.PATCH,
          authEntity(token, mapOf("count" to 0)),
          Void::class.java
      )
      assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
  }
  ```

- [ ] **Step 2: Run the tests to confirm they fail**

  ```bash
  ./mvnw test -pl . -Dtest=ShoppingListControllerTest#"PATCH item*" -q
  ```

  Expected: 3 test failures with `404 Not Found` or connection refused (endpoint doesn't exist yet).

---

### Task 2: Implement the PATCH endpoint

**Files:**
- Modify: `src/main/kotlin/com/example/dinnerservice/ShoppingListItem.kt` (make `count` mutable)
- Modify: `src/main/kotlin/com/example/dinnerservice/Dto.kt` (add request DTO)
- Modify: `src/main/kotlin/com/example/dinnerservice/ShoppingListController.kt` (add endpoint)

- [ ] **Step 1: Make `count` mutable in the entity**

  In `src/main/kotlin/com/example/dinnerservice/ShoppingListItem.kt`, change line 11 from `val count` to `var count`:

  ```kotlin
  // Before:
  val count: Double? = null,

  // After:
  var count: Double? = null,
  ```

- [ ] **Step 2: Add `UpdateItemCountRequest` DTO**

  In `src/main/kotlin/com/example/dinnerservice/Dto.kt`, add this data class in the `── Shopping List ──` section (e.g. after `AddItemRequest`):

  ```kotlin
  data class UpdateItemCountRequest(val count: Int)
  ```

- [ ] **Step 3: Add the PATCH endpoint to the controller**

  In `src/main/kotlin/com/example/dinnerservice/ShoppingListController.kt`, add this method after `toggleItem` (around line 98):

  ```kotlin
  @PatchMapping("/{id}/items/{itemId}")
  fun updateItemCount(
      @PathVariable id: Long,
      @PathVariable itemId: Long,
      @RequestBody req: UpdateItemCountRequest
  ): ResponseEntity<ShoppingListItemDto> {
      val user = currentUserService.currentUser()
      val item = shoppingListItemRepository.findById(itemId).orElse(null)
          ?: return ResponseEntity.notFound().build()
      if (item.addedBy?.id != user.id && item.shoppingList?.owner?.id != user.id) {
          return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
      }
      if (req.count < 1) return ResponseEntity.badRequest().build()
      item.count = req.count.toDouble()
      val saved = shoppingListItemRepository.save(item)
      return ResponseEntity.ok(saved.toDto())
  }
  ```

- [ ] **Step 4: Run the tests to confirm they pass**

  ```bash
  ./mvnw test -pl . -Dtest=ShoppingListControllerTest -q
  ```

  Expected: all tests in `ShoppingListControllerTest` pass, including the 3 new ones.

- [ ] **Step 5: Commit**

  ```bash
  git add src/main/kotlin/com/example/dinnerservice/ShoppingListItem.kt \
          src/main/kotlin/com/example/dinnerservice/Dto.kt \
          src/main/kotlin/com/example/dinnerservice/ShoppingListController.kt \
          src/test/kotlin/com/example/dinnerservice/ShoppingListControllerTest.kt
  git commit -m "feat: add PATCH endpoint to update shopping list item count"
  ```

---

### Task 3: Add frontend API function and stepper UI

**Files:**
- Modify: `frontend/src/api/shoppingLists.ts` (add `updateItemCount`)
- Modify: `frontend/src/pages/ShoppingListDetailPage.tsx` (add mutation + stepper UI)

- [ ] **Step 1: Add `updateItemCount` to the API client**

  In `frontend/src/api/shoppingLists.ts`, add this export after `toggleItem`:

  ```typescript
  export const updateItemCount = (listId: number, itemId: number, count: number) =>
    apiClient.patch<ShoppingListItem>(`/shopping-lists/${listId}/items/${itemId}`, { count }).then(r => r.data)
  ```

- [ ] **Step 2: Import `updateItemCount` in the detail page**

  In `frontend/src/pages/ShoppingListDetailPage.tsx`, update the import on line 4:

  ```typescript
  // Before:
  import { getShoppingList, addItem, toggleItem, deleteItem, clearChecked, shareList, unshareList } from '../api/shoppingLists'

  // After:
  import { getShoppingList, addItem, toggleItem, deleteItem, clearChecked, shareList, unshareList, updateItemCount } from '../api/shoppingLists'
  ```

- [ ] **Step 3: Add the `updateCount` mutation**

  In `frontend/src/pages/ShoppingListDetailPage.tsx`, add this mutation after the `unshare` mutation (around line 66, before the `if (isLoading` guard):

  ```typescript
  const updateCount = useMutation({
    mutationFn: ({ itemId, count }: { itemId: number; count: number }) =>
      updateItemCount(listId, itemId, count),
    onMutate: async ({ itemId, count }) => {
      await qc.cancelQueries({ queryKey: ['shopping-list', listId] })
      const prev = qc.getQueryData(['shopping-list', listId])
      qc.setQueryData(['shopping-list', listId], (old: ShoppingListDetail | undefined) => old ? {
        ...old,
        items: old.items.map(i => i.id === itemId ? { ...i, count } : i)
      } : old)
      return { prev }
    },
    onError: (_err, _vars, ctx) => { if (ctx?.prev) qc.setQueryData(['shopping-list', listId], ctx.prev) },
    onSettled: invalidate
  })
  ```

- [ ] **Step 4: Replace static count display with stepper buttons**

  In `frontend/src/pages/ShoppingListDetailPage.tsx`, find the item row `<span>` at around line 155–158:

  ```tsx
  <span className={`flex-1 text-sm ${item.checked ? 'line-through text-[#b0a090]' : 'text-[#3d1f08]'}`}>
    {item.count && `${item.count} × `}{item.name}
    {item.totalPrice != null && <span className="text-[#7a5c3a] ml-2">{item.totalPrice.toFixed(2)}</span>}
  </span>
  ```

  Replace it with:

  ```tsx
  <span className={`flex-1 text-sm flex items-center gap-2 ${item.checked ? 'line-through text-[#b0a090]' : 'text-[#3d1f08]'}`}>
    {item.count == null ? (
      <button
        type="button"
        onClick={() => updateCount.mutate({ itemId: item.id, count: 1 })}
        className="w-5 h-5 flex items-center justify-center rounded border border-[#e8c9a0] text-[#7a5c3a] hover:bg-[#fdf0e0] text-xs leading-none flex-shrink-0"
      >+</button>
    ) : (
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
    )}
    {item.name}
    {item.totalPrice != null && <span className="text-[#7a5c3a] ml-2">{item.totalPrice.toFixed(2)}</span>}
  </span>
  ```

- [ ] **Step 5: Verify frontend builds without errors**

  ```bash
  cd frontend && npm run build 2>&1 | tail -20
  ```

  Expected: build completes with no TypeScript errors.

- [ ] **Step 6: Commit**

  ```bash
  git add frontend/src/api/shoppingLists.ts frontend/src/pages/ShoppingListDetailPage.tsx
  git commit -m "feat: add quantity stepper buttons to shopping list items"
  ```
