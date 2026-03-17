package com.example.dinnerservice

import jakarta.servlet.http.HttpSession
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

@Controller
class ShoppingListController(
    private val shoppingListRepository: ShoppingListRepository,
    private val shoppingListItemRepository: ShoppingListItemRepository,
    private val userRepository: UserRepository
) {

    private fun currentUser(session: HttpSession): User? {
        val email = session.getAttribute("email") as? String ?: return null
        return userRepository.findByEmail(email).orElse(null)
    }

    @GetMapping("/shopping-lists")
    fun list(session: HttpSession, model: Model): String {
        val user = currentUser(session) ?: return "redirect:/login"
        model.addAttribute("owned", shoppingListRepository.findByOwner(user))
        model.addAttribute("shared", shoppingListRepository.findBySharedWithContaining(user))
        model.addAttribute("email", session.getAttribute("email"))
        return "shopping-lists"
    }

    @PostMapping("/shopping-lists/new")
    fun create(@RequestParam name: String, session: HttpSession): String {
        val user = currentUser(session) ?: return "redirect:/login"
        val list = shoppingListRepository.save(ShoppingList(name = name, owner = user))
        return "redirect:/shopping-lists/${list.id}"
    }

    @GetMapping("/shopping-lists/{id}")
    fun view(@PathVariable id: Long, session: HttpSession, model: Model): String {
        val user = currentUser(session) ?: return "redirect:/login"
        val list = shoppingListRepository.findById(id).orElse(null) ?: return "redirect:/shopping-lists"
        if (list.owner?.id != user.id && !list.sharedWith.any { it.id == user.id }) {
            return "redirect:/shopping-lists"
        }
        val items = shoppingListItemRepository.findByShoppingList(list)
        val totalPrice = items.mapNotNull { it.totalPrice }.sum()
        model.addAttribute("list", list)
        model.addAttribute("items", items)
        model.addAttribute("totalItems", items.size)
        model.addAttribute("totalPrice", totalPrice)
        model.addAttribute("isOwner", list.owner?.id == user.id)
        model.addAttribute("email", session.getAttribute("email"))
        return "shopping-list"
    }

    @PostMapping("/shopping-lists/{id}/items")
    fun addItem(
        @PathVariable id: Long,
        @RequestParam name: String,
        @RequestParam(required = false) count: Double?,
        @RequestParam(required = false) unitPrice: Double?,
        session: HttpSession
    ): String {
        val user = currentUser(session) ?: return "redirect:/login"
        val list = shoppingListRepository.findById(id).orElse(null) ?: return "redirect:/shopping-lists"
        if (list.owner?.id != user.id && !list.sharedWith.any { it.id == user.id }) {
            return "redirect:/shopping-lists"
        }
        shoppingListItemRepository.save(
            ShoppingListItem(name = name, count = count, unitPrice = unitPrice, addedBy = user, shoppingList = list)
        )
        return "redirect:/shopping-lists/$id"
    }

    @PostMapping("/shopping-lists/{id}/items/{itemId}/toggle")
    fun toggleItem(@PathVariable id: Long, @PathVariable itemId: Long, session: HttpSession): String {
        currentUser(session) ?: return "redirect:/login"
        val item = shoppingListItemRepository.findById(itemId).orElse(null) ?: return "redirect:/shopping-lists/$id"
        item.checked = !item.checked
        shoppingListItemRepository.save(item)
        return "redirect:/shopping-lists/$id"
    }

    @PostMapping("/shopping-lists/{id}/items/{itemId}/delete")
    fun deleteItem(@PathVariable id: Long, @PathVariable itemId: Long, session: HttpSession): String {
        val user = currentUser(session) ?: return "redirect:/login"
        val item = shoppingListItemRepository.findById(itemId).orElse(null) ?: return "redirect:/shopping-lists/$id"
        if (item.addedBy?.id == user.id || item.shoppingList?.owner?.id == user.id) {
            shoppingListItemRepository.delete(item)
        }
        return "redirect:/shopping-lists/$id"
    }

    @PostMapping("/shopping-lists/{id}/share")
    fun share(@PathVariable id: Long, @RequestParam email: String, session: HttpSession): String {
        val user = currentUser(session) ?: return "redirect:/login"
        val list = shoppingListRepository.findById(id).orElse(null) ?: return "redirect:/shopping-lists"
        if (list.owner?.id != user.id) return "redirect:/shopping-lists/$id"
        val target = userRepository.findByEmail(email.trim().lowercase()).orElse(null)
            ?: return "redirect:/shopping-lists/$id?shareError"
        list.sharedWith.add(target)
        shoppingListRepository.save(list)
        return "redirect:/shopping-lists/$id"
    }
}
