<template>
  <div class="view-container p-4">
    <h1 style="font-size: 1.25rem; font-weight: 600; margin-bottom: 0.75rem">Danh Mục File</h1>

    <div class="toolbar" style="margin-bottom: 0.75rem">
      <div class="toolbar-filters">
        <IconField>
          <InputIcon class="pi pi-search" />
          <InputText
            v-model="filters.keyword"
            placeholder="Tìm kiếm theo tên file..."
            class="filter-input"
            @input="onSearchInput"
          />
        </IconField>
        <Dropdown
          v-model="filters.groupId"
          :options="activeGroups"
          optionLabel="name"
          optionValue="id"
          placeholder="Lọc theo nhóm"
          showClear
          style="width: 200px"
          @change="onFilterChange"
        />
        <div class="checkbox-group">
          <Checkbox
            v-model="filters.includeInactive"
            :binary="true"
            inputId="includeInactive"
            @change="onFilterChange"
          />
          <label for="includeInactive" class="cursor-pointer text-sm">Hiển thị cả nhóm ngưng HĐ</label>
        </div>
        <Button
          v-if="hasActiveFilters"
          icon="pi pi-filter-slash"
          severity="secondary"
          text
          size="small"
          @click="clearFilters"
          v-tooltip.top="'Xóa bộ lọc'"
        />
      </div>
      <div class="toolbar-actions">
        <Button label="Thêm mới" icon="pi pi-plus" @click="openCreate" />
      </div>
    </div>

    <DataTable
      :value="records"
      :loading="loading"
      :lazy="true"
      :paginator="true"
      :rows="pageSize"
      :totalRecords="totalRecords"
      :first="first"
      :rowsPerPageOptions="[5, 10, 20, 50, 100]"
      paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink RowsPerPageDropdown"
      @page="onPageChange"
      dataKey="id"
      stripedRows
      emptyMessage="Chưa có dữ liệu"
      class="p-datatable-sm"
    >
      <Column field="fileName" header="Tên file" />
      <Column field="fileUrl" header="Link file">
        <template #body="{ data }">
          <a :href="data.fileUrl" target="_blank" rel="noopener noreferrer" class="text-primary">
            {{ data.fileUrl.length > 60 ? data.fileUrl.substring(0, 60) + '...' : data.fileUrl }}
          </a>
        </template>
      </Column>
      <Column field="groupName" header="Nhóm file" />
      <Column field="createdAt" header="Ngày tạo">
        <template #body="{ data }">{{ formatDate(data.createdAt) }}</template>
      </Column>
      <Column field="createdBy" header="Người tạo" />
      <Column header="Thao tác" style="width: 10rem">
        <template #body="{ data }">
          <div class="flex gap-2">
            <Button icon="pi pi-pencil" severity="secondary" size="small" text @click="openEdit(data)" />
            <Button icon="pi pi-trash" severity="danger" size="small" text @click="confirmDelete(data)" />
          </div>
        </template>
      </Column>
    </DataTable>

    <FileRecordDialog
      v-if="showDialog"
      :record="selectedRecord"
      @saved="onSaved"
      @close="showDialog = false"
    />

    <ConfirmDialog />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import IconField from 'primevue/iconfield'
import InputIcon from 'primevue/inputicon'
import Dropdown from 'primevue/dropdown'
import Checkbox from 'primevue/checkbox'
import ConfirmDialog from 'primevue/confirmdialog'
import { useConfirm } from 'primevue/useconfirm'
import { useToast } from 'primevue/usetoast'
import type { FileRecordResponse, FileGroupActiveItem } from '@/types/handbook'
import { fileRecordService } from '@/services/fileRecordService'
import { fileGroupService } from '@/services/fileGroupService'
import FileRecordDialog from '@/components/handbook/FileRecordDialog.vue'

const confirm = useConfirm()
const toast = useToast()

const records = ref<FileRecordResponse[]>([])
const activeGroups = ref<FileGroupActiveItem[]>([])
const loading = ref(false)
const showDialog = ref(false)
const selectedRecord = ref<FileRecordResponse | null>(null)
const totalRecords = ref(0)
const currentPage = ref(0)
const pageSize = ref(20)
const first = ref(0)

const filters = ref({
  keyword: '',
  groupId: null as string | null,
  includeInactive: false
})

let searchTimeout: ReturnType<typeof setTimeout> | null = null

const hasActiveFilters = computed(() =>
  !!filters.value.keyword || !!filters.value.groupId || filters.value.includeInactive
)

onMounted(async () => {
  activeGroups.value = await fileGroupService.getActive()
  loadRecords()
})

async function loadRecords() {
  loading.value = true
  try {
    const result = await fileRecordService.getAll({
      keyword: filters.value.keyword || undefined,
      groupId: filters.value.groupId || undefined,
      includeInactive: filters.value.includeInactive,
      page: currentPage.value,
      size: pageSize.value
    })
    records.value = result.content
    totalRecords.value = result.totalElements
  } catch (e) {
    console.error('loadRecords error:', e)
  } finally {
    loading.value = false
  }
}

function onSearchInput() {
  if (searchTimeout) clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => {
    currentPage.value = 0
    first.value = 0
    loadRecords()
  }, 300)
}

function onFilterChange() {
  currentPage.value = 0
  first.value = 0
  loadRecords()
}

function clearFilters() {
  filters.value = { keyword: '', groupId: null, includeInactive: false }
  currentPage.value = 0
  first.value = 0
  loadRecords()
}

function onPageChange(event: { page: number; rows: number; first: number }) {
  currentPage.value = event.page
  pageSize.value = event.rows
  first.value = event.first
  loadRecords()
}

function openCreate() {
  selectedRecord.value = null
  showDialog.value = true
}

function openEdit(record: FileRecordResponse) {
  selectedRecord.value = record
  showDialog.value = true
}

function onSaved() {
  showDialog.value = false
  loadRecords()
  toast.add({ severity: 'success', summary: 'Thành công', detail: 'Lưu bản ghi file thành công', life: 3000 })
}

function confirmDelete(record: FileRecordResponse) {
  confirm.require({
    message: `Xóa file "${record.fileName}"?`,
    header: 'Xác nhận xóa',
    icon: 'pi pi-exclamation-triangle',
    acceptClass: 'p-button-danger',
    accept: () => doDelete(record),
    reject: () => {}
  })
}

async function doDelete(record: FileRecordResponse) {
  try {
    await fileRecordService.delete(record.id)
    toast.add({ severity: 'success', summary: 'Đã xóa', detail: `File "${record.fileName}" đã bị xóa`, life: 3000 })
    loadRecords()
  } catch {
    // Error toast handled by api interceptor
  }
}

function formatDate(iso: string): string {
  return new Date(iso).toLocaleDateString('vi-VN')
}
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

.checkbox-group {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  white-space: nowrap;
  flex-shrink: 0;
}

.toolbar-actions {
  display: flex;
  gap: 0.5rem;
  align-items: center;
  flex-shrink: 0;
}

.filter-input {
  width: 240px;
}
</style>
