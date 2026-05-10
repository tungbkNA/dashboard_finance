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
  height: 60px;
  background: #ffffff;
  color: #0f172a;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 1.75rem;
  border-bottom: 1px solid #e2e8f0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.header-brand {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.header-icon {
  font-size: 1.35rem;
  color: #ef4444;
}

.header-title {
  font-size: 1.05rem;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: #0f172a;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
  font-size: 0.85rem;
}

.header-user {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.4rem 0.75rem;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-weight: 500;
  color: #334155;
}

.user-role {
  color: #94a3b8;
  font-size: 0.8rem;
  font-weight: 400;
}

.header-version {
  font-size: 0.8rem;
  color: #94a3b8;
}

.logout-btn {
  color: #64748b !important;
  border-color: #e2e8f0 !important;
  border-radius: 8px !important;
  font-weight: 500;
  transition: all 0.2s ease;
}

.logout-btn:hover {
  color: #ef4444 !important;
  border-color: #fecaca !important;
  background: #fef2f2 !important;
}
</style>

