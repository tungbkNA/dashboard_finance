import api from './api'
import type { ApiResponse } from '@/types/api'
import type { ProjectRequest, ProjectResponse } from '@/types/project-settings'

const BASE = '/api/binance/projects'

export const projectService = {
  getAll(): Promise<ApiResponse<ProjectResponse[]>> {
    return api.get(BASE).then((r) => r.data)
  },

  getById(id: string): Promise<ApiResponse<ProjectResponse>> {
    return api.get(`${BASE}/${id}`).then((r) => r.data)
  },

  create(request: ProjectRequest): Promise<ApiResponse<ProjectResponse>> {
    return api.post(BASE, request).then((r) => r.data)
  },

  update(id: string, request: ProjectRequest, confirmShrink = false): Promise<ApiResponse<ProjectResponse>> {
    return api.put(`${BASE}/${id}?confirmShrink=${confirmShrink}`, request).then((r) => r.data)
  },

  softDelete(id: string): Promise<ApiResponse<void>> {
    return api.delete(`${BASE}/${id}`).then((r) => r.data)
  }
}
