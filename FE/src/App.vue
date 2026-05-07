<template>
  <Toast />
  <ConfirmDialog />
  <LoadingState v-if="isCheckingHealth" text="Đang kết nối..." />
  <template v-else>
    <RouterView v-if="isAuthLayout" />
    <AppLayout v-else />
  </template>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import Toast from 'primevue/toast'
import ConfirmDialog from 'primevue/confirmdialog'
import AppLayout from './components/AppLayout.vue'
import LoadingState from './components/common/LoadingState.vue'
import { checkHealth } from './services/healthService'

const route = useRoute()
const isCheckingHealth = ref(true)

// Routes with meta.layout = 'auth' render without sidebar/header
const isAuthLayout = computed(() => route.meta.layout === 'auth')

onMounted(async () => {
  try {
    await checkHealth()
    // Silent success — no notification per FR-FE-007
  } catch {
    // Error handled by Axios interceptor (api.ts) — toast shown there
  } finally {
    isCheckingHealth.value = false
  }
})
</script>
