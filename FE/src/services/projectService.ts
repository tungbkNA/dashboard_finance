import api from './api'
import type { ApiResponse, PageResponse } from '@/types/api'
import type { ProjectRequest, ProjectResponse, ProjectImportResult } from '@/types/project-settings'

const BASE = '/api/binance/projects'

export interface ProjectSearchParams {
  keyword?: string
  projectTypeId?: string | null
  customerId?: string | null
  statusContract?: string | null
  statusProject?: string | null
  page?: number
  size?: number
}

export const projectService = {
  getAll(): Promise<ApiResponse<ProjectResponse[]>> {
    return api.get(BASE).then((r) => r.data)
  },

  search(params: ProjectSearchParams): Promise<ApiResponse<PageResponse<ProjectResponse>>> {
    const query: Record<string, string | number> = {
      page: params.page ?? 0,
      size: params.size ?? 20
    }
    if (params.keyword) query.keyword = params.keyword
    if (params.projectTypeId) query.projectTypeId = params.projectTypeId
    if (params.customerId) query.customerId = params.customerId
    if (params.statusContract) query.statusContract = params.statusContract
    if (params.statusProject) query.statusProject = params.statusProject
    return api.get(`${BASE}/search`, { params: query }).then((r) => r.data)
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
  },

  downloadTemplate(): Promise<Blob> {
    return api.get(`${BASE}/import/template`, { responseType: 'blob' }).then((r) => r.data)
  },

  importExcel(file: File): Promise<ApiResponse<ProjectImportResult>> {
    const formData = new FormData()
    formData.append('file', file)
    return api.post(`${BASE}/import`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    }).then((r) => r.data)
  }
}
