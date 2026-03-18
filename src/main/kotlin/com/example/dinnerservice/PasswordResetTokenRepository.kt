package com.example.dinnerservice

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.transaction.annotation.Transactional
import java.util.Optional

interface PasswordResetTokenRepository : JpaRepository<PasswordResetToken, Long> {
    fun findByToken(token: String): Optional<PasswordResetToken>

    @Transactional
    fun deleteByUser(user: User)
}
