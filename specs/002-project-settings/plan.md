# Implementation Plan: Cài Đặt Dự Án (Project Settings)

**Branch**: `002-project-settings` | **Date**: 2026-05-07 | **Spec**: [spec.md](./spec.md)

---

## Summary

Xây dựng feature Cài Đặt Dự Án cho hệ thống quản lý dự án nội bộ. Backend: 3 entity (Project, ProjectType, Customer) với CRUD đầy đủ, soft delete toàn bộ, Bean Validation, Flyway migration, và 6 test case bắt buộc. Frontend: 1 màn hình `/project-settings` với 3 tab (PrimeVue TabView), mỗi tab là DataTable + Dialog form + ConfirmDialog, gọi API qua service layer.

---

## Technical Context

**Backend Language/Version**: Java 21 / Spring Boot 3.4.5
**Frontend Language/Version**: TypeScript 5.x / Vue 3.5.x + Vite 6.x
**Primary Dependencies (BE)**: Spring Web, Spring Data JPA, Bean Validation, Flyway, PostgreSQL, Lombok, springdoc-openapi 2.8.6
**Primary Dependencies (FE)**: Vue Router 4.x, Pinia 2.x, PrimeVue 4.3.x (Aura + red), PrimeIcons 7.x, Axios 1.x
**Storage**: PostgreSQL 15 (local), tables: `project_type`, `customer`, `project`
**Testing (BE)**: JUnit 5 / Spring Boot Test (unit + @DataJpaTest integration)
**Testing (FE)**: None in this feature (no Vitest setup)
**Project Type**: web-service (BE) + web-app (FE)
**Performance Goals**: API response ≤ 2s; no pagination in MVP
**Constraints**: BigDecimal for price; all deletes are soft delete; represent_id nullable (no FK)
**Scale/Scope**: Internal tool, ≤ 100 projects; no pagination in MVP

---

## Constitution Check

| Gate | Status | Notes |
|------|--------|-------|
| §3 BE in BE/, FE in FE/ | PASS | All code in correct dirs |
| §4.1 Java 21 / Spring Boot / Maven / PostgreSQL | PASS | Existing pom.xml |
| §4.2 All 9 package layers present | PASS | All stubs exist; filling entity, mapper, validation |
| §4.3 BigDecimal for price | PASS | price → NUMERIC(19,4) / BigDecimal |
| §4.4 Unit tests for critical logic | PASS | 6 required test cases defined |
| §5.1 Vue 3 / Vite / TypeScript / PrimeVue / PrimeIcons | PASS | Existing FE scaffold |
| §5.2 Red color theme | PASS | RedPreset already in main.ts |
| §5.3 Table + Form + Dialog + Toast + Loading + Empty + Error | PASS | All required per screen |
| §6.2 RESTful URL /api/binance/{resource} | PASS | /projects, /project-types, /customers |
| §6.3 Response wrapper {code, message, data} | PASS | ApiResponse<T> already exists |
| §7.1 UUID IDs | PASS | All entities use UUID PK |
| §7.2 Core project fields present | PASS | All 10 fields in Project entity |
| §8.1 statusProject enum | PASS | OPEN/INPROGRESS/PENDING/DONE/CLOSE |
| §8.2 statusContract | PASS | NO_CONTRACT / HAS_CONTRACT |

Post-Design Re-check: All gates pass. No violations.

---

## Project Structure

### Documentation (this feature)

```
specs/002-project-settings/
├── plan.md
├── spec.md
├── research.md
├── data-model.md
├── checklists/requirements.md
└── contracts/
    ├── projects-api.md
    ├── project-types-api.md
    └── customers-api.md
```

### Source Code

```
BE/src/main/java/com/internal/projectmgmt/
├── entity/
│   ├── Project.java
│   ├── ProjectType.java
│   ├── Customer.java
│   ├── StatusProject.java        (enum)
│   └── StatusContract.java       (enum)
├── dto/
│   ├── project/
│   │   ├── ProjectRequest.java
│   │   └── ProjectResponse.java
│   ├── projecttype/
│   │   ├── ProjectTypeRequest.java
│   │   └── ProjectTypeResponse.java
│   └── customer/
│       ├── CustomerRequest.java
│       └── CustomerResponse.java
├── repository/
│   ├── ProjectRepository.java
│   ├── ProjectTypeRepository.java
│   └── CustomerRepository.java
├── service/
│   ├── ProjectService.java
│   ├── ProjectTypeService.java
│   └── CustomerService.java
├── mapper/
│   ├── ProjectMapper.java
│   ├── ProjectTypeMapper.java
│   └── CustomerMapper.java
├── controller/
│   ├── ProjectController.java
│   ├── ProjectTypeController.java
│   └── CustomerController.java
└── validation/
    └── MonthYear.java + MonthYearValidator.java

BE/src/main/resources/db/migration/
└── V2__project_settings_schema.sql

FE/src/
├── types/
│   └── project-settings.ts
├── services/
│   ├── projectService.ts
│   ├── projectTypeService.ts
│   └── customerService.ts
└── views/
    └── ProjectSettingsView.vue   (replaces skeleton, 3-tab layout)
```

---

## Implementation Phases

### Phase 1 — Backend Foundation

T001: Flyway migration V2__project_settings_schema.sql
- Create enums: project_status, project_status_contract
- Create tables: project_type, customer, project
- Create partial unique indexes (LOWER + WHERE deleted=false)

T002: Java enums — StatusProject, StatusContract
- StatusProject: OPEN, INPROGRESS, PENDING, DONE, CLOSE
- StatusContract: NO_CONTRACT, HAS_CONTRACT

T003: Custom validation — @MonthYear annotation + MonthYearValidator
- Regex: ^(0[1-9]|1[0-2])/[2-9][0-9]{3}$
- Message: "Tháng phải có định dạng mm/yyyy (ví dụ: 01/2026)"

T004: Entities — ProjectType, Customer, Project
- @Entity, @Table, Lombok @Data/@Builder
- @Enumerated(EnumType.STRING) for status fields
- No @Where — filter explicitly in repository

T005: Repositories — ProjectTypeRepository, CustomerRepository, ProjectRepository
- findAllByDeletedFalse()
- findByIdAndDeletedFalse(UUID id)
- existsByProjectCodeIgnoreCaseAndDeletedFalse(String code)
- countByProjectTypeIdAndDeletedFalse(UUID id)
- countByCustomerIdAndDeletedFalse(UUID id)

T006: DTOs — Request/Response for all 3 entities
- Request: Bean Validation (@NotBlank, @DecimalMin, @MonthYear)
- Response: flat DTO with resolved names (customerName, projectTypeName)

T007: Mappers — ProjectMapper, ProjectTypeMapper, CustomerMapper
- Manual entity <-> DTO conversion

T008: Services — ProjectTypeService, CustomerService, ProjectService
- CRUD + soft delete logic
- Uniqueness check before create/update (throw AppException)
- Month comparison for monthEnd >= monthStart
- Usage count check for delete warning

T009: Controllers — ProjectTypeController, CustomerController, ProjectController
- RESTful endpoints per contracts/
- DELETE: first call checks usage; ?confirmed=true performs soft delete

### Phase 2 — Backend Tests

T010: Unit test — ProjectServiceTest
- Duplicate projectCode -> AppException(PROJECT_CODE_DUPLICATE)
- monthEnd < monthStart -> AppException(MONTH_RANGE_INVALID)

T011: Unit test — ProjectTypeServiceTest
- Duplicate key -> AppException(PROJECT_TYPE_KEY_DUPLICATE)
- Soft delete in-use -> inUse=true returned

T012: Unit test — CustomerServiceTest
- Duplicate customerCode -> AppException(CUSTOMER_CODE_DUPLICATE)
- Soft delete in-use -> inUse=true returned

### Phase 3 — Frontend

T013: TypeScript types in types/project-settings.ts

T014: Service layer (projectTypeService, customerService, projectService)

T015: ProjectSettingsView.vue — 3-tab layout with TabView

T016: Tab 1 — Dự án: DataTable + create/edit Dialog + delete ConfirmDialog

T017: Tab 2 — Loại dự án: DataTable + Dialog + in-use warning pattern

T018: Tab 3 — Khách hàng: DataTable + Dialog + in-use warning pattern

---

## Error Codes Reference

| Code | HTTP | Message |
|------|------|---------|
| PROJECT_CODE_DUPLICATE | 422 | Mã dự án đã tồn tại |
| PROJECT_NOT_FOUND | 404 | Dự án không tồn tại |
| MONTH_RANGE_INVALID | 422 | Tháng kết thúc phải >= tháng bắt đầu |
| PROJECT_TYPE_KEY_DUPLICATE | 422 | Key loại dự án đã tồn tại |
| PROJECT_TYPE_NOT_FOUND | 404 | Loại dự án không tồn tại |
| CUSTOMER_CODE_DUPLICATE | 422 | Mã khách hàng đã tồn tại |
| CUSTOMER_NOT_FOUND | 404 | Khách hàng không tồn tại |
| IN_USE_WARNING | 200 | Entity đang được N dự án sử dụng |
| VALIDATION_ERROR | 400 | Field-level validation message |

---

## Open Questions / Risks

- represent_id nullable UUID, no FK — will need migration to add FK when personnel feature is built
- PostgreSQL enum types are immutable — adding values requires ALTER TYPE; chosen values are final
- No pagination in MVP — follow-up task if list grows large
