<template>
  <div>
    <!-- Toolbar -->
    <div class="flex justify-end mb-3">
      <Button label="Tạo dự án" icon="pi pi-plus" @click="openCreate" />
    </div>

    <!-- Loading -->
    <div v-if="loading" class="flex justify-center py-8">
      <ProgressSpinner style="width: 40px; height: 40px" />
    </div>

    <!-- Error -->
    <Message v-else-if="error" severity="error" :closable="false">
      {{ error }}
    </Message>

    <!-- Empty -->
    <div v-else-if="!projects.length" class="text-center py-8 text-gray-400">
      <i class="pi pi-folder-open text-4xl mb-2 block" />
      <span>Chưa có dự án nào</span>
    </div>

    <!-- Table -->
    <DataTable v-else :value="projects" :paginator="projects.length > 20" :rows="20"
               tableStyle="min-width: 60rem" stripedRows>
      <Column field="projectCode" header="Mã dự án" sortable style="width: 120px" />
      <Column field="projectName" header="Tên dự án" sortable />
      <Column field="projectTypeName" header="Loại dự án" />
      <Column field="customerName" header="Khách hàng" />
      <Column field="statusContract" header="HĐ" style="width: 140px">
        <template #body="{ data }">
          <Tag :value="statusContractLabel(data.statusContract)"
               :severity="data.statusContract === 'HAS_CONTRACT' ? 'success' : 'secondary'" />
        </template>
      </Column>
      <Column field="statusProject" header="Dự án" style="width: 130px">
        <template #body="{ data }">
          <Tag :value="data.statusProject" :severity="statusProjectSeverity(data.statusProject)" />
        </template>
      </Column>
      <Column field="monthStart" header="Từ tháng" style="width: 100px" />
      <Column field="monthEnd" header="Đến tháng" style="width: 100px" />
      <Column header="Hành động" style="width: 110px">
        <template #body="{ data }">
          <Button icon="pi pi-pencil" text rounded class="mr-1" @click="openEdit(data)" />
          <Button icon="pi pi-trash" text rounded severity="danger" @click="confirmDelete(data)" />
        </template>
      </Column>
    </DataTable>

    <!-- Create/Edit Dialog -->
    <Dialog v-model:visible="dialogVisible" :header="editingProject ? 'Sửa dự án' : 'Tạo dự án'"
            modal :style="{ width: '620px' }" :draggable="false">
      <form @submit.prevent="() => saveProject()" class="dialog-form pt-2">

        <div class="form-row">
          <label>Mã dự án <span class="text-red-500">*</span></label>
          <div class="form-input">
            <InputText v-model="form.projectCode" class="w-full"
                       placeholder="VD: PRJ-001"
                       :class="{ 'p-invalid': formErrors.projectCode }" />
            <small class="p-error">{{ formErrors.projectCode }}</small>
          </div>
        </div>

        <div class="form-row">
          <label>Tên dự án <span class="text-red-500">*</span></label>
          <div class="form-input">
            <InputText v-model="form.projectName" class="w-full"
                       placeholder="Nhập tên dự án"
                       :class="{ 'p-invalid': formErrors.projectName }" />
            <small class="p-error">{{ formErrors.projectName }}</small>
          </div>
        </div>

        <div class="form-row">
          <label>Khách hàng <span class="text-red-500">*</span></label>
          <div class="form-input">
            <Dropdown v-model="form.customerId" :options="customers"
                      optionLabel="customerName" optionValue="id"
                      placeholder="Chọn khách hàng" class="w-full"
                      :loading="loadingDropdowns"
                      :class="{ 'p-invalid': formErrors.customerId }" />
            <small class="p-error">{{ formErrors.customerId }}</small>
          </div>
        </div>

        <div class="form-row">
          <label>Loại dự án <span class="text-red-500">*</span></label>
          <div class="form-input">
            <Dropdown v-model="form.projectTypeId" :options="projectTypes"
                      optionLabel="value" optionValue="id"
                      placeholder="Chọn loại dự án" class="w-full"
                      :loading="loadingDropdowns"
                      :class="{ 'p-invalid': formErrors.projectTypeId }" />
            <small class="p-error">{{ formErrors.projectTypeId }}</small>
          </div>
        </div>

        <div class="form-row">
          <label>Đơn giá <span class="text-red-500">*</span></label>
          <div class="form-input">
            <InputNumber v-model="form.price" :min="0" :minFractionDigits="0" :maxFractionDigits="4"
                         class="w-full" placeholder="0" :class="{ 'p-invalid': formErrors.price }" />
            <small class="p-error">{{ formErrors.price }}</small>
          </div>
        </div>

        <div class="form-row">
          <label>Trạng thái HĐ <span class="text-red-500">*</span></label>
          <div class="form-input">
            <Dropdown v-model="form.statusContract" :options="statusContractOptions"
                      optionLabel="label" optionValue="value"
                      placeholder="Chọn trạng thái HĐ"
                      class="w-full" :class="{ 'p-invalid': formErrors.statusContract }" />
            <small class="p-error">{{ formErrors.statusContract }}</small>
          </div>
        </div>

        <div class="form-row">
          <label>Trạng thái DA <span class="text-red-500">*</span></label>
          <div class="form-input">
            <Dropdown v-model="form.statusProject" :options="statusProjectOptions"
                      optionLabel="label" optionValue="value"
                      placeholder="Chọn trạng thái DA"
                      class="w-full" :class="{ 'p-invalid': formErrors.statusProject }" />
            <small class="p-error">{{ formErrors.statusProject }}</small>
          </div>
        </div>

        <div class="form-row">
          <label>Tháng bắt đầu <span class="text-red-500">*</span></label>
          <div class="form-input">
            <InputText v-model="form.monthStart" class="w-full" placeholder="mm/yyyy"
                       :class="{ 'p-invalid': formErrors.monthStart }" />
            <small class="p-error">{{ formErrors.monthStart }}</small>
          </div>
        </div>

        <div class="form-row">
          <label>Tháng kết thúc <span class="text-red-500">*</span></label>
          <div class="form-input">
            <InputText v-model="form.monthEnd" class="w-full" placeholder="mm/yyyy"
                       :class="{ 'p-invalid': formErrors.monthEnd }" />
            <small class="p-error">{{ formErrors.monthEnd }}</small>
          </div>
        </div>

        <div class="form-row">
          <label>Người đại diện</label>
          <div class="form-input">
            <Dropdown
              v-model="form.representUserId"
              :options="activeUsers"
              optionLabel="displayName"
              optionValue="id"
              placeholder="-- Không chọn --"
              :showClear="true"
              class="w-full"
            />
          </div>
        </div>

        <div class="flex justify-end gap-2 pt-2">
          <Button label="Hủy" severity="secondary" text @click="dialogVisible = false" type="button" />
          <Button :label="editingProject ? 'Lưu thay đổi' : 'Tạo'" type="submit" :loading="saving" />
        </div>
      </form>
    </Dialog>

    <!-- Delete Confirm Dialog -->
    <ConfirmDialog />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useToast } from 'primevue/usetoast'
import { useConfirm } from 'primevue/useconfirm'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Button from 'primevue/button'
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'
import InputNumber from 'primevue/inputnumber'
import Dropdown from 'primevue/dropdown'
import Tag from 'primevue/tag'
import Message from 'primevue/message'
import ProgressSpinner from 'primevue/progressspinner'
import ConfirmDialog from 'primevue/confirmdialog'
import { projectService } from '@/services/projectService'
import { projectTypeService } from '@/services/projectTypeService'
import { customerService } from '@/services/customerService'
import { userService } from '@/services/userService'
import type { AppUser } from '@/types/user'
import type { ProjectResponse, ProjectRequest, ProjectTypeResponse, CustomerResponse, StatusContract, StatusProject } from '@/types/project-settings'

const toast = useToast()
const confirm = useConfirm()

const projects = ref<ProjectResponse[]>([])
const projectTypes = ref<ProjectTypeResponse[]>([])
const customers = ref<CustomerResponse[]>([])
const activeUsers = ref<AppUser[]>([])
const loading = ref(false)
const error = ref<string | null>(null)
const saving = ref(false)

const dialogVisible = ref(false)
const editingProject = ref<ProjectResponse | null>(null)

const emptyForm = (): ProjectRequest => ({
  projectCode: '',
  projectName: '',
  customerId: '',
  projectTypeId: '',
  price: 0,
  statusContract: 'NO_CONTRACT',
  statusProject: 'OPEN',
  monthStart: '',
  monthEnd: '',
  representUserId: null
})

const form = ref<ProjectRequest>(emptyForm())
const formErrors = ref<Partial<Record<keyof ProjectRequest, string>>>({})

const statusContractOptions = [
  { label: 'Chưa có hợp đồng', value: 'NO_CONTRACT' },
  { label: 'Có hợp đồng', value: 'HAS_CONTRACT' }
]

const statusProjectOptions = [
  { label: 'OPEN', value: 'OPEN' },
  { label: 'INPROGRESS', value: 'INPROGRESS' },
  { label: 'PENDING', value: 'PENDING' },
  { label: 'DONE', value: 'DONE' },
  { label: 'CLOSE', value: 'CLOSE' }
]

function statusContractLabel(val: StatusContract) {
  return val === 'HAS_CONTRACT' ? 'Có HĐ' : 'Chưa có HĐ'
}

function statusProjectSeverity(val: StatusProject) {
  const map: Record<StatusProject, string> = {
    OPEN: 'info', INPROGRESS: 'warning', PENDING: 'secondary', DONE: 'success', CLOSE: 'danger'
  }
  return map[val] ?? 'secondary'
}

async function loadData() {
  loading.value = true
  error.value = null
  try {
    const [pRes, ptRes, cRes] = await Promise.all([
      projectService.getAll(),
      projectTypeService.getAll(),
      customerService.getAll()
    ])
    projects.value = pRes.data ?? []
    projectTypes.value = ptRes.data ?? []
    customers.value = cRes.data ?? []
  } catch {
    error.value = 'Không thể tải danh sách dự án'
  } finally {
    loading.value = false
  }
}

const loadingDropdowns = ref(false)

async function openCreate() {
  editingProject.value = null
  form.value = emptyForm()
  formErrors.value = {}
  dialogVisible.value = true
  loadingDropdowns.value = true
  try {
    const [ptRes, cRes, uRes] = await Promise.all([
      projectTypeService.getAll(),
      customerService.getAll(),
      userService.listUsers(true)
    ])
    projectTypes.value = ptRes.data ?? []
    customers.value = cRes.data ?? []
    activeUsers.value = uRes
  } finally {
    loadingDropdowns.value = false
  }
}

function openEdit(project: ProjectResponse) {
  editingProject.value = project
  form.value = {
    projectCode: project.projectCode,
    projectName: project.projectName,
    customerId: project.customerId,
    projectTypeId: project.projectTypeId,
    price: project.price,
    statusContract: project.statusContract,
    statusProject: project.statusProject,
    monthStart: project.monthStart,
    monthEnd: project.monthEnd,
    representUserId: project.representUserId ?? null
  }
  formErrors.value = {}
  dialogVisible.value = true
  userService.listUsers(true).then(users => { activeUsers.value = users }).catch(() => {})
}

function validateForm(): boolean {
  const errors: Partial<Record<keyof ProjectRequest, string>> = {}
  const monthRegex = /^(0[1-9]|1[0-2])\/[2-9]\d{3}$/

  if (!form.value.projectCode) errors.projectCode = 'Mã dự án không được để trống'
  else if (!/^[A-Za-z0-9_-]{1,50}$/.test(form.value.projectCode))
    errors.projectCode = 'Mã dự án chỉ được chứa chữ cái, số, _ hoặc - (tối đa 50 ký tự)'

  if (!form.value.projectName) errors.projectName = 'Tên dự án không được để trống'
  if (!form.value.customerId) errors.customerId = 'Khách hàng không được để trống'
  if (!form.value.projectTypeId) errors.projectTypeId = 'Loại dự án không được để trống'
  if (form.value.price == null || form.value.price < 0) errors.price = 'Đơn giá phải là số không âm'
  if (!form.value.statusContract) errors.statusContract = 'Trạng thái hợp đồng không được để trống'
  if (!form.value.statusProject) errors.statusProject = 'Trạng thái dự án không được để trống'

  if (!form.value.monthStart) errors.monthStart = 'Tháng bắt đầu không được để trống'
  else if (!monthRegex.test(form.value.monthStart)) errors.monthStart = 'Định dạng mm/yyyy (VD: 01/2026)'

  if (!form.value.monthEnd) errors.monthEnd = 'Tháng kết thúc không được để trống'
  else if (!monthRegex.test(form.value.monthEnd)) errors.monthEnd = 'Định dạng mm/yyyy (VD: 12/2026)'

  formErrors.value = errors
  return Object.keys(errors).length === 0
}

async function saveProject(confirmShrink = false) {
  if (!validateForm()) return
  saving.value = true
  try {
    if (editingProject.value) {
      const res = await projectService.update(editingProject.value.id, form.value, confirmShrink)
      if (res.code === 'MONTH_RANGE_SHRINK_WARNING') {
        const months: string[] = (res.data as unknown as string[]) ?? []
        confirm.require({
          message: `Phạm vi tháng thu hẹp sẽ đánh dấu inactive ${months.length} bản ghi tháng (${months.join(', ')}). Dữ liệu không bị xóa. Xác nhận?`,
          header: 'Xác nhận thu hẹp tháng',
          icon: 'pi pi-exclamation-triangle',
          rejectProps: { label: 'Hủy', severity: 'secondary', outlined: true },
          acceptProps: { label: 'Xác nhận', severity: 'warning' },
          accept: () => saveProject(true)
        })
        return
      }
      toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã cập nhật dự án', life: 3000 })
    } else {
      await projectService.create(form.value)
      toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã tạo dự án mới', life: 3000 })
    }
    dialogVisible.value = false
    await loadData()
  } finally {
    saving.value = false
  }
}

function confirmDelete(project: ProjectResponse) {
  confirm.require({
    message: `Xác nhận xóa dự án "${project.projectName}"?`,
    header: 'Xóa dự án',
    icon: 'pi pi-exclamation-triangle',
    rejectProps: { label: 'Hủy', severity: 'secondary', outlined: true },
    acceptProps: { label: 'Xóa', severity: 'danger' },
    accept: async () => {
      await projectService.softDelete(project.id)
      toast.add({ severity: 'success', summary: 'Đã xóa', detail: 'Dự án đã được xóa', life: 3000 })
      await loadData()
    }
  })
}

onMounted(loadData)
</script>

<style scoped>
.dialog-form {
  display: flex;
  flex-direction: column;
  gap: 0.85rem;
}

.form-row {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
}

.form-row > label {
  width: 120px;
  min-width: 120px;
  padding-top: 0.55rem;
  font-weight: 500;
  text-align: right;
  font-size: 0.875rem;
}

.form-input {
  flex: 1;
}
</style>
