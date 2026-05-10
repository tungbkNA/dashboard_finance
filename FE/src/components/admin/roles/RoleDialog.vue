<template>
  <Dialog
    v-model:visible="visible"
    :header="isEdit ? 'Sửa Role' : 'Tạo Role Mới'"
    modal
    :style="{ width: '520px' }"
    @hide="$emit('close')"
  >
    <form @submit.prevent="handleSave" class="dialog-form pt-2">
      <div class="form-row">
        <label for="roleName">Tên Role <span class="text-red-500">*</span></label>
        <div class="form-input">
          <InputText
            id="roleName"
            v-model="form.roleName"
            :class="{ 'p-invalid': errors.roleName }"
            class="w-full"
            maxlength="100"
            placeholder="Nhập tên role"
          />
          <small v-if="errors.roleName" class="p-error">{{ errors.roleName }}</small>
        </div>
      </div>

      <div class="form-row">
        <label>Mã Role</label>
        <div class="form-input">
          <InputText
            :modelValue="generatedCode"
            class="w-full"
            disabled
            placeholder="Tự động sinh từ tên role"
          />
          <small style="color: var(--p-text-muted-color)">Mã tự động sinh: viết hoa tên, thay khoảng trắng bằng _</small>
        </div>
      </div>

      <div class="form-row">
        <label for="description">Mô tả</label>
        <div class="form-input">
          <Textarea
            id="description"
            v-model="form.description"
            :class="{ 'p-invalid': errors.description }"
            class="w-full"
            rows="3"
            maxlength="1000"
            placeholder="Nhập mô tả cho role"
          />
          <small v-if="errors.description" class="p-error">{{ errors.description }}</small>
        </div>
      </div>

      <div v-if="isEdit" class="form-row">
        <label for="active">Trạng thái</label>
        <div class="form-input">
          <div style="display: flex; align-items: center; gap: 0.5rem; padding-top: 0.35rem">
            <ToggleSwitch v-model="form.active" inputId="active" />
            <span>{{ form.active ? 'Active' : 'Inactive' }}</span>
          </div>
        </div>
      </div>

      <div style="display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 0.5rem">
        <Button label="Hủy" severity="secondary" text @click="$emit('close')" />
        <Button type="submit" label="Lưu" :loading="saving" />
      </div>
    </form>
  </Dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'
import Textarea from 'primevue/textarea'
import Button from 'primevue/button'
import ToggleSwitch from 'primevue/toggleswitch'
import type { Role } from '@/types/role'
import { roleService } from '@/services/roleService'

const props = defineProps<{ role: Role | null }>()
const emit = defineEmits<{ saved: []; close: [] }>()

const visible = ref(true)
const saving = ref(false)
const isEdit = ref(!!props.role)

const form = ref({ roleName: '', description: '', active: true })
const errors = ref({ roleName: '', description: '' })

const generatedCode = computed(() => {
  const name = form.value.roleName.trim()
  return name ? name.toUpperCase().replace(/\s+/g, '_') : ''
})

watch(() => props.role, (r) => {
  isEdit.value = !!r
  form.value.roleName = r?.roleName ?? ''
  form.value.description = r?.description ?? ''
  form.value.active = r?.active ?? true
}, { immediate: true })

function validate(): boolean {
  errors.value = { roleName: '', description: '' }
  if (!form.value.roleName.trim()) {
    errors.value.roleName = 'Tên role không được để trống'
    return false
  }
  return true
}

async function handleSave() {
  if (!validate()) return
  saving.value = true
  try {
    const payload: { roleName: string; description: string; active?: boolean } = {
      roleName: form.value.roleName,
      description: form.value.description
    }
    if (isEdit.value && props.role) {
      payload.active = form.value.active
      await roleService.updateRole(props.role.id, payload)
    } else {
      await roleService.createRole(payload)
    }
    emit('saved')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.dialog-form {
  display: flex;
  flex-direction: column;
  gap: 0.85rem;
}

.form-row {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
}

.form-row > label {
  width: 80px;
  min-width: 80px;
  padding-top: 0.55rem;
  font-weight: 500;
  text-align: right;
  font-size: 0.875rem;
}

.form-input {
  flex: 1;
}
</style>
