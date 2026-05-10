import { createRouter, createWebHistory } from 'vue-router'
import DashboardView from '@/views/DashboardView.vue'
import ProjectSettingsView from '@/views/ProjectSettingsView.vue'
import ProjectManagementView from '@/views/ProjectManagementView.vue'
import { useAuthStore } from '@/stores/authStore'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/auth/LoginView.vue'),
      meta: { layout: 'auth' }
    },
    {
      path: '/',
      name: 'dashboard',
      component: DashboardView,
      meta: { requiresAuth: true, permission: 'VIEW_DASHBOARD' }
    },
    {
      path: '/project-settings',
      name: 'project-settings',
      component: ProjectSettingsView,
      meta: { requiresAuth: true, permission: 'MANAGE_PROJECT_SETTINGS' }
    },
    {
      path: '/projects',
      name: 'projects',
      component: ProjectManagementView,
      meta: { requiresAuth: true, permission: 'MANAGE_PROJECT' }
    },
    {
      path: '/admin/roles',
      name: 'admin-roles',
      component: () => import('@/views/admin/roles/RoleListView.vue'),
      meta: { requiresAuth: true, permission: 'MANAGE_ROLE' }
    },
    {
      path: '/admin/users',
      name: 'admin-users',
      component: () => import('@/views/admin/users/UserListView.vue'),
      meta: { requiresAuth: true, permission: 'MANAGE_USER' }
    },
    {
      path: '/handbook/file-groups',
      name: 'handbook-file-groups',
      component: () => import('@/views/handbook/FileGroupView.vue'),
      meta: { requiresAuth: true, permission: 'MANAGE_HANDBOOK' }
    },
    {
      path: '/handbook/files',
      name: 'handbook-files',
      component: () => import('@/views/handbook/FileListView.vue'),
      meta: { requiresAuth: true, permission: 'MANAGE_HANDBOOK' }
    }
  ]
})

router.beforeEach((to) => {
  const auth = useAuthStore()

  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }

  if (to.name === 'login' && auth.isAuthenticated) {
    return { name: 'dashboard' }
  }

  if (to.meta.permission && !auth.hasPermission(to.meta.permission as string)) {
    return { name: 'dashboard' }
  }
})

export default router
