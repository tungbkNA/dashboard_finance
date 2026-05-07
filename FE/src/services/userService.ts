import api from '@/services/api'
import type { AppUser, UserRequest, UpdateUserRequest, ResetPasswordRequest } from '@/types/user'

export const userService = {
  async listUsers(active?: boolean): Promise<AppUser[]> {
    const params = active !== undefined ? { active } : {}
    const res = await api.get<{ data: AppUser[] }>('/api/users', { params })
    return res.data.data
  },

  async getUser(id: string): Promise<AppUser> {
    const res = await api.get<{ data: AppUser }>(`/api/users/${id}`)
    return res.data.data
  },

  async createUser(request: UserRequest): Promise<AppUser> {
    const res = await api.post<{ data: AppUser }>('/api/users', request)
    return res.data.data
  },

  async updateUser(id: string, request: UpdateUserRequest): Promise<AppUser> {
    const res = await api.put<{ data: AppUser }>(`/api/users/${id}`, request)
    return res.data.data
  },

  async resetPassword(id: string, request: ResetPasswordRequest): Promise<void> {
    await api.put(`/api/users/${id}/password`, request)
  },

  async deleteUser(id: string): Promise<void> {
    await api.delete(`/api/users/${id}`)
  }
}
