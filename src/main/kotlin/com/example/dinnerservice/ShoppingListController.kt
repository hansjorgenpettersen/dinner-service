package com.example.dinnerservice

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/shopping-lists")
class ShoppingListController(
    private val shoppingListRepository: ShoppingListRepository,
    private val shoppingListItemRepository: ShoppingListItemRepository,
    private val userRepository: UserRepository,
    private val productRepository: ProductRepository,
    private val currentUserService: CurrentUserService
) {
    private fun ShoppingList.toSummary() = ShoppingListSummaryDto(id, name, owner?.email)

    private fun ShoppingListItem.toDto() = ShoppingListItemDto(
        id = id, name = name, count = count, unitPrice = unitPrice, totalPrice = totalPrice,
        checked = checked, categoryId = category?.id, categoryName = category?.name,
        categoryColor = category?.color, addedByEmail = addedBy?.email
    )

    private fun accessibleList(listId: Long, user: User): ShoppingList {
        val list = shoppingListRepository.findById(listId).orElse(null)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "List not found")
        if (list.owner?.id != user.id && !list.sharedWith.any { it.id == user.id }) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied")
        }
        return list
    }

    @GetMapping
    fun list(): ResponseEntity<ShoppingListsResponse> {
        val user = currentUserService.currentUser()
        return ResponseEntity.ok(
            ShoppingListsResponse(
                owned = shoppingListRepository.findByOwner(user).map { it.toSummary() },
                shared = shoppingListRepository.findBySharedWithContaining(user).map { it.toSummary() }
            )
        )
    }

    @PostMapping
    fun create(@Valid @RequestBody req: CreateShoppingListRequest): ResponseEntity<ShoppingListSummaryDto> {
        val user = currentUserService.currentUser()
        val list = shoppingListRepository.save(ShoppingList(name = req.name.trim(), owner = user))
        return ResponseEntity.status(HttpStatus.CREATED).body(list.toSummary())
    }

    @GetMapping("/{id}")
    fun view(@PathVariable id: Long): ResponseEntity<ShoppingListDetailDto> {
        val user = currentUserService.currentUser()
        val list = accessibleList(id, user)
        val items = shoppingListItemRepository.findByShoppingList(list)
            .sortedWith(compareBy({ it.category?.name ?: "zzz" }, { it.name.lowercase() }))
        val totalPrice = items.mapNotNull { it.totalPrice }.sum()
        return ResponseEntity.ok(
            ShoppingListDetailDto(
                id = list.id, name = list.name,
                items = items.map { it.toDto() },
                totalPrice = totalPrice,
                isOwner = list.owner?.id == user.id,
                sharedWith = list.sharedWith.mapNotNull { it.email }
            )
        )
    }

    @PostMapping("/{id}/items")
    fun addItem(
        @PathVariable id: Long,
        @Valid @RequestBody req: AddItemRequest
    ): ResponseEntity<ShoppingListItemDto> {
        val user = currentUserService.currentUser()
        val list = accessibleList(id, user)
        val trimmed = req.name.trim()
        val product = productRepository.findByNameIgnoreCase(trimmed).orElseGet {
            productRepository.save(Product(name = trimmed, price = req.unitPrice))
        }
        val item = shoppingListItemRepository.save(
            ShoppingListItem(name = trimmed, count = req.count, unitPrice = req.unitPrice,
                category = product.category, addedBy = user, shoppingList = list)
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(item.toDto())
    }

    @PostMapping("/{id}/items/{itemId}/toggle")
    fun toggleItem(@PathVariable id: Long, @PathVariable itemId: Long): ResponseEntity<ShoppingListItemDto> {
        currentUserService.currentUser()
        val item = shoppingListItemRepository.findById(itemId).orElse(null)
            ?: return ResponseEntity.notFound().build()
        item.checked = !item.checked
        val saved = shoppingListItemRepository.save(item)
        return ResponseEntity.ok(saved.toDto())
    }

    @DeleteMapping("/{id}/items/{itemId}")
    fun deleteItem(@PathVariable id: Long, @PathVariable itemId: Long): ResponseEntity<Void> {
        val user = currentUserService.currentUser()
        val item = shoppingListItemRepository.findById(itemId).orElse(null)
            ?: return ResponseEntity.notFound().build()
        if (item.addedBy?.id == user.id || item.shoppingList?.owner?.id == user.id) {
            shoppingListItemRepository.delete(item)
        }
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{id}/items/clear-checked")
    fun clearChecked(@PathVariable id: Long): ResponseEntity<Void> {
        currentUserService.currentUser()
        val list = shoppingListRepository.findById(id).orElse(null)
            ?: return ResponseEntity.notFound().build()
        val checked = shoppingListItemRepository.findByShoppingList(list).filter { it.checked }
        shoppingListItemRepository.deleteAll(checked)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{id}/share")
    fun share(@PathVariable id: Long, @Valid @RequestBody req: ShareRequest): ResponseEntity<Void> {
        val user = currentUserService.currentUser()
        val list = shoppingListRepository.findById(id).orElse(null)
            ?: return ResponseEntity.notFound().build()
        if (list.owner?.id != user.id) return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        val target = userRepository.findByEmail(req.email.trim().lowercase()).orElse(null)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        list.sharedWith.add(target)
        shoppingListRepository.save(list)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{id}/unshare")
    fun unshare(@PathVariable id: Long, @Valid @RequestBody req: ShareRequest): ResponseEntity<Void> {
        val user = currentUserService.currentUser()
        val list = shoppingListRepository.findById(id).orElse(null)
            ?: return ResponseEntity.notFound().build()
        if (list.owner?.id != user.id) return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        val target = userRepository.findByEmail(req.email.trim().lowercase()).orElse(null)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        list.sharedWith.remove(target)
        shoppingListRepository.save(list)
        return ResponseEntity.noContent().build()
    }
}
