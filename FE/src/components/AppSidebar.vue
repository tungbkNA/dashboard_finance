<template>
  <div class="app-sidebar">
    <div class="sidebar-header">
      <span class="sidebar-logo pi pi-chart-bar" />
      <span class="sidebar-title">Dashboard Finance</span>
    </div>
    <nav class="sidebar-nav">
      <template v-for="item in visibleMenuItems" :key="item.key">
        <!-- Group with children -->
        <template v-if="item.children">
          <div
            class="sidebar-item sidebar-group-header"
            :class="{ 'sidebar-group-header--open': expandedGroups[item.key] }"
            @click="toggleGroup(item.key)"
          >
            <span :class="['pi', item.icon, 'sidebar-item-icon']" />
            <span class="sidebar-item-label">{{ item.label }}</span>
            <span class="pi pi-chevron-down sidebar-chevron" />
          </div>
          <transition name="slide">
            <div v-if="expandedGroups[item.key]" class="sidebar-group-children">
              <RouterLink
                v-for="child in item.children"
                :key="child.route"
                :to="child.route"
                class="sidebar-item sidebar-item--sub"
                active-class="sidebar-item--active"
              >
                <span :class="['pi', child.icon, 'sidebar-item-icon']" />
                <span class="sidebar-item-label">{{ child.label }}</span>
              </RouterLink>
            </div>
          </transition>
        </template>
        <!-- Simple link -->
        <RouterLink
          v-else
          :to="item.route!"
          class="sidebar-item"
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
import { computed, reactive } from 'vue'
import { useAuthStore } from '@/stores/authStore'
import { useRoute } from 'vue-router'

interface MenuItem {
  key: string
  label: string
  icon: string
  route?: string
  permission?: string
  children?: { label: string; icon: string; route: string; permission?: string }[]
}

const authStore = useAuthStore()
const route = useRoute()

const allMenuItems: MenuItem[] = [
  { key: 'dashboard',   label: 'Dashboard',           icon: 'pi-home',      route: '/',                 permission: 'VIEW_DASHBOARD' },
  { key: 'proj-settings', label: 'Cài đặt dự án',    icon: 'pi-folder',    route: '/project-settings', permission: 'MANAGE_PROJECT_SETTINGS' },
  { key: 'projects',    label: 'Quản Lý Các Dự Án',   icon: 'pi-briefcase', route: '/projects',         permission: 'MANAGE_PROJECT' },
  {
    key: 'settings',
    label: 'Cài đặt',
    icon: 'pi-cog',
    children: [
      { label: 'Quản lý Phân quyền', icon: 'pi-shield', route: '/admin/roles', permission: 'MANAGE_ROLE' },
      { label: 'Quản lý Người dùng', icon: 'pi-users',  route: '/admin/users', permission: 'MANAGE_USER' },
    ],
  },
  {
    key: 'handbook',
    label: 'Sổ tay trung tâm',
    icon: 'pi-book',
    children: [
      { label: 'Quản lý nhóm file', icon: 'pi-folder', route: '/handbook/file-groups', permission: 'MANAGE_HANDBOOK' },
      { label: 'Danh mục file',     icon: 'pi-file',   route: '/handbook/files',       permission: 'MANAGE_HANDBOOK' },
    ],
  },
]

// Auto-expand groups whose child route is active
const isChildActive = (item: MenuItem) =>
  item.children?.some(c => route.path === c.route || route.path.startsWith(c.route + '/'))

const expandedGroups = reactive<Record<string, boolean>>(
  Object.fromEntries(
    allMenuItems
      .filter(i => i.children)
      .map(i => [i.key, !!isChildActive(i)])
  )
)

const toggleGroup = (key: string) => {
  expandedGroups[key] = !expandedGroups[key]
}

const visibleMenuItems = computed(() =>
  allMenuItems
    .map(item => {
      if (item.children) {
        const visibleChildren = item.children.filter(
          c => !c.permission || authStore.hasPermission(c.permission)
        )
        return visibleChildren.length ? { ...item, children: visibleChildren } : null
      }
      return !item.permission || authStore.hasPermission(item.permission) ? item : null
    })
    .filter(Boolean) as MenuItem[]
)
</script>

<style scoped>
.app-sidebar {
  width: 260px;
  min-height: 100vh;
  background: linear-gradient(180deg, #0f172a 0%, #1e293b 100%);
  color: #f1f5f9;
  display: flex;
  flex-direction: column;
  border-right: 1px solid rgba(255, 255, 255, 0.06);
}

.sidebar-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1.5rem 1.25rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.sidebar-logo {
  font-size: 1.5rem;
  color: #f87171;
  filter: drop-shadow(0 0 8px rgba(248, 113, 113, 0.3));
}

.sidebar-title {
  font-size: 1.05rem;
  font-weight: 700;
  letter-spacing: -0.02em;
  background: linear-gradient(135deg, #fff 0%, #cbd5e1 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.sidebar-nav {
  display: flex;
  flex-direction: column;
  padding: 0.75rem 0.625rem;
  gap: 2px;
}

.sidebar-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.625rem 0.875rem;
  color: #94a3b8;
  text-decoration: none;
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 450;
  transition: all 0.2s ease;
}

.sidebar-item:hover {
  background: rgba(255, 255, 255, 0.06);
  color: #f1f5f9;
}

.sidebar-item--active {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%) !important;
  color: #fff !important;
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
  font-weight: 500;
}

.sidebar-item--sub {
  padding-left: 2.5rem;
  font-size: 0.825rem;
  color: #64748b;
}

.sidebar-item--sub:hover {
  color: #cbd5e1;
}

.sidebar-group-header {
  cursor: pointer;
  user-select: none;
  justify-content: flex-start;
}

.sidebar-chevron {
  margin-left: auto;
  font-size: 0.7rem;
  transition: transform 0.25s ease;
}

.sidebar-group-header--open .sidebar-chevron {
  transform: rotate(180deg);
}

.sidebar-group-children {
  display: flex;
  flex-direction: column;
  gap: 2px;
  overflow: hidden;
}

.slide-enter-active,
.slide-leave-active {
  transition: all 0.25s ease;
  max-height: 200px;
}

.slide-enter-from,
.slide-leave-to {
  max-height: 0;
  opacity: 0;
}

.sidebar-item-icon {
  font-size: 1rem;
  width: 1.25rem;
  text-align: center;
  opacity: 0.85;
}

.sidebar-item--active .sidebar-item-icon {
  opacity: 1;
}

.sidebar-item-label {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
