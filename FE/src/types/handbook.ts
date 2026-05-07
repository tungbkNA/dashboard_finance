// handbook.ts — TypeScript types for Central Handbook module (Feature 007)

export interface FileGroupResponse {
  id: string
  name: string
  description: string | null
  active: boolean
  fileCount: number
  createdAt: string
  updatedAt: string
}

export interface FileGroupActiveItem {
  id: string
  name: string
}

export interface FileGroupRequest {
  name: string
  description?: string
}

export interface FileGroupUpdateRequest {
  name: string
  description?: string
  active: boolean
}

export interface FileRecordResponse {
  id: string
  fileName: string
  fileUrl: string
  groupId: string
  groupName: string
  createdBy: string
  createdAt: string
  updatedAt: string
}

export interface FileRecordRequest {
  fileName: string
  fileUrl: string
  groupId: string
}

export interface FileRecordPageResponse {
  content: FileRecordResponse[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}
