export interface ApiResponse<T> {
  code: string
  message: string
  data: T | null
}

export interface HealthStatusDto {
  status: string
  service: string
  version: string
}
