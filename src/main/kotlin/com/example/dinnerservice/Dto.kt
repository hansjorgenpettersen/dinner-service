package com.example.dinnerservice

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

// ── Auth ──────────────────────────────────────────────────────────────────────

data class LoginRequest(
    @field:NotBlank val email: String,
    @field:NotBlank val password: String
)

data class RegisterRequest(
    @field:NotBlank @field:Email val email: String,
    @field:Size(min = 8, message = "Password must be at least 8 characters") val password: String,
    @field:NotBlank val confirmPassword: String
)

data class AuthResponse(val token: String, val email: String)

data class ForgotPasswordRequest(@field:NotBlank @field:Email val email: String)

data class ResetPasswordRequest(
    @field:NotBlank val token: String,
    @field:Size(min = 8, message = "Password must be at least 8 characters") val password: String,
    @field:NotBlank val confirmPassword: String
)

// ── Recipe ────────────────────────────────────────────────────────────────────

data class RecipeSummaryDto(val id: Long, val name: String, val description: String, val previewImage: String?)

data class IngredientDto(val id: Long, val productId: Long?, val productName: String?, val quantity: Double?, val unit: String)

data class RecipeImageDto(val id: Long, val filename: String, val originalName: String)

data class RecipeDetailDto(
    val id: Long,
    val name: String,
    val description: String,
    val ingredients: List<IngredientDto>,
    val images: List<RecipeImageDto>,
    val shoppingLists: List<ShoppingListSummaryDto>,
    val selectedListId: Long?,
    val ingredientCounts: Map<Long, Int>
)

data class CreateRecipeRequest(@field:NotBlank val name: String, val description: String = "")

data class AddIngredientRequest(
    val productId: Long,
    val quantity: Double?,
    @field:NotBlank val unit: String
)

data class SelectListRequest(val listId: Long?)

// ── Shopping List ─────────────────────────────────────────────────────────────

data class ShoppingListSummaryDto(val id: Long, val name: String, val ownerEmail: String?)

data class ShoppingListItemDto(
    val id: Long,
    val name: String,
    val count: Double?,
    val unitPrice: Double?,
    val totalPrice: Double?,
    val checked: Boolean,
    val categoryId: Long?,
    val categoryName: String?,
    val categoryColor: String?,
    val addedByEmail: String?
)

data class ShoppingListDetailDto(
    val id: Long,
    val name: String,
    val items: List<ShoppingListItemDto>,
    val totalPrice: Double,
    val isOwner: Boolean,
    val sharedWith: List<String>
)

data class ShoppingListsResponse(
    val owned: List<ShoppingListSummaryDto>,
    val shared: List<ShoppingListSummaryDto>
)

data class CreateShoppingListRequest(@field:NotBlank val name: String)

data class AddItemRequest(
    @field:NotBlank val name: String,
    val count: Double?,
    val unitPrice: Double?
)

// count is Int (not Double?) because the stepper UI only supports whole-number steps.
// The entity stores Double? internally but this endpoint enforces integer-only updates.
data class UpdateItemCountRequest(@field:Min(1) val count: Int)

data class ShareRequest(@field:NotBlank @field:Email val email: String)

// ── Product ───────────────────────────────────────────────────────────────────

data class ProductDto(
    val id: Long,
    val name: String,
    val price: Double?,
    val categoryId: Long?,
    val categoryName: String?,
    val categoryColor: String?
)

data class CategoryDto(val id: Long, val name: String, val color: String)

data class CreateProductRequest(
    @field:NotBlank val name: String,
    val price: Double?,
    val categoryId: Long?
)

data class CreateCategoryRequest(
    @field:NotBlank val name: String,
    val color: String = "#cccccc"
)

// ── User ──────────────────────────────────────────────────────────────────────

data class UserProfileDto(
    val email: String,
    val defaultListId: Long?,
    val allLists: List<ShoppingListSummaryDto>,
    val sharedLists: List<ShoppingListSummaryDto>
)

data class SetDefaultListRequest(val listId: Long?)
