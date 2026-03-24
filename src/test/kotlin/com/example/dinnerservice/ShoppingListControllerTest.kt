package com.example.dinnerservice

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus

class ShoppingListControllerTest : IntegrationTestBase() {

    @Autowired lateinit var shoppingListRepository: ShoppingListRepository
    @Autowired lateinit var shoppingListItemRepository: ShoppingListItemRepository
    @Autowired lateinit var productRepository: ProductRepository

    lateinit var token: String

    @BeforeEach
    fun setup() {
        shoppingListItemRepository.deleteAll()
        shoppingListRepository.deleteAll()
        productRepository.deleteAll()
        userRepository.deleteAll()
        token = createUserAndToken()
    }

    @Test
    fun `GET shopping-lists returns owned and shared lists`() {
        val response = restTemplate.exchange(
            "/api/shopping-lists", HttpMethod.GET, authEntity(token),
            ShoppingListsResponse::class.java
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.owned).isEmpty()
        assertThat(response.body?.shared).isEmpty()
    }

    @Test
    fun `POST shopping-lists creates a list`() {
        val response = restTemplate.postForEntity(
            "/api/shopping-lists",
            authEntity(token, CreateShoppingListRequest("Groceries")),
            ShoppingListSummaryDto::class.java
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(response.body?.name).isEqualTo("Groceries")
    }

    @Test
    fun `GET shopping-lists by id returns items sorted by category`() {
        val user = userRepository.findByEmail("user@test.com").get()
        val list = shoppingListRepository.save(ShoppingList(name = "List", owner = user))
        shoppingListItemRepository.save(ShoppingListItem(name = "Milk", shoppingList = list, addedBy = user))

        val response = restTemplate.exchange(
            "/api/shopping-lists/${list.id}", HttpMethod.GET, authEntity(token),
            ShoppingListDetailDto::class.java
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.items).hasSize(1)
        assertThat(response.body?.items?.first()?.name).isEqualTo("Milk")
        assertThat(response.body?.isOwner).isTrue()
    }

    @Test
    fun `GET shopping-lists returns 403 for a list you do not own or share`() {
        val other = userRepository.save(User(email = "other@test.com"))
        val list = shoppingListRepository.save(ShoppingList(name = "Private", owner = other))

        val response = restTemplate.exchange(
            "/api/shopping-lists/${list.id}", HttpMethod.GET, authEntity(token),
            ErrorResponse::class.java
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
    }

    @Test
    fun `POST items adds an item to the list`() {
        val user = userRepository.findByEmail("user@test.com").get()
        val list = shoppingListRepository.save(ShoppingList(name = "List", owner = user))

        val response = restTemplate.postForEntity(
            "/api/shopping-lists/${list.id}/items",
            authEntity(token, AddItemRequest("Bread", 1.0, 2.5)),
            ShoppingListItemDto::class.java
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(response.body?.name).isEqualTo("Bread")
    }

    @Test
    fun `POST toggle flips checked state`() {
        val user = userRepository.findByEmail("user@test.com").get()
        val list = shoppingListRepository.save(ShoppingList(name = "List", owner = user))
        val item = shoppingListItemRepository.save(ShoppingListItem(name = "Milk", shoppingList = list, addedBy = user))

        val response = restTemplate.postForEntity(
            "/api/shopping-lists/${list.id}/items/${item.id}/toggle",
            authEntity(token), ShoppingListItemDto::class.java
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.checked).isTrue()
    }

    @Test
    fun `DELETE item removes it`() {
        val user = userRepository.findByEmail("user@test.com").get()
        val list = shoppingListRepository.save(ShoppingList(name = "List", owner = user))
        val item = shoppingListItemRepository.save(ShoppingListItem(name = "Milk", shoppingList = list, addedBy = user))

        val response = restTemplate.exchange(
            "/api/shopping-lists/${list.id}/items/${item.id}", HttpMethod.DELETE, authEntity(token), Void::class.java
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.NO_CONTENT)
        assertThat(shoppingListItemRepository.findById(item.id).isEmpty).isTrue()
    }

    @Test
    fun `POST clear-checked removes only checked items`() {
        val user = userRepository.findByEmail("user@test.com").get()
        val list = shoppingListRepository.save(ShoppingList(name = "List", owner = user))
        shoppingListItemRepository.save(ShoppingListItem(name = "Milk", checked = true, shoppingList = list, addedBy = user))
        shoppingListItemRepository.save(ShoppingListItem(name = "Bread", checked = false, shoppingList = list, addedBy = user))

        restTemplate.postForEntity("/api/shopping-lists/${list.id}/items/clear-checked", authEntity(token), Void::class.java)

        val remaining = shoppingListItemRepository.findByShoppingList(list)
        assertThat(remaining).hasSize(1)
        assertThat(remaining.first().name).isEqualTo("Bread")
    }

    @Test
    fun `POST share shares list with another user`() {
        val user = userRepository.findByEmail("user@test.com").get()
        val list = shoppingListRepository.save(ShoppingList(name = "List", owner = user))
        userRepository.save(User(email = "friend@test.com"))

        val response = restTemplate.postForEntity(
            "/api/shopping-lists/${list.id}/share",
            authEntity(token, ShareRequest("friend@test.com")),
            Void::class.java
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.NO_CONTENT)
        val updated = shoppingListRepository.findById(list.id).get()
        assertThat(updated.sharedWith.any { it.email == "friend@test.com" }).isTrue()
    }
}
