<template>
  <div class="login-card">
    <div class="login-header">
      <span class="pi pi-chart-bar login-icon" />
      <h1 class="login-title">Hệ Thống Quản Lý Dự Án</h1>
      <p class="login-subtitle">Vui lòng đăng nhập để tiếp tục</p>
    </div>

    <form @submit.prevent="handleSubmit" class="login-form">
      <div class="field">
        <label for="username">Tên đăng nhập</label>
        <InputText
          id="username"
          v-model="form.username"
          :class="{ 'p-invalid': errors.username }"
          placeholder="Nhập tên đăng nhập"
          autocomplete="username"
          class="w-full"
        />
        <small v-if="errors.username" class="p-error">{{ errors.username }}</small>
      </div>

      <div class="field">
        <label for="password">Mật khẩu</label>
        <Password
          id="password"
          v-model="form.password"
          :class="{ 'p-invalid': errors.password }"
          placeholder="Nhập mật khẩu"
          autocomplete="current-password"
          class="w-full"
          :feedback="false"
          toggleMask
        />
        <small v-if="errors.password" class="p-error">{{ errors.password }}</small>
      </div>

      <Message v-if="apiError" severity="error" :closable="false">{{ apiError }}</Message>

      <Button
        type="submit"
        label="Đăng nhập"
        icon="pi pi-sign-in"
        :loading="loading"
        class="w-full login-btn"
      />
    </form>

    <div v-if="expiredMessage" class="expired-notice">
      <Message severity="warn" :closable="false">{{ expiredMessage }}</Message>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import InputText from 'primevue/inputtext'
import Password from 'primevue/password'
import Button from 'primevue/button'
import Message from 'primevue/message'
import { useAuthStore } from '@/stores/authStore'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const form = ref({ username: '', password: '' })
const errors = ref({ username: '', password: '' })
const apiError = ref('')
const loading = ref(false)
const expiredMessage = ref('')

onMounted(() => {
  if (route.query.expired === '1') {
    expiredMessage.value = 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.'
  }
})

function validate(): boolean {
  errors.value = { username: '', password: '' }
  let valid = true
  if (!form.value.username.trim()) {
    errors.value.username = 'Tên đăng nhập không được để trống'
    valid = false
  }
  if (!form.value.password) {
    errors.value.password = 'Mật khẩu không được để trống'
    valid = false
  }
  return valid
}

const errorMessages: Record<string, string> = {
  AUTH_INVALID_CREDENTIALS: 'Tên đăng nhập hoặc mật khẩu không đúng',
  AUTH_ACCOUNT_INACTIVE: 'Tài khoản đã bị vô hiệu hoá. Liên hệ quản trị viên.',
  AUTH_ROLE_INACTIVE: 'Nhóm quyền của tài khoản bị vô hiệu hoá. Liên hệ quản trị viên.'
}

async function handleSubmit() {
  if (!validate()) return
  loading.value = true
  apiError.value = ''
  try {
    await authStore.login({ username: form.value.username, password: form.value.password })
    const redirect = route.query.redirect as string | undefined
    router.push(redirect && redirect !== '/login' ? redirect : { name: 'dashboard' })
  } catch (err: unknown) {
    const code = (err as { response?: { data?: { code?: string } } })?.response?.data?.code
    apiError.value = errorMessages[code ?? ''] ?? 'Đăng nhập thất bại. Vui lòng thử lại.'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-card {
  width: 100%;
  max-width: 420px;
  background: #ffffff;
  border-radius: 20px;
  padding: 2.75rem 2.5rem;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.08), 0 0 0 1px rgba(0, 0, 0, 0.04);
}

.login-header {
  text-align: center;
  margin-bottom: 2.25rem;
}

.login-icon {
  font-size: 2.5rem;
  color: #ef4444;
  filter: drop-shadow(0 4px 8px rgba(239, 68, 68, 0.2));
}

.login-title {
  font-size: 1.5rem;
  font-weight: 700;
  margin: 0.75rem 0 0.35rem;
  color: #0f172a;
  letter-spacing: -0.03em;
}

.login-subtitle {
  font-size: 0.875rem;
  color: #94a3b8;
  margin: 0;
  font-weight: 400;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.field label {
  font-size: 0.825rem;
  font-weight: 600;
  color: #475569;
  letter-spacing: 0.01em;
}

.login-btn {
  margin-top: 0.25rem;
  height: 44px;
  font-weight: 600;
  font-size: 0.9rem;
  border-radius: 10px !important;
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%) !important;
  border-color: #dc2626 !important;
  box-shadow: 0 4px 14px rgba(239, 68, 68, 0.3);
  transition: all 0.2s ease;
}

.login-btn:hover {
  box-shadow: 0 6px 20px rgba(239, 68, 68, 0.4);
  transform: translateY(-1px);
}

.expired-notice {
  margin-top: 1rem;
}
</style>
