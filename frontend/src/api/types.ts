// Auth
export interface AuthResponse { token: string; email: string }
export interface LoginRequest { email: string; password: string }
export interface RegisterRequest { email: string; password: string; confirmPassword: string }
export interface ForgotPasswordRequest { email: string }
export interface ResetPasswordRequest { token: string; password: string; confirmPassword: string }

// Recipes
export interface RecipeSummary { id: number; name: string; description: string; previewImage: string | null }
export interface Ingredient { id: number; productId: number | null; productName: string | null; quantity: number | null; unit: string }
export interface RecipeImage { id: number; filename: string; originalName: string }
export interface ShoppingListSummary { id: number; name: string; ownerEmail: string | null }
export interface RecipeDetail {
  id: number; name: string; description: string
  ingredients: Ingredient[]
  images: RecipeImage[]
  shoppingLists: ShoppingListSummary[]
  selectedListId: number | null
  ingredientCounts: Record<number, number>
}

// Shopping Lists
export interface ShoppingListItem {
  id: number; name: string; count: number | null; unitPrice: number | null; totalPrice: number | null
  checked: boolean; categoryId: number | null; categoryName: string | null; categoryColor: string | null
  addedByEmail: string | null
}
export interface ShoppingListDetail {
  id: number; name: string; items: ShoppingListItem[]; totalPrice: number; isOwner: boolean
}
export interface ShoppingListsResponse { owned: ShoppingListSummary[]; shared: ShoppingListSummary[] }

// Products
export interface Product {
  id: number; name: string; price: number | null
  categoryId: number | null; categoryName: string | null; categoryColor: string | null
}
export interface Category { id: number; name: string; color: string }

// User
export interface UserProfile {
  email: string; defaultListId: number | null
  allLists: ShoppingListSummary[]; sharedLists: ShoppingListSummary[]
}

// Errors
export interface ApiError { error: string; message: string }
