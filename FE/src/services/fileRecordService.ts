import api from '@/services/api'
import type { FileRecordResponse, FileRecordRequest, FileRecordPageResponse } from '@/types/handbook'

export const fileRecordService = {
  async getAll(params: {
    keyword?: string
    groupId?: string
    includeInactive?: boolean
    page?: number
    size?: number
  } = {}): Promise<FileRecordPageResponse> {
    const res = await api.get<{ data: FileRecordPageResponse }>('/api/handbook/file-records', { params })
    return res.data.data
  },

  async create(request: FileRecordRequest): Promise<FileRecordResponse> {
    const res = await api.post<{ data: FileRecordResponse }>('/api/handbook/file-records', request)
    return res.data.data
  },

  async update(id: string, request: FileRecordRequest): Promise<FileRecordResponse> {
    const res = await api.put<{ data: FileRecordResponse }>(`/api/handbook/file-records/${id}`, request)
    return res.data.data
  },

  async delete(id: string): Promise<void> {
    await api.delete(`/api/handbook/file-records/${id}`)
  }
}
