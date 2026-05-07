<template>
  <div class="login-page">
    <LoginForm />
  </div>
</template>

<script setup lang="ts">
import LoginForm from '@/components/auth/LoginForm.vue'
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: var(--p-surface-100, #f4f4f5);
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
