package com.example.dinnerservice

import jakarta.persistence.*

@Entity
@Table(name = "recipe_ingredients")
class RecipeIngredient(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @ManyToOne
    @JoinColumn(name = "recipe_id")
    val recipe: Recipe? = null,
    @ManyToOne
    @JoinColumn(name = "product_id")
    val product: Product? = null,
    val quantity: Double? = null,
    val unit: String = ""
)
