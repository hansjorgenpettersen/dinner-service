package com.example.dinnerservice

import jakarta.persistence.*
import java.time.LocalDateTime
import java.time.ZoneOffset

@Entity
@Table(name = "password_reset_tokens")
class PasswordResetToken(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(unique = true, nullable = false)
    val token: String = "",
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    val user: User = User(),
    val expiresAt: LocalDateTime = LocalDateTime.now(ZoneOffset.UTC).plusHours(1)
)
