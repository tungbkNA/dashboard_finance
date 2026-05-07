<template>
  <div class="error-state">
    <span class="pi pi-exclamation-triangle error-icon" />
    <p class="error-message">{{ message }}</p>
    <Button
      v-if="showRetry"
      label="Thử lại"
      icon="pi pi-refresh"
      severity="danger"
      outlined
      @click="emit('retry')"
    />
  </div>
</template>

<script setup lang="ts">
import Button from 'primevue/button'

defineProps<{
  message: string
  showRetry?: boolean
}>()

const emit = defineEmits<{
  retry: []
}>()
</script>

<style scoped>
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  padding: 3rem 2rem;
}

.error-icon {
  font-size: 3rem;
  color: var(--p-red-500, #ef4444);
}

.error-message {
  color: var(--p-text-color, #374151);
  font-size: 0.95rem;
  text-align: center;
}
</style>
