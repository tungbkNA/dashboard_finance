// project-monthly-record.ts — TypeScript types for Features 003 + 004

// ---- Summary (list view) ----

export interface ProjectMonthRecordSummary {
  id: string
  projectId: string
  projectCode: string
  projectName: string
  monthKey: string
  active: boolean
  customerName: string | null
  g1SlsxTonTuSxHd: number | null
  g4DoanhThu: number | null
  g5DoanhThu: number | null
  g5TongSlnt: number | null
  g6SlsxTon: number | null
  updatedAt: string | null
}

// ---- Full detail (aligned with BE ProjectMonthRecordResponse) ----

export interface ProjectMonthRecordDetail {
  id: string
  projectId: string
  projectCode: string
  projectName: string
  monthKey: string
  active: boolean
  isFirstMonth: boolean
  price: number | null
  customerName: string | null
  representUserName: string | null
  createdAt: string | null
  updatedAt: string | null

  // G1 — Tồn đầu kỳ
  g1RaTon: number | null
  g1SlsxTonTuSxHd: number | null
  g1SlsxTonTuSxHtHd: number | null
  g1SlsxTonTuSxDdHd: number | null
  g1SlsxOsTon: number | null
  g1SlsxOsTonHt: number | null

  // G2 — Kế hoạch tháng
  g2Headcount: number | null
  g2Ra: number | null
  g2SlsxTuSx: number | null
  g2SlsxOs: number | null
  g2LienKet: number | null
  g2TongSlsxDuKien: number | null
  g2SlsxTuSxHtTrongThang: number | null
  g2SlsxTuSxDd: number | null
  g2SlsxOsHt: number | null
  g2SlsxOsDd: number | null
  g2Cpbqtb: number | null
  g2TySuatLng: number | null

  // G3 — Thực hiện SLSX đến NGÀY
  g3Ra: number | null
  g3TongSlsxHd: number | null
  g3Ee: number | null
  g3SlsxTuSxHt: number | null
  g3SlsxTuSxDd: number | null
  g3SlsxOsDd: number | null
  g3SlsxOsTonHt: number | null

  // G4 — Kế hoạch doanh thu
  g4TuSlsxTonHt: number | null
  g4TuSlsxTrongThang: number | null
  g4SlsxOsTon: number | null
  g4SlsxOsTrongThang: number | null
  g4Lk: number | null
  g4Tong: number | null
  g4DoanhThu: number | null
  g4TiSuatLngDuKien: number | null
  g4LngDuKien: number | null

  // G5 — Thực hiện nghiệm thu
  g5RaTuongUngSlnt: number | null
  g5NtSlsxTonHt: number | null
  g5NtSlsxTrongThang: number | null
  g5NtSlsxOsTon: number | null
  g5NtSlsxOsTrongThang: number | null
  g5TongSlnt: number | null
  g5DoanhThu: number | null
  g5TiSuatLng: number | null
  g5LngVnd: number | null

  // G6 — Tồn cuối kỳ (all formula)
  g6RaTon: number | null
  g6SlsxTonHt: number | null
  g6SlsxTonDd: number | null
  g6SlsxOsTon: number | null
  g6SlsxOsTonHt: number | null
  g6SlsxTon: number | null

  // Feature 005: cross-month propagation result
  affectedMonths: number
}

// ---- Update request (manual fields only — aligned with BE ProjectMonthRecordRequest) ----

export interface ProjectMonthRecordUpdateRequest {
  // G1
  g1RaTon?: number | null
  g1SlsxTonTuSxHd?: number | null
  g1SlsxTonTuSxHtHd?: number | null
  g1SlsxTonTuSxDdHd?: number | null
  g1SlsxOsTon?: number | null
  g1SlsxOsTonHt?: number | null

  // G2
  g2Headcount?: number | null
  g2Ra?: number | null
  g2SlsxTuSx?: number | null
  g2SlsxOs?: number | null
  g2LienKet?: number | null
  g2SlsxTuSxHtTrongThang?: number | null
  g2SlsxTuSxDd?: number | null
  g2SlsxOsHt?: number | null
  g2SlsxOsDd?: number | null
  g2Cpbqtb?: number | null
  g2TySuatLng?: number | null

  // G3 (non-formula)
  g3Ra?: number | null
  g3TongSlsxHd?: number | null
  g3SlsxTuSxHt?: number | null
  g3SlsxTuSxDd?: number | null
  g3SlsxOsDd?: number | null
  g3SlsxOsTonHt?: number | null

  // G4 (non-formula)
  g4TuSlsxTonHt?: number | null
  g4TuSlsxTrongThang?: number | null
  g4SlsxOsTon?: number | null
  g4SlsxOsTrongThang?: number | null
  g4Lk?: number | null
  g4TiSuatLngDuKien?: number | null
  g4LngDuKien?: number | null

  // G5 (non-formula)
  g5RaTuongUngSlnt?: number | null
  g5NtSlsxTonHt?: number | null
  g5NtSlsxTrongThang?: number | null
  g5NtSlsxOsTon?: number | null
  g5NtSlsxOsTrongThang?: number | null
  g5TiSuatLng?: number | null
  g5LngVnd?: number | null
}

// ---- Field Metadata (Feature 004) ----

export interface GroupMetadata {
  groupId: string
  groupName: string
  manualFields: string[]
  formulaFields: string[]
  cascadedFromPrevMonthFields: string[]
}

export interface FieldMetadata {
  groups: GroupMetadata[]
}

