<template>
  <div class="view-container p-4">
    <div class="dashboard-title-row">
      <h1 style="font-size: 1.25rem; font-weight: 600; margin: 0">Dashboard</h1>
      <Button
        icon="pi pi-cog"
        text
        rounded
        size="small"
        v-tooltip.left="'Chọn thẻ hiển thị'"
        @click="showCardPicker = !showCardPicker"
      />
    </div>

    <!-- Card picker popover -->
    <div v-if="showCardPicker" class="card-picker">
      <div class="card-picker-title">Hiển thị thẻ tổng quan</div>
      <div v-for="card in allCards" :key="card.key" class="card-picker-item">
        <Checkbox
          v-model="visibleCardKeys"
          :inputId="card.key"
          :value="card.key"
          :disabled="visibleCardKeys.length <= 5 && visibleCardKeys.includes(card.key)"
        />
        <label :for="card.key" style="cursor: pointer; margin-left: 0.5rem">{{ card.label }}</label>
      </div>
    </div>

    <!-- Overview stat cards -->
    <div v-if="overviewLoading" style="display: flex; justify-content: center; padding: 1.5rem 0">
      <ProgressSpinner style="width: 32px; height: 32px" />
    </div>
    <div v-else class="stat-grid">
      <div
        v-for="card in visibleCards"
        :key="card.key"
        class="stat-card"
        :style="{ borderTop: `3px solid ${card.color}` }"
      >
        <div class="stat-icon" :style="{ background: card.color + '18', color: card.color }">
          <i :class="card.icon" />
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ card.value }}</div>
          <div class="stat-label">{{ card.label }}</div>
        </div>
      </div>
    </div>

    <!-- Charts row -->
    <div class="charts-row">
      <!-- Revenue Comparison Card -->
      <div class="dashboard-card">
        <div class="card-header">
          <h2 class="card-title">So sánh KH (G2) &amp; TH (G3) — {{ displayMonth }}</h2>
          <DatePicker
            v-model="selectedMonth"
            view="month"
            dateFormat="mm/yy"
            placeholder="Chọn tháng"
            showIcon
            :maxDate="maxDate"
            style="width: 150px"
          />
        </div>

        <div v-if="chartLoading" style="display: flex; justify-content: center; padding: 3rem 0">
          <ProgressSpinner style="width: 40px; height: 40px" />
        </div>

        <div v-else-if="chartData" class="chart-wrapper">
          <Chart type="bar" :data="chartData" :options="chartOptions" style="height: 360px" />
        </div>

        <div v-else style="text-align: center; padding: 3rem 0; color: #9ca3af">
          Không có dữ liệu cho tháng này
        </div>
      </div>

      <!-- Monthly Revenue Card -->
      <div class="dashboard-card">
        <div class="card-header">
          <h2 class="card-title">Doanh thu tháng {{ displayMonth2 }}</h2>
          <DatePicker
            v-model="selectedMonth2"
            view="month"
            dateFormat="mm/yy"
            placeholder="Chọn tháng"
            showIcon
            :maxDate="maxDate"
            style="width: 150px"
          />
        </div>

        <div v-if="revenueLoading" style="display: flex; justify-content: center; padding: 3rem 0">
          <ProgressSpinner style="width: 40px; height: 40px" />
        </div>

        <div v-else-if="revenueChartData" class="chart-wrapper">
          <Chart type="bar" :data="revenueChartData" :options="revenueChartOptions" style="height: 360px" />
        </div>

        <div v-else style="text-align: center; padding: 3rem 0; color: #9ca3af">
          Không có dữ liệu cho tháng này
        </div>
      </div>
    </div>

    <!-- G5 Summary charts row -->
    <div class="charts-row">
      <!-- Doanh thu tháng chart -->
      <div class="dashboard-card">
        <div class="card-header">
          <h2 class="card-title">Doanh thu tháng (G5) — 12 tháng gần nhất</h2>
        </div>
        <div v-if="g5Loading" style="display: flex; justify-content: center; padding: 3rem 0">
          <ProgressSpinner style="width: 40px; height: 40px" />
        </div>
        <div v-else class="chart-wrapper">
          <Chart type="bar" :data="g5DoanhThuChartData" :options="g5ChartOptions" style="height: 360px" />
        </div>
      </div>

      <!-- Tổng SLNT chart -->
      <div class="dashboard-card">
        <div class="card-header">
          <h2 class="card-title">Tổng SLNT (G5) — 12 tháng gần nhất</h2>
        </div>
        <div v-if="g5Loading" style="display: flex; justify-content: center; padding: 3rem 0">
          <ProgressSpinner style="width: 40px; height: 40px" />
        </div>
        <div v-else class="chart-wrapper">
          <Chart type="bar" :data="g5TongSlntChartData" :options="g5ChartOptions" style="height: 360px" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import Chart from 'primevue/chart'
import DatePicker from 'primevue/datepicker'
import ProgressSpinner from 'primevue/progressspinner'
import Button from 'primevue/button'
import Checkbox from 'primevue/checkbox'
import { dashboardService, type RevenueComparison, type OverviewStats, type MonthlyRevenue, type MonthlyG5Entry } from '@/services/dashboardService'

// ---- Status label mapping ----
const STATUS_LABELS: Record<string, string> = {
  OPEN: 'Open',
  INPROGRESS: 'In Progress',
  PENDING: 'Pending',
  DONE: 'Done',
  CLOSE: 'Close'
}

const STATUS_COLORS: Record<string, string> = {
  OPEN: '#3b82f6',
  INPROGRESS: '#f59e0b',
  PENDING: '#8b5cf6',
  DONE: '#10b981',
  CLOSE: '#6b7280'
}

const STATUS_ICONS: Record<string, string> = {
  OPEN: 'pi pi-folder-open',
  INPROGRESS: 'pi pi-spin pi-spinner',
  PENDING: 'pi pi-clock',
  DONE: 'pi pi-check-circle',
  CLOSE: 'pi pi-lock'
}

// ---- Overview stats ----
const overviewLoading = ref(false)
const overview = ref<OverviewStats | null>(null)
const showCardPicker = ref(false)

interface StatCard {
  key: string
  label: string
  value: number
  icon: string
  color: string
}

const allCards = computed<StatCard[]>(() => {
  const o = overview.value
  if (!o) return []
  const cards: StatCard[] = [
    { key: 'totalProjects', label: 'Tổng dự án', value: o.totalProjects, icon: 'pi pi-briefcase', color: '#0ea5e9' },
  ]
  // Status cards
  for (const [status, count] of Object.entries(o.projectsByStatus)) {
    cards.push({
      key: `status_${status}`,
      label: `DA ${STATUS_LABELS[status] || status}`,
      value: count,
      icon: STATUS_ICONS[status] || 'pi pi-circle',
      color: STATUS_COLORS[status] || '#6b7280'
    })
  }
  cards.push(
    { key: 'hasContract', label: 'Có hợp đồng', value: o.hasContract, icon: 'pi pi-file', color: '#10b981' },
    { key: 'noContract', label: 'Chưa có HĐ', value: o.noContract, icon: 'pi pi-file-excel', color: '#ef4444' },
    { key: 'activeUsers', label: 'TK hoạt động', value: o.activeUsers, icon: 'pi pi-users', color: '#8b5cf6' },
    { key: 'inactiveUsers', label: 'TK không HĐ', value: o.inactiveUsers, icon: 'pi pi-user-minus', color: '#f97316' },
  )
  return cards
})

// Persist selection in localStorage
const STORAGE_KEY = 'dashboard_visible_cards'
const defaultKeys = ['totalProjects', 'status_OPEN', 'status_INPROGRESS', 'hasContract', 'activeUsers']

function loadVisibleKeys(): string[] {
  try {
    const saved = localStorage.getItem(STORAGE_KEY)
    if (saved) {
      const parsed = JSON.parse(saved) as string[]
      if (Array.isArray(parsed) && parsed.length >= 5) return parsed
    }
  } catch { /* ignore */ }
  return [...defaultKeys]
}

const visibleCardKeys = ref<string[]>(loadVisibleKeys())

watch(visibleCardKeys, (keys) => {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(keys))
}, { deep: true })

const visibleCards = computed(() => allCards.value.filter(c => visibleCardKeys.value.includes(c.key)))

async function loadOverview() {
  overviewLoading.value = true
  try {
    overview.value = await dashboardService.getOverviewStats()
  } catch {
    overview.value = null
  } finally {
    overviewLoading.value = false
  }
}

// ---- Revenue chart ----
const now = new Date()
const prevMonth = new Date(now.getFullYear(), now.getMonth() - 1, 1)
const selectedMonth = ref<Date>(prevMonth)
const maxDate = new Date(now.getFullYear(), now.getMonth() - 1, 28)

const chartLoading = ref(false)
const chartRaw = ref<RevenueComparison | null>(null)

const displayMonth = computed(() => {
  const d = selectedMonth.value
  return `${String(d.getMonth() + 1).padStart(2, '0')}/${d.getFullYear()}`
})

function getMonthKey(d: Date): string {
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
}

const chartData = computed(() => {
  if (!chartRaw.value) return null
  const d = chartRaw.value
  const hasData = d.g2Ra || d.g3Ra || d.g2TongSlsxDuKien || d.g3TongSlsxHd ||
    d.g2SlsxTuSxHtTrongThang || d.g3SlsxTuSxHt || d.g2SlsxTuSxDd || d.g3SlsxTuSxDd
  if (!hasData) return null

  return {
    labels: ['RA', 'Tổng SLSX DK / HĐ', 'SX HT', 'SX DD'],
    datasets: [
      {
        label: 'G2 - Kế hoạch tháng',
        backgroundColor: '#3b82f6',
        borderRadius: 4,
        data: [d.g2Ra, d.g2TongSlsxDuKien, d.g2SlsxTuSxHtTrongThang, d.g2SlsxTuSxDd]
      },
      {
        label: 'G3 - Thực hiện SLSX',
        backgroundColor: '#f59e0b',
        borderRadius: 4,
        data: [d.g3Ra, d.g3TongSlsxHd, d.g3SlsxTuSxHt, d.g3SlsxTuSxDd]
      }
    ]
  }
})

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      position: 'top' as const,
      labels: { usePointStyle: true, padding: 20 }
    },
    tooltip: {
      callbacks: {
        label: (ctx: { dataset: { label: string }; parsed: { y: number } }) => {
          const val = ctx.parsed.y?.toLocaleString('vi-VN') ?? '0'
          return `${ctx.dataset.label}: ${val}`
        }
      }
    }
  },
  scales: {
    x: {
      grid: { display: false }
    },
    y: {
      beginAtZero: true,
      grid: { display: false },
      ticks: {
        callback: (value: number | string) => Number(value).toLocaleString('vi-VN')
      }
    }
  }
}

async function loadChart() {
  chartLoading.value = true
  try {
    chartRaw.value = await dashboardService.getRevenueComparison(getMonthKey(selectedMonth.value))
  } catch {
    chartRaw.value = null
  } finally {
    chartLoading.value = false
  }
}

watch(selectedMonth, () => loadChart())

// ---- Monthly revenue chart ----
const selectedMonth2 = ref<Date>(new Date(now.getFullYear(), now.getMonth() - 1, 1))
const revenueLoading = ref(false)
const revenueRaw = ref<MonthlyRevenue | null>(null)

const displayMonth2 = computed(() => {
  const d = selectedMonth2.value
  return `${String(d.getMonth() + 1).padStart(2, '0')}/${d.getFullYear()}`
})

const revenueChartData = computed(() => {
  if (!revenueRaw.value) return null
  const d = revenueRaw.value
  const hasData = d.g2TongSlsxDuKien || d.g5TongSlnt || d.g5RaTuongUngSlnt
  if (!hasData) return null

  return {
    labels: ['Tổng SLSX DK (G2)', 'Tổng SLNT (G5)', 'Ra tương ứng SLNT (G5)'],
    datasets: [
      {
        label: `Tháng ${displayMonth2.value}`,
        backgroundColor: ['#3b82f6', '#10b981', '#f59e0b'],
        borderRadius: 4,
        data: [d.g2TongSlsxDuKien, d.g5TongSlnt, d.g5RaTuongUngSlnt]
      }
    ]
  }
})

const revenueChartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      display: false
    },
    tooltip: {
      callbacks: {
        label: (ctx: { dataset: { label: string }; parsed: { y: number } }) => {
          const val = ctx.parsed.y?.toLocaleString('vi-VN') ?? '0'
          return `${ctx.dataset.label}: ${val}`
        }
      }
    }
  },
  scales: {
    x: {
      grid: { display: false }
    },
    y: {
      beginAtZero: true,
      grid: { display: false },
      ticks: {
        callback: (value: number | string) => Number(value).toLocaleString('vi-VN')
      }
    }
  }
}

async function loadRevenue() {
  revenueLoading.value = true
  try {
    revenueRaw.value = await dashboardService.getMonthlyRevenue(getMonthKey(selectedMonth2.value))
  } catch {
    revenueRaw.value = null
  } finally {
    revenueLoading.value = false
  }
}

watch(selectedMonth2, () => loadRevenue())

// ---- G5 monthly summary (tables) ----
const g5Loading = ref(false)
const g5Entries = ref<MonthlyG5Entry[]>([])

function formatMonthLabel(monthKey: string): string {
  const [y, m] = monthKey.split('-')
  return `${m}/${y}`
}

function formatNumber(val: number): string {
  return val?.toLocaleString('vi-VN') ?? '0'
}

const g5DoanhThuChartData = computed(() => ({
  labels: g5Entries.value.map(e => formatMonthLabel(e.monthKey)),
  datasets: [{
    label: 'Doanh thu (G5)',
    backgroundColor: '#3b82f6',
    borderRadius: 4,
    data: g5Entries.value.map(e => e.g5DoanhThu ?? 0)
  }]
}))

const g5TongSlntChartData = computed(() => ({
  labels: g5Entries.value.map(e => formatMonthLabel(e.monthKey)),
  datasets: [{
    label: 'Tổng SLNT (G5)',
    backgroundColor: '#10b981',
    borderRadius: 4,
    data: g5Entries.value.map(e => e.g5TongSlnt ?? 0)
  }]
}))

const g5ChartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { display: false },
    tooltip: {
      callbacks: {
        label: (ctx: { dataset: { label: string }; parsed: { y: number } }) => {
          const val = ctx.parsed.y?.toLocaleString('vi-VN') ?? '0'
          return `${ctx.dataset.label}: ${val}`
        }
      }
    }
  },
  scales: {
    x: {
      grid: { display: false }
    },
    y: {
      beginAtZero: true,
      grid: { display: false },
      ticks: {
        callback: (value: number | string) => Number(value).toLocaleString('vi-VN')
      }
    }
  }
}

async function loadG5Summary() {
  g5Loading.value = true
  try {
    const data = await dashboardService.getMonthlyG5Summary()
    g5Entries.value = data.entries
  } catch {
    g5Entries.value = []
  } finally {
    g5Loading.value = false
  }
}

onMounted(() => {
  loadOverview()
  loadChart()
  loadRevenue()
  loadG5Summary()
})
</script>

<style scoped>
.dashboard-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1rem;
}

/* ---- Card picker ---- */
.card-picker {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 0.75rem 1rem;
  margin-bottom: 1rem;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}
.card-picker-title {
  font-weight: 600;
  font-size: 0.85rem;
  margin-bottom: 0.5rem;
  color: #475569;
}
.card-picker-item {
  display: inline-flex;
  align-items: center;
  margin-right: 1.25rem;
  margin-bottom: 0.35rem;
  font-size: 0.85rem;
}

/* ---- Stat grid ---- */
.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1.25rem;
}
.stat-card {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  padding: 1.5rem 1.75rem;
  display: flex;
  align-items: center;
  gap: 1.25rem;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05);
  transition: box-shadow 0.15s, transform 0.15s;
  min-height: 90px;
}
.stat-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  transform: translateY(-1px);
}
.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  flex-shrink: 0;
}
.stat-content {
  min-width: 0;
}
.stat-value {
  font-size: 2rem;
  font-weight: 700;
  line-height: 1.2;
  color: #1e293b;
}
.stat-label {
  font-size: 0.9rem;
  color: #64748b;
  margin-top: 3px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
@media (max-width: 1200px) {
  .stat-grid { grid-template-columns: repeat(3, 1fr); }
}
@media (max-width: 768px) {
  .stat-grid { grid-template-columns: repeat(2, 1fr); }
  .charts-row { grid-template-columns: 1fr; }
}

/* ---- Chart card ---- */
.charts-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.25rem;
  margin-top: 1.25rem;
}
.dashboard-card {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 1.25rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
}
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1rem;
  gap: 1rem;
  flex-wrap: wrap;
}
.card-title {
  font-size: 0.95rem;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}
.chart-wrapper {
  position: relative;
  width: 100%;
}
</style>
