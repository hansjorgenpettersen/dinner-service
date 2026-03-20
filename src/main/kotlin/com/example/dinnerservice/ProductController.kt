package com.example.dinnerservice

import jakarta.servlet.http.HttpSession
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

@Controller
class ProductController(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val shoppingListItemRepository: ShoppingListItemRepository
) {

    @GetMapping("/products")
    fun list(session: HttpSession, model: Model): String {
        session.getAttribute("email") ?: return "redirect:/login"
        model.addAttribute("products", productRepository.findAll().sortedWith(
            compareBy({ it.category?.name ?: "zzz" }, { it.name.lowercase() })
        ))
        model.addAttribute("categories", categoryRepository.findAll().sortedBy { it.name.lowercase() })
        return "products"
    }

    @PostMapping("/products/new")
    fun create(
        @RequestParam name: String,
        @RequestParam(required = false) price: Double?,
        @RequestParam(required = false) categoryId: Long?,
        session: HttpSession
    ): String {
        session.getAttribute("email") ?: return "redirect:/login"
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return "redirect:/products"
        val category = categoryId?.let { categoryRepository.findById(it).orElse(null) }
        if (productRepository.findByNameIgnoreCase(trimmed).isEmpty) {
            productRepository.save(Product(name = trimmed, price = price, category = category))
        }
        return "redirect:/products"
    }

    @PostMapping("/products/{id}/edit")
    fun edit(
        @PathVariable id: Long,
        @RequestParam name: String,
        @RequestParam(required = false) price: Double?,
        @RequestParam(required = false) categoryId: Long?,
        session: HttpSession
    ): String {
        session.getAttribute("email") ?: return "redirect:/login"
        val product = productRepository.findById(id).orElse(null) ?: return "redirect:/products"
        val category = categoryId?.let { categoryRepository.findById(it).orElse(null) }
        val trimmedName = name.trim()
        productRepository.save(Product(id = product.id, name = trimmedName, price = price, category = category))
        shoppingListItemRepository.findByNameIgnoreCase(trimmedName).forEach { item ->
            shoppingListItemRepository.save(
                ShoppingListItem(id = item.id, name = item.name, count = item.count,
                    unitPrice = item.unitPrice, checked = item.checked, category = category,
                    addedBy = item.addedBy, shoppingList = item.shoppingList)
            )
        }
        return "redirect:/products"
    }

    @PostMapping("/products/{id}/delete")
    fun delete(@PathVariable id: Long, session: HttpSession): String {
        session.getAttribute("email") ?: return "redirect:/login"
        productRepository.deleteById(id)
        return "redirect:/products"
    }

    @PostMapping("/categories/new")
    fun createCategory(
        @RequestParam name: String,
        @RequestParam(defaultValue = "#cccccc") color: String,
        session: HttpSession
    ): String {
        session.getAttribute("email") ?: return "redirect:/login"
        categoryRepository.save(Category(name = name.trim(), color = color))
        return "redirect:/products"
    }

    @PostMapping("/categories/{id}/delete")
    fun deleteCategory(@PathVariable id: Long, session: HttpSession): String {
        session.getAttribute("email") ?: return "redirect:/login"
        categoryRepository.deleteById(id)
        return "redirect:/products"
    }

    @GetMapping("/products/search")
    @ResponseBody
    fun search(@RequestParam q: String): List<Map<String, Any?>> {
        return productRepository.findByNameContainingIgnoreCase(q)
            .sortedBy { it.name.lowercase() }
            .map { mapOf("name" to it.name, "price" to it.price, "categoryColor" to it.category?.color) }
    }
}
