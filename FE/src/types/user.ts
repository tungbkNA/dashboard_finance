// user.ts — TypeScript types for user management (Feature 006)

export interface AppUser {
  id: string
  username: string
  email: string
  displayName: string
  roleId: string
  roleName: string
  active: boolean
  phone?: string
  position?: string
  employeeCode?: string
  createdAt: string
}

export interface UserRequest {
  username: string
  email: string
  displayName: string
  password: string
  roleId: string
  phone?: string
  position?: string
  employeeCode?: string
}

export interface UpdateUserRequest {
  email?: string
  displayName?: string
  roleId?: string
  active?: boolean
  phone?: string | null
  position?: string | null
  employeeCode?: string | null
}

export interface ResetPasswordRequest {
  newPassword: string
}
