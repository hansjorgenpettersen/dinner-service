package com.example.dinnerservice

import jakarta.persistence.*

@Entity
@Table(name = "products")
class Product(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(unique = true)
    val name: String = "",
    val price: Double? = null,
    @ManyToOne
    @JoinColumn(name = "category_id")
    val category: Category? = null
)
