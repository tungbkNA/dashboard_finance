<template>
  <div class="view-container">
    <h1 class="page-title">Cấu hình</h1>

    <div class="config-grid">
      <div class="config-card">
        <span class="pi pi-cog config-card-icon" />
        <div class="config-card-body">
          <h3>Cấu hình hệ thống</h3>
          <p>Thiết lập thông số chung cho hệ thống</p>
        </div>
      </div>

      <RouterLink v-if="authStore.hasPermission('MANAGE_ROLE')" to="/admin/roles" class="config-card config-card--link">
        <span class="pi pi-shield config-card-icon" />
        <div class="config-card-body">
          <h3>Quản lý Phân quyền</h3>
          <p>Tạo và phân quyền các nhóm vai trò trong hệ thống</p>
        </div>
        <span class="pi pi-arrow-right config-card-arrow" />
      </RouterLink>

      <RouterLink v-if="authStore.hasPermission('MANAGE_USER')" to="/admin/users" class="config-card config-card--link">
        <span class="pi pi-users config-card-icon" />
        <div class="config-card-body">
          <h3>Quản lý Người dùng</h3>
          <p>Tạo, chỉnh sửa và phân quyền tài khoản người dùng</p>
        </div>
        <span class="pi pi-arrow-right config-card-arrow" />
      </RouterLink>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '@/stores/authStore'

const authStore = useAuthStore()
</script>

<style scoped>
.page-title {
  margin-bottom: 1.5rem;
}

.config-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1rem;
}

.config-card {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1.25rem 1.5rem;
  background: var(--p-surface-0, #fff);
  border: 1px solid var(--p-surface-200, #e5e7eb);
  border-radius: 0.5rem;
  text-decoration: none;
  color: inherit;
}

.config-card--link {
  cursor: pointer;
  transition: box-shadow 0.2s, border-color 0.2s;
}

.config-card--link:hover {
  border-color: var(--p-primary-400, #f87171);
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}

.config-card-icon {
  font-size: 1.75rem;
  color: var(--p-primary-500, #ef4444);
  flex-shrink: 0;
}

.config-card-body {
  flex: 1;
}

.config-card-body h3 {
  margin: 0 0 0.25rem;
  font-size: 1rem;
  font-weight: 600;
}

.config-card-body p {
  margin: 0;
  font-size: 0.875rem;
  color: var(--p-surface-500, #6b7280);
}

.config-card-arrow {
  font-size: 0.875rem;
  color: var(--p-surface-400, #9ca3af);
}
</style>
