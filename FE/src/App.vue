<template>
  <Toast />
  <LoadingState v-if="isCheckingHealth" text="Đang kết nối..." />
  <AppLayout v-else />
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import Toast from 'primevue/toast'
import AppLayout from './components/AppLayout.vue'
import LoadingState from './components/common/LoadingState.vue'
import { checkHealth } from './services/healthService'

const isCheckingHealth = ref(true)

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
