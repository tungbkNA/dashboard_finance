import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { AuthUser, LoginRequest } from '@/types/auth'
import { authService } from '@/services/authService'

const TOKEN_KEY = 'auth_token'
const USER_KEY = 'auth_user'

function decodeJwtPayload(token: string): Record<string, unknown> | null {
  try {
    const parts = token.split('.')
    if (parts.length !== 3) return null
    const payload = parts[1].replace(/-/g, '+').replace(/_/g, '/')
    const padded = payload + '='.repeat((4 - (payload.length % 4)) % 4)
    return JSON.parse(atob(padded))
  } catch {
    return null
  }
}

function isTokenExpired(token: string): boolean {
  const payload = decodeJwtPayload(token)
  if (!payload || typeof payload.exp !== 'number') return true
  return Date.now() >= payload.exp * 1000
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem(TOKEN_KEY))
  const user = ref<AuthUser | null>((() => {
    const stored = localStorage.getItem(USER_KEY)
    return stored ? (JSON.parse(stored) as AuthUser) : null
  })())

  const isAuthenticated = computed(() => {
    return token.value !== null && !isTokenExpired(token.value)
  })

  function initFromStorage() {
    const storedToken = localStorage.getItem(TOKEN_KEY)
    const storedUser = localStorage.getItem(USER_KEY)
    if (storedToken && !isTokenExpired(storedToken)) {
      token.value = storedToken
      user.value = storedUser ? (JSON.parse(storedUser) as AuthUser) : null
    } else {
      logout()
    }
  }

  async function login(credentials: LoginRequest): Promise<void> {
    const response = await authService.login(credentials)
    token.value = response.token
    user.value = response.user
    localStorage.setItem(TOKEN_KEY, response.token)
    localStorage.setItem(USER_KEY, JSON.stringify(response.user))
  }

  function logout(): void {
    token.value = null
    user.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }

  function hasPermission(code: string): boolean {
    return user.value?.permissions.includes(code) ?? false
  }

  return { token, user, isAuthenticated, initFromStorage, login, logout, hasPermission }
})
