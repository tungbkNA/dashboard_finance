# Implementation Plan: Sổ tay trung tâm

**Branch**: `007-central-handbook` | **Date**: 2026-05-07 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `specs/007-central-handbook/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command.

## Summary

Module "Sổ tay trung tâm" cho phép người dùng quản lý tài liệu/liên kết, phân loại theo nhóm, tra cứu nhanh. Gồm 2 entity (FileGroup, FileRecord), 2 bộ CRUD API + 2 màn hình FE, bảo vệ bởi permission `MANAGE_HANDBOOK`. Sử dụng kiến trúc BE (Spring Boot) + FE (Vue 3/PrimeVue) hiện có, Flyway migration V10+V11.

## Technical Context

**Backend Language/Version**: Java 21 / Spring Boot 3.4.5  
**Frontend Language/Version**: TypeScript 5.x / Vue 3.5.x + Vite 6.x  
**Primary Dependencies (BE)**: Spring Boot, Maven, PostgreSQL 15+, Flyway, Spring Data JPA, Lombok, JJWT 0.12.6  
**Primary Dependencies (FE)**: Vue Router 4, Pinia, PrimeVue 4.3.x (Aura), PrimeIcons, Axios  
**Storage**: PostgreSQL — 2 new tables (`file_group`, `file_record`)  
**Testing (BE)**: JUnit 5 / Spring Boot Test  
**Testing (FE)**: Not required for this feature (no complex logic)  
**Project Type**: web-service (BE) + web-app (FE)  
**Performance Goals**: API response < 2s, search/filter results instant for 100+ records  
**Constraints**: UUID PKs, soft-delete not used for this module (hard-delete with FK check), `BigDecimal` not needed (no financial fields), Flyway V10+V11  
**Scale/Scope**: Internal team tool, low concurrency, < 1000 records expected

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| # | Principle | Status | Notes |
|---|-----------|--------|-------|
| 2.1 | Spec-driven development | ✅ PASS | spec.md fully defines all requirements |
| 2.2 | BE = source of truth for business rules | ✅ PASS | Validation + FK constraints in BE |
| 3 | BE/ + FE/ layout | ✅ PASS | Following existing structure |
| 4.2 | Package layers (controller/service/repo/dto/entity/mapper) | ✅ PASS | All layers planned |
| 4.3 | BigDecimal for financial fields | ✅ N/A | No financial fields in this module |
| 4.4 | Unit test for important logic | ✅ PASS | Test for delete-with-FK-check logic |
| 4.5 | Cross-month consistency | ✅ N/A | No cross-month data |
| 5.1 | Vue 3 + Composition API + PrimeVue | ✅ PASS | Following existing FE patterns |
| 5.2 | Red theme | ✅ PASS | Using existing design system |
| 5.3 | UI components (Table/Form/Dialog/Toast/Loading/Empty/Error) | ✅ PASS | All planned for both screens |
| 6.1 | One API = one spec | ✅ PASS | contracts/ will define all endpoints |
| 6.2 | RESTful naming | ✅ PASS | `/api/handbook/file-groups`, `/api/handbook/file-records` |
| 6.3 | Standard response format | ✅ PASS | Using existing `ApiResponse<T>` |
| 7.1 | UUID IDs | ✅ PASS | Both entities use UUID PK |
| 10.1 | Screen-based UI | ✅ PASS | 2 screens: FileGroupView, FileListView |

**Gate Result**: ✅ ALL PASS — No violations. Proceed to Phase 0.

## Project Structure

### Documentation (this feature)

```text
specs/007-central-handbook/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
│   ├── file-group-api.md
│   └── file-record-api.md
└── tasks.md             # Phase 2 output (/speckit.tasks)
```

### Source Code (repository root)

```text
BE/
├── src/main/java/com/internal/projectmgmt/
│   ├── controller/
│   │   ├── FileGroupController.java
│   │   └── FileRecordController.java
│   ├── service/
│   │   ├── FileGroupService.java
│   │   └── FileRecordService.java
│   ├── repository/
│   │   ├── FileGroupRepository.java
│   │   └── FileRecordRepository.java
│   ├── dto/
│   │   ├── filegroup/
│   │   │   ├── FileGroupRequest.java
│   │   │   ├── FileGroupUpdateRequest.java
│   │   │   └── FileGroupResponse.java
│   │   └── filerecord/
│   │       ├── FileRecordRequest.java
│   │       └── FileRecordResponse.java
│   ├── entity/
│   │   ├── FileGroup.java
│   │   └── FileRecord.java
│   ├── mapper/
│   │   ├── FileGroupMapper.java
│   │   └── FileRecordMapper.java
│   └── ...existing packages...
├── src/main/resources/db/migration/
│   ├── V10__file_group_and_record_schema.sql
│   └── V11__seed_file_groups_and_permission.sql
└── src/test/java/com/internal/projectmgmt/service/
    └── FileGroupServiceTest.java

FE/
├── src/
│   ├── views/handbook/
│   │   ├── FileGroupView.vue
│   │   └── FileListView.vue
│   ├── components/handbook/
│   │   ├── FileGroupDialog.vue
│   │   └── FileRecordDialog.vue
│   ├── services/
│   │   ├── fileGroupService.ts
│   │   └── fileRecordService.ts
│   ├── types/
│   │   └── handbook.ts
│   ├── router/index.ts          # Updated with new routes
│   └── components/AppSidebar.vue # Updated with new menu items
```

**Structure Decision**: Follows existing BE/ + FE/ monorepo layout per constitution §3. New `handbook/` subdirectories under `views/`, `components/`, `services/`, `types/` to keep module isolated. DTOs organized in subpackages `filegroup/` and `filerecord/` following existing `projecttype/` pattern.

## Complexity Tracking

No constitution violations — this table is empty.
