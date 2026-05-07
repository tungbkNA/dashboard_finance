import api from './api'
import type { ApiResponse } from '@/types/api'
import type { ProjectTypeRequest, ProjectTypeResponse } from '@/types/project-settings'

const BASE = '/api/binance/project-types'

export const projectTypeService = {
  getAll(): Promise<ApiResponse<ProjectTypeResponse[]>> {
    return api.get(BASE).then((r) => r.data)
  },

  create(request: ProjectTypeRequest): Promise<ApiResponse<ProjectTypeResponse>> {
    return api.post(BASE, request).then((r) => r.data)
  },

  update(id: string, request: ProjectTypeRequest): Promise<ApiResponse<ProjectTypeResponse>> {
    return api.put(`${BASE}/${id}`, request).then((r) => r.data)
  },

  softDelete(id: string, confirmed = false): Promise<ApiResponse<{ inUse: boolean; usageCount: number } | null>> {
    return api.delete(`${BASE}/${id}`, { params: { confirmed } }).then((r) => r.data)
  }
}
