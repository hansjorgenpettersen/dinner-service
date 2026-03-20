package com.example.dinnerservice

import jakarta.servlet.http.HttpSession
import jakarta.transaction.Transactional
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

@Controller
class RecipeController(
    private val recipeRepository: RecipeRepository,
    private val recipeIngredientRepository: RecipeIngredientRepository,
    private val productRepository: ProductRepository
) {

    companion object {
        val UNITS = listOf("pcs", "g", "kg", "ml", "dl", "L", "tsp", "tbsp", "cup")
    }

    @GetMapping("/recipes")
    fun list(session: HttpSession, model: Model): String {
        session.getAttribute("email") ?: return "redirect:/login"
        model.addAttribute("recipes", recipeRepository.findAll().sortedBy { it.name.lowercase() })
        return "recipes"
    }

    @PostMapping("/recipes/new")
    fun create(
        @RequestParam name: String,
        @RequestParam(defaultValue = "") description: String,
        session: HttpSession
    ): String {
        session.getAttribute("email") ?: return "redirect:/login"
        val recipe = recipeRepository.save(Recipe(name = name.trim(), description = description.trim()))
        return "redirect:/recipes/${recipe.id}"
    }

    @GetMapping("/recipes/{id}")
    fun view(@PathVariable id: Long, session: HttpSession, model: Model): String {
        session.getAttribute("email") ?: return "redirect:/login"
        val recipe = recipeRepository.findById(id).orElse(null) ?: return "redirect:/recipes"
        model.addAttribute("recipe", recipe)
        model.addAttribute("ingredients", recipeIngredientRepository.findByRecipe(recipe))
        model.addAttribute("products", productRepository.findAll().sortedBy { it.name.lowercase() })
        model.addAttribute("units", UNITS)
        return "recipe"
    }

    @PostMapping("/recipes/{id}/edit")
    fun edit(
        @PathVariable id: Long,
        @RequestParam name: String,
        @RequestParam(defaultValue = "") description: String,
        session: HttpSession
    ): String {
        session.getAttribute("email") ?: return "redirect:/login"
        val recipe = recipeRepository.findById(id).orElse(null) ?: return "redirect:/recipes"
        recipeRepository.save(Recipe(id = recipe.id, name = name.trim(), description = description.trim()))
        return "redirect:/recipes/$id"
    }

    @PostMapping("/recipes/{id}/delete")
    @Transactional
    fun delete(@PathVariable id: Long, session: HttpSession): String {
        session.getAttribute("email") ?: return "redirect:/login"
        val recipe = recipeRepository.findById(id).orElse(null) ?: return "redirect:/recipes"
        recipeIngredientRepository.deleteByRecipe(recipe)
        recipeRepository.deleteById(id)
        return "redirect:/recipes"
    }

    @PostMapping("/recipes/{id}/ingredients")
    fun addIngredient(
        @PathVariable id: Long,
        @RequestParam productId: Long,
        @RequestParam(required = false) quantity: Double?,
        @RequestParam unit: String,
        session: HttpSession
    ): String {
        session.getAttribute("email") ?: return "redirect:/login"
        val recipe = recipeRepository.findById(id).orElse(null) ?: return "redirect:/recipes"
        val product = productRepository.findById(productId).orElse(null) ?: return "redirect:/recipes/$id"
        recipeIngredientRepository.save(
            RecipeIngredient(recipe = recipe, product = product, quantity = quantity, unit = unit)
        )
        return "redirect:/recipes/$id"
    }

    @PostMapping("/recipes/{id}/ingredients/{ingId}/delete")
    fun deleteIngredient(
        @PathVariable id: Long,
        @PathVariable ingId: Long,
        session: HttpSession
    ): String {
        session.getAttribute("email") ?: return "redirect:/login"
        recipeIngredientRepository.deleteById(ingId)
        return "redirect:/recipes/$id"
    }
}
