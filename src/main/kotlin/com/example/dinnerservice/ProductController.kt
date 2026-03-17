package com.example.dinnerservice

import jakarta.servlet.http.HttpSession
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

@Controller
class ProductController(private val productRepository: ProductRepository) {

    @GetMapping("/products")
    fun list(session: HttpSession, model: Model): String {
        session.getAttribute("email") ?: return "redirect:/login"
        model.addAttribute("products", productRepository.findAll().sortedBy { it.name.lowercase() })
        return "products"
    }

    @PostMapping("/products/new")
    fun create(
        @RequestParam name: String,
        @RequestParam(required = false) price: Double?,
        session: HttpSession
    ): String {
        session.getAttribute("email") ?: return "redirect:/login"
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return "redirect:/products"
        if (productRepository.findByNameIgnoreCase(trimmed).isEmpty) {
            productRepository.save(Product(name = trimmed, price = price))
        }
        return "redirect:/products"
    }

    @PostMapping("/products/{id}/delete")
    fun delete(@PathVariable id: Long, session: HttpSession): String {
        session.getAttribute("email") ?: return "redirect:/login"
        productRepository.deleteById(id)
        return "redirect:/products"
    }

    @GetMapping("/products/search")
    @ResponseBody
    fun search(@RequestParam q: String): List<Map<String, Any?>> {
        return productRepository.findByNameContainingIgnoreCase(q)
            .sortedBy { it.name.lowercase() }
            .map { mapOf("name" to it.name, "price" to it.price) }
    }
}
