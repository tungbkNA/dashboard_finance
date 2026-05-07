---
description: "Task list for Feature 005 — Cross-Month Recalculation"
---

# Tasks: Feature 005 — Kiểm Tra Ảnh Hưởng Liên Tháng & Cập Nhật Cascade

**Input**: Design documents from `specs/005-cross-month-recalculation/`
**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅, contracts/ ✅

**Tests**: Included — 5 BE test scenarios explicitly required by spec + plan.md. No FE tests in scope.

**Organization**: Tasks grouped by user story to enable independent implementation and testing.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no incomplete dependencies)
- **[US1/US2/US3]**: User story from spec.md
- Exact file paths included in every description

---

## Phase 1: Setup

**Purpose**: DB migration — enables all subsequent phases

- [X] T001 Create Flyway V5 migration with `locked` column + `field_change_audit_log` table in `BE/src/main/resources/db/migration/V5__add_locked_and_audit_log.sql`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: BE data layer — entities, repositories, DTOs, and field-metadata reconciliation required by US2 & US3. No user story work can begin until this phase is complete.

**⚠️ CRITICAL**: T002–T008 must complete before CrossMonthPropagationService can be implemented

- [X] T002 [P] Add `locked boolean NOT NULL DEFAULT false` field + `@Builder.Default` to `ProjectMonthRecord` entity in `BE/src/main/java/com/internal/projectmgmt/entity/ProjectMonthRecord.java`
- [X] T003 [P] Create `FieldChangeAuditLog` JPA entity with all columns and `@PrePersist` hook in `BE/src/main/java/com/internal/projectmgmt/entity/FieldChangeAuditLog.java`
- [X] T004 Create `FieldChangeAuditLogRepository` extending `JpaRepository<FieldChangeAuditLog, UUID>` with `boolean existsByEventId(String eventId)` query in `BE/src/main/java/com/internal/projectmgmt/repository/FieldChangeAuditLogRepository.java`
- [X] T005 [P] Create `CrossMonthPropagationResult` DTO with `List<String> affectedMonthKeys` and `StoppedReason` enum (`NO_MORE_MONTHS`, `INACTIVE_MONTH`, `LOCKED_MONTH`, `NO_CHANGE_DETECTED`) in `BE/src/main/java/com/internal/projectmgmt/dto/monthlyrecord/CrossMonthPropagationResult.java`
- [X] T006 [P] Add `private int affectedMonths` field (default `0`) to `ProjectMonthRecordResponse` in `BE/src/main/java/com/internal/projectmgmt/dto/monthlyrecord/ProjectMonthRecordResponse.java`
- [X] T007 Remove `"g1SlsxTonTuSxHd"` from `cascadedFromPrevMonthFields` list for G1 group in `getFieldMetadata()` to comply with FR-008 in `BE/src/main/java/com/internal/projectmgmt/service/ProjectMonthRecordService.java`
- [X] T008 Update field metadata test to assert `cascadedFromPrevMonthFields` has size 5 and does NOT contain `"g1SlsxTonTuSxHd"` in `BE/src/test/java/com/internal/projectmgmt/controller/ProjectMonthRecordControllerFieldMetadataTest.java`
- [X] T028 Add `locked` guard to `ProjectMonthRecordService.update()`: after loading the record, check `record.isLocked()` and throw `new AppException("MONTHLY_RECORD_LOCKED", "Bản ghi tháng này đã bị khóa và không thể chỉnh sửa")` if true. Add corresponding 400 handling in `ProjectMonthRecordController.update()` in `BE/src/main/java/com/internal/projectmgmt/service/ProjectMonthRecordService.java` and `BE/src/main/java/com/internal/projectmgmt/controller/ProjectMonthRecordController.java`

**Checkpoint**: Foundation ready — US1 FE work and US2 BE work can begin in parallel

---

## Phase 3: User Story 1 — Cảnh Báo Trước Khi Ghi Đè (Priority: P1) 🎯 MVP

**Goal**: FE hiển thị ConfirmDialog trước khi ghi đè dữ liệu đã có; hủy không gọi API

**Independent Test**: Mở bản ghi tháng đã có dữ liệu → sửa trường nhập tay → nhấn Save → ConfirmDialog xuất hiện → nhấn "Hủy" → không có API call, form giữ nguyên → nhấn "Xác nhận" → API được gọi, dữ liệu được lưu

- [X] T009 [P] [US1] Add `affectedMonths: number` field to `ProjectMonthRecordDetail` interface in `FE/src/types/project-monthly-record.ts`
- [X] T010 [US1] Add ConfirmDialog (PrimeVue `useConfirm` + `<ConfirmDialog>`) to group save handler in `ProjectCard.vue`: show when any manual field in the group has non-null value in `detail`, return early without API call when user dismisses in `FE/src/components/project-management/ProjectCard.vue`
- [X] T011 [US1] Add `ConfirmDialog` PrimeVue component import and `<ConfirmDialog>` tag to template in `FE/src/components/project-management/ProjectCard.vue`

**Checkpoint**: User Story 1 fully functional — ConfirmDialog appears and cancel prevents save

---

## Phase 4: User Story 2 — Cascade Recalculation Liên Tháng (Priority: P2)

**Goal**: BE tự động propagate G6→G1 chain sau khi save; toàn bộ chain trong một `@Transactional`; `affectedMonths` returned

**Independent Test**: Tạo project 3 tháng liên tiếp → sửa trường nhập tay ở tháng 1 đủ để thay đổi G6 → confirm save → kiểm tra tháng 2 và 3 đã được cập nhật G1 đúng theo mapping → kiểm tra `affectedMonths = 2`

### Tests for User Story 2

- [X] T012 [US2] Create `CrossMonthPropagationServiceTest` class with `@SpringBootTest` setup, test data builder helper, and test: sửa tháng đầu tiên → G6 thay đổi → 2 tháng kế tiếp bị cập nhật → `affectedMonths = 2` in `BE/src/test/java/com/internal/projectmgmt/service/CrossMonthPropagationServiceTest.java`
- [X] T013 [US2] Add test: sửa tháng giữa (không phải tháng đầu) → chỉ các tháng sau tháng đó bị cập nhật; tháng trước không bị ảnh hưởng in `BE/src/test/java/com/internal/projectmgmt/service/CrossMonthPropagationServiceTest.java`
- [X] T014 [US2] Add test: sửa trường G2/G3/G4/G5 không làm thay đổi G6 → `affectedMonths = 0`, không tháng nào bị cập nhật in `BE/src/test/java/com/internal/projectmgmt/service/CrossMonthPropagationServiceTest.java`
- [X] T015 [US2] Add test: lỗi DB ở tháng T+1 → `@Transactional` rollback toàn bộ, origin record không được lưu in `BE/src/test/java/com/internal/projectmgmt/service/CrossMonthPropagationServiceTest.java`

### Implementation for User Story 2

- [X] T016 [US2] Create `CrossMonthPropagationService` with `propagateFrom(origin, eventId)`, `snapshotG6()`, `applyG6ToG1()` (5-field mapping per FR-008, excluding `g1SlsxTonTuSxHd`), and `g6Unchanged()` in `BE/src/main/java/com/internal/projectmgmt/service/CrossMonthPropagationService.java`
- [X] T017 [US2] Refactor `ProjectMonthRecordService.update()`: add G6 snapshot before/after, remove `cascadeClosingToNextMonth()` call, inject and call `propagationService.propagateFrom()`, update `mapper.toResponse()` call to include `affectedMonths` in `BE/src/main/java/com/internal/projectmgmt/service/ProjectMonthRecordService.java`
- [X] T018 [US2] Update `ProjectMonthRecordMapper.toResponse()` to accept `int affectedMonths` parameter and map it to response in `BE/src/main/java/com/internal/projectmgmt/mapper/ProjectMonthRecordMapper.java`
- [X] T019 [US2] Remove the now-deleted `cascadeClosingToNextMonth()` private method from `ProjectMonthRecordService` in `BE/src/main/java/com/internal/projectmgmt/service/ProjectMonthRecordService.java`

**Checkpoint**: User Stories 1 + 2 complete — ConfirmDialog + save triggers cascade propagation; API returns `affectedMonths`

---

## Phase 5: User Story 3 — Thông Báo Kết Quả & Audit Trail (Priority: P3)

**Goal**: Audit trail ghi nhận mỗi field-level thay đổi trong chain; FE toast hiển thị số tháng bị ảnh hưởng; idempotent replay không tạo duplicate audit

**Independent Test**: Trigger propagation 2 tháng → kiểm tra toast "Lưu thành công. Đã cập nhật thêm 2 tháng liên quan." → query `field_change_audit_log` → xác nhận có đúng records với project_id, month_key, field_name, old_value, new_value, triggered_by_month_key đúng → chạy lại cùng request → không có audit record mới

### Tests for User Story 3

- [X] T020 [US3] Add test: chạy lại cùng request (idempotent) → `affectedMonths = 0` lần 2; số rows trong `field_change_audit_log` không tăng in `BE/src/test/java/com/internal/projectmgmt/service/CrossMonthPropagationServiceTest.java`

### Implementation for User Story 3 (BE)

- [X] T021 [US3] Add `writeAuditLogs()` helper to `CrossMonthPropagationService`: for each changed G6 field, persist a `FieldChangeAuditLog` record with project_id, month_key, field_name, old_value, new_value, triggered_by_month_key, event_id in `BE/src/main/java/com/internal/projectmgmt/service/CrossMonthPropagationService.java`
- [X] T022 [US3] Add idempotency check to `CrossMonthPropagationService.propagateFrom()`: call `auditLogRepository.existsByEventId(eventId)` before processing; if already processed, return empty result immediately in `BE/src/main/java/com/internal/projectmgmt/service/CrossMonthPropagationService.java`

### Implementation for User Story 3 (FE)

- [X] T023 [US3] Update group save success handler in `ProjectCard.vue`: if `result.data.affectedMonths > 0` show toast "Lưu thành công. Đã cập nhật thêm {N} tháng liên quan.", else show "Lưu thành công." in `FE/src/components/project-management/ProjectCard.vue`
- [X] T024 [US3] Update group save error handler in `ProjectCard.vue`: on API error show error toast with BE message and do NOT update local detail data in `FE/src/components/project-management/ProjectCard.vue`

**Checkpoint**: All 3 user stories complete — ConfirmDialog + cascade chain + audit trail + toast with count

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Verification, cleanup, and final integration checks

- [X] T025 Run `mvn test` in `BE/` and confirm all tests pass including updated field metadata test and all 5 `CrossMonthPropagationServiceTest` scenarios
- [X] T026 [P] Verify `locked = true` stopping condition works: manually set a record to locked in DB, trigger propagation chain that would reach it → confirm chain stops at that month and does not overwrite it in `BE/src/test/java/com/internal/projectmgmt/service/CrossMonthPropagationServiceTest.java`
- [X] T027 [P] Verify `active = false` stopping condition in propagation — add assertion to existing `CrossMonthPropagationServiceTest` setup in `BE/src/test/java/com/internal/projectmgmt/service/CrossMonthPropagationServiceTest.java`

---

## Dependencies

```
T001 (migration)
  └─► T002, T003 (entity changes — parallel)
        ├─► T004 (repo — depends T003)
        ├─► T005 (DTO — parallel with T002/T003)
        ├─► T006 (response DTO — parallel)
        └─► T007 → T008 (metadata reconciliation + test fix)
        └─► T028 (locked guard in update() — depends T002)

T002+T003+T004+T005+T006 (all complete)
  └─► T016 (CrossMonthPropagationService)
        └─► T017 (ProjectMonthRecordService refactor)
              └─► T018 (mapper update)
              └─► T019 (remove old cascade method)

T009 (FE types) — parallel with Phase 2 BE work
T010 → T011 (ConfirmDialog impl, sequential in same file)

T016 (propagation service)
  └─► T012 → T013 → T014 → T015 (tests, sequential in same file)

T016+T021+T022 (propagation + audit)
  └─► T020 (idempotency test)
  └─► T023 → T024 (FE toast, sequential in same file)

T012+T013+T014+T015+T020 + T026+T027
  └─► T025 (mvn test — final verification)
```

## Parallel Execution Examples

**After Phase 1 completes (T001)**:
- BE Team A: T002, T003 → T004 (entity + repo layer)
- BE Team B: T005, T006 → T007 → T008 (DTOs + metadata fix)
- FE Team: T009 → T010 → T011 (FE types + ConfirmDialog)

**After Phase 2 completes**:
- BE: T016 → T017 → T018 → T019 (propagation core)
- Parallel: T012–T015 tests after T016

## Implementation Strategy

**MVP scope (Phase 1 + 2 + Phase 3 US1 + Phase 4 US2)**: T001–T019
- DB migration + entities + propagation chain + FE ConfirmDialog = fully functional core
- US3 (audit trail + idempotency) can be added incrementally without breaking US1/US2

**Deliver US1 first**: ConfirmDialog (T009–T011) is fully independent of BE propagation changes — can be developed and tested immediately after Phase 2 foundational work.

**Critical path**: T001 → T002/T003 → T016 → T017 → T025
**FR-015 path**: T001 → T002 → T028
