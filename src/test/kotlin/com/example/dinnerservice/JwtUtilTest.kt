package com.example.dinnerservice

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class JwtUtilTest {

    private val validSecret = "test-secret-that-is-at-least-32-characters-long"
    private val jwtUtil = JwtUtil(validSecret)

    @Test
    fun `generateToken produces a valid token containing the email`() {
        val token = jwtUtil.generateToken("user@test.com")
        assertThat(jwtUtil.isValid(token)).isTrue()
        assertThat(jwtUtil.extractEmail(token)).isEqualTo("user@test.com")
    }

    @Test
    fun `isValid returns false for a tampered token`() {
        val token = jwtUtil.generateToken("user@test.com") + "tampered"
        assertThat(jwtUtil.isValid(token)).isFalse()
    }

    @Test
    fun `isValid returns false for a garbage string`() {
        assertThat(jwtUtil.isValid("not.a.jwt")).isFalse()
    }

    @Test
    fun `constructor fails fast when secret is shorter than 32 characters`() {
        assertThatThrownBy { JwtUtil("tooshort") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("32")
    }
}
