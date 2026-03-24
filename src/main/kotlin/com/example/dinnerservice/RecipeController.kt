package com.example.dinnerservice

import jakarta.transaction.Transactional
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.UUID

@RestController
@RequestMapping("/api")
class RecipeController(
    private val recipeRepository: RecipeRepository,
    private val recipeIngredientRepository: RecipeIngredientRepository,
    private val recipeImageRepository: RecipeImageRepository,
    private val productRepository: ProductRepository,
    private val shoppingListRepository: ShoppingListRepository,
    private val shoppingListItemRepository: ShoppingListItemRepository,
    private val currentUserService: CurrentUserService,
    @Value("\${app.upload-dir}") private val uploadDir: String
) {
    companion object {
        val UNITS = listOf("pcs", "g", "kg", "ml", "dl", "L", "tsp", "tbsp", "cup")
        val ALLOWED_TYPES = setOf("image/jpeg", "image/png", "image/webp", "image/gif")
    }

    private fun Recipe.toSummary(): RecipeSummaryDto {
        val preview = recipeImageRepository.findByRecipe(this).firstOrNull()?.filename
        return RecipeSummaryDto(id, name, description, preview)
    }

    private fun RecipeIngredient.toDto() =
        IngredientDto(id, product?.id, product?.name, quantity, unit)

    private fun RecipeImage.toDto() = RecipeImageDto(id, filename, originalName)

    @GetMapping("/recipes")
    fun list(): ResponseEntity<List<RecipeSummaryDto>> =
        ResponseEntity.ok(
            recipeRepository.findAll()
                .sortedBy { it.name.lowercase() }
                .map { it.toSummary() }
        )

    @PostMapping("/recipes")
    fun create(@Valid @RequestBody req: CreateRecipeRequest): ResponseEntity<RecipeSummaryDto> {
        val recipe = recipeRepository.save(Recipe(name = req.name.trim(), description = req.description.trim()))
        return ResponseEntity.status(HttpStatus.CREATED).body(recipe.toSummary())
    }

    @GetMapping("/recipes/{id}")
    fun view(@PathVariable id: Long, @RequestParam(required = false) selectedListId: Long?): ResponseEntity<RecipeDetailDto> {
        val user = currentUserService.currentUser()
        val recipe = recipeRepository.findById(id).orElse(null)
            ?: return ResponseEntity.notFound().build()

        val owned = shoppingListRepository.findByOwner(user)
        val shared = shoppingListRepository.findBySharedWithContaining(user)
        val shoppingLists = (owned + shared)
            .sortedBy { it.name.lowercase() }
            .map { ShoppingListSummaryDto(it.id, it.name, it.owner?.email) }

        val ingredients = recipeIngredientRepository.findByRecipe(recipe)

        val ingredientCounts: Map<Long, Int> = if (selectedListId != null) {
            val list = shoppingListRepository.findById(selectedListId).orElse(null)
            if (list != null) {
                val listItems = shoppingListItemRepository.findByShoppingList(list)
                ingredients.associate { ing ->
                    val count = listItems
                        .find { it.name.equals(ing.product?.name, ignoreCase = true) }
                        ?.count?.toInt() ?: 0
                    ing.id to count
                }
            } else emptyMap()
        } else emptyMap()

        return ResponseEntity.ok(
            RecipeDetailDto(
                id = recipe.id,
                name = recipe.name,
                description = recipe.description,
                ingredients = ingredients.map { it.toDto() },
                images = recipeImageRepository.findByRecipe(recipe).map { it.toDto() },
                shoppingLists = shoppingLists,
                selectedListId = selectedListId,
                ingredientCounts = ingredientCounts
            )
        )
    }

    @PutMapping("/recipes/{id}")
    fun edit(@PathVariable id: Long, @Valid @RequestBody req: CreateRecipeRequest): ResponseEntity<RecipeSummaryDto> {
        val recipe = recipeRepository.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()
        val updated = recipeRepository.save(Recipe(id = recipe.id, name = req.name.trim(), description = req.description.trim()))
        return ResponseEntity.ok(updated.toSummary())
    }

    @DeleteMapping("/recipes/{id}")
    @Transactional
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        val recipe = recipeRepository.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()
        recipeIngredientRepository.deleteByRecipe(recipe)
        recipeImageRepository.findByRecipe(recipe).forEach { deleteImageFile(it.filename) }
        recipeImageRepository.deleteByRecipe(recipe)
        recipeRepository.deleteById(id)
        return ResponseEntity.noContent().build()
    }

    // ── Images ────────────────────────────────────────────────────────────────

    @GetMapping("/recipe-images/{filename}")
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
        @RequestParam("files") files: List<MultipartFile>
    ): ResponseEntity<List<RecipeImageDto>> {
        val recipe = recipeRepository.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()
        val dir = Paths.get(uploadDir, "recipe-images")
        Files.createDirectories(dir)
        val saved = files.filter { !it.isEmpty && it.contentType in ALLOWED_TYPES }.map { file ->
            val ext = file.originalFilename?.substringAfterLast('.', "jpg") ?: "jpg"
            val filename = "${UUID.randomUUID()}.$ext"
            Files.copy(file.inputStream, dir.resolve(filename))
            recipeImageRepository.save(RecipeImage(recipe = recipe, filename = filename, originalName = file.originalFilename ?: filename))
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(saved.map { it.toDto() })
    }

    @DeleteMapping("/recipes/{id}/images/{imageId}")
    fun deleteImage(@PathVariable id: Long, @PathVariable imageId: Long): ResponseEntity<Void> {
        val image = recipeImageRepository.findById(imageId).orElse(null)
            ?: return ResponseEntity.notFound().build()
        deleteImageFile(image.filename)
        recipeImageRepository.deleteById(imageId)
        return ResponseEntity.noContent().build()
    }

    private fun deleteImageFile(filename: String) {
        Files.deleteIfExists(Paths.get(uploadDir, "recipe-images", filename))
    }

    // ── Ingredients ───────────────────────────────────────────────────────────

    @PostMapping("/recipes/{id}/ingredients")
    fun addIngredient(
        @PathVariable id: Long,
        @Valid @RequestBody req: AddIngredientRequest
    ): ResponseEntity<IngredientDto> {
        val recipe = recipeRepository.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()
        val product = productRepository.findById(req.productId).orElse(null)
            ?: return ResponseEntity.badRequest().build()
        val ingredient = recipeIngredientRepository.save(
            RecipeIngredient(recipe = recipe, product = product, quantity = req.quantity, unit = req.unit)
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(ingredient.toDto())
    }

    @DeleteMapping("/recipes/{id}/ingredients/{ingId}")
    fun deleteIngredient(@PathVariable id: Long, @PathVariable ingId: Long): ResponseEntity<Void> {
        if (!recipeIngredientRepository.existsById(ingId)) return ResponseEntity.notFound().build()
        recipeIngredientRepository.deleteById(ingId)
        return ResponseEntity.noContent().build()
    }

    // ── Ingredient ↔ Shopping List ────────────────────────────────────────────

    @PostMapping("/recipes/{id}/ingredients/{ingId}/add-to-list")
    fun addIngredientToList(
        @PathVariable id: Long,
        @PathVariable ingId: Long,
        @RequestParam listId: Long
    ): ResponseEntity<Void> {
        val user = currentUserService.currentUser()
        val list = shoppingListRepository.findById(listId).orElse(null)
            ?: return ResponseEntity.notFound().build()
        if (list.owner?.id != user.id && !list.sharedWith.any { it.id == user.id }) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
        val ingredient = recipeIngredientRepository.findById(ingId).orElse(null)
            ?: return ResponseEntity.notFound().build()
        val productName = ingredient.product?.name ?: return ResponseEntity.badRequest().build()

        val existing = shoppingListItemRepository.findByShoppingList(list)
            .find { it.name.equals(productName, ignoreCase = true) }

        if (existing != null) {
            shoppingListItemRepository.save(
                ShoppingListItem(id = existing.id, name = existing.name,
                    count = (existing.count ?: 0.0) + 1.0,
                    unitPrice = existing.unitPrice, checked = existing.checked,
                    category = existing.category, addedBy = existing.addedBy, shoppingList = existing.shoppingList)
            )
        } else {
            val product = productRepository.findByNameIgnoreCase(productName).orElseGet {
                productRepository.save(Product(name = productName, price = ingredient.product?.price))
            }
            shoppingListItemRepository.save(
                ShoppingListItem(name = productName, count = 1.0,
                    unitPrice = ingredient.product?.price,
                    category = product.category, addedBy = user, shoppingList = list)
            )
        }
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/recipes/{id}/ingredients/{ingId}/remove-from-list")
    fun removeIngredientFromList(
        @PathVariable id: Long,
        @PathVariable ingId: Long,
        @RequestParam listId: Long
    ): ResponseEntity<Void> {
        val user = currentUserService.currentUser()
        val list = shoppingListRepository.findById(listId).orElse(null)
            ?: return ResponseEntity.notFound().build()
        if (list.owner?.id != user.id && !list.sharedWith.any { it.id == user.id }) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
        val ingredient = recipeIngredientRepository.findById(ingId).orElse(null)
            ?: return ResponseEntity.notFound().build()
        val productName = ingredient.product?.name ?: return ResponseEntity.badRequest().build()

        val existing = shoppingListItemRepository.findByShoppingList(list)
            .find { it.name.equals(productName, ignoreCase = true) }
            ?: return ResponseEntity.noContent().build()

        val newCount = (existing.count ?: 1.0) - 1.0
        if (newCount <= 0) {
            shoppingListItemRepository.delete(existing)
        } else {
            shoppingListItemRepository.save(
                ShoppingListItem(id = existing.id, name = existing.name, count = newCount,
                    unitPrice = existing.unitPrice, checked = existing.checked,
                    category = existing.category, addedBy = existing.addedBy, shoppingList = existing.shoppingList)
            )
        }
        return ResponseEntity.noContent().build()
    }
}
