<template>
  <ConfirmDialog />
  <Dialog
    :visible="true"
    @update:visible="handleVisibilityChange"
    modal
    :dismissableMask="mode === 'view'"
    style="width: clamp(480px, 54vw, 740px)"
    :maximizable="true"
    :contentStyle="{ padding: '0' }"
  >
    <!-- Custom header -->
    <template #header>
      <div class="flex items-center gap-3">
        <i :class="mode === 'view' ? 'pi pi-eye text-blue-500' : 'pi pi-pencil text-orange-500'" class="text-lg" />
        <div>
          <div class="font-bold text-base">{{ record.projectCode }} — {{ record.projectName }}</div>
          <div class="text-xs text-gray-400 font-normal mt-0.5">
            {{ mode === 'view' ? 'Chế độ xem — chỉ đọc' : 'Chế độ chỉnh sửa' }}
          </div>
        </div>
      </div>
    </template>

    <!-- Body -->
    <div class="p-4 flex flex-col gap-2">
      <!-- Loading detail -->
      <div v-if="detailLoading" class="flex justify-center py-10">
        <ProgressSpinner style="width: 32px; height: 32px" />
      </div>
      <Message v-else-if="detailError" severity="error" :closable="false">{{ detailError }}</Message>

      <!-- 6 Attribute groups -->
      <template v-else>
        <div v-for="group in fieldMetadata.groups" :key="group.groupId">
          <Fieldset
            :legend="group.groupName"
            :toggleable="true"
            v-model:collapsed="groupCollapsed[group.groupId]"
            @toggle="onGroupToggle(group.groupId)"
          >
            <!-- Field rows -->
            <div class="divide-y divide-gray-100">
              <div
                v-for="field in groupAllFields[group.groupId]"
                :key="field"
                style="display: flex; align-items: center; gap: 0.75rem; padding: 0.5rem 1rem 0.5rem 4rem;"
              >
                <!-- Left: label + badges — fixed width, right-aligned text -->
                <div style="width: 220px; flex-shrink: 0; display: flex; align-items: center; gap: 0.4rem; justify-content: flex-end;">
                  <Tag
                    v-if="isFormulaField(group.groupId, field)"
                    value="Tự tính"
                    severity="secondary"
                    class="shrink-0 !text-xs !py-0.5"
                  />
                  <span
                    v-if="isCascadedField(group.groupId, field) && detail && !detail.isFirstMonth && !isFormulaField(group.groupId, field)"
                    class="text-xs text-blue-500 whitespace-nowrap"
                  ></span>
                  <span class="text-sm text-gray-600 text-right" style="white-space: nowrap;">{{ fieldLabel(field) }}</span>
                </div>

                <!-- Right: input fixed width -->
                <div style="width: 180px; flex-shrink: 0; display: flex; flex-direction: column;">
                  <InputNumber
                    :modelValue="fieldValue(group.groupId, field)"
                    @update:modelValue="onFieldInput(group.groupId, field, $event)"
                    :disabled="!isActivelyEditable(group.groupId, field)"
                    :inputClass="fieldInputClass(group.groupId, field)"
                    :useGrouping="false"
                    mode="decimal"
                    :maxFractionDigits="4"
                    :invalid="!!fieldErrors[field]"
                    class="w-full"
                    fluid
                  />
                  <small v-if="fieldErrors[field]" class="text-red-500 text-xs mt-0.5">{{ fieldErrors[field] }}</small>
                </div>
              </div>
            </div>

            <!-- Group footer: only in edit mode, only for groups with manual fields -->
            <div v-if="mode === 'edit' && group.manualFields.length > 0"
                 class="flex justify-end gap-2 mt-3 pt-2 border-t border-gray-100">
              <template v-if="activeEditGroup === group.groupId">
                <Button label="Hủy" severity="secondary" outlined size="small" icon="pi pi-times"
                        :disabled="saving" @click="cancelEdit(group.groupId)" />
                <Button label="Lưu" size="small" icon="pi pi-save" :loading="saving"
                        @click="saveGroup(group.groupId)" />
              </template>
              <template v-else-if="detail && !detailLoading">
                <Button label="Sửa" severity="secondary" outlined size="small" icon="pi pi-pencil"
                        @click="startEdit(group.groupId)" />
              </template>
            </div>
          </Fieldset>
        </div>
      </template>
    </div>
  </Dialog>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import Dialog from 'primevue/dialog'
import Fieldset from 'primevue/fieldset'
import InputNumber from 'primevue/inputnumber'
import Button from 'primevue/button'
import Tag from 'primevue/tag'
import Message from 'primevue/message'
import ProgressSpinner from 'primevue/progressspinner'
import ConfirmDialog from 'primevue/confirmdialog'
import { useConfirm } from 'primevue/useconfirm'
import { useToast } from 'primevue/usetoast'
import { projectMonthlyRecordService } from '@/services/projectMonthlyRecordService'
import type {
  ProjectMonthRecordDetail,
  ProjectMonthRecordUpdateRequest,
  FieldMetadata
} from '@/types/project-monthly-record'

// ---- Props & Emits ----

const props = defineProps<{
  record: ProjectMonthRecordDetail
  fieldMetadata: FieldMetadata
  mode: 'view' | 'edit'
}>()

const emits = defineEmits<{
  close: []
  saved: []
}>()

// ---- Composables ----

const confirm = useConfirm()
const toast = useToast()

// ---- Ordered field list per group ----

const GROUP_ALL_FIELDS: Record<string, string[]> = {
  g1: ['g1RaTon', 'g1SlsxTonTuSxHd', 'g1SlsxTonTuSxHtHd', 'g1SlsxTonTuSxDdHd', 'g1SlsxOsTon', 'g1SlsxOsTonHt'],
  g2: ['g2Headcount', 'g2Ra', 'g2SlsxTuSx', 'g2SlsxOs', 'g2LienKet', 'g2TongSlsxDuKien',
       'g2SlsxTuSxHtTrongThang', 'g2SlsxTuSxDd', 'g2SlsxOsHt', 'g2SlsxOsDd', 'g2Cpbqtb', 'g2TySuatLng'],
  g3: ['g3Ra', 'g3TongSlsxHd', 'g3Ee', 'g3SlsxTuSxHt', 'g3SlsxTuSxDd', 'g3SlsxOsDd', 'g3SlsxOsTonHt'],
  g4: ['g4TuSlsxTonHt', 'g4TuSlsxTrongThang', 'g4SlsxOsTon', 'g4SlsxOsTrongThang', 'g4Lk',
       'g4Tong', 'g4DoanhThu', 'g4TiSuatLngDuKien', 'g4LngDuKien'],
  g5: ['g5RaTuongUngSlnt', 'g5NtSlsxTonHt', 'g5NtSlsxTrongThang', 'g5NtSlsxOsTon',
       'g5NtSlsxOsTrongThang', 'g5TongSlnt', 'g5DoanhThu', 'g5TiSuatLng', 'g5LngVnd'],
  g6: ['g6RaTon', 'g6SlsxTonHt', 'g6SlsxTonDd', 'g6SlsxOsTon', 'g6SlsxOsTonHt', 'g6SlsxTon']
}

// ---- Field labels (Vietnamese) ----

const FIELD_LABELS: Record<string, string> = {
  g1RaTon: 'Ra tồn', g1SlsxTonTuSxHd: 'SLSX tồn từ SX (HĐ)',
  g1SlsxTonTuSxHtHd: 'SLSX tồn từ SX HT (HĐ)', g1SlsxTonTuSxDdHd: 'SLSX tồn từ SX DD (HĐ)',
  g1SlsxOsTon: 'SLSX OS tồn', g1SlsxOsTonHt: 'SLSX OS tồn HT',
  g2Headcount: 'Headcount', g2Ra: 'Ra', g2SlsxTuSx: 'SLSX tự SX', g2SlsxOs: 'SLSX OS',
  g2LienKet: 'Liên kết', g2TongSlsxDuKien: 'Tổng SLSX dự kiến',
  g2SlsxTuSxHtTrongThang: 'SLSX tự SX HT trong tháng', g2SlsxTuSxDd: 'SLSX tự SX DD',
  g2SlsxOsHt: 'SLSX OS HT', g2SlsxOsDd: 'SLSX OS DD', g2Cpbqtb: 'CPBQTB', g2TySuatLng: 'Tỷ suất LNG',
  g3Ra: 'Ra', g3TongSlsxHd: 'Tổng SLSX (HĐ)', g3Ee: 'EE', g3SlsxTuSxHt: 'SLSX tự SX HT',
  g3SlsxTuSxDd: 'SLSX tự SX DD', g3SlsxOsDd: 'SLSX OS DD', g3SlsxOsTonHt: 'SLSX OS tồn HT',
  g4TuSlsxTonHt: 'Từ SLSX tồn HT', g4TuSlsxTrongThang: 'Từ SLSX trong tháng',
  g4SlsxOsTon: 'SLSX OS tồn', g4SlsxOsTrongThang: 'SLSX OS trong tháng',
  g4Lk: 'Liên kết', g4Tong: 'Tổng', g4DoanhThu: 'Doanh thu',
  g4TiSuatLngDuKien: 'Tỷ suất LNG dự kiến', g4LngDuKien: 'LNG dự kiến',
  g5RaTuongUngSlnt: 'Ra tương ứng SLNT', g5NtSlsxTonHt: 'NT SLSX tồn HT',
  g5NtSlsxTrongThang: 'NT SLSX trong tháng', g5NtSlsxOsTon: 'NT SLSX OS tồn',
  g5NtSlsxOsTrongThang: 'NT SLSX OS trong tháng', g5TongSlnt: 'Tổng SLNT',
  g5DoanhThu: 'Doanh thu', g5TiSuatLng: 'Tỷ suất LNG', g5LngVnd: 'LNG (VND)',
  g6RaTon: 'Ra tồn', g6SlsxTonHt: 'SLSX tồn HT', g6SlsxTonDd: 'SLSX tồn DD',
  g6SlsxOsTon: 'SLSX OS tồn', g6SlsxOsTonHt: 'SLSX OS tồn HT',
  g6SlsxTon: 'SLSX tồn'
}

const groupAllFields = GROUP_ALL_FIELDS

// ---- Field classification helpers ----

function groupMeta(groupId: string) {
  return props.fieldMetadata.groups.find(g => g.groupId === groupId)
}

function isFormulaField(groupId: string, field: string): boolean {
  return groupMeta(groupId)?.formulaFields.includes(field) ?? false
}

function isCascadedField(groupId: string, field: string): boolean {
  return groupMeta(groupId)?.cascadedFromPrevMonthFields.includes(field) ?? false
}

function isEditableInContext(groupId: string, field: string): boolean {
  if (isFormulaField(groupId, field)) return false
  if (isCascadedField(groupId, field) && detail.value && !detail.value.isFirstMonth) return false
  return true
}

/** True only when the user can actively type into this field right now */
function isActivelyEditable(groupId: string, field: string): boolean {
  if (props.mode === 'view') return false
  if (activeEditGroup.value !== groupId) return false
  return isEditableInContext(groupId, field)
}

/** Current display value: edit-form > live formula > saved detail */
function fieldValue(groupId: string, field: string): number | null {
  if (activeEditGroup.value === groupId && groupForms[groupId] && field in groupForms[groupId]) {
    return groupForms[groupId][field] ?? null
  }
  if (isFormulaField(groupId, field) && activeEditGroup.value) {
    return (liveFormulas.value[field] as number | null) ?? null
  }
  if (detail.value) {
    return (detail.value[field as keyof ProjectMonthRecordDetail] as number | null) ?? null
  }
  return null
}

function onFieldInput(groupId: string, field: string, val: number | null) {
  if (isActivelyEditable(groupId, field) && groupForms[groupId]) {
    groupForms[groupId][field] = val
  }
}

/** Input CSS: sky tint for formula, plain for manual */
function fieldInputClass(groupId: string, field: string): string {
  if (isFormulaField(groupId, field)) {
    return 'text-right w-full !bg-sky-50 !text-sky-800'
  }
  return 'text-right w-full'
}

function fieldLabel(field: string): string {
  return FIELD_LABELS[field] ?? field
}

// ---- Live formula calculation ----

function liveVal(field: string): number {
  const eg = activeEditGroup.value
  if (eg && groupForms[eg] && field in groupForms[eg]) {
    return groupForms[eg][field] ?? 0
  }
  if (detail.value) {
    const v = detail.value[field as keyof ProjectMonthRecordDetail]
    return (v as number | null) ?? 0
  }
  return 0
}

const liveFormulas = computed<Record<string, number | null>>(() => {
  if (!detail.value) return {} as Record<string, number | null>

  const g2TongSlsxDuKien = liveVal('g2SlsxTuSx') + liveVal('g2SlsxOs') + liveVal('g2LienKet')

  const g3Ra = liveVal('g3Ra')
  const g3Ee: number | null = g3Ra === 0
    ? null
    : Math.round((liveVal('g3TongSlsxHd') / g3Ra) * 100 * 100) / 100

  const g4Tong = liveVal('g4TuSlsxTonHt') + liveVal('g4TuSlsxTrongThang') +
    liveVal('g4SlsxOsTon') + liveVal('g4SlsxOsTrongThang') + liveVal('g4Lk')

  const price = (detail.value.price as number | null) ?? 0
  const g4DoanhThu = Math.round(g4Tong * price)

  const g5TongSlnt = liveVal('g5NtSlsxTonHt') + liveVal('g5NtSlsxTrongThang') +
    liveVal('g5NtSlsxOsTon') + liveVal('g5NtSlsxOsTrongThang')

  const g5DoanhThu = Math.round(g5TongSlnt * price)

  const g6RaTon = liveVal('g1RaTon') + liveVal('g3Ra') - liveVal('g5RaTuongUngSlnt')

  const g6SlsxTonHt = liveVal('g1SlsxTonTuSxHtHd') + liveVal('g3SlsxTuSxHt') -
    liveVal('g5NtSlsxTonHt') - liveVal('g5NtSlsxOsTrongThang')

  const g6SlsxTonDd = liveVal('g1SlsxTonTuSxDdHd') + liveVal('g3SlsxTuSxDd')

  const g6SlsxOsTon = liveVal('g1SlsxOsTon') + liveVal('g3SlsxOsDd')

  const g6SlsxOsTonHt = liveVal('g1SlsxOsTonHt') + liveVal('g3SlsxOsTonHt') -
    liveVal('g5NtSlsxOsTon') - liveVal('g5NtSlsxOsTrongThang')

  const g6SlsxTon = g6SlsxTonHt + g6SlsxTonDd + g6SlsxOsTon + g6SlsxOsTonHt

  return {
    g2TongSlsxDuKien, g3Ee, g4Tong, g4DoanhThu,
    g5TongSlnt, g5DoanhThu,
    g6RaTon, g6SlsxTonHt, g6SlsxTonDd, g6SlsxOsTon, g6SlsxOsTonHt, g6SlsxTon
  }
})

// ---- Detail loading ----

const detail = ref<ProjectMonthRecordDetail | null>(null)
const detailLoading = ref(false)
const detailError = ref<string | null>(null)
const detailLoaded = ref(false)

async function loadDetail() {
  if (detailLoaded.value) return
  detailLoading.value = true
  detailError.value = null
  try {
    const res = await projectMonthlyRecordService.getById(props.record.id)
    detail.value = res.data ?? null
    detailLoaded.value = true
  } catch {
    detailError.value = 'Không thể tải chi tiết bản ghi'
  } finally {
    detailLoading.value = false
  }
}

// ---- Group collapse state (all start expanded in modal) ----

const groupCollapsed = reactive<Record<string, boolean>>(
  Object.fromEntries(props.fieldMetadata.groups.map(g => [g.groupId, false]))
)

function onGroupToggle(groupId: string) {
  if (!groupCollapsed[groupId]) loadDetail()
}

// ---- Edit state ----

const activeEditGroup = ref<string | null>(null)
const groupForms = reactive<Record<string, Record<string, number | null>>>({})
const groupSnapshots = reactive<Record<string, Record<string, number | null>>>({})
const saving = ref(false)
const fieldErrors = reactive<Record<string, string>>({})

function startEdit(groupId: string) {
  if (activeEditGroup.value && activeEditGroup.value !== groupId) {
    confirm.require({
      message: 'Bạn đang chỉnh sửa nhóm khác. Lưu hoặc hủy trước khi tiếp tục.',
      header: 'Chỉnh sửa đang mở',
      icon: 'pi pi-exclamation-triangle',
      rejectProps: { label: 'Đóng', severity: 'secondary', outlined: true },
      acceptProps: { label: 'OK', severity: 'primary' },
      accept: () => {}
    })
    return
  }
  const meta = groupMeta(groupId)
  if (!meta || !detail.value) return
  const d = detail.value
  const fields = d.isFirstMonth
    ? [...meta.manualFields, ...meta.cascadedFromPrevMonthFields]
    : [...meta.manualFields]
  const snapshot: Record<string, number | null> = {}
  for (const f of fields) {
    snapshot[f] = (d[f as keyof ProjectMonthRecordDetail] as number | null) ?? null
  }
  groupSnapshots[groupId] = { ...snapshot }
  groupForms[groupId] = { ...snapshot }
  activeEditGroup.value = groupId
}

function cancelEdit(groupId: string) {
  if (groupSnapshots[groupId]) {
    groupForms[groupId] = { ...groupSnapshots[groupId] }
  }
  activeEditGroup.value = null
  clearFieldErrors()
}

function clearFieldErrors() {
  for (const key of Object.keys(fieldErrors)) {
    delete fieldErrors[key]
  }
}

// ---- Build request ----

function buildFullRequest(): ProjectMonthRecordUpdateRequest {
  const req: Record<string, number | null> = {}
  const isFirst = detail.value?.isFirstMonth ?? false
  for (const group of props.fieldMetadata.groups) {
    const isEditing = activeEditGroup.value === group.groupId
    const source = isEditing ? groupForms[group.groupId] : detail.value
    if (!source) continue
    for (const field of group.manualFields) {
      req[field] = (source as Record<string, number | null>)[field] ?? null
    }
    if (isFirst) {
      for (const field of group.cascadedFromPrevMonthFields) {
        req[field] = (source as Record<string, number | null>)[field] ?? null
      }
    }
  }
  return req as ProjectMonthRecordUpdateRequest
}

// ---- Overwrite check ----

function hasExistingDataInGroup(groupId: string): boolean {
  if (!detail.value) return false
  const meta = groupMeta(groupId)
  if (!meta) return false
  for (const field of meta.manualFields) {
    const val = detail.value[field as keyof ProjectMonthRecordDetail]
    if (val !== null && val !== undefined) return true
  }
  return false
}

// ---- Save ----

async function doSave() {
  saving.value = true
  clearFieldErrors()
  try {
    const req = buildFullRequest()
    const res = await projectMonthlyRecordService.update(props.record.id, req)
    detail.value = res.data ?? null
    activeEditGroup.value = null
    // T023: show count of affected months if any
    const affected = res.data?.affectedMonths ?? 0
    const detail_msg = affected > 0
      ? `Lưu thành công. Đã cập nhật thêm ${affected} tháng liên quan.`
      : 'Lưu thành công.'
    toast.add({ severity: 'success', summary: 'Đã lưu', detail: detail_msg, life: 3000 })
    emits('saved')
  } catch (err: unknown) {
    // T024: on error, show BE message and do NOT update local detail data
    const axiosErr = err as { response?: { status?: number; data?: { message?: string; errors?: Record<string, string> } } }
    if (axiosErr?.response?.status === 422 && axiosErr.response.data?.errors) {
      for (const [field, msg] of Object.entries(axiosErr.response.data.errors)) {
        fieldErrors[field] = msg
      }
      toast.add({ severity: 'warn', summary: 'Lỗi dữ liệu', detail: 'Vui lòng kiểm tra lại các trường', life: 4000 })
    } else {
      const beMessage = axiosErr?.response?.data?.message ?? 'Không thể lưu, vui lòng thử lại'
      toast.add({ severity: 'error', summary: 'Lỗi', detail: beMessage, life: 4000 })
    }
    // T024: detail.value is intentionally NOT updated on error
  } finally {
    saving.value = false
  }
}

function saveGroup(groupId: string) {
  if (hasExistingDataInGroup(groupId)) {
    confirm.require({
      message: 'Thay đổi này có thể sẽ làm thay đổi các nhóm giá trị trong các tháng khác',
      header: 'Xác nhận thay đổi',
      icon: 'pi pi-exclamation-triangle',
      rejectProps: { label: 'Hủy', severity: 'secondary', outlined: true },
      acceptProps: { label: 'Xác nhận', severity: 'warning' },
      accept: () => doSave(),
      reject: () => {}
    })
  } else {
    doSave()
  }
}

// ---- Dialog close ----

function handleVisibilityChange(val: boolean) {
  if (!val) {
    if (activeEditGroup.value) cancelEdit(activeEditGroup.value)
    emits('close')
  }
}

// ---- Init ----

onMounted(loadDetail)
</script>
