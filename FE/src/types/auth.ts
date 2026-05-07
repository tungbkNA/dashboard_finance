// auth.ts — TypeScript types for authentication (Feature 006)

export interface LoginRequest {
  username: string
  password: string
}

export interface AuthUser {
  id: string
  username: string
  displayName: string
  roleId: string
  roleName: string
  permissions: string[]
}

export interface LoginResponse {
  token: string
  expiresAt: string
  user: AuthUser
}
