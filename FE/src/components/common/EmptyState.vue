<template>
  <div class="empty-state">
    <span :class="['pi', icon, 'empty-icon']" />
    <p class="empty-message">{{ message }}</p>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  icon?: string
  message: string
}>()
</script>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  padding: 3rem 2rem;
}

.empty-icon {
  font-size: 3rem;
  color: var(--p-text-muted-color, #9ca3af);
}

.empty-message {
  color: var(--p-text-muted-color, #6b7280);
  font-size: 0.95rem;
  text-align: center;
}
</style>
