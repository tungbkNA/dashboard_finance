# Tasks: Cài Đặt Dự Án (Project Settings)

**Feature**: `002-project-settings`
**Input**: [spec.md](./spec.md) · [plan.md](./plan.md) · [data-model.md](./data-model.md) · [research.md](./research.md) · [contracts/](./contracts/)
**Prerequisites**: All design artifacts complete ✅

---

## Phase 1: Setup

**Purpose**: Project initialization — Flyway migration and enum types that unblock all entity work.

- [X] T001 Create Flyway migration `BE/src/main/resources/db/migration/V2__project_settings_schema.sql` (PostgreSQL enums + project_type + customer + project tables + partial unique indexes)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core backend building blocks that MUST be complete before any user story's service/controller work can begin.

⚠️ **CRITICAL**: Phases 3–5 depend on this phase being complete.

- [X] T002 [P] Create enum `BE/src/main/java/com/internal/projectmgmt/entity/StatusProject.java` (OPEN, INPROGRESS, PENDING, DONE, CLOSE)
- [X] T003 [P] Create enum `BE/src/main/java/com/internal/projectmgmt/entity/StatusContract.java` (NO_CONTRACT, HAS_CONTRACT)
- [X] T004 [P] Create custom Bean Validation annotation `BE/src/main/java/com/internal/projectmgmt/validation/MonthYear.java` + validator `MonthYearValidator.java` (regex `^(0[1-9]|1[0-2])/[2-9][0-9]{3}$`)
- [X] T005 [P] Create entity `BE/src/main/java/com/internal/projectmgmt/entity/ProjectType.java` (@Entity, @Table("project_type"), Lombok, deleted flag)
- [X] T006 [P] Create entity `BE/src/main/java/com/internal/projectmgmt/entity/Customer.java` (@Entity, @Table("customer"), Lombok, deleted flag)
- [X] T007 Create entity `BE/src/main/java/com/internal/projectmgmt/entity/Project.java` (@Entity, @Table("project"), FK to Customer + ProjectType, BigDecimal price, enum fields, deleted flag) — depends on T002 T003 T005 T006
- [X] T008 [P] Create `BE/src/main/java/com/internal/projectmgmt/repository/ProjectTypeRepository.java` (findAllByDeletedFalse, findById(UUID) [no deleted filter — used by ProjectMapper to resolve names for soft-deleted entities, required by FR-PT-008], findByIdAndDeletedFalse, existsByKeyIgnoreCaseAndDeletedFalse)
- [X] T009 [P] Create `BE/src/main/java/com/internal/projectmgmt/repository/CustomerRepository.java` (findAllByDeletedFalse, findById(UUID) [no deleted filter — used by ProjectMapper to resolve names for soft-deleted entities, required by FR-CUS-008], findByIdAndDeletedFalse, existsByCustomerCodeIgnoreCaseAndDeletedFalse)
- [X] T010 Create `BE/src/main/java/com/internal/projectmgmt/repository/ProjectRepository.java` (findAllByDeletedFalse, findByIdAndDeletedFalse, existsByProjectCodeIgnoreCaseAndDeletedFalse, countByCustomerIdAndDeletedFalse, countByProjectTypeIdAndDeletedFalse) — depends on T007
- [X] T011 [P] Create TypeScript types `FE/src/types/project-settings.ts` (ProjectResponse, ProjectRequest, ProjectTypeResponse, ProjectTypeRequest, CustomerResponse, CustomerRequest, DeleteCheckResponse)
- [X] T039 Initialize `FE/src/views/ProjectSettingsView.vue` shell with PrimeVue `<TabView>` + 3 `<TabPanel>` components (tab icons: pi-folder for Dự án, pi-tags for Loại dự án, pi-users for Khách hàng) — replaces skeleton — depends on T011

**Checkpoint**: Foundation ready — entity, repository, FE types, and TabView shell complete. Phases 3–5 tab implementation can now begin.

---

## Phase 3: User Story 1 — Quản Lý Danh Sách Dự Án (P1) 🎯 MVP

**Goal**: Người dùng có thể xem, tạo, sửa, xóa (soft) dự án với đầy đủ validation.

**Independent Test**: Start BE + FE → open `/project-settings` tab "Dự án" → create a project → edit it → delete it → verify it disappears. Validation: try duplicate projectCode → error toast. Try monthEnd < monthStart → form error.

### Tests for User Story 1

- [X] T012 [P] [US1] Create `BE/src/test/java/com/internal/projectmgmt/service/ProjectServiceTest.java` — test: duplicate projectCode throws AppException(PROJECT_CODE_DUPLICATE)
- [X] T013 [P] [US1] Add test to `ProjectServiceTest` — test: monthEnd < monthStart throws AppException(MONTH_RANGE_INVALID)

### Implementation for User Story 1

- [X] T014 [P] [US1] Create DTO `BE/src/main/java/com/internal/projectmgmt/dto/project/ProjectRequest.java` (Bean Validation: @NotBlank, @DecimalMin("0"), @MonthYear, @NotNull for enums and FKs)
- [X] T015 [P] [US1] Create DTO `BE/src/main/java/com/internal/projectmgmt/dto/project/ProjectResponse.java` (id, projectCode, projectName, customerId, customerName, projectTypeId, projectTypeName, price, statusContract, statusProject, monthStart, monthEnd, createdAt, updatedAt)
- [X] T016 [US1] Create mapper `BE/src/main/java/com/internal/projectmgmt/mapper/ProjectMapper.java` (toEntity, toResponse — resolves customerName + projectTypeName) — depends on T014 T015
- [X] T017 [US1] Create `BE/src/main/java/com/internal/projectmgmt/service/ProjectService.java` (findAll, findById, create, update, softDelete — uniqueness check, monthEnd>=monthStart check, usage count for delete warning) — depends on T010 T016
- [X] T018 [US1] Create `BE/src/main/java/com/internal/projectmgmt/controller/ProjectController.java` (GET /api/binance/projects, GET /{id}, POST, PUT /{id}, DELETE /{id}?confirmed=true) — depends on T017
- [X] T019 [P] [US1] Create FE service `FE/src/services/projectService.ts` (getAll, create, update, softDelete) — depends on T011
- [X] T020 [US1] Implement Tab 1 "Dự án" inside `FE/src/views/ProjectSettingsView.vue` (DataTable with columns: mã, tên, loại, khách hàng, trạng thái HĐ, trạng thái DA, tháng bắt đầu, tháng kết thúc, actions; create/edit Dialog with all 9 required fields including dropdowns for statusProject/statusContract/customerId/projectTypeId; ConfirmDialog for soft delete; LoadingState/EmptyState/ErrorState) — depends on T019 T039

**Checkpoint**: US1 fully functional. Projects CRUD works end-to-end with validation.

---

## Phase 4: User Story 2 — Quản Lý Loại Dự Án (P2)

**Goal**: Người dùng có thể quản lý danh sách loại dự án (key-value) dùng làm dropdown trong form dự án.

**Independent Test**: Open tab "Loại dự án" → create a new type → verify it appears in project creation dropdown → delete unused type → verify it disappears. Try duplicate key → error toast. Delete in-use type → warning dialog → confirm → type soft-deleted.

### Tests for User Story 2

- [X] T021 [P] [US2] Create `BE/src/test/java/com/internal/projectmgmt/service/ProjectTypeServiceTest.java` — test: duplicate key throws AppException(PROJECT_TYPE_KEY_DUPLICATE)
- [X] T022 [P] [US2] Add test to `ProjectTypeServiceTest` — test: softDelete of in-use ProjectType returns inUse=true without deleting; ?confirmed=true soft-deletes

### Implementation for User Story 2

- [X] T023 [P] [US2] Create DTO `BE/src/main/java/com/internal/projectmgmt/dto/projecttype/ProjectTypeRequest.java` (@NotBlank key, @NotBlank value, key pattern `^[A-Za-z0-9_-]{1,50}$`)
- [X] T024 [P] [US2] Create DTO `BE/src/main/java/com/internal/projectmgmt/dto/projecttype/ProjectTypeResponse.java` (id, key, value)
- [X] T025 [P] [US2] Create mapper `BE/src/main/java/com/internal/projectmgmt/mapper/ProjectTypeMapper.java`
- [X] T026 [US2] Create `BE/src/main/java/com/internal/projectmgmt/service/ProjectTypeService.java` (findAll, create, update, softDelete with usage check returning IN_USE_WARNING / confirmed soft delete) — depends on T008 T023 T024 T025
- [X] T027 [US2] Create `BE/src/main/java/com/internal/projectmgmt/controller/ProjectTypeController.java` (GET /api/binance/project-types, POST, PUT /{id}, DELETE /{id}?confirmed=true) — depends on T026
- [X] T028 [P] [US2] Create FE service `FE/src/services/projectTypeService.ts` (getAll, create, update, softDelete(id, confirmed?)) — depends on T011
- [X] T029 [US2] Implement Tab 2 "Loại dự án" inside `FE/src/views/ProjectSettingsView.vue` (DataTable columns: key, value, actions; Dialog form key+value; delete: if IN_USE_WARNING → extra confirm dialog "Loại dự án đang được N dự án sử dụng, xác nhận xóa?" → call again with confirmed=true; LoadingState/EmptyState/ErrorState) — depends on T028 T039

**Checkpoint**: US2 fully functional. ProjectType CRUD + in-use warning delete works. Dropdown in US1 project form now populated.

---

## Phase 5: User Story 3 — Quản Lý Khách Hàng (P3)

**Goal**: Người dùng có thể quản lý danh sách khách hàng dùng làm dropdown trong form dự án.

**Independent Test**: Open tab "Khách hàng" → create a new customer → verify it appears in project creation dropdown → delete unused customer → verify it disappears. Try duplicate customerCode → error toast. Delete in-use customer → warning dialog → confirm → customer soft-deleted.

### Tests for User Story 3

- [X] T030 [P] [US3] Create `BE/src/test/java/com/internal/projectmgmt/service/CustomerServiceTest.java` — test: duplicate customerCode throws AppException(CUSTOMER_CODE_DUPLICATE)
- [X] T031 [P] [US3] Add test to `CustomerServiceTest` — test: softDelete of in-use Customer returns inUse=true without deleting; ?confirmed=true soft-deletes

### Implementation for User Story 3

- [X] T032 [P] [US3] Create DTO `BE/src/main/java/com/internal/projectmgmt/dto/customer/CustomerRequest.java` (@NotBlank customerCode, @NotBlank customerName, code pattern `^[A-Za-z0-9_-]{1,50}$`)
- [X] T033 [P] [US3] Create DTO `BE/src/main/java/com/internal/projectmgmt/dto/customer/CustomerResponse.java` (id, customerCode, customerName)
- [X] T034 [P] [US3] Create mapper `BE/src/main/java/com/internal/projectmgmt/mapper/CustomerMapper.java`
- [X] T035 [US3] Create `BE/src/main/java/com/internal/projectmgmt/service/CustomerService.java` (findAll, create, update, softDelete with usage check returning IN_USE_WARNING / confirmed soft delete) — depends on T009 T032 T033 T034
- [X] T036 [US3] Create `BE/src/main/java/com/internal/projectmgmt/controller/CustomerController.java` (GET /api/binance/customers, POST, PUT /{id}, DELETE /{id}?confirmed=true) — depends on T035
- [X] T037 [P] [US3] Create FE service `FE/src/services/customerService.ts` (getAll, create, update, softDelete(id, confirmed?)) — depends on T011
- [X] T038 [US3] Implement Tab 3 "Khách hàng" inside `FE/src/views/ProjectSettingsView.vue` (DataTable columns: mã khách hàng, tên khách hàng, actions; Dialog form; in-use warning delete pattern same as Tab 2; LoadingState/EmptyState/ErrorState) — depends on T037 T039

**Checkpoint**: All 3 user stories functional. Full CRUD for Project, ProjectType, Customer with soft delete, validation, and in-use warnings.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Final integration, dropdown wiring, and constitution compliance validation.

- [X] T040 [P] Wire `FE/src/views/ProjectSettingsView.vue` — ensure project form dropdowns load data from projectTypeService + customerService on tab mount (both active lists)
- [X] T041 [P] Verify CORS coverage — `BE/src/main/java/com/internal/projectmgmt/config/CorsConfig.java` already covers `/api/binance/**`; add comment confirming new routes covered
- [X] T042 Run quickstart manual test: start BE + FE, create 1 ProjectType + 1 Customer + 1 Project, edit Project, soft-delete ProjectType (in-use warning), soft-delete Project

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: No dependencies — start immediately
- **Phase 2 (Foundational)**: Depends on Phase 1 — BLOCKS phases 3/4/5
- **Phase 3 (US1 — MVP)**: Depends on Phase 2
- **Phase 4 (US2)**: Depends on Phase 2 — can run in parallel with Phase 3
- **Phase 5 (US3)**: Depends on Phase 2 — can run in parallel with Phases 3/4
- **Phase 6 (Polish)**: Depends on Phases 3/4/5 complete

### User Story Dependencies

- **US1 (P1)**: Depends on entities + repos from Phase 2 only. No dependency on US2 or US3.
- **US2 (P2)**: Depends on Phase 2 only. US1 project form will use US2 data once US2 is done.
- **US3 (P3)**: Depends on Phase 2 only. Same pattern as US2.

### Parallel Opportunities Within Phases

**Phase 2** — all can run in parallel after T001:
- T002, T003, T004, T005, T006 (no interdependencies)
- T007 after T002+T003+T005+T006
- T008, T009 after T005+T006
- T010 after T007
- T011 (FE types — fully independent of BE)

**Phase 3** (US1):
- T012, T013 (tests — write first)
- T014, T015 (DTOs — parallel)
- T019 (FE service — independent of BE impl)

**Phase 4** (US2):
- T021, T022 (tests)
- T023, T024, T025 (DTO + mapper — parallel)
- T028 (FE service)

**Phase 5** (US3):
- T030, T031 (tests)
- T032, T033, T034 (DTO + mapper — parallel)
- T037 (FE service)

---

## Implementation Strategy

**MVP Scope** (US1 only — Phases 1, 2, 3, and Phase 6):
- 24 tasks: T001–T020, T039–T042
- Delivers: Full project CRUD with validation, soft delete, 3-tab shell (tabs 2+3 visible but empty)

**Full scope**: All 42 tasks across all 6 phases.

**Suggested order for solo developer**:
1. Phase 1 → Phase 2 → Phase 3 (MVP working)
2. Phase 4 (adds ProjectType tab + populates project dropdown)
3. Phase 5 (adds Customer tab + populates project dropdown)
4. Phase 6 (final wiring + validation)
