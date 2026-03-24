import { apiClient } from './client'
import type { UserProfile } from './types'

export const getProfile = () =>
  apiClient.get<UserProfile>('/user').then(r => r.data)

export const setDefaultList = (listId: number | null) =>
  apiClient.post<UserProfile>('/user/set-default-list', { listId }).then(r => r.data)

export const leaveList = (listId: number) =>
  apiClient.post(`/user/leave-list/${listId}`)
