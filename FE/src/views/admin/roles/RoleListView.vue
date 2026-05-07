<template>
  <div class="view-container p-4">
    <div class="flex align-items-center justify-content-between mb-4">
      <h1 class="text-2xl font-semibold m-0">Quản Lý Phân Quyền</h1>
      <Button
        label="Tạo mới"
        icon="pi pi-plus"
        @click="openCreate"
      />
    </div>

    <DataTable
      :value="roles"
      :loading="loading"
      dataKey="id"
      paginator
      :rows="20"
      stripedRows
      emptyMessage="Chưa có role nào"
      class="p-datatable-sm"
    >
      <Column field="roleName" header="Tên Role" />
      <Column field="description" header="Mô tả">
        <template #body="{ data }">{{ data.description ?? '—' }}</template>
      </Column>
      <Column field="active" header="Trạng thái">
        <template #body="{ data }">
          <Tag :severity="data.active ? 'success' : 'danger'" :value="data.active ? 'Active' : 'Inactive'" />
        </template>
      </Column>
      <Column field="userCount" header="Số người dùng" />
      <Column header="Hành động" style="width: 14rem">
        <template #body="{ data }">
          <div class="flex gap-2">
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
import { ref, onMounted } from 'vue'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Button from 'primevue/button'
import Tag from 'primevue/tag'
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

onMounted(loadRoles)

async function loadRoles() {
  loading.value = true
  try {
    roles.value = await roleService.listRoles()
  } finally {
    loading.value = false
  }
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
