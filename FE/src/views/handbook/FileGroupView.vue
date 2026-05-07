<template>
  <div class="view-container p-4">
    <div class="flex align-items-center justify-content-between mb-4">
      <h1 class="text-2xl font-semibold m-0">Quản Lý Nhóm File</h1>
      <Button label="Thêm mới" icon="pi pi-plus" @click="openCreate" />
    </div>

    <DataTable
      :value="groups"
      :loading="loading"
      dataKey="id"
      paginator
      :rows="20"
      stripedRows
      emptyMessage="Chưa có dữ liệu"
      class="p-datatable-sm"
    >
      <Column field="name" header="Tên nhóm" />
      <Column field="description" header="Mô tả">
        <template #body="{ data }">{{ data.description ?? '—' }}</template>
      </Column>
      <Column field="active" header="Trạng thái">
        <template #body="{ data }">
          <Tag :severity="data.active ? 'success' : 'danger'" :value="data.active ? 'Active' : 'Inactive'" />
        </template>
      </Column>
      <Column field="fileCount" header="Số file" />
      <Column header="Thao tác" style="width: 10rem">
        <template #body="{ data }">
          <div class="flex gap-2">
            <Button icon="pi pi-pencil" severity="secondary" size="small" text @click="openEdit(data)" />
            <Button icon="pi pi-trash" severity="danger" size="small" text @click="confirmDelete(data)" />
          </div>
        </template>
      </Column>
    </DataTable>

    <FileGroupDialog
      v-if="showDialog"
      :group="selectedGroup"
      @saved="onSaved"
      @close="showDialog = false"
    />

    <ConfirmDialog />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Button from 'primevue/button'
import Tag from 'primevue/tag'
import ConfirmDialog from 'primevue/confirmdialog'
import { useConfirm } from 'primevue/useconfirm'
import { useToast } from 'primevue/usetoast'
import type { FileGroupResponse } from '@/types/handbook'
import { fileGroupService } from '@/services/fileGroupService'
import FileGroupDialog from '@/components/handbook/FileGroupDialog.vue'

const confirm = useConfirm()
const toast = useToast()

const groups = ref<FileGroupResponse[]>([])
const loading = ref(false)
const showDialog = ref(false)
const selectedGroup = ref<FileGroupResponse | null>(null)

onMounted(loadGroups)

async function loadGroups() {
  loading.value = true
  try {
    groups.value = await fileGroupService.getAll()
  } finally {
    loading.value = false
  }
}

function openCreate() {
  selectedGroup.value = null
  showDialog.value = true
}

function openEdit(group: FileGroupResponse) {
  selectedGroup.value = group
  showDialog.value = true
}

function onSaved() {
  showDialog.value = false
  loadGroups()
  toast.add({ severity: 'success', summary: 'Thành công', detail: 'Lưu nhóm file thành công', life: 3000 })
}

function confirmDelete(group: FileGroupResponse) {
  confirm.require({
    message: `Xóa nhóm file "${group.name}"?`,
    header: 'Xác nhận xóa',
    icon: 'pi pi-exclamation-triangle',
    acceptClass: 'p-button-danger',
    accept: () => doDelete(group),
    reject: () => {}
  })
}

async function doDelete(group: FileGroupResponse) {
  try {
    await fileGroupService.delete(group.id)
    toast.add({ severity: 'success', summary: 'Đã xóa', detail: `Nhóm file "${group.name}" đã bị xóa`, life: 3000 })
    loadGroups()
  } catch {
    // Error toast is handled by api interceptor
  }
}
</script>
