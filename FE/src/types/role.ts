// role.ts — TypeScript types for role management (Feature 006)

export interface Role {
  id: string
  roleName: string
  description: string | null
  active: boolean
  userCount: number
  createdAt: string
}

export interface RoleDetail extends Role {
  permissions: Permission[]
}

export interface Permission {
  code: string
  displayName: string
  parentCode: string | null
  type: 'SCREEN' | 'ACTION'
  sortOrder: number
}

export interface RoleRequest {
  roleName: string
  description?: string
}

export interface UpdateRolePermissionsRequest {
  permissions: string[]
}
