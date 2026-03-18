package com.example.dinnerservice

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpSession
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import java.time.LocalDateTime
import java.util.UUID

@Controller
class AuthController(
    private val userRepository: UserRepository,
    private val resetTokenRepository: PasswordResetTokenRepository,
    private val mailSender: JavaMailSender
) {

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

    @GetMapping("/forgot-password")
    fun forgotPasswordPage(): String = "forgot-password"

    @PostMapping("/forgot-password")
    fun forgotPassword(@RequestParam email: String, request: HttpServletRequest): String {
        val trimmed = email.trim().lowercase()
        val user = userRepository.findByEmail(trimmed).orElse(null)

        // Always show "sent" to avoid revealing whether an email exists
        if (user != null) {
            resetTokenRepository.deleteByUser(user)
            val token = UUID.randomUUID().toString()
            resetTokenRepository.save(PasswordResetToken(token = token, user = user))

            val baseUrl = "${request.scheme}://${request.serverName}:${request.serverPort}"
            val resetUrl = "$baseUrl/reset-password?token=$token"

            val message = SimpleMailMessage()
            message.setTo(user.email)
            message.subject = "Dinner Service – Password Reset"
            message.text = "Click the link below to reset your password (expires in 1 hour):\n\n$resetUrl\n\nIf you did not request this, you can ignore this email."
            mailSender.send(message)
        }

        return "redirect:/forgot-password?sent"
    }

    @GetMapping("/reset-password")
    fun resetPasswordPage(@RequestParam token: String, model: Model): String {
        val resetToken = resetTokenRepository.findByToken(token).orElse(null)
        if (resetToken == null || resetToken.expiresAt.isBefore(LocalDateTime.now())) {
            return "redirect:/forgot-password?expired"
        }
        model.addAttribute("token", token)
        return "reset-password"
    }

    @PostMapping("/reset-password")
    fun resetPassword(
        @RequestParam token: String,
        @RequestParam password: String,
        @RequestParam confirmPassword: String
    ): String {
        if (password.length < 8) return "redirect:/reset-password?token=$token&error=short"
        if (password != confirmPassword) return "redirect:/reset-password?token=$token&error=mismatch"

        val resetToken = resetTokenRepository.findByToken(token).orElse(null)
        if (resetToken == null || resetToken.expiresAt.isBefore(LocalDateTime.now())) {
            return "redirect:/forgot-password?expired"
        }

        val user = resetToken.user
        user.passwordHash = encoder.encode(password)
        userRepository.save(user)
        resetTokenRepository.delete(resetToken)

        return "redirect:/login?reset"
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
