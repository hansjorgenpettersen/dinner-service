package com.example.dinnerservice

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus

class AuthControllerTest : IntegrationTestBase() {

    @Autowired
    lateinit var resetTokenRepository: PasswordResetTokenRepository

    @BeforeEach
    fun cleanup() {
        resetTokenRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun `POST login returns JWT for valid credentials`() {
        createUserAndToken("alice@test.com")
        val response = restTemplate.postForEntity(
            "/api/auth/login",
            LoginRequest("alice@test.com", "password123"),
            AuthResponse::class.java
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.token).isNotBlank()
        assertThat(response.body?.email).isEqualTo("alice@test.com")
    }

    @Test
    fun `POST login returns 401 for wrong password`() {
        createUserAndToken("alice@test.com")
        val response = restTemplate.postForEntity(
            "/api/auth/login",
            LoginRequest("alice@test.com", "wrongpassword"),
            ErrorResponse::class.java
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
    }

    @Test
    fun `POST login returns 401 for unknown email`() {
        val response = restTemplate.postForEntity(
            "/api/auth/login",
            LoginRequest("nobody@test.com", "password123"),
            ErrorResponse::class.java
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
    }

    @Test
    fun `POST register creates user and returns JWT`() {
        val response = restTemplate.postForEntity(
            "/api/auth/register",
            RegisterRequest("new@test.com", "password123", "password123"),
            AuthResponse::class.java
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(response.body?.token).isNotBlank()
        assertThat(userRepository.findByEmail("new@test.com").isPresent).isTrue()
    }

    @Test
    fun `POST register returns 409 for duplicate email`() {
        createUserAndToken("existing@test.com")
        val response = restTemplate.postForEntity(
            "/api/auth/register",
            RegisterRequest("existing@test.com", "password123", "password123"),
            ErrorResponse::class.java
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.CONFLICT)
    }

    @Test
    fun `POST register returns 400 when passwords do not match`() {
        val response = restTemplate.postForEntity(
            "/api/auth/register",
            RegisterRequest("new@test.com", "password123", "different123"),
            ErrorResponse::class.java
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `authenticated endpoint returns 401 without token`() {
        val response = restTemplate.getForEntity("/api/recipes", String::class.java)
        assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
    }
}
