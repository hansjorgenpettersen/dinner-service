package com.example.dinnerservice

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtUtil(@Value("\${app.jwt-secret}") private val secret: String) {

    init {
        require(secret.toByteArray().size >= 32) {
            "app.jwt-secret must produce at least 32 bytes (256 bits) for HS256"
        }
    }

    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(secret.toByteArray())
    }

    private val expiryMs = 7 * 24 * 60 * 60 * 1000L // 7 days

    fun generateToken(email: String): String =
        Jwts.builder()
            .subject(email)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + expiryMs))
            .signWith(key)
            .compact()

    fun extractEmail(token: String): String =
        Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
            .subject

    fun isValid(token: String): Boolean = try {
        extractEmail(token)
        true
    } catch (e: Exception) {
        false
    }
}
