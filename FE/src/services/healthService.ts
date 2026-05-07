import api from './api'
import type { ApiResponse, HealthStatusDto } from '@/types/api'

export async function checkHealth(): Promise<ApiResponse<HealthStatusDto>> {
  const response = await api.get<ApiResponse<HealthStatusDto>>('/api/binance/health')
  return response.data
}
