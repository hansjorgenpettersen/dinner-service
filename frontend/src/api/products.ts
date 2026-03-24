import { apiClient } from './client'
import type { Product, Category } from './types'

export const getProducts = () =>
  apiClient.get<Product[]>('/products').then(r => r.data)

export const searchProducts = (q: string) =>
  apiClient.get<Product[]>('/products/search', { params: { q } }).then(r => r.data)

export const getUncategorizedProducts = () =>
  apiClient.get<Product[]>('/products/uncategorized').then(r => r.data)

export const createProduct = (data: { name: string; price?: number; categoryId?: number }) =>
  apiClient.post<Product>('/products', data).then(r => r.data)

export const updateProduct = (id: number, data: { name: string; price?: number; categoryId?: number }) =>
  apiClient.put<Product>(`/products/${id}`, data).then(r => r.data)

export const deleteProduct = (id: number) =>
  apiClient.delete(`/products/${id}`)

export const getCategories = () =>
  apiClient.get<Category[]>('/categories').then(r => r.data)

export const createCategory = (data: { name: string; color?: string }) =>
  apiClient.post<Category>('/categories', data).then(r => r.data)

export const deleteCategory = (id: number) =>
  apiClient.delete(`/categories/${id}`)
