package com.example.dinnerservice

import org.springframework.data.jpa.repository.JpaRepository

interface RecipeImageRepository : JpaRepository<RecipeImage, Long> {
    fun findByRecipe(recipe: Recipe): List<RecipeImage>
    fun deleteByRecipe(recipe: Recipe)
}
