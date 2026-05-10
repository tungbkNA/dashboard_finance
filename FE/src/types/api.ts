export interface ApiResponse<T> {
  code: string
  message: string
  data: T | null
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
  first: boolean
  last: boolean
}

export interface HealthStatusDto {
  status: string
  service: string
  version: string
}
