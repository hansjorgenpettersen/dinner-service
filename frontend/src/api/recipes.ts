import { apiClient } from './client'
import type { RecipeSummary, RecipeDetail, Ingredient } from './types'

export const getRecipes = () =>
  apiClient.get<RecipeSummary[]>('/recipes').then(r => r.data)

export const getRecipe = (id: number, selectedListId?: number) =>
  apiClient.get<RecipeDetail>(`/recipes/${id}`, {
    params: selectedListId ? { selectedListId } : {}
  }).then(r => r.data)

export const createRecipe = (data: { name: string; description?: string }) =>
  apiClient.post<RecipeSummary>('/recipes', data).then(r => r.data)

export const updateRecipe = (id: number, data: { name: string; description?: string }) =>
  apiClient.put<RecipeSummary>(`/recipes/${id}`, data).then(r => r.data)

export const deleteRecipe = (id: number) =>
  apiClient.delete(`/recipes/${id}`)

export const addIngredient = (recipeId: number, data: { productId: number; quantity?: number; unit: string }) =>
  apiClient.post<Ingredient>(`/recipes/${recipeId}/ingredients`, data).then(r => r.data)

export const deleteIngredient = (recipeId: number, ingId: number) =>
  apiClient.delete(`/recipes/${recipeId}/ingredients/${ingId}`)

export const addIngredientToList = (recipeId: number, ingId: number, listId: number) =>
  apiClient.post(`/recipes/${recipeId}/ingredients/${ingId}/add-to-list`, null, { params: { listId } })

export const removeIngredientFromList = (recipeId: number, ingId: number, listId: number) =>
  apiClient.post(`/recipes/${recipeId}/ingredients/${ingId}/remove-from-list`, null, { params: { listId } })

export const uploadImages = (recipeId: number, files: FileList) => {
  const form = new FormData()
  Array.from(files).forEach(f => form.append('files', f))
  return apiClient.post(`/recipes/${recipeId}/images`, form, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export const deleteImage = (recipeId: number, imageId: number) =>
  apiClient.delete(`/recipes/${recipeId}/images/${imageId}`)
