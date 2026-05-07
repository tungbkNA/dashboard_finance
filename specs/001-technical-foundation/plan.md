# Implementation Plan: Nền Móng Kỹ Thuật Ban Đầu (Technical Foundation)

**Branch**: `001-technical-foundation` | **Date**: 2026-05-06 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/001-technical-foundation/spec.md`

## Summary

Xây dựng nền móng kỹ thuật cho hệ thống quản lý dự án nội bộ gồm 2 phần tách biệt: BE/ (Spring Boot / Java 21 / Maven / PostgreSQL) và FE/ (Vue 3 / Vite / TypeScript / PrimeVue). Feature thiết lập cấu trúc package, quy ước API, xử lý lỗi tập trung, và giao diện layout chính có sidebar/header. Không triển khai nghiệp vụ — chỉ tạo khung cho các feature tiếp theo.

## Technical Context

**Backend Language/Version**: Java 21 / Spring Boot 3.4.5
**Frontend Language/Version**: TypeScript 5.x / Vue 3.5.x + Vite 6.x
**Primary Dependencies (BE)**: Spring Web, Spring Data JPA, Bean Validation, Spring Boot Actuator, Flyway, springdoc-openapi-starter-webmvc-ui 2.8.x, PostgreSQL driver, JUnit 5, Mockito
**Primary Dependencies (FE)**: Vue Router 4.x, Pinia 2.x, PrimeVue 4.x, @primevue/themes 4.x, PrimeIcons 7.x, Axios 1.x
**Storage**: PostgreSQL 15 (local installation — không chạy qua Docker)
**Testing (BE)**: JUnit 5 + Mockito (unit), Spring Boot Test (integration) — cấu trúc thư mục sẵn, chưa có business test
**Testing (FE)**: Ngoài scope của feature này; Vitest sẽ thêm khi có business feature đầu tiên
**Project Type**: web-service (BE) + web-app (FE)
**Performance Goals**: Health check endpoint < 500ms (SC-003)
**Constraints**: API prefix `/api/binance`; response wrapper `{ code, message, data }`; CORS cho localhost:5173; log INFO (app) / ERROR (3rd party); BigDecimal cho financial fields (kiến trúc sẵn sàng, chưa có field trong feature này)
**Scale/Scope**: Internal tool, foundation scaffold only — không có business entity

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Quy tắc | §Constitution | Trạng thái | Ghi chú |
|---|---|---|---|
| BE/ + FE/ root structure | §3 | ✅ PASS | Đúng layout |
| Java 21, Spring Boot, Maven, PostgreSQL | §4.1 | ✅ PASS | Đủ mandated technologies |
| Đủ 9 package layers | §4.2 | ✅ PASS | controller/service/repository/dto/entity/mapper/validation/exception/config |
| BigDecimal cho financial fields | §4.3 | ✅ PASS (N/A) | Chưa có financial field; kiến trúc documented |
| Cấu trúc test sẵn sàng | §4.4 | ✅ PASS | Thư mục test tạo sẵn |
| Vue 3, Vite, TS, Vue Router, Pinia, PrimeVue, PrimeIcons | §5.1 | ✅ PASS | Đủ |
| Tông màu đỏ chủ đạo | §5.2 | ✅ PASS | PrimeVue Aura preset + red palette |
| Spec-first development | §2.1 | ✅ PASS | spec.md đầy đủ |
| BE là nguồn sự thật nghiệp vụ | §2.2 | ✅ PASS (N/A) | Chưa có business logic |
| API prefix /api/binance | §6.2 | ✅ PASS | Health check tại /api/binance/health |
| Response format { code, message, data } | §6.3 | ✅ PASS | ApiResponse<T> wrapper |

**Kết quả: TẤT CẢ GATES PASS ✅ — Tiến hành Phase 0**

## Project Structure

### Documentation (this feature)

```text
specs/001-technical-foundation/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
│   └── health-check.md
└── tasks.md             # Phase 2 output (/speckit.tasks command — NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
BE/
├── src/
│   ├── main/
│   │   ├── java/com/internal/projectmgmt/
│   │   │   ├── controller/
│   │   │   ├── service/
│   │   │   ├── repository/
│   │   │   ├── dto/
│   │   │   ├── entity/
│   │   │   ├── mapper/
│   │   │   ├── validation/
│   │   │   ├── exception/
│   │   │   └── config/
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-local.properties
│   │       └── db/migration/
│   │           └── V1__init_schema.sql
│   └── test/
│       └── java/com/internal/projectmgmt/
│           ├── controller/
│           ├── service/
│           └── integration/
└── pom.xml

FE/
├── src/
│   ├── components/
│   │   ├── AppLayout.vue
│   │   ├── AppSidebar.vue
│   │   ├── AppHeader.vue
│   │   └── common/
│   │       ├── LoadingState.vue
│   │       ├── EmptyState.vue
│   │       └── ErrorState.vue
│   ├── views/
│   │   ├── DashboardView.vue
│   │   ├── ProjectSettingsView.vue
│   │   ├── ProjectManagementView.vue
│   │   └── ConfigView.vue
│   ├── router/
│   │   └── index.ts
│   ├── stores/
│   │   └── index.ts        (Pinia setup only)
│   └── services/
│       ├── api.ts           (Axios instance + interceptors)
│       └── healthService.ts
├── .env.local
├── vite.config.ts
├── tsconfig.json
└── package.json

docker-compose.yml           (backend service only)
README.md                    (quickstart guide)
```

**Structure Decision**: BE/ + FE/ layout per constitution §3. Java package root `com.internal.projectmgmt`, groupId `com.internal`, artifactId `dashboard-finance` (matches `service` field in health check response).

## Complexity Tracking

Không có vi phạm constitution. Không cần complexity tracking.
