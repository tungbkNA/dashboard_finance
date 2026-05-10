import api from './api'
import type { ApiResponse, PageResponse } from '@/types/api'
import type {
  ProjectMonthRecordDetail,
  ProjectMonthRecordUpdateRequest,
  FieldMetadata
} from '@/types/project-monthly-record'

const BASE = '/api/binance/project-monthly-records'

export interface MonthRecordSearchParams {
  keyword?: string
  monthKey?: string
  page?: number
  size?: number
}

export const projectMonthlyRecordService = {
  getAll(monthKey?: string): Promise<ApiResponse<ProjectMonthRecordDetail[]>> {
    const params = monthKey ? { monthKey } : {}
    return api.get(BASE, { params }).then((r) => r.data)
  },

  search(params: MonthRecordSearchParams): Promise<ApiResponse<PageResponse<ProjectMonthRecordDetail>>> {
    const query: Record<string, string | number> = {}
    if (params.keyword) query.keyword = params.keyword
    if (params.monthKey) query.monthKey = params.monthKey
    if (params.page !== undefined) query.page = params.page
    if (params.size !== undefined) query.size = params.size
    return api.get(`${BASE}/search`, { params: query }).then((r) => r.data)
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
