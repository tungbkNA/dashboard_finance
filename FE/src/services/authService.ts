import api from '@/services/api'
import type { LoginRequest, LoginResponse, AuthUser } from '@/types/auth'

export const authService = {
  async login(request: LoginRequest): Promise<LoginResponse> {
    const response = await api.post<{ data: LoginResponse }>('/api/auth/login', request)
    return response.data.data
  },

  async logout(): Promise<void> {
    await api.post('/api/auth/logout')
  },

  async getMe(): Promise<AuthUser> {
    const response = await api.get<{ data: AuthUser }>('/api/auth/me')
    return response.data.data
  }
}
