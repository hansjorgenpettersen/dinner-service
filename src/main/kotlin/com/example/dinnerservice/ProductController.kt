package com.example.dinnerservice

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class ProductController(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val shoppingListItemRepository: ShoppingListItemRepository
) {

    private fun Product.toDto() = ProductDto(
        id = id, name = name, price = price,
        categoryId = category?.id, categoryName = category?.name, categoryColor = category?.color
    )

    private fun Category.toDto() = CategoryDto(id = id, name = name, color = color)

    @GetMapping("/products")
    fun listProducts(): ResponseEntity<List<ProductDto>> =
        ResponseEntity.ok(
            productRepository.findAll()
                .sortedWith(compareBy({ it.category?.name ?: "zzz" }, { it.name.lowercase() }))
                .map { it.toDto() }
        )

    @GetMapping("/products/uncategorized")
    fun uncategorized(): ResponseEntity<List<ProductDto>> =
        ResponseEntity.ok(
            productRepository.findAll()
                .filter { it.category == null }
                .sortedBy { it.name.lowercase() }
                .map { it.toDto() }
        )

    @GetMapping("/products/search")
    fun search(@RequestParam q: String): ResponseEntity<List<ProductDto>> =
        ResponseEntity.ok(
            productRepository.findByNameContainingIgnoreCase(q)
                .sortedBy { it.name.lowercase() }
                .map { it.toDto() }
        )

    @PostMapping("/products")
    fun createProduct(@Valid @RequestBody req: CreateProductRequest): ResponseEntity<ProductDto> {
        val trimmed = req.name.trim()
        if (productRepository.findByNameIgnoreCase(trimmed).isPresent) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build()
        }
        val category = req.categoryId?.let { categoryRepository.findById(it).orElse(null) }
        val product = productRepository.save(Product(name = trimmed, price = req.price, category = category))
        return ResponseEntity.status(HttpStatus.CREATED).body(product.toDto())
    }

    @PutMapping("/products/{id}")
    fun editProduct(
        @PathVariable id: Long,
        @Valid @RequestBody req: CreateProductRequest
    ): ResponseEntity<ProductDto> {
        val product = productRepository.findById(id).orElse(null)
            ?: return ResponseEntity.notFound().build()
        val category = req.categoryId?.let { categoryRepository.findById(it).orElse(null) }
        val trimmed = req.name.trim()
        val updated = productRepository.save(Product(id = product.id, name = trimmed, price = req.price, category = category))
        // Sync category on existing shopping list items with this product name
        shoppingListItemRepository.findByNameIgnoreCase(trimmed).forEach { item ->
            shoppingListItemRepository.save(
                ShoppingListItem(id = item.id, name = item.name, count = item.count,
                    unitPrice = item.unitPrice, checked = item.checked, category = category,
                    addedBy = item.addedBy, shoppingList = item.shoppingList)
            )
        }
        return ResponseEntity.ok(updated.toDto())
    }

    @DeleteMapping("/products/{id}")
    fun deleteProduct(@PathVariable id: Long): ResponseEntity<Void> {
        if (!productRepository.existsById(id)) return ResponseEntity.notFound().build()
        productRepository.deleteById(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/categories")
    fun listCategories(): ResponseEntity<List<CategoryDto>> =
        ResponseEntity.ok(
            categoryRepository.findAll().sortedBy { it.name.lowercase() }.map { it.toDto() }
        )

    @PostMapping("/categories")
    fun createCategory(@Valid @RequestBody req: CreateCategoryRequest): ResponseEntity<CategoryDto> {
        val category = categoryRepository.save(Category(name = req.name.trim(), color = req.color))
        return ResponseEntity.status(HttpStatus.CREATED).body(category.toDto())
    }

    @DeleteMapping("/categories/{id}")
    fun deleteCategory(@PathVariable id: Long): ResponseEntity<Void> {
        if (!categoryRepository.existsById(id)) return ResponseEntity.notFound().build()
        categoryRepository.deleteById(id)
        return ResponseEntity.noContent().build()
    }
}
