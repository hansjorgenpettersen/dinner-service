package com.example.dinnerservice

import jakarta.servlet.http.HttpSession
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class AuthController(private val userRepository: UserRepository) {

    private val encoder = BCryptPasswordEncoder()

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
    fun login(
        @RequestParam email: String,
        @RequestParam password: String,
        session: HttpSession
    ): String {
        val trimmed = email.trim().lowercase()
        if (trimmed.isEmpty() || password.isEmpty()) return "redirect:/login?error"

        val user = userRepository.findByEmail(trimmed).orElse(null)
            ?: return "redirect:/login?error=notfound"

        if (user.passwordHash == null || !encoder.matches(password, user.passwordHash)) {
            return "redirect:/login?error=badpass"
        }

        session.setAttribute("email", trimmed)
        return "redirect:/dashboard"
    }

    @GetMapping("/register")
    fun registerPage(session: HttpSession): String {
        if (session.getAttribute("email") != null) return "redirect:/dashboard"
        return "register"
    }

    @PostMapping("/register")
    fun register(
        @RequestParam email: String,
        @RequestParam password: String,
        @RequestParam confirmPassword: String,
        session: HttpSession
    ): String {
        val trimmed = email.trim().lowercase()
        if (trimmed.isEmpty() || password.isEmpty()) return "redirect:/register?error=empty"
        if (password != confirmPassword) return "redirect:/register?error=mismatch"
        if (password.length < 8) return "redirect:/register?error=short"
        if (userRepository.findByEmail(trimmed).isPresent) return "redirect:/register?error=exists"

        val hash = encoder.encode(password)
        userRepository.save(User(email = trimmed, passwordHash = hash))
        return "redirect:/login?registered"
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
