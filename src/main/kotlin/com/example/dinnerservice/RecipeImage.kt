package com.example.dinnerservice

import jakarta.persistence.*

@Entity
@Table(name = "recipe_images")
class RecipeImage(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @ManyToOne
    @JoinColumn(name = "recipe_id")
    val recipe: Recipe? = null,
    val filename: String = "",
    val originalName: String = ""
)
