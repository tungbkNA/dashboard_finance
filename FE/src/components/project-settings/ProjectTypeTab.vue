<template>
  <div>
    <!-- Toolbar -->
    <div class="flex justify-end mb-3">
      <Button label="Thêm loại dự án" icon="pi pi-plus" @click="openCreate" />
    </div>

    <!-- Loading -->
    <div v-if="loading" class="flex justify-center py-8">
      <ProgressSpinner style="width: 40px; height: 40px" />
    </div>

    <!-- Error -->
    <Message v-else-if="error" severity="error" :closable="false">{{ error }}</Message>

    <!-- Empty -->
    <div v-else-if="!projectTypes.length" class="text-center py-8 text-gray-400">
      <i class="pi pi-tags text-4xl mb-2 block" />
      <span>Chưa có loại dự án nào</span>
    </div>

    <!-- Table -->
    <DataTable v-else :value="projectTypes" stripedRows tableStyle="min-width: 30rem">
      <Column field="key" header="Key" sortable style="width: 200px" />
      <Column field="value" header="Value" sortable />
      <Column header="Hành động" style="width: 110px">
        <template #body="{ data }">
          <Button icon="pi pi-pencil" text rounded class="mr-1" @click="openEdit(data)" />
          <Button icon="pi pi-trash" text rounded severity="danger" @click="handleDelete(data)" />
        </template>
      </Column>
    </DataTable>

    <!-- Create/Edit Dialog -->
    <Dialog v-model:visible="dialogVisible"
            :header="editingItem ? 'Sửa loại dự án' : 'Thêm loại dự án'"
            modal :style="{ width: '420px' }" :draggable="false">
      <form @submit.prevent="saveItem" class="flex flex-col gap-3 pt-2">
        <div class="field">
          <label class="block mb-1 font-medium">Key <span class="text-red-500">*</span></label>
          <InputText v-model="form.key" class="w-full" placeholder="VD: DEVELOPMENT"
                     :class="{ 'p-invalid': formErrors.key }" />
          <small class="p-error">{{ formErrors.key }}</small>
        </div>
        <div class="field">
          <label class="block mb-1 font-medium">Value <span class="text-red-500">*</span></label>
          <InputText v-model="form.value" class="w-full" placeholder="VD: Phát triển phần mềm"
                     :class="{ 'p-invalid': formErrors.value }" />
          <small class="p-error">{{ formErrors.value }}</small>
        </div>
        <div class="flex justify-end gap-2 pt-2">
          <Button label="Hủy" severity="secondary" text @click="dialogVisible = false" type="button" />
          <Button :label="editingItem ? 'Lưu' : 'Thêm'" type="submit" :loading="saving" />
        </div>
      </form>
    </Dialog>

    <!-- In-use warning Dialog -->
    <Dialog v-model:visible="inUseDialogVisible" header="Xác nhận xóa" modal
            :style="{ width: '400px' }" :draggable="false">
      <p class="mb-4">
        Loại dự án này đang được <strong>{{ inUsageCount }}</strong> dự án sử dụng.
        Bạn vẫn muốn xóa?
      </p>
      <div class="flex justify-end gap-2">
        <Button label="Hủy" severity="secondary" text @click="inUseDialogVisible = false" />
        <Button label="Xóa" severity="danger" :loading="saving" @click="confirmForceDelete" />
      </div>
    </Dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useToast } from 'primevue/usetoast'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Button from 'primevue/button'
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'
import Message from 'primevue/message'
import ProgressSpinner from 'primevue/progressspinner'
import { projectTypeService } from '@/services/projectTypeService'
import type { ProjectTypeResponse, ProjectTypeRequest } from '@/types/project-settings'

const toast = useToast()

const projectTypes = ref<ProjectTypeResponse[]>([])
const loading = ref(false)
const error = ref<string | null>(null)
const saving = ref(false)

const dialogVisible = ref(false)
const editingItem = ref<ProjectTypeResponse | null>(null)
const form = ref<ProjectTypeRequest>({ key: '', value: '' })
const formErrors = ref<Partial<Record<keyof ProjectTypeRequest, string>>>({})

const inUseDialogVisible = ref(false)
const inUsageCount = ref(0)
const pendingDeleteId = ref<string | null>(null)

async function loadData() {
  loading.value = true
  error.value = null
  try {
    const res = await projectTypeService.getAll()
    projectTypes.value = res.data ?? []
  } catch {
    error.value = 'Không thể tải danh sách loại dự án'
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingItem.value = null
  form.value = { key: '', value: '' }
  formErrors.value = {}
  dialogVisible.value = true
}

function openEdit(item: ProjectTypeResponse) {
  editingItem.value = item
  form.value = { key: item.key, value: item.value }
  formErrors.value = {}
  dialogVisible.value = true
}

function validateForm(): boolean {
  const errors: Partial<Record<keyof ProjectTypeRequest, string>> = {}
  if (!form.value.key) errors.key = 'Key không được để trống'
  else if (!/^[A-Za-z0-9_-]{1,50}$/.test(form.value.key))
    errors.key = 'Key chỉ được chứa chữ cái, số, _ hoặc - (tối đa 50 ký tự)'
  if (!form.value.value) errors.value = 'Value không được để trống'
  formErrors.value = errors
  return Object.keys(errors).length === 0
}

async function saveItem() {
  if (!validateForm()) return
  saving.value = true
  try {
    if (editingItem.value) {
      await projectTypeService.update(editingItem.value.id, form.value)
      toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã cập nhật loại dự án', life: 3000 })
    } else {
      await projectTypeService.create(form.value)
      toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã thêm loại dự án', life: 3000 })
    }
    dialogVisible.value = false
    await loadData()
  } finally {
    saving.value = false
  }
}

async function handleDelete(item: ProjectTypeResponse) {
  saving.value = true
  try {
    const res = await projectTypeService.softDelete(item.id, false)
    if (res.code === 'IN_USE_WARNING' && res.data?.inUse) {
      pendingDeleteId.value = item.id
      inUsageCount.value = res.data.usageCount
      inUseDialogVisible.value = true
    } else {
      toast.add({ severity: 'success', summary: 'Đã xóa', detail: 'Loại dự án đã được xóa', life: 3000 })
      await loadData()
    }
  } finally {
    saving.value = false
  }
}

async function confirmForceDelete() {
  if (!pendingDeleteId.value) return
  saving.value = true
  try {
    await projectTypeService.softDelete(pendingDeleteId.value, true)
    inUseDialogVisible.value = false
    pendingDeleteId.value = null
    toast.add({ severity: 'success', summary: 'Đã xóa', detail: 'Loại dự án đã được xóa', life: 3000 })
    await loadData()
  } finally {
    saving.value = false
  }
}

onMounted(loadData)
</script>
