package com.example.dinnerservice

import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service

@Service
class CurrentUserService(private val userRepository: UserRepository) {

    /**
     * Returns the authenticated user. Throws AccessDeniedException if not authenticated,
     * or IllegalStateException if the JWT email doesn't match any user.
     */
    fun currentUser(): User {
        val email = SecurityContextHolder.getContext().authentication?.principal as? String
            ?: throw AccessDeniedException("Not authenticated")
        return userRepository.findByEmail(email)
            .orElseThrow { IllegalStateException("Authenticated user not found in database: $email") }
    }
}
