<template>
  <div class="view-container p-4">
    <h1 class="text-2xl font-semibold" style="margin-bottom: 1rem">Quản Lý Phân Quyền</h1>

    <div class="toolbar">
      <div class="toolbar-filters">
        <div style="position: relative; display: inline-flex; align-items: center">
          <IconField>
            <InputIcon class="pi pi-search" />
            <InputText
              v-model="keyword"
              placeholder="Tìm theo mã / tên role..."
              style="min-width: 280px; padding-right: 2rem"
              @keydown.enter="onSearch"
            />
          </IconField>
          <i
            v-if="keyword"
            class="pi pi-times"
            style="position: absolute; right: 0.5rem; cursor: pointer; color: var(--p-text-muted-color); font-size: 0.85rem"
            @click="clearSearch"
          />
        </div>
      </div>
      <div class="toolbar-actions">
        <Button label="Tạo mới" icon="pi pi-plus" @click="openCreate" />
      </div>
    </div>

    <DataTable
      :value="roles"
      :loading="loading"
      dataKey="id"
      lazy
      :paginator="true"
      :rows="pageSize"
      :totalRecords="totalRecords"
      :rowsPerPageOptions="[5, 10, 20, 50, 100]"
      :first="first"
      @page="onPage($event)"
      stripedRows
      emptyMessage="Chưa có role nào"
      class="p-datatable-sm"
      paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink RowsPerPageDropdown"
      currentPageReportTemplate="Hiển thị {first} - {last} / {totalRecords}"
    >
      <Column field="roleCode" header="Mã Role" style="width: 14rem" />
      <Column field="roleName" header="Tên Role" />
      <Column field="description" header="Mô tả">
        <template #body="{ data }">{{ data.description ?? '—' }}</template>
      </Column>
      <Column field="active" header="Trạng thái" style="width: 8rem">
        <template #body="{ data }">
          <Tag :severity="data.active ? 'success' : 'danger'" :value="data.active ? 'Active' : 'Inactive'" />
        </template>
      </Column>
      <Column field="userCount" header="Số người dùng" style="width: 10rem" />
      <Column header="Hành động" style="width: 10rem">
        <template #body="{ data }">
          <div style="display: flex; gap: 0.5rem">
            <Button icon="pi pi-pencil" severity="secondary" size="small" text @click="openEdit(data)" />
            <Button icon="pi pi-shield" severity="info" size="small" text @click="openPermissions(data)" />
            <Button icon="pi pi-trash" severity="danger" size="small" text @click="confirmDelete(data)" />
          </div>
        </template>
      </Column>
    </DataTable>

    <RoleDialog
      v-if="showDialog"
      :role="selectedRole"
      @saved="onSaved"
      @close="showDialog = false"
    />

    <PermissionTreeDialog
      v-if="showPermDialog"
      :role-id="selectedRole?.id ?? ''"
      :role-name="selectedRole?.roleName ?? ''"
      @saved="onPermSaved"
      @close="showPermDialog = false"
    />

    <ConfirmDialog />
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Button from 'primevue/button'
import Tag from 'primevue/tag'
import InputText from 'primevue/inputtext'
import IconField from 'primevue/iconfield'
import InputIcon from 'primevue/inputicon'
import ConfirmDialog from 'primevue/confirmdialog'
import { useConfirm } from 'primevue/useconfirm'
import { useToast } from 'primevue/usetoast'
import type { Role } from '@/types/role'
import { roleService } from '@/services/roleService'
import RoleDialog from '@/components/admin/roles/RoleDialog.vue'
import PermissionTreeDialog from '@/components/admin/roles/PermissionTreeDialog.vue'

const confirm = useConfirm()
const toast = useToast()

const roles = ref<Role[]>([])
const loading = ref(false)
const showDialog = ref(false)
const showPermDialog = ref(false)
const selectedRole = ref<Role | null>(null)

const keyword = ref('')
const pageSize = ref(20)
const currentPage = ref(0)
const totalRecords = ref(0)
const first = ref(0)

onMounted(loadRoles)

let debounceTimer: ReturnType<typeof setTimeout> | null = null
watch(keyword, () => {
  if (debounceTimer) clearTimeout(debounceTimer)
  debounceTimer = setTimeout(() => onSearch(), 400)
})

async function loadRoles() {
  loading.value = true
  try {
    const result = await roleService.searchRoles(keyword.value, currentPage.value, pageSize.value)
    roles.value = result.content
    totalRecords.value = result.totalElements
  } finally {
    loading.value = false
  }
}

function onSearch() {
  currentPage.value = 0
  first.value = 0
  loadRoles()
}

function clearSearch() {
  keyword.value = ''
  onSearch()
}

function onPage(event: { page: number; rows: number; first: number }) {
  currentPage.value = event.page
  pageSize.value = event.rows
  first.value = event.first
  loadRoles()
}

function openCreate() {
  selectedRole.value = null
  showDialog.value = true
}

function openEdit(role: Role) {
  selectedRole.value = role
  showDialog.value = true
}

function openPermissions(role: Role) {
  selectedRole.value = role
  showPermDialog.value = true
}

function onSaved() {
  showDialog.value = false
  loadRoles()
  toast.add({ severity: 'success', summary: 'Thành công', detail: 'Lưu role thành công', life: 3000 })
}

function onPermSaved() {
  showPermDialog.value = false
  toast.add({ severity: 'success', summary: 'Thành công', detail: 'Cập nhật phân quyền thành công', life: 3000 })
}

function confirmDelete(role: Role) {
  confirm.require({
    message: `Xóa role "${role.roleName}"?`,
    header: 'Xác nhận xóa',
    icon: 'pi pi-exclamation-triangle',
    acceptClass: 'p-button-danger',
    accept: () => doDelete(role, false),
    reject: () => {}
  })
}

async function doDelete(role: Role, force: boolean) {
  try {
    await roleService.deleteRole(role.id, force)
    toast.add({ severity: 'success', summary: 'Đã xóa', detail: `Role "${role.roleName}" đã bị xóa`, life: 3000 })
    loadRoles()
  } catch (err: unknown) {
    const code = (err as { response?: { data?: { code?: string; message?: string } } })?.response?.data?.code
    if (code === 'ROLE_DELETE_REQUIRES_CONFIRMATION') {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message ?? ''
      confirm.require({
        message: msg + ' Bạn có chắc muốn xóa?',
        header: 'Cảnh báo',
        icon: 'pi pi-exclamation-triangle',
        acceptClass: 'p-button-danger',
        accept: () => doDelete(role, true),
        reject: () => {}
      })
    }
  }
}
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1rem;
  gap: 1rem;
  flex-wrap: wrap;
}
.toolbar-filters {
  display: flex;
  gap: 0.5rem;
  flex: 1;
  flex-wrap: wrap;
}
.toolbar-actions {
  display: flex;
  gap: 0.5rem;
  flex-shrink: 0;
}
</style>
