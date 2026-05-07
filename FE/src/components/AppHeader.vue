<template>
  <header class="app-header">
    <div class="header-brand">
      <span class="pi pi-chart-bar header-icon" />
      <span class="header-title">Hệ Thống Quản Lý Dự Án Nội Bộ</span>
    </div>
    <div class="header-actions">
      <span class="header-version">v{{ version }}</span>
    </div>
  </header>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const version = ref('1.0.0')
</script>

<style scoped>
.app-header {
  height: 56px;
  background: var(--p-primary-600, #dc2626);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 1.5rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.header-brand {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-size: 1.1rem;
  font-weight: 600;
}

.header-icon {
  font-size: 1.3rem;
}

.header-actions {
  font-size: 0.8rem;
  opacity: 0.8;
}
</style>
