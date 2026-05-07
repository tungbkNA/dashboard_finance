<template>
  <div class="app-sidebar">
    <div class="sidebar-header">
      <span class="sidebar-logo pi pi-chart-bar" />
      <span class="sidebar-title">Dashboard Finance</span>
    </div>
    <nav class="sidebar-nav">
      <RouterLink
        v-for="item in menuItems"
        :key="item.route"
        :to="item.route"
        class="sidebar-item"
        active-class="sidebar-item--active"
      >
        <span :class="['pi', item.icon, 'sidebar-item-icon']" />
        <span class="sidebar-item-label">{{ item.label }}</span>
      </RouterLink>
    </nav>
  </div>
</template>

<script setup lang="ts">
const menuItems = [
  { label: 'Dashboard',              icon: 'pi-home',      route: '/' },
  { label: 'Cài đặt dự án',         icon: 'pi-folder',    route: '/project-settings' },
  { label: 'Quản Lý Các Dự Án',     icon: 'pi-briefcase', route: '/projects' },
  { label: 'Cấu hình',              icon: 'pi-cog',       route: '/config' }
]
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

.sidebar-item-icon {
  font-size: 1rem;
  width: 1.25rem;
  text-align: center;
}
</style>
