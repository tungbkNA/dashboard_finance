# Data Model: Quản Lý Các Dự Án (Project Management View)

**Feature**: 004-project-management-view  
**Date**: 2026-05-07  
**Source**: Inherits from Feature 003 — no new DB tables required.

---

## Existing Entities (Feature 003 — unchanged)

### `project_monthly_record` table

Already created by V3 Flyway migration. Key columns relevant to this feature:

| Column | Type | Notes |
|---|---|---|
| `id` | UUID PK | |
| `project_id` | UUID FK → `project` | |
| `month_key` | VARCHAR(7) | Format: `YYYY-MM` |
| `active` | BOOLEAN | Filter: only active=true shown in list |
| `is_first_month` | *(computed)* | Not stored; derived by service from repository |
| `g1_*` – `g6_*` | DECIMAL(19,4) NULLABLE | ~40 financial fields across 6 groups |
| `created_at`, `updated_at` | TIMESTAMPTZ NOT NULL | |

---

## New Backend DTOs

### `FieldMetadataResponse.java`

```java
package com.internal.projectmgmt.dto.monthlyrecord;

// One object returned by GET /api/binance/project-monthly-records/field-metadata
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class FieldMetadataResponse {
    private List<GroupMetadata> groups;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class GroupMetadata {
        private String groupId;           // "g1" … "g6"
        private String groupName;         // Vietnamese display name
        private List<String> manualFields;
        private List<String> formulaFields;
        private List<String> cascadedFromPrevMonthFields; // subset of manualFields; read-only when isFirstMonth=false
    }
}
```

---

## Frontend Type Model

### Extended `project-monthly-record.ts`

```typescript
// --- Existing types (from Feature 003) ---
export interface ProjectMonthRecordSummary { ... }   // id, projectCode, projectName, monthKey, active, g4DoanhThu, g5DoanhThu, g5TongSlnt, updatedAt
export interface ProjectMonthRecordDetail { ... }    // full record with all 40+ fields + isFirstMonth
export interface ProjectMonthRecordUpdateRequest { ... }  // manual fields only

// --- New types for Feature 004 ---

export interface GroupMetadata {
  groupId: string                         // "g1" | "g2" | "g3" | "g4" | "g5" | "g6"
  groupName: string                       // Vietnamese display name
  manualFields: string[]                  // field names user can edit
  formulaFields: string[]                 // field names that are read-only (auto-calculated)
  cascadedFromPrevMonthFields: string[]   // read-only when isFirstMonth=false
}

export interface FieldMetadata {
  groups: GroupMetadata[]
}
```

---

## Frontend Component State Model

### `ProjectManagementView.vue` state

```typescript
const records = ref<ProjectMonthRecordSummary[]>([])   // list from GET ?monthKey=
const fieldMetadata = ref<FieldMetadata | null>(null)  // loaded once on mount
const loading = ref(false)
const error = ref<string | null>(null)
const selectedMonth = ref<Date>(new Date())            // DatePicker model
const currentMonthKey = ref<string>('YYYY-MM')         // derived from selectedMonth
```

### `ProjectCard.vue` props + state

```typescript
// Props
const props = defineProps<{
  record: ProjectMonthRecordSummary        // summary shown in card header
  fieldMetadata: FieldMetadata             // passed from view
}>()

// Local state
const detail = ref<ProjectMonthRecordDetail | null>(null)  // loaded on first group expand
const detailLoading = ref(false)
const detailError = ref<string | null>(null)

// Per-group state (g1..g6)
type GroupId = 'g1' | 'g2' | 'g3' | 'g4' | 'g5' | 'g6'
const activeEditGroup = ref<GroupId | null>(null)   // which group is in edit mode
const groupForms = ref<Record<GroupId, Partial<ProjectMonthRecordUpdateRequest>>>({...})
const groupSnapshots = ref<Record<GroupId, Partial<ProjectMonthRecordDetail>>>({...})
const saving = ref(false)
```

### Group form initialization (on Edit click)

```typescript
// snapshot = current values from detail (used for overwrite detection + Cancel revert)
// form     = editable copy the user modifies
groupSnapshots.value[gid] = extractGroupFields(detail.value, gid)
groupForms.value[gid] = { ...groupSnapshots.value[gid] }
activeEditGroup.value = gid
```

### Merge strategy (on Save)

```typescript
// Build full request: current detail values for all groups + form values for edited group
function buildFullRequest(editedGroupId: GroupId): ProjectMonthRecordUpdateRequest {
  const d = detail.value!
  return {
    // G1
    g1RaTon: editedGroupId === 'g1' ? groupForms.value.g1.g1RaTon : d.g1RaTon,
    // ... all ~40 manual fields
  }
}
```

---

## Field Classification Reference

| Group | Formula Fields | Cascaded (read-only non-first month) |
|---|---|---|
| G1 — Tồn đầu kỳ | *(none)* | g1RaTon, g1SlsxTonTuSxHtHd, g1SlsxTonTuSxDdHd, g1SlsxOsTon, g1SlsxOsTonHt |
| G2 — Kế hoạch tháng | g2TongSlsxDuKien | *(none)* |
| G3 — Thực hiện SLSX đến NGÀY | g3Ee | *(none)* |
| G4 — Kế hoạch doanh thu | g4Tong, g4DoanhThu | *(none)* |
| G5 — Thực hiện nghiệm thu | g5TongSlnt, g5DoanhThu | *(none)* |
| G6 — Tồn cuối kỳ | g6RaTon, g6SlsxTonHt, g6SlsxTonDd, g6SlsxOsTon, g6SlsxOsTonHt | *(all are formula — no editable fields)* |
