import api from './api'
import type { ApiResponse } from '@/types/api'
import type { CustomerRequest, CustomerResponse } from '@/types/project-settings'

const BASE = '/api/binance/customers'

export const customerService = {
  getAll(): Promise<ApiResponse<CustomerResponse[]>> {
    return api.get(BASE).then((r) => r.data)
  },

  create(request: CustomerRequest): Promise<ApiResponse<CustomerResponse>> {
    return api.post(BASE, request).then((r) => r.data)
  },

  update(id: string, request: CustomerRequest): Promise<ApiResponse<CustomerResponse>> {
    return api.put(`${BASE}/${id}`, request).then((r) => r.data)
  },

  softDelete(id: string, confirmed = false): Promise<ApiResponse<{ inUse: boolean; usageCount: number } | null>> {
    return api.delete(`${BASE}/${id}`, { params: { confirmed } }).then((r) => r.data)
  }
}
