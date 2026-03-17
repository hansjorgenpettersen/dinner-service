package com.example.dinnerservice

import jakarta.servlet.http.HttpSession
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class AuthController(private val userRepository: UserRepository) {

    @GetMapping("/")
    fun index(session: HttpSession): String {
        if (session.getAttribute("email") != null) return "redirect:/dashboard"
        return "redirect:/login"
    }

    @GetMapping("/login")
    fun loginPage(session: HttpSession): String {
        if (session.getAttribute("email") != null) return "redirect:/dashboard"
        return "login"
    }

    @PostMapping("/login")
    fun login(@RequestParam email: String, session: HttpSession): String {
        val trimmed = email.trim().lowercase()
        if (trimmed.isEmpty()) return "redirect:/login?error"
        userRepository.findByEmail(trimmed).orElseGet { userRepository.save(User(email = trimmed)) }
        session.setAttribute("email", trimmed)
        return "redirect:/dashboard"
    }

    @GetMapping("/dashboard")
    fun dashboard(session: HttpSession, model: Model): String {
        val email = session.getAttribute("email") ?: return "redirect:/login"
        model.addAttribute("email", email)
        return "dashboard"
    }

    @PostMapping("/logout")
    fun logout(session: HttpSession): String {
        session.invalidate()
        return "redirect:/login"
    }
}
