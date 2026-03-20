package com.example.dinnerservice

import org.springframework.data.jpa.repository.JpaRepository

interface ShoppingListItemRepository : JpaRepository<ShoppingListItem, Long> {
    fun findByShoppingList(list: ShoppingList): List<ShoppingListItem>
    fun findByNameIgnoreCase(name: String): List<ShoppingListItem>
}
