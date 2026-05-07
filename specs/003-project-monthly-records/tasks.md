---
description: "Task list for feature 003-project-monthly-records"
---

# Tasks: Quản Lý Bản Ghi Dự Án Theo Tháng

**Input**: `specs/003-project-monthly-records/`
**Branch**: `003-project-monthly-records`
**Date**: 2026-05-07

## Format: `[ID] [P?] [Story?] Description`

- **[P]**: Can run in parallel (different files, no shared dependencies in flight)
- **[US1/US2/US3]**: Which user story this task belongs to
- All file paths are relative to repository root

---

## Phase 1: Setup

**Purpose**: Flyway schema migration — must run before any JPA entity can be used

- [X] T001 Create Flyway migration `V3__project_monthly_records_schema.sql` at `BE/src/main/resources/db/migration/V3__project_monthly_records_schema.sql` — CREATE TABLE `project_monthly_record` with all 40+ columns (UUID PK, project_id FK, month_key VARCHAR(7), active BOOLEAN DEFAULT TRUE, created_at/updated_at TIMESTAMPTZ, all g1–g6 fields as DECIMAL(19,4) NULLABLE), UNIQUE constraint on (project_id, month_key), index on (project_id, month_key) and (month_key)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core BE layers that ALL three user stories depend on. Must complete before any US phase.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [X] T002 Create `ProjectMonthRecord.java` JPA entity at `BE/src/main/java/com/internal/projectmgmt/entity/ProjectMonthRecord.java` — `@Entity @Table("project_monthly_record")`, UUID PK with `@GeneratedValue`, `@ManyToOne(fetch=LAZY)` to `Project`, `month_key` VARCHAR(7), `active` boolean, all 40 g1–g6 fields as `BigDecimal`, `created_at`/`updated_at` with `@CreationTimestamp`/`@UpdateTimestamp`, Lombok `@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor`

- [X] T003 Create `ProjectMonthRecordRepository.java` at `BE/src/main/java/com/internal/projectmgmt/repository/ProjectMonthRecordRepository.java` — `JpaRepository<ProjectMonthRecord, UUID>`; add: `findByMonthKeyAndActiveTrue(String monthKey)`, `findByProjectIdAndMonthKeyAndActiveTrue(UUID projectId, String monthKey)`, `findByProjectIdAndMonthKeyOrderByMonthKeyAsc(UUID projectId, String monthKey)`, `findByProjectIdAndActiveTrue(UUID projectId)`, `findByProjectIdOrderByMonthKeyAsc(UUID projectId)`, `findFirstByProjectIdOrderByMonthKeyAsc(UUID projectId)`

- [X] T004 [P] Create `ProjectMonthRecordRequest.java` DTO at `BE/src/main/java/com/internal/projectmgmt/dto/monthlyrecord/ProjectMonthRecordRequest.java` — contains only the manual-input fields (NOT formula fields): all g1 fields (6), all non-formula g2 fields (11), all non-formula g3 fields (6), all non-formula g4 fields (7 including g4_ti_suat_lng_du_kien, g4_lng_du_kien), all non-formula g5 fields (7 including g5_ti_suat_lng, g5_lng_vnd); all as `BigDecimal`, all nullable, Lombok `@Data @NoArgsConstructor`

- [X] T005 [P] Create `ProjectMonthRecordSummaryResponse.java` DTO at `BE/src/main/java/com/internal/projectmgmt/dto/monthlyrecord/ProjectMonthRecordSummaryResponse.java` — fields: `id (UUID)`, `projectId (UUID)`, `projectCode (String)`, `projectName (String)`, `monthKey (String)`, `active (boolean)`, `g4DoanhThu (BigDecimal)`, `g5DoanhThu (BigDecimal)`, `g5TongSlnt (BigDecimal)`, `updatedAt (OffsetDateTime)`; Lombok `@Data @Builder @NoArgsConstructor @AllArgsConstructor`

- [X] T006 [P] Create `ProjectMonthRecordResponse.java` DTO at `BE/src/main/java/com/internal/projectmgmt/dto/monthlyrecord/ProjectMonthRecordResponse.java` — all 40+ fields from all 6 groups (both manual and formula) plus: `id`, `projectId`, `projectCode`, `projectName`, `monthKey`, `active`, `isFirstMonth (boolean)`, `createdAt`, `updatedAt`; all numeric fields as `BigDecimal`; Lombok `@Data @Builder @NoArgsConstructor @AllArgsConstructor`

- [X] T007 Create `ProjectMonthRecordMapper.java` at `BE/src/main/java/com/internal/projectmgmt/mapper/ProjectMonthRecordMapper.java` — methods: `toSummaryResponse(ProjectMonthRecord, boolean isFirstMonth)` → `ProjectMonthRecordSummaryResponse`; `toResponse(ProjectMonthRecord, boolean isFirstMonth)` → `ProjectMonthRecordResponse`; `applyRequest(ProjectMonthRecordRequest, ProjectMonthRecord)` — copies only manual fields onto entity (ignores formula fields); `@Component`

- [X] T008 Create `MonthlyCalculationService.java` at `BE/src/main/java/com/internal/projectmgmt/service/MonthlyCalculationService.java` — pure `@Service`, no DB dependency; single public method `calculateAndFill(ProjectMonthRecord record, BigDecimal projectPrice)`; implements all 11 formula fields: (1) g2_tong_slsx_du_kien = g2_slsx_tu_sx + g2_slsx_os + g2_lien_ket; (2) g3_ee = g3_tong_slsx_hd / g3_ra × 100 (NULL when g3_ra = 0 or null), round 2dp; (3) g4_tong = sum of 5 g4 manual fields; (4) g4_doanh_thu = g4_tong × projectPrice, round 0dp; (5) g5_tong_slnt = sum of 4 g5_nt_* fields; (6) g5_doanh_thu = g5_tong_slnt × projectPrice, round 0dp; (7) g6_ra_ton = g1_ra_ton + g3_ra − g5_ra_tuong_ung_slnt; (8) g6_slsx_ton_ht = g1_slsx_ton_tu_sx_ht_hd + g3_slsx_tu_sx_ht − g5_nt_slsx_ton_ht − g5_nt_slsx_os_trong_thang; (9) g6_slsx_ton_dd = g3_slsx_tu_sx_dd + g1_slsx_ton_tu_sx_dd_hd; (10) g6_slsx_os_ton = g1_slsx_os_ton + g3_slsx_os_dd; (11) g6_slsx_os_ton_ht = g1_slsx_os_ton_ht + g3_slsx_os_ton_ht − g5_nt_slsx_os_ton − g5_nt_slsx_os_trong_thang; null input treated as BigDecimal.ZERO in arithmetic; snapshot logic: only fill field if current value IS null (don't overwrite existing snapshot)

- [X] T009 Create `MonthlyCalculationServiceTest.java` at `BE/src/test/java/com/internal/projectmgmt/service/MonthlyCalculationServiceTest.java` — JUnit 5 unit tests for all 11 formula fields (g2_tong_slsx_du_kien, g3_ee, g4_tong, g4_doanh_thu, g5_tong_slnt, g5_doanh_thu, g6_ra_ton, g6_slsx_ton_ht, g6_slsx_ton_dd, g6_slsx_os_ton, g6_slsx_os_ton_ht); test cases: normal values, all-null inputs → 0 or null, g3_ee with RA=0 → null, g3_ee with RA=null → null, VNĐ round 0dp, % round 2dp, snapshot not overwritten when field already set

**Checkpoint**: Foundation ready — all three user story phases can now proceed.

---

## Phase 3: User Story 1 — Xem và Nhập Liệu Bản Ghi Tháng (Priority: P1) 🎯 MVP

**Goal**: BE CRUD endpoints + FE màn hình `/projects` với month filter, summary table, và 6-group dialog.

**Independent Test**: Vào `/projects`, chọn tháng, mở row → nhập manual fields → lưu → kiểm tra formula fields hiển thị đúng.

- [X] T010 [US1] Create `ProjectMonthRecordService.java` at `BE/src/main/java/com/internal/projectmgmt/service/ProjectMonthRecordService.java` — methods: `@Transactional(readOnly=true) findAllByMonthKey(String monthKey)` → List of SummaryResponse (active=true only); `@Transactional(readOnly=true) findById(UUID id)` → full Response with isFirstMonth flag (first month = lowest monthKey for that projectId); `@Transactional update(UUID id, ProjectMonthRecordRequest request)` → applies manual fields via mapper, calls MonthlyCalculationService.calculateAndFill() with project.price, saves, returns full Response; throw 404 if not found, 400 if inactive

- [X] T011 [US1] Create `ProjectMonthRecordController.java` at `BE/src/main/java/com/internal/projectmgmt/controller/ProjectMonthRecordController.java` — `@RestController @RequestMapping("/api/binance/project-monthly-records")`; endpoints: `GET /` with optional `@RequestParam String monthKey` (default = current month YearMonth.now()) → `ApiResponse<List<ProjectMonthRecordSummaryResponse>>`; `GET /{id}` → `ApiResponse<ProjectMonthRecordResponse>`; `PUT /{id}` with `@RequestBody @Valid ProjectMonthRecordRequest` → `ApiResponse<ProjectMonthRecordResponse>`; error codes: MONTHLY_RECORD_NOT_FOUND (404), MONTHLY_RECORD_INACTIVE (400)

- [X] T012 [P] [US1] Create `project-monthly-record.ts` TypeScript types at `FE/src/types/project-monthly-record.ts` — interfaces: `ProjectMonthRecordSummary` (7 fields matching summary DTO), `ProjectMonthRecordDetail` (all 40+ fields matching full response DTO including isFirstMonth, projectCode, projectName), `ProjectMonthRecordUpdateRequest` (manual fields only, all optional BigDecimal as number | null); export all

- [X] T013 [P] [US1] Create `projectMonthlyRecordService.ts` Axios service at `FE/src/services/projectMonthlyRecordService.ts` — methods: `getAll(monthKey?: string): Promise<ProjectMonthRecordSummary[]>` calling `GET /api/binance/project-monthly-records?monthKey=...`; `getById(id: string): Promise<ProjectMonthRecordDetail>` calling `GET /api/binance/project-monthly-records/{id}`; `update(id: string, request: ProjectMonthRecordUpdateRequest): Promise<ProjectMonthRecordDetail>` calling `PUT /api/binance/project-monthly-records/{id}`; handle `data.data` unwrapping from ApiResponse wrapper

- [X] T014 [US1] Create `MonthlyRecordDialog.vue` at `FE/src/components/project-management/MonthlyRecordDialog.vue` — PrimeVue Dialog (`:visible` prop + `update:visible`); header shows projectCode + projectName + monthKey; body has 6 `<Fieldset>` panels, one per group (Tồn đầu kỳ, Kế hoạch tháng, Thực hiện SLSX, Kế hoạch doanh thu, Nghiệm thu, Tồn cuối kỳ); manual fields use `<InputNumber>` (currency mode for VNĐ, suffix="%" for %); formula fields use read-only `<InputNumber :disabled="true">`; loading state on submit; emits `saved` after successful PUT; uses `projectMonthlyRecordService.update()`; **overwrite guard (constitution §9.3)**: when the loaded record has at least one non-null manual field (record already contains data), show a PrimeVue `ConfirmDialog` on submit with message "Bản ghi đã có dữ liệu. Bạn có muốn ghi đè không?" — only proceed with PUT after user confirms; if record is brand-new (all manual fields null), save directly without confirmation

- [X] T015 [US1] Extend `ProjectManagementView.vue` at `FE/src/views/ProjectManagementView.vue` — add month picker (PrimeVue DatePicker `view="month"` format `YYYY-MM`) defaulting to current month; add Pinia state or local `ref` for `selectedMonthKey`; add `<DataTable>` showing `ProjectMonthRecordSummary` rows with columns: Mã DA, Tên DA, Doanh thu KH (g4DoanhThu), Doanh thu NT (g5DoanhThu), Tổng SLNT (g5TongSlnt); on month change reload via `projectMonthlyRecordService.getAll(monthKey)`; on row click load full record via `getById()` and open `MonthlyRecordDialog`; refresh list after `saved` event

**Checkpoint**: US1 fully functional — users can view the summary table and edit any monthly record.

---

## Phase 4: User Story 2 — Tự Động Sinh Bản Ghi Tháng (Priority: P2)

**Goal**: Auto-generate records on project create; expand on monthEnd increase; warn + mark inactive on shrink.

**Independent Test**: Tạo dự án 3 tháng → kiểm tra 3 bản ghi sinh ra. Mở rộng monthEnd → 2 bản ghi mới. Rút ngắn → confirm dialog → bản ghi inactive biến khỏi danh sách.

- [X] T016 [US2] Add `generateRecordsForProject()` and `reactivateOrCreateRecords()` to `ProjectMonthRecordService.java` — `generateRecordsForProject(Project project)`: iterates YearMonth range [monthStart, monthEnd], calls `repo.findByProjectIdAndMonthKey()` for each month: if not found → create new record (active=true, all fields null); if found and inactive → reactivate (active=true); if found and active → skip; saves all new/reactivated records in batch; `markInactiveForShrink(UUID projectId, List<String> monthKeys)`: sets `active=false` for each given monthKey

- [X] T017 [US2] Modify `ProjectService.java` at `BE/src/main/java/com/internal/projectmgmt/service/ProjectService.java` — inject `ProjectMonthRecordService`; in `create()`: after saving project, call `projectMonthRecordService.generateRecordsForProject(project)` within same `@Transactional`; in `update()`: if new monthEnd > old monthEnd call `generateRecordsForProject` (existing records untouched, only new months added); if new monthEnd < old monthEnd or new monthStart > old monthStart: compute list of affected monthKeys, do NOT mark inactive yet — instead return warning

- [X] T018 [US2] Add shrink-confirm flow to `ProjectService.java` and `ProjectMonthRecordService.java` — `ProjectService.update()` accepts additional `boolean confirmShrink` parameter; if shrink detected AND `confirmShrink=false` (default): return special result object with code=MONTH_RANGE_SHRINK_WARNING and list of affected months (do NOT save project yet); if `confirmShrink=true`: call `projectMonthRecordService.markInactiveForShrink()` then save project normally; `markInactiveForShrink()` already implemented in T016

- [X] T019 [US2] Update `ProjectController.java` at `BE/src/main/java/com/internal/projectmgmt/controller/ProjectController.java` — add `@RequestParam(defaultValue="false") boolean confirmShrink` to `PUT /api/binance/projects/{id}`; pass to `projectService.update()`; when service returns MONTH_RANGE_SHRINK_WARNING: return `ResponseEntity` with `ApiResponse` code=MONTH_RANGE_SHRINK_WARNING, message, data containing `pendingInactiveMonths: List<String>`

- [X] T020 [US2] Extend `ProjectTab.vue` at `FE/src/components/project-settings/ProjectTab.vue` — in the existing project save handler: detect response code `MONTH_RANGE_SHRINK_WARNING`; if detected: show PrimeVue `ConfirmDialog` listing the affected months (data.pendingInactiveMonths), message "Bản ghi tháng sẽ bị đánh dấu inactive. Dữ liệu không bị xóa."; on confirm: re-call `projectService.update()` with `confirmShrink=true` appended to URL params; on cancel: stay in edit mode

**Checkpoint**: US2 complete — records auto-created on project save; shrink-confirm flow protects data.

---

## Phase 5: User Story 3 — Cascade Tồn Cuối Kỳ → Tồn Đầu Kỳ (Priority: P3)

**Goal**: Saving month N automatically propagates closing stock (g6) to opening stock (g1) of month N+1 in the same transaction; FE shows g1 as read-only for non-first months.

**Independent Test**: Nhập đầy đủ tháng 01/2026, lưu → mở tháng 02/2026 → 5 trường Tồn đầu kỳ khớp với Tồn cuối kỳ tháng 01. Thay đổi tháng 01 → lưu → mở lại tháng 02 → Tồn đầu kỳ cập nhật.

- [X] T021 [US3] Add `cascadeClosingToNextMonth()` to `ProjectMonthRecordService.java` — private method called at end of `update()` within same `@Transactional`; computes nextMonthKey = YearMonth.parse(monthKey).plusMonths(1).toString(); looks up next record with `repo.findByProjectIdAndMonthKey()`; if found: copies g6_ra_ton→g1_ra_ton, g6_slsx_ton_ht→g1_slsx_ton_tu_sx_ht_hd, g6_slsx_ton_dd→g1_slsx_ton_tu_sx_dd_hd, g6_slsx_os_ton→g1_slsx_os_ton, g6_slsx_os_ton_ht→g1_slsx_os_ton_ht on next record; then recalculates formulas for next record with `MonthlyCalculationService.calculateAndFill()` and saves — ensuring snapshot is fresh; note: `g1_slsx_ton_tu_sx_hd` is NOT cascaded (always manual)

- [X] T022 [US3] Add `isFirstMonth` detection to `ProjectMonthRecordService.java` — in `findById()`: query `repo.findFirstByProjectIdOrderByMonthKeyAsc(projectId)` to get the earliest monthKey for that project; `isFirstMonth = record.getMonthKey().equals(firstMonthKey)`; set on `ProjectMonthRecordResponse.isFirstMonth`; also expose via mapper in `toResponse()`

- [X] T023 [US3] Update `MonthlyRecordDialog.vue` at `FE/src/components/project-management/MonthlyRecordDialog.vue` — use `isFirstMonth` from loaded record detail; in Nhóm 1 (Tồn đầu kỳ) Fieldset: if `isFirstMonth=true`: all 6 g1 fields are editable InputNumber; if `isFirstMonth=false`: g1_ra_ton, g1_slsx_ton_tu_sx_ht_hd, g1_slsx_ton_tu_sx_dd_hd, g1_slsx_os_ton, g1_slsx_os_ton_ht rendered as read-only (`:disabled="true"`) showing auto-populated values from cascade; g1_slsx_ton_tu_sx_hd remains editable for all months; add helper text below read-only fields: "Tự động từ Tồn cuối kỳ tháng trước"

- [X] T025 [US3] Create `ProjectMonthRecordServiceIntegrationTest.java` at `BE/src/test/java/com/internal/projectmgmt/service/ProjectMonthRecordServiceIntegrationTest.java` — `@SpringBootTest` + `@Transactional` integration tests (constitution §4.4 MUST); test cases: (a) **cascade**: save record for month N with g3_ra=100 → verify month N+1 g1_ra_ton auto-updated to g6_ra_ton value in same transaction; (b) **generate**: call `generateRecordsForProject()` for project with 3-month range → verify exactly 3 records created with active=true; (c) **reactivate**: create 3 records, mark month 3 inactive, call generateRecordsForProject with same 3-month range → verify month 3 reactivated (not duplicated); (d) **shrink**: call `markInactiveForShrink()` → verify targeted records have active=false and untargeted records still active=true; use H2 or Testcontainers PostgreSQL per project test config

**Checkpoint**: US3 complete — cascade chain Tồn cuối kỳ ↔ Tồn đầu kỳ fully functional across all months. Cross-month integration tests satisfy constitution §4.4.

---

## Phase 6: Polish & Cross-Cutting Concerns

- [X] T024 Add `@Tag` and `@Operation` annotations to `ProjectMonthRecordController.java` at `BE/src/main/java/com/internal/projectmgmt/controller/ProjectMonthRecordController.java` — annotate all 3 endpoints with summary and description matching the API contract in `specs/003-project-monthly-records/contracts/project-monthly-records-api.md`

---

## Dependencies

```
T001 → T002 → T003
             → T004 (parallel with T003)
             → T005 (parallel with T003)
             → T006 (parallel with T003)
T003 + T004 + T005 + T006 → T007
T002 → T008 → T009
T003 + T007 + T008 → T010 → T011

T012 (parallel, no BE dependency)
T012 → T013
T013 → T014 → T015

T010 → T016 → T017 → T018 → T019
T019 ← depends on T020 (FE parallel)
T010 → T021 → T022
T021 → T025 (integration test for cascade)
T014 → T023

T011 → T024 (polish)
```

**User Story order**: US1 → US2 → US3 (each story depends on previous)

## Parallel Execution Opportunities

**Within Phase 2**:
- T004, T005, T006 can all be written simultaneously (3 separate DTO files)
- T009 (tests) can be written in parallel with T004–T007 once T008 is done

**Between Phases 3–4**:
- FE tasks T012 + T013 can start as soon as contract is clear (before BE is finished)
- T014 can start once T012 done (mock data for development)

## Implementation Strategy

**MVP = Phase 1 + Phase 2 + Phase 3 (US1)**

Delivers: Working `/projects` screen with month filter, summary table, and 6-group edit dialog. All formulas computed by backend. User can view and update any monthly record.

**Increment 2 = Phase 4 (US2)**: Auto-generation makes the system self-maintaining — records appear automatically when projects are created/updated.

**Increment 3 = Phase 5 (US3)**: Cascade logic ensures data integrity across months — the most complex business rule, built on top of working CRUD.

## Summary

| Phase | Tasks | Scope |
|-------|-------|-------|
| Phase 1 (Setup) | 1 | Flyway migration |
| Phase 2 (Foundational) | 8 | Entity, Repo, DTOs, Mapper, Calc service + tests |
| Phase 3 (US1) | 6 | CRUD endpoints + FE table + dialog |
| Phase 4 (US2) | 5 | Auto-generate + shrink-confirm flow |
| Phase 5 (US3) | 4 | Cascade + isFirstMonth logic + integration tests |
| Phase 6 (Polish) | 1 | OpenAPI docs |
| **Total** | **25** | |

**Formula fields covered by tests (T009)**: g2_tong_slsx_du_kien, g3_ee, g4_tong, g4_doanh_thu, g5_tong_slnt, g5_doanh_thu, g6_ra_ton, g6_slsx_ton_ht, g6_slsx_ton_dd, g6_slsx_os_ton, g6_slsx_os_ton_ht — **11 formula fields, 100% coverage target**
