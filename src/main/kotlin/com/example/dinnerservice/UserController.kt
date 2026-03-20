package com.example.dinnerservice

import jakarta.servlet.http.HttpSession
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

@Controller
class UserController(
    private val userRepository: UserRepository,
    private val shoppingListRepository: ShoppingListRepository
) {

    private fun currentUser(session: HttpSession): User? {
        val email = session.getAttribute("email") as? String ?: return null
        return userRepository.findByEmail(email).orElse(null)
    }

    @GetMapping("/user")
    fun userPage(session: HttpSession, model: Model): String {
        val user = currentUser(session) ?: return "redirect:/login"
        val owned = shoppingListRepository.findByOwner(user)
        val shared = shoppingListRepository.findBySharedWithContaining(user)
        model.addAttribute("email", user.email)
        model.addAttribute("allLists", (owned + shared).sortedBy { it.name.lowercase() })
        model.addAttribute("sharedLists", shared)
        model.addAttribute("defaultListId", user.defaultListId)
        return "user"
    }

    @PostMapping("/user/set-default-list")
    fun setDefaultList(
        @RequestParam(required = false) listId: Long?,
        session: HttpSession
    ): String {
        val user = currentUser(session) ?: return "redirect:/login"
        user.defaultListId = listId
        userRepository.save(user)
        return "redirect:/user"
    }

    @PostMapping("/user/leave-list/{id}")
    fun leaveList(@PathVariable id: Long, session: HttpSession): String {
        val user = currentUser(session) ?: return "redirect:/login"
        val list = shoppingListRepository.findById(id).orElse(null) ?: return "redirect:/user"
        list.sharedWith.remove(user)
        if (user.defaultListId == id) {
            user.defaultListId = null
            userRepository.save(user)
        }
        shoppingListRepository.save(list)
        return "redirect:/user"
    }
}
