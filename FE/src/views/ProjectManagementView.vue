<template>
  <div class="view-container">
    <div class="flex items-center justify-between mb-4">
      <h1 class="text-xl font-semibold">Quản Lý Các Dự Án</h1>
      <DatePicker v-model="selectedMonth" view="month" dateFormat="mm/yy"
                  placeholder="Chọn tháng" showIcon :showButtonBar="false"
                  class="w-48" @date-select="onMonthChange" />
    </div>

    <!-- Loading -->
    <div v-if="loading" class="flex justify-center py-10">
      <ProgressSpinner style="width: 40px; height: 40px" />
    </div>

    <!-- Error -->
    <Message v-else-if="error" severity="error" :closable="false">{{ error }}</Message>

    <!-- Table -->
    <DataTable
      v-else
      :value="records"
      stripedRows
      showGridlines
      size="small"
      class="text-sm"
      :rowHover="true"
    >
      <template #empty>
        <div class="text-center py-8 text-gray-400">
          <i class="pi pi-calendar text-3xl mb-2 block" />
          <span>Không có dữ liệu dự án cho tháng này</span>
        </div>
      </template>

      <Column header="STT" headerClass="text-center" bodyClass="text-center" style="width: 56px">
        <template #body="{ index }">{{ index + 1 }}</template>
      </Column>
      <Column field="projectCode" header="Mã dự án" style="width: 120px" />
      <Column field="projectName" header="Tên dự án" />
      <Column header="Người đại diện" style="width: 160px">
        <template #body="{ data }">{{ data.customerName ?? '—' }}</template>
      </Column>
      <Column header="SLSX tồn từ SX" headerClass="text-right" bodyClass="text-right font-mono" style="width: 150px">
        <template #body="{ data }">{{ fmt(data.g1SlsxTonTuSxHd) }}</template>
      </Column>
      <Column header="SLSX tồn" headerClass="text-right" bodyClass="text-right font-mono" style="width: 120px">
        <template #body="{ data }">{{ fmt(data.g6SlsxTon) }}</template>
      </Column>
      <Column header="Thao tác" headerClass="text-center" bodyClass="text-center" style="width: 90px">
        <template #body="{ data }">
          <div class="flex gap-1 justify-center">
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
import { ref, onMounted } from 'vue'
import DatePicker from 'primevue/datepicker'
import Message from 'primevue/message'
import ProgressSpinner from 'primevue/progressspinner'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Button from 'primevue/button'
import { projectMonthlyRecordService } from '@/services/projectMonthlyRecordService'
import ProjectCard from '@/components/project-management/ProjectCard.vue'
import type { ProjectMonthRecordSummary, FieldMetadata } from '@/types/project-monthly-record'

// ---- State ----

const records = ref<ProjectMonthRecordSummary[]>([])
const fieldMetadata = ref<FieldMetadata | null>(null)
const loading = ref(false)
const error = ref<string | null>(null)
const activeRecord = ref<ProjectMonthRecordSummary | null>(null)
const activeMode = ref<'view' | 'edit'>('view')

const now = new Date()
const currentMonthKey = ref(`${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`)
const selectedMonth = ref<Date>(new Date(now.getFullYear(), now.getMonth(), 1))

// ---- Helpers ----

function fmt(v: number | null | undefined): string {
  if (v == null) return '—'
  return v.toLocaleString('vi-VN')
}

// ---- Modal control ----

function openModal(record: ProjectMonthRecordSummary, mode: 'view' | 'edit') {
  activeRecord.value = record
  activeMode.value = mode
}

function onSaved() {
  loadAll()
}

// ---- Data loading ----

async function loadAll() {
  loading.value = true
  error.value = null
  try {
    const [recordsRes, metaRes] = await Promise.all([
      projectMonthlyRecordService.getAll(currentMonthKey.value),
      fieldMetadata.value ? Promise.resolve({ data: fieldMetadata.value }) : projectMonthlyRecordService.getFieldMetadata()
    ])
    records.value = recordsRes.data ?? []
    if (!fieldMetadata.value) {
      fieldMetadata.value = metaRes.data ?? null
    }
  } catch {
    error.value = 'Không thể tải dữ liệu dự án'
  } finally {
    loading.value = false
  }
}

// ---- Month picker ----

function onMonthChange(date: Date) {
  currentMonthKey.value = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`
  loadAll()
}

// ---- Init ----

onMounted(loadAll)
</script>

