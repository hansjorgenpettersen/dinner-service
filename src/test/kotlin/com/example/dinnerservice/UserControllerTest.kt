package com.example.dinnerservice

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus

class UserControllerTest : IntegrationTestBase() {

    @Autowired lateinit var shoppingListRepository: ShoppingListRepository

    lateinit var token: String

    @BeforeEach
    fun setup() {
        shoppingListRepository.deleteAll()
        userRepository.deleteAll()
        token = createUserAndToken()
    }

    @Test
    fun `GET user returns profile with email and empty lists`() {
        val response = restTemplate.exchange(
            "/api/user", HttpMethod.GET, authEntity(token), UserProfileDto::class.java
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.email).isEqualTo("user@test.com")
        assertThat(response.body?.allLists).isEmpty()
        assertThat(response.body?.defaultListId).isNull()
    }

    @Test
    fun `POST set-default-list sets default list id`() {
        val user = userRepository.findByEmail("user@test.com").get()
        val list = shoppingListRepository.save(ShoppingList(name = "My List", owner = user))

        val response = restTemplate.postForEntity(
            "/api/user/set-default-list",
            authEntity(token, SetDefaultListRequest(list.id)),
            UserProfileDto::class.java
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.defaultListId).isEqualTo(list.id)
    }

    @Test
    fun `POST leave-list removes user from shared list`() {
        val owner = userRepository.save(User(email = "owner@test.com"))
        val user = userRepository.findByEmail("user@test.com").get()
        val list = shoppingListRepository.save(ShoppingList(name = "Shared", owner = owner))
        list.sharedWith.add(user)
        shoppingListRepository.save(list)

        val response = restTemplate.postForEntity(
            "/api/user/leave-list/${list.id}",
            authEntity(token), Void::class.java
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.NO_CONTENT)
        val updated = shoppingListRepository.findById(list.id).get()
        assertThat(updated.sharedWith.any { it.email == "user@test.com" }).isFalse()
    }
}
