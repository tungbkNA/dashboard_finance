import { createRouter, createWebHistory } from 'vue-router'
import DashboardView from '@/views/DashboardView.vue'
import ProjectSettingsView from '@/views/ProjectSettingsView.vue'
import ProjectManagementView from '@/views/ProjectManagementView.vue'
import ConfigView from '@/views/ConfigView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'dashboard',
      component: DashboardView
    },
    {
      path: '/project-settings',
      name: 'project-settings',
      component: ProjectSettingsView
    },
    {
      path: '/projects',
      name: 'projects',
      component: ProjectManagementView
    },
    {
      path: '/config',
      name: 'config',
      component: ConfigView
    }
  ]
})

export default router
