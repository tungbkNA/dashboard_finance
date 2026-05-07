# Research: Quản Lý Các Dự Án (Project Management View)

**Feature**: 004-project-management-view  
**Date**: 2026-05-07

---

## R1: Per-Group Update Strategy

**Problem**: The existing `PUT /api/binance/project-monthly-records/{id}` endpoint uses `applyRequest()` in the mapper, which copies ALL manual fields from the request body — a null value in the request body **clears** that field on the entity. With per-group editing (only Group 2 fields modified), sending a request with null values for Groups 1/3/4/5 would wipe out existing data in those groups.

**Decision**: **FE merge strategy** — no new BE endpoints needed.

Implementation:
1. When the user opens any group for editing on a project card, the FE calls `getById(recordId)` to load the full record detail if not already loaded.
2. The FE stores the full `ProjectMonthRecordDetail` as `currentRecord`.
3. For each of the 6 groups, the FE maintains a separate `groupForm` reactive object containing only that group's editable field values, initialized from `currentRecord`.
4. On Save for Group N, the FE constructs a `ProjectMonthRecordRequest` that contains:
   - Group N: values from the edited `groupForm` (user's new inputs)
   - All other groups: values from `currentRecord` (unchanged, preserved)
5. This full request is sent to `PUT /{id}`, which applies all fields atomically.
6. After a successful save, `currentRecord` is updated from the API response.

**Rationale**:
- No new BE surface area
- `currentRecord` is already loaded for display (free)
- The merge is a pure, testable FE operation
- BE recalculates all formula fields on every save anyway

---

## R2: Field Metadata Endpoint

**Problem**: Spec FR-PM-012: "Backend MUST reject any request that attempts to modify a formula-derived field." Spec also states backend should return metadata so FE knows which fields are formula vs manual. The classification is static (business rules, not per-record data).

**Decision**: Add `GET /api/binance/project-monthly-records/field-metadata` that returns a hard-coded `FieldMetadataResponse`.

**Response structure**:
```json
{
  "code": "SUCCESS",
  "data": {
    "groups": [
      {
        "groupId": "g1",
        "groupName": "Tồn đầu kỳ",
        "manualFields": ["g1RaTon", "g1SlsxTonTuSxHd", "g1SlsxTonTuSxHtHd", "g1SlsxTonTuSxDdHd", "g1SlsxOsTon", "g1SlsxOsTonHt"],
        "formulaFields": [],
        "cascadedFromPrevMonthFields": ["g1RaTon", "g1SlsxTonTuSxHtHd", "g1SlsxTonTuSxDdHd", "g1SlsxOsTon", "g1SlsxOsTonHt"]
      },
      {
        "groupId": "g2",
        "groupName": "Kế hoạch tháng",
        "manualFields": ["g2Headcount", "g2Ra", "g2SlsxTuSx", "g2SlsxOs", "g2LienKet", "g2SlsxTuSxHtTrongThang", "g2SlsxTuSxDd", "g2SlsxOsHt", "g2SlsxOsDd", "g2Cpbqtb", "g2TySuatLng"],
        "formulaFields": ["g2TongSlsxDuKien"],
        "cascadedFromPrevMonthFields": []
      },
      {
        "groupId": "g3",
        "groupName": "Thực hiện SLSX đến NGÀY",
        "manualFields": ["g3Ra", "g3TongSlsxHd", "g3SlsxTuSxHt", "g3SlsxTuSxDd", "g3SlsxOsDd", "g3SlsxOsTonHt"],
        "formulaFields": ["g3Ee"],
        "cascadedFromPrevMonthFields": []
      },
      {
        "groupId": "g4",
        "groupName": "Kế hoạch doanh thu",
        "manualFields": ["g4TuSlsxTonHt", "g4TuSlsxTrongThang", "g4SlsxOsTon", "g4SlsxOsTrongThang", "g4Lk", "g4TiSuatLngDuKien", "g4LngDuKien"],
        "formulaFields": ["g4Tong", "g4DoanhThu"],
        "cascadedFromPrevMonthFields": []
      },
      {
        "groupId": "g5",
        "groupName": "Thực hiện nghiệm thu",
        "manualFields": ["g5RaTuongUngSlnt", "g5NtSlsxTonHt", "g5NtSlsxTrongThang", "g5NtSlsxOsTon", "g5NtSlsxOsTrongThang", "g5TiSuatLng", "g5LngVnd"],
        "formulaFields": ["g5TongSlnt", "g5DoanhThu"],
        "cascadedFromPrevMonthFields": []
      },
      {
        "groupId": "g6",
        "groupName": "Tồn cuối kỳ",
        "manualFields": [],
        "formulaFields": ["g6RaTon", "g6SlsxTonHt", "g6SlsxTonDd", "g6SlsxOsTon", "g6SlsxOsTonHt"],
        "cascadedFromPrevMonthFields": []
      }
    ]
  }
}
```

**Note on `cascadedFromPrevMonthFields`**: Group 1 (Tồn đầu kỳ) fields that are auto-populated from the previous month's G6 closing stock when `isFirstMonth = false`. The FE uses `isFirstMonth` from the record response to determine editability of these fields.

---

## R3: Accordion/Card Layout

**Decision**: Two-level collapsible layout using PrimeVue components.

| Level | Component | Trigger |
|---|---|---|
| Project card (outer) | `Panel` with `:toggleable="true"` | Click card header |
| Attribute group (inner) | `Fieldset` with `:toggleable="true"` | Click group legend |

**Panel** shows: project code, project name, and 3 key summary values (G4 DoanhThu, G5 DoanhThu, G5 TongSlnt) in the header via `header` slot.  
**Fieldset** shows: group name in the legend, field rows inside.

All 6 Fieldsets start collapsed by default (`:collapsed="true"`). The outer Panel also starts collapsed. Only the Panel need expand to see groups; groups themselves start collapsed inside.

**Edit button placement**: each Fieldset legend includes an Edit button (shown only when the Fieldset is expanded, `v-if="!groupXEditMode && isExpanded"`).

---

## R4: Per-Group Edit State Machine

Each group has 3 states:
1. **View** (default): fields shown as plain text values
2. **Edit**: fields shown as `InputNumber` inputs; Save + Cancel buttons visible
3. **Saving**: Save button shows loading spinner; inputs disabled

State transitions:
- `View → Edit`: user clicks group's Edit button
- `Edit → Saving`: user clicks Save
- `Saving → View`: save API call completes (success)
- `Saving → Edit`: save API call fails (stay in edit, show error toast)
- `Edit → View`: user clicks Cancel (revert form to pre-edit snapshot)

**One active edit group at a time**: When the user clicks Edit on Group N while Group M is in Edit state, show a PrimeVue `ConfirmDialog`: "Bạn đang chỉnh sửa nhóm [M]. Lưu hoặc hủy trước khi chỉnh sửa nhóm khác." (per FR-PM-009a).

---

## R5: Overwrite Warning Logic

**Trigger**: When user clicks Save on a group, before calling API:
1. Compare each form field value against the pre-edit snapshot value
2. For each field where: `snapshot[field] !== null` AND `form[field] !== snapshot[field]` → mark as "overwriting existing data"
3. If any such field exists → show ConfirmDialog with message from spec
4. If user confirms → proceed with API call
5. If user cancels → stay in Edit state (form values unchanged)

**Note**: If all changed fields were previously null (new data), no warning is shown.

---

## R6: Formula Field Visual Design

**"Tự tính" badge**: A small `Tag` component with severity `secondary` and value `"Tự tính"` placed to the right of the field label. Formula field `InputNumber` has `disabled` and a distinct background via PrimeVue's disabled styling.

**Editable field indicator**: No special badge. Editable fields in Edit mode show a white background input. In View mode, they display as plain text (not an InputNumber).

---

## Summary: What Needs to Be Built

| # | Layer | Item | Type |
|---|---|---|---|
| 1 | BE | `FieldMetadataResponse.java` DTO | CREATE |
| 2 | BE | `getFieldMetadata()` in `ProjectMonthRecordService` | MODIFY |
| 3 | BE | `/field-metadata` endpoint in `ProjectMonthRecordController` | MODIFY |
| 4 | BE | Unit test for field-metadata endpoint | CREATE |
| 5 | FE | `FieldMetadata` types in `project-monthly-record.ts` | MODIFY |
| 6 | FE | `getFieldMetadata()` in `projectMonthlyRecordService.ts` | MODIFY |
| 7 | FE | `ProjectCard.vue` component | CREATE |
| 8 | FE | `ProjectManagementView.vue` rewrite | REWRITE |
| 9 | FE | TypeScript compile check passes | VERIFY |
