<template>
  <div class="view-container p-4">
    <h1 style="font-size: 1.25rem; font-weight: 600; margin-bottom: 0.75rem">Quản Lý Người Dùng</h1>

    <!-- Toolbar: filters left, actions right -->
    <div class="toolbar" style="margin-bottom: 0.75rem">
      <div class="toolbar-filters">
        <InputText v-model="filterKeyword" placeholder="Tên đăng nhập / Mã NV..."
                   class="filter-input" @keydown.enter="doSearch" />
        <Select v-model="filterPosition" :options="positionOptions" optionLabel="label" optionValue="value"
                   placeholder="Chức vụ" showClear style="min-width: 130px" @change="doSearch" />
        <Select v-model="filterRoleId" :options="roles" optionLabel="roleName" optionValue="id"
                   placeholder="Role" showClear style="min-width: 150px" @change="doSearch" />
        <Select v-model="filterActive" :options="statusOptions" optionLabel="label" optionValue="value"
                   placeholder="Trạng thái" showClear style="min-width: 140px" @change="doSearch" />
        <Button icon="pi pi-search" severity="primary" outlined size="small" @click="doSearch" v-tooltip.top="'Tìm kiếm'" />
      </div>
      <div class="toolbar-actions">
        <Button label="Tạo mới" icon="pi pi-user-plus" @click="openCreate" />
      </div>
    </div>

    <DataTable
      :value="users"
      :loading="loading"
      dataKey="id"
      :lazy="true"
      :paginator="true"
      :rows="pageSize"
      :totalRecords="totalRecords"
      :first="first"
      @page="onPage"
      :rowsPerPageOptions="[5, 10, 20, 50, 100]"
      paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink RowsPerPageDropdown"
      stripedRows
      emptyMessage="Không tìm thấy người dùng nào"
      class="p-datatable-sm"
    >
      <Column field="username" header="Tên đăng nhập" />
      <Column field="employeeCode" header="Mã NV" />
      <Column field="displayName" header="Tên hiển thị" />
      <Column field="email" header="Email" />
      <Column field="phone" header="SĐT" />
      <Column field="position" header="Chức vụ" />
      <Column field="roleName" header="Role" />
      <Column field="active" header="Trạng thái">
        <template #body="{ data }">
          <Tag :severity="data.active ? 'success' : 'danger'" :value="data.active ? 'Active' : 'Inactive'" />
        </template>
      </Column>
      <Column header="Hành động" style="width: 14rem">
        <template #body="{ data }">
          <div class="flex gap-2">
            <Button icon="pi pi-pencil" severity="secondary" size="small" text @click="openEdit(data)" />
            <Button icon="pi pi-key" severity="warn" size="small" text @click="openResetPassword(data)" />
            <Button
              icon="pi pi-ban"
              severity="danger"
              size="small"
              text
              :disabled="data.username === authStore.user?.username"
              @click="confirmDeactivate(data)"
            />
          </div>
        </template>
      </Column>
    </DataTable>

    <UserDialog
      v-if="showDialog"
      :user="selectedUser"
      @saved="onSaved"
      @close="showDialog = false"
    />

    <Dialog
      v-model:visible="showPasswordDialog"
      header="Đặt lại mật khẩu"
      modal
      :style="{ width: '400px' }"
    >
      <div class="form-row mt-2">
        <label for="newPw">Mật khẩu mới <span class="text-red-500">*</span></label>
        <div class="form-input">
          <Password id="newPw" v-model="newPassword" class="w-full" :feedback="false" toggleMask placeholder="Chữ hoa, thường, số, đặc biệt, ≥8 ký tự" />
          <small v-if="newPasswordError" class="p-error">{{ newPasswordError }}</small>
        </div>
      </div>
      <template #footer>
        <Button label="Hủy" severity="secondary" text @click="showPasswordDialog = false" />
        <Button label="Lưu" :loading="savingPassword" @click="doResetPassword" />
      </template>
    </Dialog>

    <ConfirmDialog />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Button from 'primevue/button'
import Tag from 'primevue/tag'
import Dialog from 'primevue/dialog'
import Password from 'primevue/password'
import InputText from 'primevue/inputtext'
import Select from 'primevue/select'
import ConfirmDialog from 'primevue/confirmdialog'
import { useConfirm } from 'primevue/useconfirm'
import { useToast } from 'primevue/usetoast'
import type { AppUser } from '@/types/user'
import type { Role } from '@/types/role'
import { userService } from '@/services/userService'
import { roleService } from '@/services/roleService'
import { useAuthStore } from '@/stores/authStore'
import UserDialog from '@/components/admin/users/UserDialog.vue'

const confirm = useConfirm()
const toast = useToast()
const authStore = useAuthStore()

const PASSWORD_REGEX = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^a-zA-Z0-9]).{8,}$/

const users = ref<AppUser[]>([])
const roles = ref<Role[]>([])
const loading = ref(false)
const showDialog = ref(false)
const selectedUser = ref<AppUser | null>(null)
const showPasswordDialog = ref(false)
const newPassword = ref('')
const newPasswordError = ref('')
const savingPassword = ref(false)
let passwordTargetId = ''

// ---- Pagination ----
const totalRecords = ref(0)
const currentPage = ref(0)
const pageSize = ref(20)
const first = ref(0)

// ---- Filters ----
const filterKeyword = ref('')
const filterPosition = ref<string | null>(null)
const filterRoleId = ref<string | null>(null)
const filterActive = ref<boolean | null>(null)

const positionOptions = [
  { label: 'PM', value: 'PM' },
  { label: 'PU', value: 'PU' }
]

const statusOptions = [
  { label: 'Active', value: true },
  { label: 'Inactive', value: false }
]

let searchTimeout: ReturnType<typeof setTimeout> | null = null

watch(filterKeyword, () => {
  if (searchTimeout) clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => doSearch(), 400)
})

function onPage(event: { page: number; rows: number; first: number }) {
  currentPage.value = event.page
  pageSize.value = event.rows
  first.value = event.first
  loadUsers()
}

function doSearch() {
  currentPage.value = 0
  first.value = 0
  loadUsers()
}

onMounted(async () => {
  try {
    roles.value = await roleService.listRoles()
  } catch { /* silent */ }
  loadUsers()
})

async function loadUsers() {
  loading.value = true
  try {
    const page = await userService.searchUsers({
      keyword: filterKeyword.value || undefined,
      position: filterPosition.value,
      roleId: filterRoleId.value,
      active: filterActive.value,
      page: currentPage.value,
      size: pageSize.value
    })
    users.value = page.content ?? []
    totalRecords.value = page.totalElements ?? 0
  } finally {
    loading.value = false
  }
}

function openCreate() {
  selectedUser.value = null
  showDialog.value = true
}

function openEdit(user: AppUser) {
  selectedUser.value = user
  showDialog.value = true
}

function openResetPassword(user: AppUser) {
  passwordTargetId = user.id
  newPassword.value = ''
  newPasswordError.value = ''
  showPasswordDialog.value = true
}

function onSaved() {
  showDialog.value = false
  loadUsers()
  toast.add({ severity: 'success', summary: 'Thành công', detail: 'Lưu người dùng thành công', life: 3000 })
}

async function doResetPassword() {
  if (!newPassword.value || !PASSWORD_REGEX.test(newPassword.value)) {
    newPasswordError.value = 'Mật khẩu phải có ít nhất 8 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt'
    return
  }
  savingPassword.value = true
  try {
    await userService.resetPassword(passwordTargetId, { newPassword: newPassword.value })
    showPasswordDialog.value = false
    toast.add({ severity: 'success', summary: 'Đã đặt lại', detail: 'Mật khẩu đã được cập nhật', life: 3000 })
  } finally {
    savingPassword.value = false
  }
}

function confirmDeactivate(user: AppUser) {
  confirm.require({
    message: `Vô hiệu hoá tài khoản "${user.displayName}"?`,
    header: 'Xác nhận',
    icon: 'pi pi-exclamation-triangle',
    acceptClass: 'p-button-danger',
    accept: () => doDeactivate(user)
  })
}

async function doDeactivate(user: AppUser) {
  try {
    await userService.deleteUser(user.id)
    toast.add({ severity: 'success', summary: 'Đã vô hiệu hoá', detail: `Tài khoản "${user.displayName}" đã bị vô hiệu hoá`, life: 3000 })
    loadUsers()
  } catch {
    // error handled by api interceptor
  }
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
  flex-wrap: wrap;
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

.form-row {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
}

.form-row > label {
  width: 100px;
  min-width: 100px;
  padding-top: 0.55rem;
  font-weight: 500;
  text-align: right;
  font-size: 0.875rem;
}

.form-input {
  flex: 1;
}
</style>
