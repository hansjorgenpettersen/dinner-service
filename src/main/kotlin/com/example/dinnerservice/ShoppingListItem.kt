package com.example.dinnerservice

import jakarta.persistence.*

@Entity
@Table(name = "shopping_list_items")
class ShoppingListItem(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val name: String = "",
    var count: Double? = null,
    val unitPrice: Double? = null,
    var checked: Boolean = false,
    @ManyToOne
    @JoinColumn(name = "category_id")
    val category: Category? = null,
    @ManyToOne
    @JoinColumn(name = "added_by_id")
    val addedBy: User? = null,
    @ManyToOne
    @JoinColumn(name = "list_id")
    val shoppingList: ShoppingList? = null
) {
    val totalPrice: Double?
        get() {
            val c = count
            val p = unitPrice
            return when {
                c != null && p != null -> c * p
                p != null -> p
                else -> null
            }
        }
}
