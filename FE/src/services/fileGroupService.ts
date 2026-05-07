import api from '@/services/api'
import type { FileGroupResponse, FileGroupActiveItem, FileGroupRequest, FileGroupUpdateRequest } from '@/types/handbook'

export const fileGroupService = {
  async getAll(): Promise<FileGroupResponse[]> {
    const res = await api.get<{ data: FileGroupResponse[] }>('/api/handbook/file-groups')
    return res.data.data
  },

  async getActive(): Promise<FileGroupActiveItem[]> {
    const res = await api.get<{ data: FileGroupActiveItem[] }>('/api/handbook/file-groups/active')
    return res.data.data
  },

  async create(request: FileGroupRequest): Promise<FileGroupResponse> {
    const res = await api.post<{ data: FileGroupResponse }>('/api/handbook/file-groups', request)
    return res.data.data
  },

  async update(id: string, request: FileGroupUpdateRequest): Promise<FileGroupResponse> {
    const res = await api.put<{ data: FileGroupResponse }>(`/api/handbook/file-groups/${id}`, request)
    return res.data.data
  },

  async delete(id: string): Promise<void> {
    await api.delete(`/api/handbook/file-groups/${id}`)
  }
}
