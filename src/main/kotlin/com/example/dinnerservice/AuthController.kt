package com.example.dinnerservice

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val userRepository: UserRepository,
    private val resetTokenRepository: PasswordResetTokenRepository,
    private val mailSender: JavaMailSender,
    private val jwtUtil: JwtUtil
) {
    private val encoder = BCryptPasswordEncoder()

    @PostMapping("/login")
    fun login(@Valid @RequestBody req: LoginRequest): ResponseEntity<AuthResponse> {
        val user = userRepository.findByEmail(req.email.trim().lowercase())
            .orElseThrow { ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials") }
        if (user.passwordHash == null || !encoder.matches(req.password, user.passwordHash)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials")
        }
        return ResponseEntity.ok(AuthResponse(jwtUtil.generateToken(user.email), user.email))
    }

    @PostMapping("/register")
    fun register(@Valid @RequestBody req: RegisterRequest): ResponseEntity<AuthResponse> {
        if (req.password != req.confirmPassword) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match")
        }
        val email = req.email.trim().lowercase()
        if (userRepository.findByEmail(email).isPresent) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Email already in use")
        }
        val user = userRepository.save(User(email = email, passwordHash = encoder.encode(req.password)))
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(AuthResponse(jwtUtil.generateToken(user.email), user.email))
    }

    @PostMapping("/logout")
    fun logout(): ResponseEntity<Void> = ResponseEntity.noContent().build()

    @PostMapping("/forgot-password")
    fun forgotPassword(
        @Valid @RequestBody req: ForgotPasswordRequest,
        request: HttpServletRequest
    ): ResponseEntity<Map<String, String>> {
        val user = userRepository.findByEmail(req.email.trim().lowercase()).orElse(null)
        if (user != null) {
            resetTokenRepository.deleteByUser(user)
            val token = UUID.randomUUID().toString()
            resetTokenRepository.save(PasswordResetToken(token = token, user = user))

            val baseUrl = "${request.scheme}://${request.serverName}:${request.serverPort}"
            val resetUrl = "$baseUrl/reset-password?token=$token"

            val message = SimpleMailMessage()
            message.setTo(user.email)
            message.subject = "Dinner Service – Password Reset"
            message.text = "Click the link below to reset your password (expires in 1 hour):\n\n$resetUrl"
            mailSender.send(message)
        }
        // Always return success to avoid revealing whether an email exists
        return ResponseEntity.ok(mapOf("message" to "If that email exists, a reset link has been sent."))
    }

    @PostMapping("/reset-password")
    fun resetPassword(@Valid @RequestBody req: ResetPasswordRequest): ResponseEntity<Map<String, String>> {
        if (req.password != req.confirmPassword) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match")
        }
        val resetToken = resetTokenRepository.findByToken(req.token).orElse(null)
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "TOKEN_INVALID")
        if (resetToken.expiresAt.isBefore(LocalDateTime.now(ZoneOffset.UTC))) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "TOKEN_EXPIRED")
        }
        val user = resetToken.user
        user.passwordHash = encoder.encode(req.password)
        userRepository.save(user)
        resetTokenRepository.delete(resetToken)
        return ResponseEntity.ok(mapOf("message" to "Password reset successfully."))
    }
}
