<template>
  <Dialog
    v-model:visible="visible"
    :header="isEdit ? 'Sửa Người Dùng' : 'Tạo Người Dùng Mới'"
    modal
    :style="{ width: '600px' }"
    @hide="$emit('close')"
  >
    <form @submit.prevent="handleSave" class="dialog-form pt-2">
      <!-- Username (create only) -->
      <div class="form-row">
        <label>Tên đăng nhập <span class="text-red-500">*</span></label>
        <div class="form-input">
          <InputText v-model="form.username" :disabled="isEdit" :class="{ 'p-invalid': errors.username }" class="w-full" placeholder="Nhập tên đăng nhập" />
          <small v-if="errors.username" class="p-error">{{ errors.username }}</small>
        </div>
      </div>

      <!-- Email -->
      <div class="form-row">
        <label>Email <span class="text-red-500">*</span></label>
        <div class="form-input">
          <InputText v-model="form.email" :class="{ 'p-invalid': errors.email }" class="w-full" placeholder="example@email.com" />
          <small v-if="errors.email" class="p-error">{{ errors.email }}</small>
        </div>
      </div>

      <!-- Display Name -->
      <div class="form-row">
        <label>Tên hiển thị <span class="text-red-500">*</span></label>
        <div class="form-input">
          <InputText v-model="form.displayName" :class="{ 'p-invalid': errors.displayName }" class="w-full" placeholder="Nhập tên hiển thị" />
          <small v-if="errors.displayName" class="p-error">{{ errors.displayName }}</small>
        </div>
      </div>

      <!-- Password (create only) -->
      <div v-if="!isEdit" class="form-row">
        <label>Mật khẩu <span class="text-red-500">*</span></label>
        <div class="form-input">
          <Password v-model="form.password" :class="{ 'p-invalid': errors.password }" class="w-full" :feedback="false" toggleMask placeholder="Chữ hoa, thường, số, đặc biệt, ≥8 ký tự" />
          <small v-if="errors.password" class="p-error">{{ errors.password }}</small>
        </div>
      </div>

      <!-- Role -->
      <div class="form-row">
        <label>Role <span class="text-red-500">*</span></label>
        <div class="form-input">
          <Dropdown
            v-model="form.roleId"
            :options="roles"
            optionLabel="roleName"
            optionValue="id"
            placeholder="Chọn role"
            :class="{ 'p-invalid': errors.roleId }"
            class="w-full"
            :loading="loadingRoles"
          />
          <small v-if="errors.roleId" class="p-error">{{ errors.roleId }}</small>
        </div>
      </div>

      <!-- Mã nhân viên -->
      <div class="form-row">
        <label>Mã nhân viên</label>
        <div class="form-input">
          <InputText v-model="form.employeeCode" :class="{ 'p-invalid': errors.employeeCode }" class="w-full" placeholder="VD: NV001" />
          <small v-if="errors.employeeCode" class="p-error">{{ errors.employeeCode }}</small>
        </div>
      </div>

      <!-- Chức vụ -->
      <div class="form-row">
        <label>Chức vụ</label>
        <div class="form-input">
          <Dropdown
            v-model="form.position"
            :options="positionOptions"
            optionLabel="label"
            optionValue="value"
            placeholder="Chọn chức vụ"
            showClear
            :class="{ 'p-invalid': errors.position }"
            class="w-full"
          />
          <small v-if="errors.position" class="p-error">{{ errors.position }}</small>
        </div>
      </div>

      <!-- Phone -->
      <div class="form-row">
        <label>Số điện thoại</label>
        <div class="form-input">
          <InputText v-model="form.phone" :class="{ 'p-invalid': errors.phone }" class="w-full" placeholder="VD: 0912xxxxx" />
          <small v-if="errors.phone" class="p-error">{{ errors.phone }}</small>
        </div>
      </div>

      <!-- Active toggle (edit only) -->
      <div v-if="isEdit" class="form-row">
        <label>Trạng thái</label>
        <div class="form-input">
          <ToggleButton
            v-model="form.active"
            onLabel="Active"
            offLabel="Inactive"
            onIcon="pi pi-check"
            offIcon="pi pi-times"
          />
        </div>
      </div>

      <div class="flex justify-end gap-2 mt-3">
        <Button label="Hủy" severity="secondary" text @click="$emit('close')" />
        <Button type="submit" label="Lưu" :loading="saving" />
      </div>
    </form>
  </Dialog>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'
import Password from 'primevue/password'
import Dropdown from 'primevue/dropdown'
import ToggleButton from 'primevue/togglebutton'
import Button from 'primevue/button'
import type { AppUser } from '@/types/user'
import type { Role } from '@/types/role'
import { userService } from '@/services/userService'
import { roleService } from '@/services/roleService'

const props = defineProps<{ user: AppUser | null }>()
const emit = defineEmits<{ saved: []; close: [] }>()

const visible = ref(true)
const saving = ref(false)
const loadingRoles = ref(false)
const isEdit = ref(!!props.user)
const roles = ref<Role[]>([])

const positionOptions = [
  { label: 'PM (Project Manager)', value: 'PM' },
  { label: 'PU (Project User)', value: 'PU' }
]

const PHONE_REGEX = /^0\d{8}$/
const PASSWORD_REGEX = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^a-zA-Z0-9]).{8,}$/

function validatePassword(pw: string): string {
  if (!pw) return 'Bắt buộc'
  if (!PASSWORD_REGEX.test(pw)) return 'Mật khẩu phải có ít nhất 8 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt'
  return ''
}

const form = ref({
  username: '', email: '', displayName: '', password: '',
  roleId: '', phone: '', position: '', employeeCode: '', active: true
})
const errors = ref({
  username: '', email: '', displayName: '', password: '',
  roleId: '', phone: '', position: '', employeeCode: ''
})

onMounted(async () => {
  loadingRoles.value = true
  try {
    roles.value = await roleService.listRoles()
  } finally {
    loadingRoles.value = false
  }
})

watch(() => props.user, (u) => {
  isEdit.value = !!u
  form.value.username = u?.username ?? ''
  form.value.email = u?.email ?? ''
  form.value.displayName = u?.displayName ?? ''
  form.value.password = ''
  form.value.roleId = u?.roleId ?? ''
  form.value.phone = u?.phone ?? ''
  form.value.position = u?.position ?? ''
  form.value.employeeCode = u?.employeeCode ?? ''
  form.value.active = u?.active ?? true
}, { immediate: true })

function validate(): boolean {
  errors.value = { username: '', email: '', displayName: '', password: '', roleId: '', phone: '', position: '', employeeCode: '' }
  let valid = true
  if (!isEdit.value && !form.value.username.trim()) { errors.value.username = 'Bắt buộc'; valid = false }
  if (!form.value.email.trim()) { errors.value.email = 'Bắt buộc'; valid = false }
  if (!form.value.displayName.trim()) { errors.value.displayName = 'Bắt buộc'; valid = false }
  if (!isEdit.value) {
    const pwError = validatePassword(form.value.password)
    if (pwError) { errors.value.password = pwError; valid = false }
  }
  if (!form.value.roleId) { errors.value.roleId = 'Bắt buộc'; valid = false }
  if (form.value.phone && !PHONE_REGEX.test(form.value.phone)) {
    errors.value.phone = 'Số điện thoại phải gồm 9 chữ số bắt đầu bằng số 0'; valid = false
  }
  return valid
}

async function handleSave() {
  if (!validate()) return
  saving.value = true
  try {
    if (isEdit.value && props.user) {
      await userService.updateUser(props.user.id, {
        email: form.value.email,
        displayName: form.value.displayName,
        roleId: form.value.roleId,
        active: form.value.active,
        phone: form.value.phone || null,
        position: form.value.position || null,
        employeeCode: form.value.employeeCode || null
      })
    } else {
      await userService.createUser({
        username: form.value.username,
        email: form.value.email,
        displayName: form.value.displayName,
        password: form.value.password,
        roleId: form.value.roleId,
        phone: form.value.phone || undefined,
        position: form.value.position || undefined,
        employeeCode: form.value.employeeCode || undefined
      })
    }
    emit('saved')
  } finally {
    saving.value = false
  }
}
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
