package com.example.dinnerservice

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus

class ProductControllerTest : IntegrationTestBase() {

    @Autowired lateinit var productRepository: ProductRepository
    @Autowired lateinit var categoryRepository: CategoryRepository

    lateinit var token: String

    @BeforeEach
    fun setup() {
        productRepository.deleteAll()
        categoryRepository.deleteAll()
        userRepository.deleteAll()
        token = createUserAndToken()
    }

    @Test
    fun `GET products returns empty list initially`() {
        val response = restTemplate.exchange(
            "/api/products", HttpMethod.GET, authEntity<Void>(token),
            object : ParameterizedTypeReference<List<ProductDto>>() {}
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isEmpty()
    }

    @Test
    fun `POST products creates a product`() {
        val response = restTemplate.postForEntity(
            "/api/products", authEntity(token, CreateProductRequest("Milk", 1.5, null)),
            ProductDto::class.java
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(response.body?.name).isEqualTo("Milk")
        assertThat(response.body?.price).isEqualTo(1.5)
    }

    @Test
    fun `POST categories creates a category`() {
        val response = restTemplate.postForEntity(
            "/api/categories", authEntity(token, CreateCategoryRequest("Dairy", "#ffffff")),
            CategoryDto::class.java
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(response.body?.name).isEqualTo("Dairy")
    }

    @Test
    fun `GET products search returns matching products`() {
        restTemplate.postForEntity("/api/products", authEntity(token, CreateProductRequest("Whole Milk", null, null)), ProductDto::class.java)
        restTemplate.postForEntity("/api/products", authEntity(token, CreateProductRequest("Butter", null, null)), ProductDto::class.java)

        val response = restTemplate.exchange(
            "/api/products/search?q=milk", HttpMethod.GET, authEntity<Void>(token),
            object : ParameterizedTypeReference<List<ProductDto>>() {}
        )
        assertThat(response.body).hasSize(1)
        assertThat(response.body?.first()?.name).isEqualTo("Whole Milk")
    }

    @Test
    fun `PUT products updates name and category`() {
        val category = categoryRepository.save(Category(name = "Dairy", color = "#fff"))
        val created = restTemplate.postForEntity(
            "/api/products", authEntity(token, CreateProductRequest("Mlk", null, null)),
            ProductDto::class.java
        ).body!!

        val response = restTemplate.exchange(
            "/api/products/${created.id}", HttpMethod.PUT,
            authEntity(token, CreateProductRequest("Milk", null, category.id)),
            ProductDto::class.java
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.name).isEqualTo("Milk")
        assertThat(response.body?.categoryName).isEqualTo("Dairy")
    }

    @Test
    fun `DELETE products removes the product`() {
        val created = restTemplate.postForEntity(
            "/api/products", authEntity(token, CreateProductRequest("Temp", null, null)),
            ProductDto::class.java
        ).body!!

        val deleteResponse = restTemplate.exchange(
            "/api/products/${created.id}", HttpMethod.DELETE, authEntity<Void>(token), Void::class.java
        )
        assertThat(deleteResponse.statusCode).isEqualTo(HttpStatus.NO_CONTENT)
        assertThat(productRepository.findById(created.id).isEmpty).isTrue()
    }
}
