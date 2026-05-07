<template>
  <Dialog
    v-model:visible="visible"
    :header="isEdit ? 'Sửa bản ghi file' : 'Thêm bản ghi file mới'"
    modal
    :style="{ width: '520px' }"
    @hide="$emit('close')"
  >
    <form @submit.prevent="handleSave" class="dialog-form pt-2">
      <div class="form-row">
        <label for="fileName">Tên file <span class="text-red-500">*</span></label>
        <div class="form-input">
          <InputText
            id="fileName"
            v-model="form.fileName"
            :class="{ 'p-invalid': errors.fileName }"
            class="w-full"
            maxlength="200"
            placeholder="Nhập tên file"
          />
          <small v-if="errors.fileName" class="p-error">{{ errors.fileName }}</small>
        </div>
      </div>

      <div class="form-row">
        <label for="fileUrl">Link file <span class="text-red-500">*</span></label>
        <div class="form-input">
          <InputText
            id="fileUrl"
            v-model="form.fileUrl"
            :class="{ 'p-invalid': errors.fileUrl }"
            class="w-full"
            maxlength="2048"
            placeholder="https://..."
          />
          <small v-if="errors.fileUrl" class="p-error">{{ errors.fileUrl }}</small>
        </div>
      </div>

      <div class="form-row">
        <label for="groupId">Nhóm file <span class="text-red-500">*</span></label>
        <div class="form-input">
          <Dropdown
            id="groupId"
            v-model="form.groupId"
            :options="activeGroups"
            optionLabel="name"
            optionValue="id"
            :class="{ 'p-invalid': errors.groupId }"
            class="w-full"
            placeholder="Chọn nhóm file"
            :loading="loadingGroups"
          />
          <small v-if="errors.groupId" class="p-error">{{ errors.groupId }}</small>
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
import { ref, watch, onMounted } from 'vue'
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'
import Dropdown from 'primevue/dropdown'
import Button from 'primevue/button'
import type { FileRecordResponse, FileGroupActiveItem } from '@/types/handbook'
import { fileRecordService } from '@/services/fileRecordService'
import { fileGroupService } from '@/services/fileGroupService'

const props = defineProps<{ record: FileRecordResponse | null }>()
const emit = defineEmits<{ saved: []; close: [] }>()

const visible = ref(true)
const saving = ref(false)
const isEdit = ref(!!props.record)
const loadingGroups = ref(false)
const activeGroups = ref<FileGroupActiveItem[]>([])

const form = ref({ fileName: '', fileUrl: '', groupId: '' })
const errors = ref({ fileName: '', fileUrl: '', groupId: '' })

watch(() => props.record, (r) => {
  isEdit.value = !!r
  form.value.fileName = r?.fileName ?? ''
  form.value.fileUrl = r?.fileUrl ?? ''
  form.value.groupId = r?.groupId ?? ''
}, { immediate: true })

onMounted(async () => {
  loadingGroups.value = true
  try {
    activeGroups.value = await fileGroupService.getActive()
  } finally {
    loadingGroups.value = false
  }
})

function validate(): boolean {
  errors.value = { fileName: '', fileUrl: '', groupId: '' }
  let valid = true
  if (!form.value.fileName.trim()) {
    errors.value.fileName = 'Tên file không được để trống'
    valid = false
  }
  if (!form.value.fileUrl.trim()) {
    errors.value.fileUrl = 'Link file không được để trống'
    valid = false
  } else if (!/^https?:\/\//.test(form.value.fileUrl)) {
    errors.value.fileUrl = 'Link file phải bắt đầu bằng http:// hoặc https://'
    valid = false
  }
  if (!form.value.groupId) {
    errors.value.groupId = 'Vui lòng chọn nhóm file'
    valid = false
  }
  return valid
}

async function handleSave() {
  if (!validate()) return
  saving.value = true
  try {
    const request = {
      fileName: form.value.fileName,
      fileUrl: form.value.fileUrl,
      groupId: form.value.groupId
    }
    if (isEdit.value && props.record) {
      await fileRecordService.update(props.record.id, request)
    } else {
      await fileRecordService.create(request)
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
