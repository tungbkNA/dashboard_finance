<template>
  <Dialog v-model:visible="visible" modal :style="{ width: '900px' }"
          :header="`Bản ghi tháng ${props.monthKey} — ${record?.projectName ?? ''}`"
          :draggable="false" @hide="onHide">

    <!-- Loading -->
    <div v-if="loading" class="flex justify-center py-10">
      <ProgressSpinner style="width: 40px; height: 40px" />
    </div>

    <!-- Error -->
    <Message v-else-if="loadError" severity="error" :closable="false">{{ loadError }}</Message>

    <template v-else-if="record">
      <form @submit.prevent="handleSubmit" class="dialog-form pt-2">

        <!-- G1 — Tồn đầu kỳ -->
        <Fieldset legend="G1 — Tồn đầu kỳ" :toggleable="true">
          <div class="form-grid">
            <div class="form-row">
              <label>Ra Tồn</label>
              <div class="form-input">
                <InputNumber v-model="form.g1RaTon" class="w-full"
                             :disabled="!record.isFirstMonth"
                             :use-grouping="false" mode="decimal" :max-fraction-digits="4" placeholder="Tự động" />
              </div>
            </div>
            <div class="form-row">
              <label>SLSX Tồn TXSX Ht HĐ</label>
              <div class="form-input">
                <InputNumber v-model="form.g1SlsxTonTuSxHtHd" class="w-full"
                             :disabled="!record.isFirstMonth"
                             :use-grouping="false" mode="decimal" :max-fraction-digits="4" placeholder="Tự động" />
              </div>
            </div>
            <div class="form-row">
              <label>SLSX Tồn TXSX Dở dang HĐ</label>
              <div class="form-input">
                <InputNumber v-model="form.g1SlsxTonTuSxDdHd" class="w-full"
                             :disabled="!record.isFirstMonth"
                             :use-grouping="false" mode="decimal" :max-fraction-digits="4" placeholder="Tự động" />
              </div>
            </div>
            <div class="form-row">
              <label>SLSX Tồn TXSX HĐ</label>
              <div class="form-input">
                <InputNumber v-model="form.g1SlsxTonTuSxHd" class="w-full"
                             :use-grouping="false" mode="decimal" :max-fraction-digits="4" />
              </div>
            </div>
            <div class="form-row">
              <label>SLSX OS Tồn</label>
              <div class="form-input">
                <InputNumber v-model="form.g1SlsxOsTon" class="w-full"
                             :disabled="!record.isFirstMonth"
                             :use-grouping="false" mode="decimal" :max-fraction-digits="4" placeholder="Tự động" />
              </div>
            </div>
            <div class="form-row">
              <label>SLSX OS Tồn Ht</label>
              <div class="form-input">
                <InputNumber v-model="form.g1SlsxOsTonHt" class="w-full"
                             :disabled="!record.isFirstMonth"
                             :use-grouping="false" mode="decimal" :max-fraction-digits="4" placeholder="Tự động" />
              </div>
            </div>
          </div>
        </Fieldset>

        <!-- G2 — Kế hoạch SLSX -->
        <Fieldset legend="G2 — Kế hoạch tháng" :toggleable="true">
          <div class="form-grid">
            <div class="form-row">
              <label>Headcount</label>
              <div class="form-input">
                <InputNumber v-model="form.g2Headcount" class="w-full"
                             :use-grouping="false" mode="decimal" :max-fraction-digits="4" />
              </div>
            </div>
            <div class="form-row">
              <label>Ra</label>
              <div class="form-input">
                <InputNumber v-model="form.g2Ra" class="w-full"
                             :use-grouping="false" mode="decimal" :max-fraction-digits="4" />
              </div>
            </div>
            <div class="form-row">
              <label>SLSX Tự SX</label>
              <div class="form-input">
                <InputNumber v-model="form.g2SlsxTuSx" class="w-full"
                             :use-grouping="false" mode="decimal" :max-fraction-digits="4" />
              </div>
            </div>
            <div class="form-row">
              <label>SLSX OS</label>
              <div class="form-input">
                <InputNumber v-model="form.g2SlsxOs" class="w-full"
                             :use-grouping="false" mode="decimal" :max-fraction-digits="4" />
              </div>
            </div>
            <div class="form-row">
              <label>Liên kết</label>
              <div class="form-input">
                <InputNumber v-model="form.g2LienKet" class="w-full"
                             :use-grouping="false" mode="decimal" :max-fraction-digits="4" />
              </div>
            </div>
            <div class="form-row">
              <label class="text-gray-500">Tổng SLSX Dự kiến (CT)</label>
              <div class="form-input">
                <InputNumber :model-value="record.g2TongSlsxDuKien" class="w-full" disabled
                             :use-grouping="false" mode="decimal" :max-fraction-digits="4" />
              </div>
            </div>
            <div class="form-row">
              <label>SLSX Tự SX HT trong tháng</label>
              <div class="form-input">
                <InputNumber v-model="form.g2SlsxTuSxHtTrongThang" class="w-full"
                             :use-grouping="false" mode="decimal" :max-fraction-digits="4" />
              </div>
            </div>
            <div class="form-row">
              <label>SLSX Tự SX DD</label>
              <div class="form-input">
                <InputNumber v-model="form.g2SlsxTuSxDd" class="w-full"
                             :use-grouping="false" mode="decimal" :max-fraction-digits="4" />
              </div>
            </div>
            <div class="form-row">
              <label>SLSX OS HT</label>
              <div class="form-input">
                <InputNumber v-model="form.g2SlsxOsHt" class="w-full"
                             :use-grouping="false" mode="decimal" :max-fraction-digits="4" />
              </div>
            </div>
            <div class="form-row">
              <label>SLSX OS DD</label>
              <div class="form-input">
                <InputNumber v-model="form.g2SlsxOsDd" class="w-full"
                             :use-grouping="false" mode="decimal" :max-fraction-digits="4" />
              </div>
            </div>
            <div class="form-row">
              <label>CPBQTB</label>
              <div class="form-input">
                <InputNumber v-model="form.g2Cpbqtb" class="w-full"
                             :use-grouping="false" mode="decimal" :max-fraction-digits="4" />
              </div>
            </div>
            <div class="form-row">
              <label>Tỷ suất LNG</label>
              <div class="form-input">
                <InputNumber v-model="form.g2TySuatLng" class="w-full"
                             :use-grouping="false" mode="decimal" :max-fraction-digits="4" />
              </div>
            </div>
          </div>
        </Fieldset>

        <!-- G3 — Thực tế SX -->
        <Fieldset legend="G3 — Thực tế SX" :toggleable="true">
          <div class="form-grid">
            <div class="form-row">
              <label>Ra</label>
              <div class="form-input">
                <InputNumber v-model="form.g3Ra" class="w-full"
                             :use-grouping="false" mode="decimal" :max-fraction-digits="4" />
              </div>
            </div>
            <div class="form-row">
              <label>Tổng SLSX HĐ</label>
              <div class="form-input">
                <InputNumber v-model="form.g3TongSlsxHd" class="w-full"
                             :use-grouping="false" mode="decimal" :max-fraction-digits="4" />
              </div>
            </div>
            <div class="form-row">
              <label class="text-gray-500">EE % (CT)</label>
              <div class="form-input">
                <InputNumber :model-value="record.g3Ee" class="w-full" disabled
                             :use-grouping="false" mode="decimal" :max-fraction-digits="2" suffix="%" />
              </div>
            </div>
            <div class="form-row">
              <label>SLSX TXSX Ht</label>
              <div class="form-input">
                <InputNumber v-model="form.g3SlsxTuSxHt" class="w-full"
                             :use-grouping="false" mode="decimal" :max-fraction-digits="4" />
              </div>
            </div>
            <div class="form-row">
              <label>SLSX TXSX DD</label>
              <div class="form-input">
                <InputNumber v-model="form.g3SlsxTuSxDd" class="w-full"
                             :use-grouping="false" mode="decimal" :max-fraction-digits="4" />
              </div>
            </div>
            <div class="form-row">
              <label>SLSX OS DD</label>
              <div class="form-input">
                <InputNumber v-model="form.g3SlsxOsDd" class="w-full"
                             :use-grouping="false" mode="decimal" :max-fraction-digits="4" />
              </div>
            </div>
            <div class="form-row">
              <label>SLSX OS Tồn Ht</label>
              <div class="form-input">
                <InputNumber v-model="form.g3SlsxOsTonHt" class="w-full"
                             :use-grouping="false" mode="decimal" :max-fraction-digits="4" />
              </div>
            </div>
          </div>
        </Fieldset>

        <!-- G4 — Xuất kho TP -->
        <Fieldset legend="G4 — Xuất kho TP" :toggleable="true">
          <div class="form-grid">
            <div class="form-row">
              <label>Từ SLSX Tồn Ht</label>
              <div class="form-input">
                <InputNumber v-model="form.g4TuSlsxTonHt" class="w-full"
                             :use-grouping="false" mode="decimal" :max-fraction-digits="4" />
              </div>
            </div>
            <div class="form-row">
              <label>Từ SLSX trong tháng</label>
              <div class="form-input">
                <InputNumber v-model="form.g4TuSlsxTrongThang" class="w-full"
                             :use-grouping="false" mode="decimal" :max-fraction-digits="4" />
              </div>
            </div>
            <div class="form-row">
              <label>SLSX OS Tồn</label>
              <div class="form-input">
                <InputNumber v-model="form.g4SlsxOsTon" class="w-full"
                             :use-grouping="false" mode="decimal" :max-fraction-digits="4" />
              </div>
            </div>
            <div class="form-row">
              <label>SLSX OS trong tháng</label>
              <div class="form-input">
                <InputNumber v-model="form.g4SlsxOsTrongThang" class="w-full"
                             :use-grouping="false" mode="decimal" :max-fraction-digits="4" />
              </div>
            </div>
            <div class="form-row">
              <label>Liên kết</label>
              <div class="form-input">
                <InputNumber v-model="form.g4Lk" class="w-full"
                             :use-grouping="false" mode="decimal" :max-fraction-digits="4" />
              </div>
            </div>
            <div class="form-row">
              <label class="text-gray-500">Tổng (CT)</label>
              <div class="form-input">
                <InputNumber :model-value="record.g4Tong" class="w-full" disabled
                             :use-grouping="false" mode="decimal" :max-fraction-digits="4" />
              </div>
            </div>
            <div class="form-row">
              <label>Tỉ suất LNG dự kiến (%)</label>
              <div class="form-input">
                <InputNumber v-model="form.g4TiSuatLngDuKien" class="w-full"
                             :use-grouping="false" mode="decimal" :max-fraction-digits="4" suffix="%" />
              </div>
            </div>
            <div class="form-row">
              <label>LNG dự kiến</label>
              <div class="form-input">
                <InputNumber v-model="form.g4LngDuKien" class="w-full"
                             :use-grouping="false" mode="decimal" :max-fraction-digits="0" />
              </div>
            </div>
            <div class="form-row">
              <label class="text-gray-500">Doanh thu (CT)</label>
              <div class="form-input">
                <InputNumber :model-value="record.g4DoanhThu" class="w-full" disabled
                             :use-grouping="false" mode="decimal" :max-fraction-digits="0" />
              </div>
            </div>
          </div>
        </Fieldset>

        <!-- G5 — Xuất nhập TP -->
        <Fieldset legend="G5 — Xuất nhập TP" :toggleable="true">
          <div class="form-grid">
            <div class="form-row">
              <label>NT SLSX Tồn Ht</label>
              <div class="form-input">
                <InputNumber v-model="form.g5NtSlsxTonHt" class="w-full"
                             :use-grouping="false" mode="decimal" :max-fraction-digits="4" />
              </div>
            </div>
            <div class="form-row">
              <label>NT SLSX trong tháng</label>
              <div class="form-input">
                <InputNumber v-model="form.g5NtSlsxTrongThang" class="w-full"
                             :use-grouping="false" mode="decimal" :max-fraction-digits="4" />
              </div>
            </div>
            <div class="form-row">
              <label>NT SLSX OS Tồn</label>
              <div class="form-input">
                <InputNumber v-model="form.g5NtSlsxOsTon" class="w-full"
                             :use-grouping="false" mode="decimal" :max-fraction-digits="4" />
              </div>
            </div>
            <div class="form-row">
              <label>NT SLSX OS trong tháng</label>
              <div class="form-input">
                <InputNumber v-model="form.g5NtSlsxOsTrongThang" class="w-full"
                             :use-grouping="false" mode="decimal" :max-fraction-digits="4" />
              </div>
            </div>
            <div class="form-row">
              <label class="text-gray-500">Tổng SLNT (CT)</label>
              <div class="form-input">
                <InputNumber :model-value="record.g5TongSlnt" class="w-full" disabled
                             :use-grouping="false" mode="decimal" :max-fraction-digits="4" />
              </div>
            </div>
            <div class="form-row">
              <label>Ra tương ứng SLNT</label>
              <div class="form-input">
                <InputNumber v-model="form.g5RaTuongUngSlnt" class="w-full"
                             :use-grouping="false" mode="decimal" :max-fraction-digits="4" />
              </div>
            </div>
            <div class="form-row">
              <label>Tỉ suất LNG (%)</label>
              <div class="form-input">
                <InputNumber v-model="form.g5TiSuatLng" class="w-full"
                             :use-grouping="false" mode="decimal" :max-fraction-digits="4" suffix="%" />
              </div>
            </div>
            <div class="form-row">
              <label>LNG (VNĐ)</label>
              <div class="form-input">
                <InputNumber v-model="form.g5LngVnd" class="w-full"
                             :use-grouping="false" mode="decimal" :max-fraction-digits="0" />
              </div>
            </div>
            <div class="form-row">
              <label class="text-gray-500">Doanh thu (CT)</label>
              <div class="form-input">
                <InputNumber :model-value="record.g5DoanhThu" class="w-full" disabled
                             :use-grouping="false" mode="decimal" :max-fraction-digits="0" />
              </div>
            </div>
          </div>
        </Fieldset>

        <!-- G6 — Tồn cuối kỳ (computed) -->
        <Fieldset legend="G6 — Tồn cuối kỳ (tính toán)" :toggleable="true" :collapsed="true">
          <div class="form-grid">
            <div class="form-row">
              <label class="text-gray-500">Ra Tồn</label>
              <div class="form-input">
                <InputNumber :model-value="record.g6RaTon" class="w-full" disabled
                             :use-grouping="false" mode="decimal" :max-fraction-digits="4" />
              </div>
            </div>
            <div class="form-row">
              <label class="text-gray-500">SLSX Tồn Ht</label>
              <div class="form-input">
                <InputNumber :model-value="record.g6SlsxTonHt" class="w-full" disabled
                             :use-grouping="false" mode="decimal" :max-fraction-digits="4" />
              </div>
            </div>
            <div class="form-row">
              <label class="text-gray-500">SLSX Tồn DD</label>
              <div class="form-input">
                <InputNumber :model-value="record.g6SlsxTonDd" class="w-full" disabled
                             :use-grouping="false" mode="decimal" :max-fraction-digits="4" />
              </div>
            </div>
            <div class="form-row">
              <label class="text-gray-500">SLSX OS Tồn</label>
              <div class="form-input">
                <InputNumber :model-value="record.g6SlsxOsTon" class="w-full" disabled
                             :use-grouping="false" mode="decimal" :max-fraction-digits="4" />
              </div>
            </div>
            <div class="form-row">
              <label class="text-gray-500">SLSX OS Tồn Ht</label>
              <div class="form-input">
                <InputNumber :model-value="record.g6SlsxOsTonHt" class="w-full" disabled
                             :use-grouping="false" mode="decimal" :max-fraction-digits="4" />
              </div>
            </div>
          </div>
        </Fieldset>

        <!-- Footer actions -->
        <div class="flex justify-end gap-2 pt-2 border-t">
          <Button label="Hủy" severity="secondary" outlined @click="visible = false" :disabled="saving" />
          <Button type="submit" label="Lưu" icon="pi pi-save" :loading="saving" />
        </div>

      </form>
    </template>

  </Dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import Dialog from 'primevue/dialog'
import ProgressSpinner from 'primevue/progressspinner'
import Message from 'primevue/message'
import Fieldset from 'primevue/fieldset'
import InputNumber from 'primevue/inputnumber'
import Button from 'primevue/button'
import { useToast } from 'primevue/usetoast'
import { useConfirm } from 'primevue/useconfirm'
import { projectMonthlyRecordService } from '@/services/projectMonthlyRecordService'
import type { ProjectMonthRecordDetail, ProjectMonthRecordUpdateRequest } from '@/types/project-monthly-record'

// ---- Props / emits ----

const props = defineProps<{
  recordId: string | null
  monthKey: string
}>()

const emit = defineEmits<{
  (e: 'saved'): void
}>()

// ---- State ----

const toast = useToast()
const confirm = useConfirm()

const visible = defineModel<boolean>('visible', { default: false })
const record = ref<ProjectMonthRecordDetail | null>(null)
const loading = ref(false)
const loadError = ref<string | null>(null)
const saving = ref(false)

// ---- Form ----

const emptyForm = (): ProjectMonthRecordUpdateRequest => ({
  g1RaTon: null, g1SlsxTonTuSxHtHd: null,
  g1SlsxTonTuSxDdHd: null, g1SlsxTonTuSxHd: null, g1SlsxOsTon: null, g1SlsxOsTonHt: null,
  g2Headcount: null, g2Ra: null, g2SlsxTuSx: null, g2SlsxOs: null, g2LienKet: null,
  g2SlsxTuSxHtTrongThang: null, g2SlsxTuSxDd: null, g2SlsxOsHt: null,
  g2SlsxOsDd: null, g2Cpbqtb: null, g2TySuatLng: null,
  g3Ra: null, g3TongSlsxHd: null, g3SlsxTuSxHt: null, g3SlsxTuSxDd: null,
  g3SlsxOsDd: null, g3SlsxOsTonHt: null,
  g4TuSlsxTonHt: null, g4TuSlsxTrongThang: null, g4SlsxOsTon: null,
  g4SlsxOsTrongThang: null, g4Lk: null, g4TiSuatLngDuKien: null, g4LngDuKien: null,
  g5NtSlsxTonHt: null, g5NtSlsxTrongThang: null, g5NtSlsxOsTon: null,
  g5NtSlsxOsTrongThang: null, g5RaTuongUngSlnt: null, g5TiSuatLng: null, g5LngVnd: null
})

const form = ref<ProjectMonthRecordUpdateRequest>(emptyForm())

// ---- Load when dialog opens ----

watch(visible, async (val) => {
  if (val && props.recordId) {
    loading.value = true
    loadError.value = null
    record.value = null
    try {
      const res = await projectMonthlyRecordService.getById(props.recordId)
      const r = res.data
      record.value = r
      if (!r) return
      // Pre-fill manual fields from loaded record
      form.value = {
        g1RaTon: r.g1RaTon, g1SlsxTonTuSxHtHd: r.g1SlsxTonTuSxHtHd,
        g1SlsxTonTuSxDdHd: r.g1SlsxTonTuSxDdHd,
        g1SlsxTonTuSxHd: r.g1SlsxTonTuSxHd, g1SlsxOsTon: r.g1SlsxOsTon,
        g1SlsxOsTonHt: r.g1SlsxOsTonHt,
        g2Headcount: r.g2Headcount, g2Ra: r.g2Ra, g2SlsxTuSx: r.g2SlsxTuSx,
        g2SlsxOs: r.g2SlsxOs, g2LienKet: r.g2LienKet,
        g2SlsxTuSxHtTrongThang: r.g2SlsxTuSxHtTrongThang, g2SlsxTuSxDd: r.g2SlsxTuSxDd,
        g2SlsxOsHt: r.g2SlsxOsHt, g2SlsxOsDd: r.g2SlsxOsDd,
        g2Cpbqtb: r.g2Cpbqtb, g2TySuatLng: r.g2TySuatLng,
        g3Ra: r.g3Ra, g3TongSlsxHd: r.g3TongSlsxHd, g3SlsxTuSxHt: r.g3SlsxTuSxHt,
        g3SlsxTuSxDd: r.g3SlsxTuSxDd, g3SlsxOsDd: r.g3SlsxOsDd, g3SlsxOsTonHt: r.g3SlsxOsTonHt,
        g4TuSlsxTonHt: r.g4TuSlsxTonHt, g4TuSlsxTrongThang: r.g4TuSlsxTrongThang,
        g4SlsxOsTon: r.g4SlsxOsTon, g4SlsxOsTrongThang: r.g4SlsxOsTrongThang,
        g4Lk: r.g4Lk, g4TiSuatLngDuKien: r.g4TiSuatLngDuKien, g4LngDuKien: r.g4LngDuKien,
        g5NtSlsxTonHt: r.g5NtSlsxTonHt, g5NtSlsxTrongThang: r.g5NtSlsxTrongThang,
        g5NtSlsxOsTon: r.g5NtSlsxOsTon, g5NtSlsxOsTrongThang: r.g5NtSlsxOsTrongThang,
        g5RaTuongUngSlnt: r.g5RaTuongUngSlnt, g5TiSuatLng: r.g5TiSuatLng, g5LngVnd: r.g5LngVnd
      }
    } catch {
      loadError.value = 'Không thể tải bản ghi tháng'
    } finally {
      loading.value = false
    }
  }
})

// ---- Overwrite guard (FR-REC-032) ----

function hasExistingData(): boolean {
  if (!record.value) return false
  const r = record.value
  const manualFields: (keyof typeof r)[] = [
    'g1RaTon', 'g1SlsxTonTuSxHtHd', 'g1SlsxTonTuSxDdHd',
    'g1SlsxTonTuSxHd', 'g1SlsxOsTon', 'g1SlsxOsTonHt',
    'g2Headcount', 'g2Ra', 'g2SlsxTuSx', 'g2SlsxOs', 'g2LienKet',
    'g2SlsxTuSxHtTrongThang', 'g2SlsxTuSxDd', 'g2SlsxOsHt', 'g2SlsxOsDd', 'g2Cpbqtb', 'g2TySuatLng',
    'g3Ra', 'g3TongSlsxHd', 'g3SlsxTuSxHt', 'g3SlsxTuSxDd', 'g3SlsxOsDd', 'g3SlsxOsTonHt',
    'g4TuSlsxTonHt', 'g4TuSlsxTrongThang', 'g4SlsxOsTon', 'g4SlsxOsTrongThang', 'g4Lk',
    'g4TiSuatLngDuKien', 'g4LngDuKien',
    'g5NtSlsxTonHt', 'g5NtSlsxTrongThang', 'g5NtSlsxOsTon', 'g5NtSlsxOsTrongThang',
    'g5RaTuongUngSlnt', 'g5TiSuatLng', 'g5LngVnd'
  ]
  return manualFields.some((f) => r[f] != null)
}

function handleSubmit() {
  if (hasExistingData()) {
    confirm.require({
      message: 'Bản ghi đã có dữ liệu. Bạn có muốn ghi đè không?',
      header: 'Xác nhận ghi đè',
      icon: 'pi pi-exclamation-triangle',
      rejectProps: { label: 'Hủy', severity: 'secondary', outlined: true },
      acceptProps: { label: 'Ghi đè', severity: 'warning' },
      accept: () => doSave()
    })
  } else {
    doSave()
  }
}

async function doSave() {
  if (!props.recordId) return
  saving.value = true
  try {
    await projectMonthlyRecordService.update(props.recordId, form.value)
    toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã lưu bản ghi tháng', life: 3000 })
    visible.value = false
    emit('saved')
  } catch {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể lưu bản ghi tháng', life: 4000 })
  } finally {
    saving.value = false
  }
}

function onHide() {
  record.value = null
  form.value = emptyForm()
  loadError.value = null
}
</script>

<style scoped>
.dialog-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.75rem 1.5rem;
}

.form-row {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.form-row > label {
  width: 180px;
  min-width: 180px;
  font-weight: 500;
  text-align: right;
  font-size: 0.8125rem;
  line-height: 1.3;
}

.form-input {
  flex: 1;
}
</style>
