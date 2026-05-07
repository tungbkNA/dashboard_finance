# Tasks: Sổ tay trung tâm

**Input**: Design documents from `specs/007-central-handbook/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/

**Tests**: Unit test included for FileGroupService delete-with-FK logic (per user request).

**Organization**: Tasks grouped by user story. US1 and US2 are both P1 but US1 must complete first (FileGroup is FK dependency for FileRecord).

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story (US1, US2, US3)
- Exact file paths included

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Database schema, seed data, and shared entity/DTO foundation

- [x] T001 Create Flyway migration `BE/src/main/resources/db/migration/V10__file_group_and_record_schema.sql` with file_group and file_record tables, FK constraint, indexes
- [x] T002 Create Flyway migration `BE/src/main/resources/db/migration/V11__seed_file_groups_and_permission.sql` with 3 default file groups + MANAGE_HANDBOOK permission + admin role assignment
- [x] T003 [P] Create FileGroup entity in `BE/src/main/java/com/internal/projectmgmt/entity/FileGroup.java` with UUID PK, name (unique), description, active, createdAt/updatedAt, @PrePersist/@PreUpdate
- [x] T004 [P] Create FileRecord entity in `BE/src/main/java/com/internal/projectmgmt/entity/FileRecord.java` with UUID PK, fileName, fileUrl, @ManyToOne FileGroup (LAZY), createdBy, createdAt/updatedAt
- [x] T005 [P] Create FE TypeScript types in `FE/src/types/handbook.ts` — FileGroupResponse, FileGroupRequest, FileGroupUpdateRequest, FileRecordResponse, FileRecordRequest, FileRecordPageResponse

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Repositories shared by multiple user stories

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [x] T006 [P] Create FileGroupRepository in `BE/src/main/java/com/internal/projectmgmt/repository/FileGroupRepository.java` — findAll, findByActiveTrue, existsByNameIgnoreCase, countFileRecordsByGroupId
- [x] T007 [P] Create FileRecordRepository in `BE/src/main/java/com/internal/projectmgmt/repository/FileRecordRepository.java` — findAll with search/filter/pagination using @Query JPQL, countByFileGroupId

**Checkpoint**: Foundation ready — user story implementation can begin

---

## Phase 3: User Story 1 — Quản lý nhóm file (Priority: P1) 🎯 MVP

**Goal**: CRUD complète cho nhóm file (FileGroup) với DataTable + Dialog, permission MANAGE_HANDBOOK

**Independent Test**: Truy cập "Quản lý nhóm file", tạo/sửa/xóa nhóm, xác nhận bảng hiển thị đúng. Xóa nhóm đang có file → bị từ chối.

### Unit Test

- [x] T008 [US1] Create unit test `BE/src/test/java/com/internal/projectmgmt/service/FileGroupServiceTest.java` — test delete nhóm có file (expect AppException), delete nhóm rỗng (success), create trùng tên (expect AppException)

### Backend Implementation

- [x] T009 [P] [US1] Create FileGroupRequest DTO in `BE/src/main/java/com/internal/projectmgmt/dto/filegroup/FileGroupRequest.java` — record with @NotBlank name (max 100), @Size description (max 255)
- [x] T010 [P] [US1] Create FileGroupUpdateRequest DTO in `BE/src/main/java/com/internal/projectmgmt/dto/filegroup/FileGroupUpdateRequest.java` — record extending FileGroupRequest + boolean active
- [x] T011 [P] [US1] Create FileGroupResponse DTO in `BE/src/main/java/com/internal/projectmgmt/dto/filegroup/FileGroupResponse.java` — record with id, name, description, active, fileCount, createdAt, updatedAt
- [x] T012 [US1] Create FileGroupMapper in `BE/src/main/java/com/internal/projectmgmt/mapper/FileGroupMapper.java` — toEntity(request), toResponse(entity, fileCount)
- [x] T013 [US1] Create FileGroupService in `BE/src/main/java/com/internal/projectmgmt/service/FileGroupService.java` — findAll (with fileCount), findAllActive (id+name only), create (unique name check), update, delete (FK check → AppException if in use)
- [x] T014 [US1] Create FileGroupController in `BE/src/main/java/com/internal/projectmgmt/controller/FileGroupController.java` — GET /, GET /active, POST /, PUT /{id}, DELETE /{id}, all @PreAuthorize("hasAuthority('MANAGE_HANDBOOK')")

### Frontend Implementation

- [x] T015 [P] [US1] Create fileGroupService in `FE/src/services/fileGroupService.ts` — getAll, getActive, create, update, delete API calls
- [x] T016 [US1] Create FileGroupDialog component in `FE/src/components/handbook/FileGroupDialog.vue` — Dialog with horizontal form layout (label 100px), fields: Tên nhóm, Mô tả, Trạng thái (ToggleButton, edit only), validation, Toast on save
- [x] T017 [US1] Create FileGroupView in `FE/src/views/handbook/FileGroupView.vue` — DataTable with columns (Tên nhóm, Mô tả, Trạng thái, Số file, Thao tác), add/edit/delete buttons, confirm dialog for delete, empty state, loading state, error state

### Integration

- [x] T018 [US1] Add routes for handbook module in `FE/src/router/index.ts` — /handbook/file-groups (permission: MANAGE_HANDBOOK), /handbook/files (permission: MANAGE_HANDBOOK), lazy-loaded
- [x] T019 [US1] Add "Sổ tay trung tâm" menu section in `FE/src/components/AppSidebar.vue` — 2 sub-items: "Quản lý nhóm file" (/handbook/file-groups) and "Danh mục file" (/handbook/files), permission MANAGE_HANDBOOK

**Checkpoint**: User Story 1 fully functional — nhóm file CRUD works end-to-end

---

## Phase 4: User Story 2 — Quản lý bản ghi file (Priority: P1)

**Goal**: CRUD cho bản ghi file (FileRecord) với DataTable + Dialog, link mở tab mới, dropdown nhóm file, createdBy tự động

**Independent Test**: Tạo file mới (chọn nhóm, nhập tên + URL), click link mở tab mới, sửa/xóa file, xác nhận Toast notification.

### Backend Implementation

- [x] T020 [P] [US2] Create FileRecordRequest DTO in `BE/src/main/java/com/internal/projectmgmt/dto/filerecord/FileRecordRequest.java` — record with @NotBlank fileName (max 200), @NotBlank @Pattern fileUrl (http/https prefix, max 2048), @NotNull groupId (UUID)
- [x] T021 [P] [US2] Create FileRecordResponse DTO in `BE/src/main/java/com/internal/projectmgmt/dto/filerecord/FileRecordResponse.java` — record with id, fileName, fileUrl, groupId, groupName, createdBy, createdAt, updatedAt
- [x] T022 [US2] Create FileRecordMapper in `BE/src/main/java/com/internal/projectmgmt/mapper/FileRecordMapper.java` — toEntity(request, fileGroup, createdBy), toResponse(entity)
- [x] T023 [US2] Create FileRecordService in `BE/src/main/java/com/internal/projectmgmt/service/FileRecordService.java` — findAll (paginated, with search/filter/includeInactive), create (validate group active, set createdBy from Authentication), update, delete
- [x] T024 [US2] Create FileRecordController in `BE/src/main/java/com/internal/projectmgmt/controller/FileRecordController.java` — GET / (paginated with keyword, groupId, includeInactive params), POST /, PUT /{id}, DELETE /{id}, all @PreAuthorize("hasAuthority('MANAGE_HANDBOOK')")

### Frontend Implementation

- [x] T025 [P] [US2] Create fileRecordService in `FE/src/services/fileRecordService.ts` — getAll (with keyword, groupId, includeInactive, page, size params), create, update, delete API calls
- [x] T026 [US2] Create FileRecordDialog component in `FE/src/components/handbook/FileRecordDialog.vue` — Dialog with horizontal form layout (label 100px), fields: Tên file (InputText), Link file (InputText, pattern validation http/https), Nhóm file (Dropdown from getActive API), validation, Toast on save
- [x] T027 [US2] Create FileListView in `FE/src/views/handbook/FileListView.vue` — DataTable with columns (Tên file, Link file as `<a target="_blank">`, Nhóm file, Ngày tạo, Người tạo, Thao tác), add/edit/delete buttons, confirm dialog for delete, pagination (Paginator), empty state, loading state

**Checkpoint**: User Stories 1 AND 2 both functional — complete CRUD for both entities

---

## Phase 5: User Story 3 — Tìm kiếm và lọc file (Priority: P2)

**Goal**: Thanh tìm kiếm + dropdown lọc nhóm + toggle hiển thị nhóm inactive trên màn hình Danh mục file

**Independent Test**: Tạo nhiều file thuộc các nhóm khác nhau, tìm theo tên, lọc theo nhóm, bật toggle xem nhóm inactive, xóa bộ lọc → hiển thị lại toàn bộ.

### Implementation

- [x] T028 [US3] Add search bar (InputText with pi-search icon), group filter (Dropdown from getActive), and includeInactive toggle (Checkbox "Hiển thị cả nhóm ngưng HĐ") to `FE/src/views/handbook/FileListView.vue` — debounced search (300ms), auto-reload on filter change, clear button to reset all filters

**Checkpoint**: All 3 user stories functional — search, filter, and inactive toggle working

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Final verification and cleanup

- [x] T029 [P] Run BE compile check (`cd BE && mvn compile -q`) and fix any errors
- [x] T030 [P] Run unit tests (`cd BE && mvn test -pl . -Dtest=FileGroupServiceTest`) and fix failures
- [x] T031 Verify quickstart.md steps in `specs/007-central-handbook/quickstart.md` — login, sidebar menu, CRUD operations, link click, search/filter

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — start immediately
- **Foundational (Phase 2)**: Depends on T003, T004 (entities must exist before repositories)
- **User Story 1 (Phase 3)**: Depends on Phase 2 completion — BLOCKS US2 (FileGroup is FK for FileRecord)
- **User Story 2 (Phase 4)**: Depends on Phase 3 (needs FileGroup CRUD + dropdown API)
- **User Story 3 (Phase 5)**: Depends on Phase 4 (adds filters to FileListView created in Phase 4)
- **Polish (Phase 6)**: Depends on all user stories complete

### User Story Dependencies

- **US1 (P1)**: FileGroup is foundation entity — MUST complete before US2
- **US2 (P1)**: Requires US1 (FileGroup dropdown API, FK relationship)
- **US3 (P2)**: Requires US2 (adds search/filter to FileListView from US2)

### Within Each User Story

- DTOs and Mapper before Service
- Service before Controller
- BE complete before FE (API must exist for FE to call)
- FE Service before FE Component

### Parallel Opportunities

- T003 + T004 + T005 (entities + types — different files, no deps)
- T006 + T007 (repositories — different files)
- T009 + T010 + T011 (DTOs — different files)
- T020 + T021 (DTOs — different files)
- T015 (FE service) can start once T014 (controller) done
- T025 (FE service) can start once T024 (controller) done
- T029 + T030 (verification — independent checks)

---

## Implementation Strategy

### MVP Scope

User Story 1 (Phase 3) delivers a testable MVP: complete FileGroup management with CRUD, DataTable, Dialog, Toast, and permission control. Can be demoed independently.

### Incremental Delivery

1. **Phase 1+2+3** → MVP: FileGroup CRUD (13 tasks)
2. **Phase 4** → Full CRUD: FileRecord CRUD with link click (8 tasks)
3. **Phase 5** → Enhanced UX: Search/filter/inactive toggle (1 task)
4. **Phase 6** → Quality: Compile check, test run, quickstart validation (3 tasks)

### Task Count Summary

| Phase | Tasks | Parallel |
|-------|-------|----------|
| Phase 1: Setup | 5 | 3 |
| Phase 2: Foundational | 2 | 2 |
| Phase 3: US1 — Nhóm file | 12 | 5 |
| Phase 4: US2 — Bản ghi file | 8 | 3 |
| Phase 5: US3 — Tìm kiếm & lọc | 1 | 0 |
| Phase 6: Polish | 3 | 2 |
| **Total** | **31** | **15** |
