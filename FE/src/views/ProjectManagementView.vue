<template>
  <div class="view-container">
    <h1 style="font-size: 1.25rem; font-weight: 600; margin-bottom: 0.75rem">Quản Lý Các Dự Án</h1>

    <!-- Toolbar: filters left, actions right -->
    <div class="toolbar" style="margin-bottom: 0.75rem">
      <div class="toolbar-filters">
        <InputText v-model="filterKeyword" placeholder="Mã / Tên dự án..." class="filter-input"
                   @keydown.enter="doSearch" />
        <DatePicker v-model="selectedMonth" view="month" dateFormat="mm/yy"
                    placeholder="Tất cả tháng" showIcon :showButtonBar="true" showClear
                    style="min-width: 160px" />
        <Button icon="pi pi-search" severity="primary" outlined size="small" @click="doSearch" v-tooltip.top="'Tìm kiếm'" />
      </div>
      <div class="toolbar-actions">
        <Button label="Thu gọn tất cả" icon="pi pi-minus" severity="secondary" outlined size="small"
                @click="collapseAll" :disabled="expandedGroups.size === 0" />
        <Button label="Mở rộng tất cả" icon="pi pi-plus" severity="secondary" outlined size="small"
                @click="expandAll" :disabled="expandedGroups.size === groups.length" />
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" style="display: flex; justify-content: center; padding: 2.5rem 0">
      <ProgressSpinner style="width: 40px; height: 40px" />
    </div>

    <!-- Error -->
    <Message v-else-if="error" severity="error" :closable="false">{{ error }}</Message>

    <!-- Table -->
    <DataTable
      v-else
      :value="records"
      :lazy="true"
      :paginator="true"
      :rows="pageSize"
      :totalRecords="totalRecords"
      :first="first"
      @page="onPage"
      :rowsPerPageOptions="[5, 10, 20, 50, 100]"
      paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink RowsPerPageDropdown"
      stripedRows
      showGridlines
      size="small"
      class="text-sm project-mgmt-table"
      :rowHover="true"
      scrollable
      scrollHeight="calc(100vh - 280px)"
    >
      <template #empty>
        <div style="text-align: center; padding: 2rem 0; color: #9ca3af">
          <i class="pi pi-calendar" style="font-size: 1.5rem; display: block; margin-bottom: 0.5rem" />
          <span>Không có dữ liệu dự án</span>
        </div>
      </template>

      <ColumnGroup type="header">
        <!-- Row 1: group headers -->
        <Row>
          <Column header="STT" :rowspan="2" frozen
                  headerStyle="width:50px;min-width:50px;text-align:center;vertical-align:middle" />
          <Column header="Mã DA" :rowspan="2" frozen
                  headerStyle="width:100px;min-width:100px;vertical-align:middle" />
          <Column header="Tên dự án" :rowspan="2" frozen
                  headerStyle="width:180px;min-width:180px;vertical-align:middle" />
          <Column header="Khách hàng" :rowspan="2" frozen
                  headerStyle="width:130px;min-width:130px;vertical-align:middle" />
          <Column header="GĐ/PGĐ/PM" :rowspan="2" frozen
                  headerStyle="width:130px;min-width:130px;vertical-align:middle" />

          <template v-for="g in groups" :key="'h1-'+g.id">
            <Column
              :colspan="expandedGroups.has(g.id) ? GROUP_FIELDS[g.id].length : 1"
              :headerStyle="groupHeaderStyle(g.id)"
            >
              <template #header>
                <div class="group-header" @click.stop="toggleGroup(g.id)">
                  <i :class="expandedGroups.has(g.id) ? 'pi pi-chevron-down' : 'pi pi-chevron-right'" />
                  <span>{{ g.name }}</span>
                </div>
              </template>
            </Column>
          </template>

          <Column header="Thao tác" :rowspan="2"
                  headerStyle="width:90px;min-width:90px;text-align:center;vertical-align:middle" />
        </Row>

        <!-- Row 2: field sub-headers -->
        <Row>
          <template v-for="g in groups" :key="'h2-'+g.id">
            <template v-if="expandedGroups.has(g.id)">
              <Column
                v-for="field in GROUP_FIELDS[g.id]" :key="field"
                :header="FIELD_LABELS[field] || field"
                headerStyle="min-width:110px;text-align:right;font-size:0.7rem;white-space:nowrap"
              />
            </template>
            <Column v-else header=""
                    headerStyle="width:40px;min-width:40px" />
          </template>
        </Row>
      </ColumnGroup>

      <!-- ====== Body columns (must match header layout) ====== -->
      <Column frozen bodyClass="text-center" bodyStyle="width:50px">
        <template #body="{ index }">{{ first + index + 1 }}</template>
      </Column>
      <Column field="projectCode" frozen bodyStyle="width:100px" />
      <Column field="projectName" frozen bodyStyle="width:180px" />
      <Column frozen bodyStyle="width:130px">
        <template #body="{ data }">{{ data.customerName ?? '—' }}</template>
      </Column>
      <Column frozen bodyStyle="width:130px">
        <template #body="{ data }">{{ data.representUserName ?? '—' }}</template>
      </Column>

      <template v-for="g in groups" :key="'b-'+g.id">
        <template v-if="expandedGroups.has(g.id)">
          <Column
            v-for="field in GROUP_FIELDS[g.id]" :key="field"
            bodyClass="text-right font-mono"
            bodyStyle="min-width:110px"
          >
            <template #body="{ data }">{{ fmt(data[field as keyof typeof data]) }}</template>
          </Column>
        </template>
        <Column v-else bodyStyle="width:40px;text-align:center">
          <template #body>
            <i class="pi pi-ellipsis-h text-gray-300 text-xs" />
          </template>
        </Column>
      </template>

      <Column bodyClass="text-center" bodyStyle="width:90px">
        <template #body="{ data }">
          <div style="display: flex; gap: 0.25rem; justify-content: center">
            <Button icon="pi pi-eye" text rounded size="small" severity="info"
                    v-tooltip.top="'Xem chi tiết'" @click="openModal(data, 'view')" />
            <Button icon="pi pi-pencil" text rounded size="small" severity="warning"
                    v-tooltip.top="'Chỉnh sửa'" @click="openModal(data, 'edit')" />
          </div>
        </template>
      </Column>
    </DataTable>

    <!-- View / Edit modal -->
    <ProjectCard
      v-if="activeRecord"
      :record="activeRecord"
      :field-metadata="fieldMetadata!"
      :mode="activeMode"
      @close="activeRecord = null"
      @saved="onSaved"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import DatePicker from 'primevue/datepicker'
import InputText from 'primevue/inputtext'
import Message from 'primevue/message'
import ProgressSpinner from 'primevue/progressspinner'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import ColumnGroup from 'primevue/columngroup'
import Row from 'primevue/row'
import Button from 'primevue/button'
import { projectMonthlyRecordService } from '@/services/projectMonthlyRecordService'
import ProjectCard from '@/components/project-management/ProjectCard.vue'
import type { ProjectMonthRecordDetail, FieldMetadata } from '@/types/project-monthly-record'

// ---- Constants ----

const groups = [
  { id: 'g1', name: 'G1 - Tồn đầu kỳ' },
  { id: 'g2', name: 'G2 - Kế hoạch tháng' },
  { id: 'g3', name: 'G3 - TH SLSX đến ngày' },
  { id: 'g4', name: 'G4 - KH doanh thu' },
  { id: 'g5', name: 'G5 - TH nghiệm thu' },
  { id: 'g6', name: 'G6 - Tồn cuối kỳ' },
]

const GROUP_COLORS: Record<string, string> = {
  g1: '#eff6ff', g2: '#f0fdf4', g3: '#fefce8',
  g4: '#fff7ed', g5: '#fdf2f8', g6: '#f5f3ff',
}

const GROUP_FIELDS: Record<string, string[]> = {
  g1: ['g1RaTon', 'g1SlsxTonTuSxHd', 'g1SlsxTonTuSxHtHd', 'g1SlsxTonTuSxDdHd', 'g1SlsxOsTon', 'g1SlsxOsTonHt'],
  g2: ['g2Headcount', 'g2Ra', 'g2SlsxTuSx', 'g2SlsxOs', 'g2LienKet', 'g2TongSlsxDuKien',
       'g2SlsxTuSxHtTrongThang', 'g2SlsxTuSxDd', 'g2SlsxOsHt', 'g2SlsxOsDd', 'g2Cpbqtb', 'g2TySuatLng'],
  g3: ['g3Ra', 'g3TongSlsxHd', 'g3Ee', 'g3SlsxTuSxHt', 'g3SlsxTuSxDd', 'g3SlsxOsDd', 'g3SlsxOsTonHt'],
  g4: ['g4TuSlsxTonHt', 'g4TuSlsxTrongThang', 'g4SlsxOsTon', 'g4SlsxOsTrongThang', 'g4Lk',
       'g4Tong', 'g4DoanhThu', 'g4TiSuatLngDuKien', 'g4LngDuKien'],
  g5: ['g5RaTuongUngSlnt', 'g5NtSlsxTonHt', 'g5NtSlsxTrongThang', 'g5NtSlsxOsTon',
       'g5NtSlsxOsTrongThang', 'g5TongSlnt', 'g5DoanhThu', 'g5TiSuatLng', 'g5LngVnd'],
  g6: ['g6RaTon', 'g6SlsxTonHt', 'g6SlsxTonDd', 'g6SlsxOsTon', 'g6SlsxOsTonHt', 'g6SlsxTon'],
}

const FIELD_LABELS: Record<string, string> = {
  g1RaTon: 'Ra tồn', g1SlsxTonTuSxHd: 'SLSX tồn từ SX (HĐ)',
  g1SlsxTonTuSxHtHd: 'SLSX tồn SX HT', g1SlsxTonTuSxDdHd: 'SLSX tồn SX DD',
  g1SlsxOsTon: 'SLSX OS tồn', g1SlsxOsTonHt: 'SLSX OS tồn HT',
  g2Headcount: 'Headcount', g2Ra: 'Ra', g2SlsxTuSx: 'SLSX tự SX', g2SlsxOs: 'SLSX OS',
  g2LienKet: 'Liên kết', g2TongSlsxDuKien: 'Tổng SLSX DK',
  g2SlsxTuSxHtTrongThang: 'SX HT trong tháng', g2SlsxTuSxDd: 'SX DD',
  g2SlsxOsHt: 'OS HT', g2SlsxOsDd: 'OS DD', g2Cpbqtb: 'CPBQTB', g2TySuatLng: 'Tỷ suất LNG',
  g3Ra: 'Ra', g3TongSlsxHd: 'Tổng SLSX (HĐ)', g3Ee: 'EE', g3SlsxTuSxHt: 'SX HT',
  g3SlsxTuSxDd: 'SX DD', g3SlsxOsDd: 'OS DD', g3SlsxOsTonHt: 'OS tồn HT',
  g4TuSlsxTonHt: 'Từ SLSX tồn HT', g4TuSlsxTrongThang: 'Từ SX trong tháng',
  g4SlsxOsTon: 'OS tồn', g4SlsxOsTrongThang: 'OS trong tháng',
  g4Lk: 'Liên kết', g4Tong: 'Tổng', g4DoanhThu: 'Doanh thu',
  g4TiSuatLngDuKien: 'Tỷ suất LNG DK', g4LngDuKien: 'LNG DK',
  g5RaTuongUngSlnt: 'Ra tương ứng SLNT', g5NtSlsxTonHt: 'NT tồn HT',
  g5NtSlsxTrongThang: 'NT trong tháng', g5NtSlsxOsTon: 'NT OS tồn',
  g5NtSlsxOsTrongThang: 'NT OS trong tháng', g5TongSlnt: 'Tổng SLNT',
  g5DoanhThu: 'Doanh thu', g5TiSuatLng: 'Tỷ suất LNG', g5LngVnd: 'LNG (VND)',
  g6RaTon: 'Ra tồn', g6SlsxTonHt: 'SLSX tồn HT', g6SlsxTonDd: 'SLSX tồn DD',
  g6SlsxOsTon: 'SLSX OS tồn', g6SlsxOsTonHt: 'SLSX OS tồn HT', g6SlsxTon: 'SLSX tồn',
}

// ---- State ----

const records = ref<ProjectMonthRecordDetail[]>([])
const fieldMetadata = ref<FieldMetadata | null>(null)
const loading = ref(false)
const error = ref<string | null>(null)
const activeRecord = ref<ProjectMonthRecordDetail | null>(null)
const activeMode = ref<'view' | 'edit'>('view')
const expandedGroups = reactive(new Set<string>())

// ---- Pagination ----
const totalRecords = ref(0)
const currentPage = ref(0)
const pageSize = ref(20)
const first = ref(0)

// ---- Filters ----
const filterKeyword = ref('')
const selectedMonth = ref<Date | null>(null)

let searchTimeout: ReturnType<typeof setTimeout> | null = null

watch(filterKeyword, () => {
  if (searchTimeout) clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => doSearch(), 400)
})

watch(selectedMonth, () => {
  doSearch()
})

function getMonthKey(): string | undefined {
  if (!selectedMonth.value) return undefined
  const d = selectedMonth.value
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
}

function onPage(event: { page: number; rows: number; first: number }) {
  currentPage.value = event.page
  pageSize.value = event.rows
  first.value = event.first
  loadRecords()
}

function doSearch() {
  currentPage.value = 0
  first.value = 0
  loadRecords()
}



// ---- Helpers ----

function fmt(v: unknown): string {
  if (v == null) return '—'
  if (typeof v === 'number') return v.toLocaleString('vi-VN')
  return String(v)
}

function toggleGroup(groupId: string) {
  if (expandedGroups.has(groupId)) {
    expandedGroups.delete(groupId)
  } else {
    expandedGroups.add(groupId)
  }
}

function expandAll() {
  groups.forEach(g => expandedGroups.add(g.id))
}

function collapseAll() {
  expandedGroups.clear()
}

function groupHeaderStyle(groupId: string) {
  const bg = GROUP_COLORS[groupId] || '#f9fafb'
  return `background:${bg};text-align:center;cursor:pointer;user-select:none;white-space:nowrap`
}

// ---- Modal control ----

function openModal(record: ProjectMonthRecordDetail, mode: 'view' | 'edit') {
  activeRecord.value = record
  activeMode.value = mode
}

function onSaved() {
  loadRecords()
}

// ---- Data loading ----

async function loadRecords() {
  loading.value = true
  error.value = null
  try {
    const [recordsRes, metaRes] = await Promise.all([
      projectMonthlyRecordService.search({
        keyword: filterKeyword.value || undefined,
        monthKey: getMonthKey(),
        page: currentPage.value,
        size: pageSize.value
      }),
      fieldMetadata.value ? Promise.resolve({ data: fieldMetadata.value }) : projectMonthlyRecordService.getFieldMetadata()
    ])
    const page = recordsRes.data
    records.value = page?.content ?? []
    totalRecords.value = page?.totalElements ?? 0
    if (!fieldMetadata.value) {
      fieldMetadata.value = metaRes.data ?? null
    }
  } catch {
    error.value = 'Không thể tải dữ liệu dự án'
  } finally {
    loading.value = false
  }
}

// ---- Init ----

onMounted(loadRecords)
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 0.6rem 1rem;
}

.toolbar-filters {
  display: flex;
  gap: 0.5rem;
  align-items: center;
  flex: 1;
}

.toolbar-actions {
  display: flex;
  gap: 0.5rem;
  align-items: center;
  flex-shrink: 0;
}

.filter-input {
  min-width: 180px;
  max-width: 220px;
}

.group-header {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  font-weight: 600;
  font-size: 0.8rem;
  white-space: nowrap;
}
.group-header:hover {
  opacity: 0.7;
}
</style>

