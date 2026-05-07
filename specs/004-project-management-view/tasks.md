# Tasks: Quản Lý Các Dự Án (Project Management View)

**Input**: Design documents from `specs/004-project-management-view/`
**Prerequisites**: plan.md ✅ | spec.md ✅ | research.md ✅ | data-model.md ✅ | contracts/ ✅

---

## Phase 1: Setup

**Purpose**: Confirm existing infrastructure is in place; no new scaffolding needed (BE/FE both exist from Features 001–003).

- [X] T001 Verify TypeScript compile passes in FE/ with `npx vue-tsc --noEmit` (baseline before any changes)
- [X] T002 Verify existing BE tests pass with `mvn test -Dtest="!DashboardFinanceApplicationTests"` in BE/

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: New BE DTO and endpoint that the FE depends on before any user story work can proceed.

**⚠️ CRITICAL**: T003–T005 must be complete before US1/US2/US3/US4 FE work begins.

- [X] T003 Create `BE/src/main/java/com/internal/projectmgmt/dto/monthlyrecord/FieldMetadataResponse.java` with inner static class `GroupMetadata` (fields: groupId, groupName, manualFields, formulaFields, cascadedFromPrevMonthFields as List<String>)
- [X] T004 Add `getFieldMetadata()` method to `BE/src/main/java/com/internal/projectmgmt/service/ProjectMonthRecordService.java` that returns hard-coded `FieldMetadataResponse` constant (6 groups per contracts/field-metadata-api.md, no DB query)
- [X] T005 Add `GET /api/binance/project-monthly-records/field-metadata` endpoint to `BE/src/main/java/com/internal/projectmgmt/controller/ProjectMonthRecordController.java` with `@Operation` annotation; returns `ApiResponse<FieldMetadataResponse>`

**Checkpoint**: `curl http://localhost:8080/api/binance/project-monthly-records/field-metadata` returns 200 with 6 groups

---

## Phase 3: User Story 1 — View Project Monthly Data (Priority: P1) 🎯 MVP

**Goal**: User opens the screen, sees all active projects for the current month in an accordion/card list, and can change the month filter to refresh the list.

**Independent Test**: Navigate to `/projects`, verify month filter defaults to current month (mm/yyyy format), confirm all projects with active records appear as Panel cards, and verify empty-state message when no records exist.

- [X] T006 [P] [US1] Add `FieldMetadata`, `GroupMetadata` types to `FE/src/types/project-monthly-record.ts`
- [X] T007 [P] [US1] Add `getFieldMetadata()` method to `FE/src/services/projectMonthlyRecordService.ts` calling `GET /api/binance/project-monthly-records/field-metadata`
- [X] T008 [US1] Rewrite `FE/src/views/ProjectManagementView.vue`: accordion/card layout using PrimeVue `Panel` per project; month picker (`DatePicker`, view="month", dateFormat="mm/yy") defaulting to current month; calls `getAll(monthKey)` and `getFieldMetadata()` on mount; passes `fieldMetadata` prop to each `ProjectCard`; loading state (`ProgressSpinner`); empty state message "Không có dữ liệu dự án cho tháng này"; error state (`Message` severity="error"); imports all PrimeVue components locally
- [X] T009 [US1] Create `FE/src/components/project-management/ProjectCard.vue`: accepts props `record: ProjectMonthRecordSummary` and `fieldMetadata: FieldMetadata`; renders a `Panel` card showing project code, project name, and 3 summary values (g4DoanhThu, g5DoanhThu, g5TongSlnt formatted as Vietnamese locale numbers) in the header slot; contains 6 collapsed `Fieldset` sections (one per group, legend = Vietnamese group name from fieldMetadata); each Fieldset starts collapsed (`:collapsed="true"`); all PrimeVue components imported locally; `detail` ref is null initially (loaded lazily)

**Checkpoint**: US1 independently testable — screen loads, shows project cards, month filter works, empty state shows

---

## Phase 4: User Story 2 — Expand/Collapse Attribute Groups (Priority: P2)

**Goal**: User expands a group Fieldset inside a project card to view individual field values. Formula fields show "Tự tính" badge. Group collapse/expand is client-side only.

**Independent Test**: Expand each of the 6 groups on a project card, verify field values appear (or "—" for null), "Tự tính" badges appear on formula fields, collapsing hides values, changing month resets groups to collapsed.

- [X] T010 [US2] Add lazy detail loading to `ProjectCard.vue`: on first expand of any Fieldset, call `projectMonthlyRecordService.getById(record.id)` and store in `detail` ref; show inline `ProgressSpinner` while loading; show inline `Message` on error; subsequent group expands use cached `detail`
- [X] T011 [US2] Implement field display rows inside each Fieldset in `ProjectCard.vue`: view mode shows field label + formatted value (or "—") + `Tag` badge with value "Tự tính" severity="secondary" for formula fields (derived from `fieldMetadata`); all 6 groups rendered with their correct fields per data-model.md field classification table; cascaded G1 fields show "(Từ tháng trước)" note when `detail.isFirstMonth === false`
- [X] T012 [US2] Ensure groups reset to collapsed when month filter changes in `ProjectManagementView.vue` (`:key="record.id + currentMonthKey"` on each `ProjectCard` to force remount, or emit reset event)

**Checkpoint**: US2 independently testable — all 6 groups expand/collapse, values display correctly, formula badges visible, month change resets state

---

## Phase 5: User Story 3 — Edit Manual-Entry Fields (Priority: P3)

**Goal**: User clicks Edit on an expanded group, edits manual fields as InputNumber inputs, clicks Save, API is called with merged full-record request, success toast shown. Cancel reverts to pre-edit values. Save failure keeps edit mode and shows error toast.

**Independent Test**: Expand Group 2 (Kế hoạch tháng), click Edit, change a manual field (e.g. g2Headcount), click Save, verify API PUT is called with all fields merged, success toast appears, group exits edit mode showing new value.

- [X] T013 [US3] Add per-group edit state to `ProjectCard.vue`: `activeEditGroup ref<'g1'|'g2'|'g3'|'g4'|'g5'|'g6'|null>`, `groupForms` (reactive per-group form objects), `groupSnapshots` (pre-edit snapshot per group), `saving ref<boolean>`; Edit button visible only when Fieldset is expanded (using `@toggle` event or collapsed prop tracking)
- [X] T014 [US3] Implement Edit button click handler in `ProjectCard.vue`: if another group is already in edit mode, show `ConfirmDialog` "Bạn đang chỉnh sửa nhóm khác. Lưu hoặc hủy trước khi tiếp tục." (FR-PM-009a); otherwise snapshot current detail values for the group and set `activeEditGroup`
- [X] T015 [US3] Implement inline edit fields in each Fieldset in `ProjectCard.vue`: when `activeEditGroup === groupId`, show `InputNumber` inputs (v-model to groupForms[groupId]) for manual fields; formula fields remain disabled with "Tự tính" badge regardless; cascaded G1 fields disabled when `!detail.isFirstMonth`; Save button (icon="pi pi-save", :loading="saving") and Cancel button (severity="secondary") rendered inside the Fieldset footer when in edit mode
- [X] T016 [US3] Implement Cancel handler in `ProjectCard.vue`: restores `groupForms[groupId]` from `groupSnapshots[groupId]` and sets `activeEditGroup = null`
- [X] T017 [US3] Implement `buildFullRequest()` helper in `ProjectCard.vue`: merges all ~40 manual fields from `detail` (unchanged groups) with `groupForms[activeEditGroup]` (edited group) into a `ProjectMonthRecordUpdateRequest`; returns the merged object ready for PUT call
- [X] T018 [US3] Implement Save handler (pre-warning) in `ProjectCard.vue`: calls `buildFullRequest()`, calls API `projectMonthlyRecordService.update(record.id, fullRequest)`, sets `saving = true`; on success: updates `detail` from response, clears `activeEditGroup`, shows success toast "Đã lưu thành công"; on 422 validation error: parse the field-level error messages from the response body and display them adjacent to the corresponding `InputNumber` inputs (FR-PM-021 / SC-007), keep `activeEditGroup` in edit mode; on other failure: shows error toast "Không thể lưu, vui lòng thử lại" and keeps `activeEditGroup` in edit mode with inputs intact (FR-PM-020a); `saving = false` in finally

**Checkpoint**: US3 independently testable — edit/save/cancel all work, formula fields blocked, error keeps edit mode

---

## Phase 6: User Story 4 — Overwrite Warning for Existing Data (Priority: P3)

**Goal**: When Save is clicked and any changed field had a prior non-null value, a single ConfirmDialog appears with the exact warning message before the API is called.

**Independent Test**: Load a record that has existing data in Group 2, click Edit, change g2Headcount, click Save — verify ConfirmDialog appears with "Thay đổi này có thể sẽ làm thay đổi các nhóm giá trị trong các tháng khác". Confirm → data saved. Test again with Cancel → no change.

- [X] T019 [US4] Add `hasOverwrittenFields(groupId)` helper in `ProjectCard.vue`: compares each field in `groupForms[groupId]` against `groupSnapshots[groupId]`; returns `true` if any field changed from a non-null snapshot value to a different value (FR-PM-015, FR-PM-019)
- [X] T020 [US4] Wrap Save handler in `ProjectCard.vue` with overwrite check: if `hasOverwrittenFields(activeEditGroup)` is true, call `confirm.require({ message: 'Thay đổi này có thể sẽ làm thay đổi các nhóm giá trị trong các tháng khác', header: 'Xác nhận thay đổi', icon: 'pi pi-exclamation-triangle', accept: () => doSave(), reject: () => {} })` (FR-PM-015a — single dialog per Save, regardless of field count); if false, call `doSave()` directly; import `ConfirmDialog` and `useConfirm` in `ProjectCard.vue`

**Checkpoint**: US4 independently testable — overwrite warning fires once per save when any changed field had prior data; no warning for all-empty fields

---

## Phase 7: Polish & Cross-Cutting Concerns

- [X] T021 [P] Add `Toast` and `ConfirmDialog` to `FE/src/App.vue` or verify they are already present (required globally for `useToast()` and `useConfirm()` to work in `ProjectCard.vue`)
- [X] T022 [P] Add `@Operation` and `@Tag` OpenAPI annotations to the new `/field-metadata` endpoint in `ProjectMonthRecordController.java`
- [X] T023 Add unit test `BE/src/test/java/com/internal/projectmgmt/controller/ProjectMonthRecordControllerFieldMetadataTest.java`: verify `GET /field-metadata` returns HTTP 200 with 6 groups, each group has correct groupId and non-empty field lists per contracts/field-metadata-api.md
- [X] T024 [P] Verify TypeScript compiles cleanly: run `npx vue-tsc --noEmit` in FE/ — fix any remaining errors in `ProjectManagementView.vue`, `ProjectCard.vue`
- [X] T025 [P] Run manual quickstart walkthrough per `specs/004-project-management-view/quickstart.md`: create project → navigate to screen → expand groups → edit → save → overwrite warning → cancel → month filter

---

## Dependencies

```
T001 → (baseline — no dependencies)
T002 → (baseline — no dependencies)
T003 → T004 → T005   (BE: DTO → service → controller, sequential)
T006 ⟂ T007          (FE types and service, parallel — no dependency on each other)
T006, T007, T005 → T008 → T009    (view rewrite needs FE types, service, BE endpoint)
T009 → T010 → T011 → T012         (US2 builds on ProjectCard from US1)
T011 → T013 → T014 → T015 → T016 → T017 → T018   (US3 sequential within ProjectCard)
T018 → T019 → T020                (US4 wraps the save handler from US3)
T018, T020 → T021, T022, T023, T024, T025  (polish after all stories)
```

## Parallel Execution Opportunities

| Story | Parallelizable Tasks |
|---|---|
| Foundational | T001 ⟂ T002 |
| US1 | T006 ⟂ T007 (while T003–T005 are in progress on BE) |
| Polish | T021 ⟂ T022 ⟂ T023 ⟂ T024 ⟂ T025 |

## Implementation Strategy

**MVP scope (minimum to demonstrate value)**: Complete T001–T011 → User Stories 1 + 2 working. User can view all projects by month with group expand/collapse and field values visible.

**Full scope**: T001–T025 → All 4 user stories including editing, save, cancel, and overwrite warning.

**Suggested delivery order**:
1. T001–T005 (BE foundations) → test `/field-metadata` endpoint
2. T006–T009 (FE view + ProjectCard shell) → test screen loads
3. T010–T012 (lazy detail + group display) → test expand/collapse with values
4. T013–T018 (per-group edit/save/cancel) → test full edit flow
5. T019–T020 (overwrite warning) → test warning dialog
6. T021–T025 (polish + verification)

---

## Summary

| Metric | Count |
|---|---|
| Total tasks | 25 |
| Phase 1 (Setup) | 2 |
| Phase 2 (Foundational BE) | 3 |
| Phase 3 (US1 — View list) | 4 |
| Phase 4 (US2 — Expand/collapse) | 3 |
| Phase 5 (US3 — Edit/save) | 6 |
| Phase 6 (US4 — Overwrite warning) | 2 |
| Phase 7 (Polish) | 5 |
| Parallelizable [P] tasks | 10 |
| MVP scope (US1+US2) | T001–T012 (12 tasks) |
