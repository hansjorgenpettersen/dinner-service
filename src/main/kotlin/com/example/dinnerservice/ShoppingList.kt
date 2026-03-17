package com.example.dinnerservice

import jakarta.persistence.*

@Entity
@Table(name = "shopping_lists")
class ShoppingList(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val name: String = "",
    @ManyToOne
    @JoinColumn(name = "owner_id")
    val owner: User? = null,
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "shopping_list_shares",
        joinColumns = [JoinColumn(name = "list_id")],
        inverseJoinColumns = [JoinColumn(name = "user_id")]
    )
    val sharedWith: MutableSet<User> = mutableSetOf()
)
