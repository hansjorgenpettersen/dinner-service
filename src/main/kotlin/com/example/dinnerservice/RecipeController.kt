package com.example.dinnerservice

import jakarta.servlet.http.HttpSession
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths
import java.util.UUID

@Controller
class RecipeController(
    private val recipeRepository: RecipeRepository,
    private val recipeIngredientRepository: RecipeIngredientRepository,
    private val recipeImageRepository: RecipeImageRepository,
    private val productRepository: ProductRepository,
    private val shoppingListRepository: ShoppingListRepository,
    private val shoppingListItemRepository: ShoppingListItemRepository,
    private val userRepository: UserRepository,
    @Value("\${app.upload-dir}") private val uploadDir: String
) {

    companion object {
        val UNITS = listOf("pcs", "g", "kg", "ml", "dl", "L", "tsp", "tbsp", "cup")
        val ALLOWED_TYPES = setOf("image/jpeg", "image/png", "image/webp", "image/gif")
    }

    private fun currentUser(session: HttpSession): User? {
        val email = session.getAttribute("email") as? String ?: return null
        return userRepository.findByEmail(email).orElse(null)
    }

    @GetMapping("/recipes")
    fun list(session: HttpSession, model: Model): String {
        session.getAttribute("email") ?: return "redirect:/login"
        val recipes = recipeRepository.findAll().sortedBy { it.name.lowercase() }
        val previewImages = recipes.associate { recipe ->
            recipe.id to recipeImageRepository.findByRecipe(recipe).firstOrNull()?.filename
        }
        model.addAttribute("recipes", recipes)
        model.addAttribute("previewImages", previewImages)
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
        val user = currentUser(session) ?: return "redirect:/login"
        val recipe = recipeRepository.findById(id).orElse(null) ?: return "redirect:/recipes"
        val owned = shoppingListRepository.findByOwner(user)
        val shared = shoppingListRepository.findBySharedWithContaining(user)
        val shoppingLists = (owned + shared).sortedBy { it.name.lowercase() }

        val selectedListId = session.getAttribute("selectedListId") as? Long
        val selectedList = selectedListId?.let { lid ->
            shoppingLists.find { it.id == lid }
        }

        val ingredients = recipeIngredientRepository.findByRecipe(recipe)

        // Build map of ingredientId -> current count in selected list
        val ingredientCounts: Map<Long, Int> = if (selectedList != null) {
            val listItems = shoppingListItemRepository.findByShoppingList(selectedList)
            ingredients.associate { ing ->
                val count = listItems
                    .find { it.name.equals(ing.product?.name, ignoreCase = true) }
                    ?.count?.toInt() ?: 0
                ing.id to count
            }
        } else emptyMap()

        model.addAttribute("recipe", recipe)
        model.addAttribute("ingredients", ingredients)
        model.addAttribute("images", recipeImageRepository.findByRecipe(recipe))
        model.addAttribute("products", productRepository.findAll().sortedBy { it.name.lowercase() })
        model.addAttribute("units", UNITS)
        model.addAttribute("shoppingLists", shoppingLists)
        model.addAttribute("selectedListId", selectedListId)
        model.addAttribute("ingredientCounts", ingredientCounts)
        return "recipe"
    }

    @PostMapping("/recipes/{id}/select-list")
    fun selectList(
        @PathVariable id: Long,
        @RequestParam(required = false) listId: Long?,
        session: HttpSession
    ): String {
        session.getAttribute("email") ?: return "redirect:/login"
        if (listId != null) session.setAttribute("selectedListId", listId)
        else session.removeAttribute("selectedListId")
        return "redirect:/recipes/$id"
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
        recipeImageRepository.findByRecipe(recipe).forEach { deleteImageFile(it.filename) }
        recipeImageRepository.deleteByRecipe(recipe)
        recipeRepository.deleteById(id)
        return "redirect:/recipes"
    }

    @GetMapping("/recipe-images/{filename}")
    @ResponseBody
    fun serveImage(@PathVariable filename: String): ResponseEntity<Resource> {
        val file = Paths.get(uploadDir, "recipe-images", filename)
        if (!Files.exists(file)) return ResponseEntity.notFound().build()
        val contentType = Files.probeContentType(file) ?: "application/octet-stream"
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .body(FileSystemResource(file))
    }

    @PostMapping("/recipes/{id}/images")
    fun uploadImages(
        @PathVariable id: Long,
        @RequestParam("files") files: List<MultipartFile>,
        session: HttpSession
    ): String {
        session.getAttribute("email") ?: return "redirect:/login"
        val recipe = recipeRepository.findById(id).orElse(null) ?: return "redirect:/recipes"
        val dir = Paths.get(uploadDir, "recipe-images")
        Files.createDirectories(dir)
        files.filter { !it.isEmpty && it.contentType in ALLOWED_TYPES }.forEach { file ->
            val ext = file.originalFilename?.substringAfterLast('.', "jpg") ?: "jpg"
            val filename = "${UUID.randomUUID()}.$ext"
            Files.copy(file.inputStream, dir.resolve(filename))
            recipeImageRepository.save(
                RecipeImage(recipe = recipe, filename = filename, originalName = file.originalFilename ?: filename)
            )
        }
        return "redirect:/recipes/$id"
    }

    @PostMapping("/recipes/{id}/images/{imageId}/delete")
    fun deleteImage(
        @PathVariable id: Long,
        @PathVariable imageId: Long,
        session: HttpSession
    ): String {
        session.getAttribute("email") ?: return "redirect:/login"
        val image = recipeImageRepository.findById(imageId).orElse(null) ?: return "redirect:/recipes/$id"
        deleteImageFile(image.filename)
        recipeImageRepository.deleteById(imageId)
        return "redirect:/recipes/$id"
    }

    private fun deleteImageFile(filename: String) {
        val file = Paths.get(uploadDir, "recipe-images", filename)
        Files.deleteIfExists(file)
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

    @PostMapping("/recipes/{id}/ingredients/{ingId}/remove-from-list")
    fun removeIngredientFromList(
        @PathVariable id: Long,
        @PathVariable ingId: Long,
        session: HttpSession
    ): String {
        val user = currentUser(session) ?: return "redirect:/login"
        val listId = session.getAttribute("selectedListId") as? Long ?: return "redirect:/recipes/$id"
        val list = shoppingListRepository.findById(listId).orElse(null) ?: return "redirect:/recipes/$id"
        if (list.owner?.id != user.id && !list.sharedWith.any { it.id == user.id }) {
            return "redirect:/recipes/$id"
        }
        val ingredient = recipeIngredientRepository.findById(ingId).orElse(null) ?: return "redirect:/recipes/$id"
        val productName = ingredient.product?.name ?: return "redirect:/recipes/$id"

        val existing = shoppingListItemRepository.findByShoppingList(list)
            .find { it.name.equals(productName, ignoreCase = true) }
            ?: return "redirect:/recipes/$id"

        val newCount = (existing.count ?: 1.0) - 1.0
        if (newCount <= 0) {
            shoppingListItemRepository.delete(existing)
        } else {
            shoppingListItemRepository.save(
                ShoppingListItem(
                    id = existing.id, name = existing.name, count = newCount,
                    unitPrice = existing.unitPrice, checked = existing.checked,
                    category = existing.category, addedBy = existing.addedBy,
                    shoppingList = existing.shoppingList
                )
            )
        }
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

    @PostMapping("/recipes/{id}/ingredients/{ingId}/add-to-list")
    fun addIngredientToList(
        @PathVariable id: Long,
        @PathVariable ingId: Long,
        session: HttpSession
    ): String {
        val user = currentUser(session) ?: return "redirect:/login"
        val listId = session.getAttribute("selectedListId") as? Long ?: return "redirect:/recipes/$id"
        val list = shoppingListRepository.findById(listId).orElse(null) ?: return "redirect:/recipes/$id"
        if (list.owner?.id != user.id && !list.sharedWith.any { it.id == user.id }) {
            return "redirect:/recipes/$id"
        }
        val ingredient = recipeIngredientRepository.findById(ingId).orElse(null) ?: return "redirect:/recipes/$id"
        val productName = ingredient.product?.name ?: return "redirect:/recipes/$id"

        val existing = shoppingListItemRepository.findByShoppingList(list)
            .find { it.name.equals(productName, ignoreCase = true) }

        if (existing != null) {
            shoppingListItemRepository.save(
                ShoppingListItem(
                    id = existing.id, name = existing.name,
                    count = (existing.count ?: 0.0) + 1.0,
                    unitPrice = existing.unitPrice, checked = existing.checked,
                    category = existing.category, addedBy = existing.addedBy,
                    shoppingList = existing.shoppingList
                )
            )
        } else {
            val product = productRepository.findByNameIgnoreCase(productName).orElseGet {
                productRepository.save(Product(name = productName, price = ingredient.product?.price))
            }
            shoppingListItemRepository.save(
                ShoppingListItem(
                    name = productName, count = 1.0,
                    unitPrice = ingredient.product?.price,
                    category = product.category, addedBy = user, shoppingList = list
                )
            )
        }
        return "redirect:/recipes/$id"
    }
}
