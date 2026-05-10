import api from '@/services/api'
import type { Role, RoleDetail, RoleRequest, UpdateRolePermissionsRequest, Permission } from '@/types/role'
import type { PageResponse } from '@/types/api'

export const roleService = {
  async listRoles(): Promise<Role[]> {
    const res = await api.get<{ data: Role[] }>('/api/roles')
    return res.data.data
  },

  async searchRoles(keyword: string, page: number, size: number): Promise<PageResponse<Role>> {
    const res = await api.get<{ data: PageResponse<Role> }>('/api/roles/search', {
      params: { keyword, page, size }
    })
    return res.data.data!
  },

  async getRole(id: string): Promise<RoleDetail> {
    const res = await api.get<{ data: RoleDetail }>(`/api/roles/${id}`)
    return res.data.data
  },

  async createRole(request: RoleRequest): Promise<Role> {
    const res = await api.post<{ data: Role }>('/api/roles', request)
    return res.data.data
  },

  async updateRole(id: string, request: RoleRequest): Promise<Role> {
    const res = await api.put<{ data: Role }>(`/api/roles/${id}`, request)
    return res.data.data
  },

  async deleteRole(id: string, force = false): Promise<void> {
    await api.delete(`/api/roles/${id}`, { params: { force } })
  },

  async getRolePermissions(id: string): Promise<RoleDetail> {
    const res = await api.get<{ data: RoleDetail }>(`/api/roles/${id}/permissions`)
    return res.data.data
  },

  async updateRolePermissions(id: string, request: UpdateRolePermissionsRequest): Promise<void> {
    await api.put(`/api/roles/${id}/permissions`, request)
  },

  async listPermissions(): Promise<Permission[]> {
    const res = await api.get<{ data: Permission[] }>('/api/permissions')
    return res.data.data
  }
}
