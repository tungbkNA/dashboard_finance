<template>
  <div class="loading-state">
    <ProgressSpinner
      style="width: 40px; height: 40px"
      strokeWidth="4"
    />
    <p v-if="text" class="loading-text">{{ text }}</p>
  </div>
</template>

<script setup lang="ts">
import ProgressSpinner from 'primevue/progressspinner'

defineProps<{
  text?: string
}>()
</script>

<style scoped>
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  padding: 2rem;
}

.loading-text {
  color: var(--p-text-muted-color, #6b7280);
  font-size: 0.9rem;
}
</style>
