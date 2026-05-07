import api from './api'
import type { ApiResponse } from '@/types/api'
import type {
  ProjectMonthRecordSummary,
  ProjectMonthRecordDetail,
  ProjectMonthRecordUpdateRequest,
  FieldMetadata
} from '@/types/project-monthly-record'

const BASE = '/api/binance/project-monthly-records'

export const projectMonthlyRecordService = {
  getAll(monthKey?: string): Promise<ApiResponse<ProjectMonthRecordSummary[]>> {
    const params = monthKey ? { monthKey } : {}
    return api.get(BASE, { params }).then((r) => r.data)
  },

  getById(id: string): Promise<ApiResponse<ProjectMonthRecordDetail>> {
    return api.get(`${BASE}/${id}`).then((r) => r.data)
  },

  update(id: string, request: ProjectMonthRecordUpdateRequest): Promise<ApiResponse<ProjectMonthRecordDetail>> {
    return api.put(`${BASE}/${id}`, request).then((r) => r.data)
  },

  getFieldMetadata(): Promise<ApiResponse<FieldMetadata>> {
    return api.get(`${BASE}/field-metadata`).then((r) => r.data)
  }
}
