package com.example.dinnerservice

import org.springframework.data.jpa.repository.JpaRepository

interface RecipeRepository : JpaRepository<Recipe, Long>
