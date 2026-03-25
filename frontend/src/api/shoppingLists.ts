import { apiClient } from './client'
import type { ShoppingListsResponse, ShoppingListDetail, ShoppingListSummary, ShoppingListItem } from './types'

export const getShoppingLists = () =>
  apiClient.get<ShoppingListsResponse>('/shopping-lists').then(r => r.data)

export const getShoppingList = (id: number) =>
  apiClient.get<ShoppingListDetail>(`/shopping-lists/${id}`).then(r => r.data)

export const createShoppingList = (name: string) =>
  apiClient.post<ShoppingListSummary>('/shopping-lists', { name }).then(r => r.data)

export const addItem = (listId: number, data: { name: string; count?: number; unitPrice?: number }) =>
  apiClient.post<ShoppingListItem>(`/shopping-lists/${listId}/items`, data).then(r => r.data)

export const toggleItem = (listId: number, itemId: number) =>
  apiClient.post<ShoppingListItem>(`/shopping-lists/${listId}/items/${itemId}/toggle`).then(r => r.data)

export const deleteItem = (listId: number, itemId: number) =>
  apiClient.delete(`/shopping-lists/${listId}/items/${itemId}`)

export const clearChecked = (listId: number) =>
  apiClient.post(`/shopping-lists/${listId}/items/clear-checked`)

export const shareList = (listId: number, email: string) =>
  apiClient.post(`/shopping-lists/${listId}/share`, { email })

export const unshareList = (listId: number, email: string) =>
  apiClient.post(`/shopping-lists/${listId}/unshare`, { email })
