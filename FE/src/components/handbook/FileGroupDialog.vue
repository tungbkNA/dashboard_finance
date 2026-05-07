<template>
  <Dialog
    v-model:visible="visible"
    :header="isEdit ? 'Sửa nhóm file' : 'Tạo nhóm file mới'"
    modal
    :style="{ width: '480px' }"
    @hide="$emit('close')"
  >
    <form @submit.prevent="handleSave" class="dialog-form pt-2">
      <div class="form-row">
        <label for="name">Tên nhóm <span class="text-red-500">*</span></label>
        <div class="form-input">
          <InputText
            id="name"
            v-model="form.name"
            :class="{ 'p-invalid': errors.name }"
            class="w-full"
            maxlength="100"
            placeholder="Nhập tên nhóm file"
          />
          <small v-if="errors.name" class="p-error">{{ errors.name }}</small>
        </div>
      </div>

      <div class="form-row">
        <label for="description">Mô tả</label>
        <div class="form-input">
          <Textarea
            id="description"
            v-model="form.description"
            class="w-full"
            rows="3"
            maxlength="255"
            placeholder="Nhập mô tả nhóm file"
          />
        </div>
      </div>

      <div v-if="isEdit" class="form-row">
        <label for="active">Trạng thái</label>
        <div class="form-input">
          <ToggleButton
            id="active"
            v-model="form.active"
            onLabel="Active"
            offLabel="Inactive"
            onIcon="pi pi-check"
            offIcon="pi pi-times"
          />
        </div>
      </div>

      <div class="flex justify-end gap-2 mt-2">
        <Button label="Hủy" severity="secondary" text @click="$emit('close')" />
        <Button type="submit" label="Lưu" :loading="saving" />
      </div>
    </form>
  </Dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'
import Textarea from 'primevue/textarea'
import Button from 'primevue/button'
import ToggleButton from 'primevue/togglebutton'
import type { FileGroupResponse } from '@/types/handbook'
import { fileGroupService } from '@/services/fileGroupService'

const props = defineProps<{ group: FileGroupResponse | null }>()
const emit = defineEmits<{ saved: []; close: [] }>()

const visible = ref(true)
const saving = ref(false)
const isEdit = ref(!!props.group)

const form = ref({ name: '', description: '', active: true })
const errors = ref({ name: '' })

watch(() => props.group, (g) => {
  isEdit.value = !!g
  form.value.name = g?.name ?? ''
  form.value.description = g?.description ?? ''
  form.value.active = g?.active ?? true
}, { immediate: true })

function validate(): boolean {
  errors.value = { name: '' }
  if (!form.value.name.trim()) {
    errors.value.name = 'Tên nhóm file không được để trống'
    return false
  }
  return true
}

async function handleSave() {
  if (!validate()) return
  saving.value = true
  try {
    if (isEdit.value && props.group) {
      await fileGroupService.update(props.group.id, {
        name: form.value.name,
        description: form.value.description || undefined,
        active: form.value.active
      })
    } else {
      await fileGroupService.create({
        name: form.value.name,
        description: form.value.description || undefined
      })
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
  width: 100px;
  min-width: 100px;
  padding-top: 0.55rem;
  font-weight: 500;
  text-align: right;
  font-size: 0.875rem;
}

.form-input {
  flex: 1;
}
</style>
