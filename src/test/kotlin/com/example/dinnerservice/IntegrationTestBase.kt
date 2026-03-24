package com.example.dinnerservice

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
abstract class IntegrationTestBase {

    companion object {
        @Container
        @JvmStatic
        val postgres: PostgreSQLContainer<Nothing> = PostgreSQLContainer<Nothing>("postgres:15-alpine").apply {
            withDatabaseName("dinnerservice_test")
            withUsername("test")
            withPassword("test")
        }

        @DynamicPropertySource
        @JvmStatic
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
            // Flyway creates the schema from scratch in tests — no baseline needed
            registry.add("spring.flyway.baseline-on-migrate") { "false" }
        }
    }

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var jwtUtil: JwtUtil

    private val encoder = BCryptPasswordEncoder()

    /**
     * Creates a user in the database and returns a valid JWT for that user.
     * Default password is "password123".
     */
    fun createUserAndToken(email: String = "user@test.com", password: String = "password123"): String {
        userRepository.save(User(email = email, passwordHash = encoder.encode(password)))
        return jwtUtil.generateToken(email)
    }

    /** Returns HttpHeaders with the Bearer token set. */
    fun authHeaders(token: String): HttpHeaders =
        HttpHeaders().apply { setBearerAuth(token) }

    /** Wraps a body with auth headers into an HttpEntity for use with restTemplate. */
    fun <T> authEntity(token: String, body: T? = null): HttpEntity<T> =
        HttpEntity(body, authHeaders(token))
}
