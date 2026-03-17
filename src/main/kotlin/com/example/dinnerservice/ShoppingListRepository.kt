package com.example.dinnerservice

import org.springframework.data.jpa.repository.JpaRepository

interface ShoppingListRepository : JpaRepository<ShoppingList, Long> {
    fun findByOwner(owner: User): List<ShoppingList>
    fun findBySharedWithContaining(user: User): List<ShoppingList>
}
