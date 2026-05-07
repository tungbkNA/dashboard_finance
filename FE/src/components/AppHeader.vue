<template>
  <header class="app-header">
    <div class="header-brand">
      <span class="pi pi-chart-bar header-icon" />
      <span class="header-title">Hệ Thống Quản Lý Dự Án Nội Bộ</span>
    </div>
    <div class="header-actions">
      <span v-if="authStore.user" class="header-user">
        <span class="pi pi-user" style="margin-right: 0.4rem" />
        {{ authStore.user.displayName }}
        <span class="user-role">({{ authStore.user.roleName }})</span>
      </span>
      <Button
        v-if="authStore.isAuthenticated"
        icon="pi pi-sign-out"
        label="Đăng xuất"
        severity="secondary"
        text
        size="small"
        class="logout-btn"
        @click="handleLogout"
      />
      <span v-else class="header-version">v{{ version }}</span>
    </div>
  </header>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import Button from 'primevue/button'
import { useAuthStore } from '@/stores/authStore'

const version = ref('1.0.0')
const authStore = useAuthStore()
const router = useRouter()

async function handleLogout() {
  authStore.logout()
  router.push({ name: 'login' })
}
</script>

<style scoped>
.app-header {
  height: 56px;
  background: var(--p-primary-600, #dc2626);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 1.5rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.header-brand {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-size: 1.1rem;
  font-weight: 600;
}

.header-icon {
  font-size: 1.3rem;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
  font-size: 0.875rem;
}

.header-user {
  opacity: 0.95;
  font-weight: 500;
}

.user-role {
  opacity: 0.75;
  font-size: 0.8rem;
  margin-left: 0.25rem;
}

.header-version {
  font-size: 0.8rem;
  opacity: 0.8;
}

.logout-btn {
  color: #fff !important;
  border-color: rgba(255, 255, 255, 0.4) !important;
}
</style>

