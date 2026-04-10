package com.example.dinnerservice

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus

class RecipeControllerTest : IntegrationTestBase() {

    @Autowired lateinit var recipeRepository: RecipeRepository
    @Autowired lateinit var recipeIngredientRepository: RecipeIngredientRepository
    @Autowired lateinit var recipeImageRepository: RecipeImageRepository
    @Autowired lateinit var productRepository: ProductRepository
    @Autowired lateinit var shoppingListRepository: ShoppingListRepository
    @Autowired lateinit var shoppingListItemRepository: ShoppingListItemRepository

    lateinit var token: String

    @BeforeEach
    fun setup() {
        shoppingListItemRepository.deleteAll()
        recipeIngredientRepository.deleteAll()
        recipeImageRepository.deleteAll()
        recipeRepository.deleteAll()
        productRepository.deleteAll()
        shoppingListRepository.deleteAll()
        userRepository.deleteAll()
        token = createUserAndToken()
    }

    @Test
    fun `GET recipes returns empty list initially`() {
        val response = restTemplate.exchange(
            "/api/recipes", HttpMethod.GET, authEntity<Void>(token),
            object : ParameterizedTypeReference<List<RecipeSummaryDto>>() {}
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isEmpty()
    }

    @Test
    fun `POST recipes creates recipe and returns it`() {
        val response = restTemplate.postForEntity(
            "/api/recipes", authEntity(token, CreateRecipeRequest("Pasta", "Quick dinner")),
            RecipeSummaryDto::class.java
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(response.body?.name).isEqualTo("Pasta")
        assertThat(response.body?.description).isEqualTo("Quick dinner")
    }

    @Test
    fun `GET recipe by id returns detail with empty ingredients`() {
        val created = restTemplate.postForEntity(
            "/api/recipes", authEntity(token, CreateRecipeRequest("Pasta")),
            RecipeSummaryDto::class.java
        ).body!!

        val response = restTemplate.exchange(
            "/api/recipes/${created.id}", HttpMethod.GET, authEntity<Void>(token),
            RecipeDetailDto::class.java
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.ingredients).isEmpty()
    }

    @Test
    fun `PUT recipe updates name and description`() {
        val created = restTemplate.postForEntity(
            "/api/recipes", authEntity(token, CreateRecipeRequest("Old Name")),
            RecipeSummaryDto::class.java
        ).body!!

        val response = restTemplate.exchange(
            "/api/recipes/${created.id}", HttpMethod.PUT,
            authEntity(token, CreateRecipeRequest("New Name", "Updated desc")),
            RecipeSummaryDto::class.java
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.name).isEqualTo("New Name")
    }

    @Test
    fun `DELETE recipe removes it`() {
        val created = restTemplate.postForEntity(
            "/api/recipes", authEntity(token, CreateRecipeRequest("Temp")),
            RecipeSummaryDto::class.java
        ).body!!

        val deleteResponse = restTemplate.exchange(
            "/api/recipes/${created.id}", HttpMethod.DELETE, authEntity<Void>(token), Void::class.java
        )
        assertThat(deleteResponse.statusCode).isEqualTo(HttpStatus.NO_CONTENT)
        assertThat(recipeRepository.findById(created.id).isEmpty).isTrue()
    }

    @Test
    fun `POST ingredients adds ingredient to recipe`() {
        val recipe = restTemplate.postForEntity(
            "/api/recipes", authEntity(token, CreateRecipeRequest("Pasta")),
            RecipeSummaryDto::class.java
        ).body!!
        val product = productRepository.save(Product(name = "Spaghetti"))

        val response = restTemplate.postForEntity(
            "/api/recipes/${recipe.id}/ingredients",
            authEntity(token, AddIngredientRequest(product.id, 200.0, "g")),
            IngredientDto::class.java
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(response.body?.productName).isEqualTo("Spaghetti")
    }

    @Test
    fun `POST add-to-list adds ingredient to selected shopping list`() {
        val user = userRepository.findByEmail("user@test.com").get()
        val list = shoppingListRepository.save(ShoppingList(name = "My List", owner = user))
        val product = productRepository.save(Product(name = "Tomato"))
        val recipe = recipeRepository.save(Recipe(name = "Sauce"))
        val ingredient = recipeIngredientRepository.save(
            RecipeIngredient(recipe = recipe, product = product, quantity = 3.0, unit = "pcs")
        )

        val response = restTemplate.postForEntity(
            "/api/recipes/${recipe.id}/ingredients/${ingredient.id}/add-to-list?listId=${list.id}",
            authEntity<Void>(token),
            Void::class.java
        )
        assertThat(response.statusCode).isEqualTo(HttpStatus.NO_CONTENT)
        val items = shoppingListItemRepository.findByShoppingList(list)
        assertThat(items).hasSize(1)
        assertThat(items.first().name).isEqualTo("Tomato")
    }
}
