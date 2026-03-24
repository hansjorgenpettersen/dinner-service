import { apiClient } from './client'
import type { AuthResponse, LoginRequest, RegisterRequest, ForgotPasswordRequest, ResetPasswordRequest } from './types'

export const login = (data: LoginRequest) =>
  apiClient.post<AuthResponse>('/auth/login', data).then(r => r.data)

export const register = (data: RegisterRequest) =>
  apiClient.post<AuthResponse>('/auth/register', data).then(r => r.data)

export const logout = () =>
  apiClient.post('/auth/logout').then(() => {})

export const forgotPassword = (data: ForgotPasswordRequest) =>
  apiClient.post('/auth/forgot-password', data).then(r => r.data)

export const resetPassword = (data: ResetPasswordRequest) =>
  apiClient.post('/auth/reset-password', data).then(r => r.data)
