package com.example.dinnerservice

import org.springframework.data.jpa.repository.JpaRepository

interface RecipeIngredientRepository : JpaRepository<RecipeIngredient, Long> {
    fun findByRecipe(recipe: Recipe): List<RecipeIngredient>
    fun deleteByRecipe(recipe: Recipe)
}
