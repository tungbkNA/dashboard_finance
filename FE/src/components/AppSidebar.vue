<template>
  <div class="app-sidebar">
    <div class="sidebar-header">
      <span class="sidebar-logo pi pi-chart-bar" />
      <span class="sidebar-title">Dashboard Finance</span>
    </div>
    <nav class="sidebar-nav">
      <template v-for="item in visibleMenuItems" :key="item.route">
        <RouterLink
          :to="item.route"
          class="sidebar-item"
          :class="{ 'sidebar-item--sub': item.sub }"
          active-class="sidebar-item--active"
        >
          <span :class="['pi', item.icon, 'sidebar-item-icon']" />
          <span class="sidebar-item-label">{{ item.label }}</span>
        </RouterLink>
      </template>
    </nav>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useAuthStore } from '@/stores/authStore'

const authStore = useAuthStore()

const allMenuItems = [
  { label: 'Dashboard',           icon: 'pi-home',      route: '/',                permission: null,          sub: false },
  { label: 'Cài đặt dự án',      icon: 'pi-folder',    route: '/project-settings',permission: null,          sub: false },
  { label: 'Quản Lý Các Dự Án',  icon: 'pi-briefcase', route: '/projects',        permission: null,          sub: false },
  { label: 'Cấu hình',           icon: 'pi-cog',       route: '/config',          permission: null,          sub: false },
  { label: 'Quản lý Phân quyền', icon: 'pi-shield',    route: '/admin/roles',     permission: 'MANAGE_ROLE', sub: true  },
  { label: 'Quản lý Người dùng', icon: 'pi-users',     route: '/admin/users',     permission: 'MANAGE_USER', sub: true  },
  { label: 'Sổ tay trung tâm',   icon: 'pi-book',      route: '/handbook/file-groups', permission: 'MANAGE_HANDBOOK', sub: false },
  { label: 'Quản lý nhóm file',  icon: 'pi-folder',    route: '/handbook/file-groups', permission: 'MANAGE_HANDBOOK', sub: true  },
  { label: 'Danh mục file',      icon: 'pi-file',      route: '/handbook/files',       permission: 'MANAGE_HANDBOOK', sub: true  },
]

const visibleMenuItems = computed(() =>
  allMenuItems.filter(item => !item.permission || authStore.hasPermission(item.permission))
)
</script>

<style scoped>
.app-sidebar {
  width: 240px;
  min-height: 100vh;
  background: var(--p-surface-900, #1a1a2e);
  color: var(--p-surface-0, #ffffff);
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1.25rem 1rem;
  border-bottom: 1px solid var(--p-surface-700, #333);
  font-size: 1.1rem;
  font-weight: 600;
}

.sidebar-logo {
  font-size: 1.5rem;
  color: var(--p-primary-400, #f87171);
}

.sidebar-nav {
  display: flex;
  flex-direction: column;
  padding: 0.5rem 0;
}

.sidebar-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 1rem;
  color: var(--p-surface-200, #e5e7eb);
  text-decoration: none;
  transition: background 0.2s;
}

.sidebar-item:hover {
  background: var(--p-surface-700, #374151);
}

.sidebar-item--active {
  background: var(--p-primary-600, #dc2626);
  color: #fff;
}

.sidebar-item--sub {
  padding-left: 2.25rem;
  font-size: 0.92rem;
  color: var(--p-surface-300, #d1d5db);
}

.sidebar-item--sub:hover {
  background: var(--p-surface-700, #374151);
}

.sidebar-item-icon {
  font-size: 1rem;
  width: 1.25rem;
  text-align: center;
}
</style>
