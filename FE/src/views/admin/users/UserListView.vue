<template>
  <div class="view-container p-4">
    <div class="flex align-items-center justify-content-between mb-4">
      <h1 class="text-2xl font-semibold m-0">Quản Lý Người Dùng</h1>
      <Button label="Tạo mới" icon="pi pi-user-plus" @click="openCreate" />
    </div>

    <DataTable
      :value="users"
      :loading="loading"
      dataKey="id"
      paginator
      :rows="20"
      stripedRows
      emptyMessage="Chưa có người dùng nào"
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
import { ref, onMounted } from 'vue'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Button from 'primevue/button'
import Tag from 'primevue/tag'
import Dialog from 'primevue/dialog'
import Password from 'primevue/password'
import ConfirmDialog from 'primevue/confirmdialog'
import { useConfirm } from 'primevue/useconfirm'
import { useToast } from 'primevue/usetoast'
import type { AppUser } from '@/types/user'
import { userService } from '@/services/userService'
import { useAuthStore } from '@/stores/authStore'
import UserDialog from '@/components/admin/users/UserDialog.vue'

const confirm = useConfirm()
const toast = useToast()
const authStore = useAuthStore()

const PASSWORD_REGEX = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^a-zA-Z0-9]).{8,}$/

const users = ref<AppUser[]>([])
const loading = ref(false)
const showDialog = ref(false)
const selectedUser = ref<AppUser | null>(null)
const showPasswordDialog = ref(false)
const newPassword = ref('')
const newPasswordError = ref('')
const savingPassword = ref(false)
let passwordTargetId = ''

onMounted(loadUsers)

async function loadUsers() {
  loading.value = true
  try {
    users.value = await userService.listUsers()
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
