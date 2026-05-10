import api from '@/services/api'

export interface RevenueComparison {
  monthKey: string
  g2Ra: number
  g3Ra: number
  g2TongSlsxDuKien: number
  g3TongSlsxHd: number
  g2SlsxTuSxHtTrongThang: number
  g3SlsxTuSxHt: number
  g2SlsxTuSxDd: number
  g3SlsxTuSxDd: number
}

export interface OverviewStats {
  totalProjects: number
  projectsByStatus: Record<string, number>
  hasContract: number
  noContract: number
  activeUsers: number
  inactiveUsers: number
}

export interface MonthlyRevenue {
  monthKey: string
  g2TongSlsxDuKien: number
  g5TongSlnt: number
  g5RaTuongUngSlnt: number
}

export interface MonthlyG5Entry {
  monthKey: string
  projectCount: number
  g5DoanhThu: number
  g5TongSlnt: number
}

export interface MonthlyG5Summary {
  entries: MonthlyG5Entry[]
}

export const dashboardService = {
  async getOverviewStats(): Promise<OverviewStats> {
    const res = await api.get<{ data: OverviewStats }>('/api/dashboard/overview')
    return res.data.data!
  },

  async getRevenueComparison(monthKey?: string): Promise<RevenueComparison> {
    const res = await api.get<{ data: RevenueComparison }>('/api/dashboard/revenue-comparison', {
      params: monthKey ? { monthKey } : {}
    })
    return res.data.data!
  },

  async getMonthlyRevenue(monthKey?: string): Promise<MonthlyRevenue> {
    const res = await api.get<{ data: MonthlyRevenue }>('/api/dashboard/monthly-revenue', {
      params: monthKey ? { monthKey } : {}
    })
    return res.data.data!
  },

  async getMonthlyG5Summary(): Promise<MonthlyG5Summary> {
    const res = await api.get<{ data: MonthlyG5Summary }>('/api/dashboard/monthly-g5-summary')
    return res.data.data!
  }
}
