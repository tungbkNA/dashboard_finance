---
description: "Task list template for feature implementation"
---

# Tasks: Nền Móng Kỹ Thuật Ban Đầu (Technical Foundation)

**Input**: Design documents from `/specs/001-technical-foundation/`
**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅, contracts/ ✅, quickstart.md ✅
**Regenerated**: 2026-05-06 (v2 — incorporates analysis fixes A1, I1, I2, U1)

**Tests**: Không có test tasks — FR-BE-008 chỉ yêu cầu cấu trúc thư mục test sẵn sàng, không yêu cầu viết tests trong feature này.

**Organization**: Tasks nhóm theo user story. US1 là MVP — toàn bộ hệ thống chạy được sau US1.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Có thể chạy song song (file khác nhau, không phụ thuộc nhau)
- **[Story]**: User story tương ứng (US1, US2, US3)
- Mỗi task có đường dẫn file cụ thể

---

## Phase 1: Setup (Khởi tạo project)

**Purpose**: Tạo BE và FE project từ đầu với đủ dependency

- [X] T001 [P] Khởi tạo BE Maven project Spring Boot 3.4.5 Java 21 với tất cả dependencies tại `BE/pom.xml` (Spring Web, Spring Data JPA, Bean Validation, Actuator, Flyway, springdoc-openapi-starter-webmvc-ui 2.8.6, PostgreSQL driver, JUnit 5, Mockito, Lombok)
- [X] T002 [P] Khởi tạo FE Vite Vue 3 TypeScript project và cài dependencies tại `FE/package.json`, `FE/vite.config.ts`, `FE/tsconfig.json`, `FE/index.html` (vue, vite, typescript, vue-router, pinia, primevue 4.x, @primevue/themes, primeicons, axios)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Nền tảng dùng chung — PHẢI hoàn thành trước khi bắt đầu bất kỳ user story nào

**⚠️ CRITICAL**: Không bắt đầu Phase 3+ cho đến khi Phase 2 hoàn tất

- [X] T003 Tạo BE Java package directory structure với placeholder `package-info.java` cho đủ 9 tầng tại `BE/src/main/java/com/internal/projectmgmt/{controller,service,repository,dto,entity,mapper,validation,exception,config}/`
- [X] T004 [P] Tạo `BE/src/main/resources/application.properties` với app name (`dashboard-finance`), version (`1.0.0`), actuator endpoint paths, Flyway config — **KHÔNG** bao gồm springdoc paths (thuộc T026); tạo `BE/src/main/resources/application-local.properties` với datasource PostgreSQL local và log levels (`logging.level.com.internal=INFO`, `logging.level.org.hibernate=ERROR`, `logging.level.org.springframework=ERROR`)
- [X] T005 [P] Tạo Flyway baseline migration `V1__init_schema.sql` (comment placeholder, không tạo bảng) tại `BE/src/main/resources/db/migration/V1__init_schema.sql`
- [X] T006 [P] Tạo BE test directory structure với placeholder test class tại `BE/src/test/java/com/internal/projectmgmt/{controller,service,integration}/`
- [X] T007 [P] Tạo `ApiResponse<T>` generic response wrapper DTO với static factory methods `success(T data)`, `success(String message, T data)`, `error(String code, String message)` tại `BE/src/main/java/com/internal/projectmgmt/dto/ApiResponse.java`
- [X] T008 Tạo FE src directory structure với các thư mục `FE/src/{components,components/common,views,router,stores,services}/` và file index placeholder
- [X] T009 [P] Cấu hình PrimeVue 4.x với Aura preset + red primary palette và PrimeIcons tại `FE/src/main.ts` (`definePreset` override `semantic.primary` thành red palette `{red.50}` → `{red.950}`)
- [X] T010 [P] Tạo `FE/.env.local` với `VITE_API_BASE_URL=http://localhost:8080` và cấu hình Vite server proxy + TypeScript `env.d.ts` tại `FE/vite.config.ts` và `FE/src/env.d.ts`

**Checkpoint**: Foundation sẵn sàng — bắt đầu được tất cả user stories

---

## Phase 3: User Story 1 — Lập Trình Viên Khởi Động Hệ Thống (Priority: P1) 🎯 MVP

**Goal**: Toàn bộ hệ thống chạy được: BE start → health check OK → FE hiển thị layout + gọi health check

**Independent Test**: Tạo DB local → `mvn spring-boot:run -Plocal` → `curl http://localhost:8080/api/binance/health` trả SUCCESS → `npm run dev` → trình duyệt tại `http://localhost:5173` hiển thị layout với menu 4 mục

### Implementation cho User Story 1

- [X] T011 [US1] Tạo `HealthStatusDto` Java record với 3 fields (`status`, `service`, `version`) tại `BE/src/main/java/com/internal/projectmgmt/dto/HealthStatusDto.java`
- [X] T012 [P] [US1] Tạo `HealthController` với `GET /api/binance/health` trả `ApiResponse<HealthStatusDto>` (status=UP, service=dashboard-finance, version từ `${app.version}`) tại `BE/src/main/java/com/internal/projectmgmt/controller/HealthController.java`
- [X] T013 [P] [US1] Tạo `CorsConfig` cho phép origin `http://localhost:5173` gọi tất cả `/api/binance/**` tại `BE/src/main/java/com/internal/projectmgmt/config/CorsConfig.java`
- [X] T014 [P] [US1] Tạo `OpenApiConfig` cấu hình springdoc-openapi title (`Dashboard Finance API`), description, version tại `BE/src/main/java/com/internal/projectmgmt/config/OpenApiConfig.java`
- [X] T015 [P] [US1] Tạo Axios API client với base URL từ `import.meta.env.VITE_API_BASE_URL` (placeholder interceptors — logic lỗi sẽ thêm ở T029) tại `FE/src/services/api.ts`
- [X] T016 [P] [US1] Tạo `healthService.ts` gọi `GET /api/binance/health` và trả `Promise<ApiResponse<HealthStatusDto>>` tại `FE/src/services/healthService.ts`
- [X] T017 [P] [US1] Tạo `AppSidebar.vue` với 4 menu items dùng PrimeVue PanelMenu/Menu: Dashboard (`pi-home`), Cài đặt dự án (`pi-folder`), Quản Lý Các Dự Án (`pi-briefcase`), Cấu hình (`pi-cog`) — mỗi item là `<RouterLink>` tới route tương ứng tại `FE/src/components/AppSidebar.vue`
- [X] T018 [P] [US1] Tạo `AppHeader.vue` với tiêu đề hệ thống dùng tông màu đỏ primary tại `FE/src/components/AppHeader.vue`
- [X] T019 [P] [US1] Tạo 4 view pages skeleton tại `FE/src/views/DashboardView.vue`, `FE/src/views/ProjectSettingsView.vue`, `FE/src/views/ProjectManagementView.vue`, `FE/src/views/ConfigView.vue`
- [X] T020 [US1] Cấu hình Vue Router 4 routes (`/`, `/project-settings`, `/projects`, `/config`) tương ứng 4 menu items tại `FE/src/router/index.ts`
- [X] T021 [US1] Tạo `AppLayout.vue` tích hợp `AppSidebar`, `AppHeader`, `<RouterView>` làm content area tại `FE/src/components/AppLayout.vue`
- [X] T022 [US1] Khởi tạo Pinia (`createPinia`), cấu hình `App.vue` mount `AppLayout` + đăng ký `<Toast />` component tại `FE/src/App.vue` và `FE/src/stores/index.ts`
- [X] T023 [P] [US1] Tạo `docker-compose.yml` chạy backend service (build từ `./BE`) với env `SPRING_PROFILES_ACTIVE=local`, `SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/dashboard_finance` và `extra_hosts: host.docker.internal:host-gateway` tại `docker-compose.yml`
- [X] T024 [P] [US1] Tạo `README.md` quickstart 5 bước (prerequisites → tạo DB local → run BE → verify health check → run FE) bao gồm hướng dẫn đổi port tại `README.md`

**Checkpoint**: US1 hoàn tất — BE khởi động, FE hiển thị layout, health check hoạt động end-to-end

---

## Phase 4: User Story 2 — Lập Trình Viên Kiểm Tra Cấu Trúc Dự Án (Priority: P2)

**Goal**: Cấu trúc package BE và FE rõ ràng; Swagger UI truy cập được

**Independent Test**: Mở IDE → duyệt `BE/src/` thấy 9 package layers → mở `FE/src/` thấy 5 thư mục → truy cập `http://localhost:8080/swagger-ui/index.html` thấy health check endpoint

### Implementation cho User Story 2

- [X] T025 [US2] Tạo placeholder stub (`package-info.java`) trong các package layer chưa có Java file sau US1 (entity, mapper, validation — controller/service/dto đã có) tại `BE/src/main/java/com/internal/projectmgmt/{entity,mapper,validation}/package-info.java`
- [X] T026 [P] [US2] Thêm springdoc-openapi paths vào `BE/src/main/resources/application.properties`: `springdoc.swagger-ui.path=/swagger-ui/index.html`, `springdoc.api-docs.path=/v3/api-docs`, `springdoc.swagger-ui.enabled=true` — scope tách biệt với T004

**Checkpoint**: US2 hoàn tất — cấu trúc duyệt được, Swagger UI truy cập được

---

## Phase 5: User Story 3 — Lập Trình Viên Xử Lý Lỗi Từ API (Priority: P3)

**Goal**: Global error handling — mọi lỗi từ BE đều được FE bắt và hiển thị toast; loading/empty/error states tái sử dụng được

**Independent Test**: Tắt backend → mở FE → thấy toast `"Không thể kết nối đến máy chủ"` (không crash, không blank screen)

### Implementation cho User Story 3

- [X] T027 [US3] Tạo `GlobalExceptionHandler` với `@RestControllerAdvice` xử lý `MethodArgumentNotValidException` → `ApiResponse.error("VALIDATION_ERROR", ...)` và catch-all `Exception` → `ApiResponse.error("INTERNAL_ERROR", "Lỗi hệ thống nội bộ")` tại `BE/src/main/java/com/internal/projectmgmt/exception/GlobalExceptionHandler.java`
- [X] T028 [P] [US3] Tạo base `AppException` (unchecked, extends RuntimeException) với `code` và `message` fields; thêm handler cho `AppException` vào `GlobalExceptionHandler` tại `BE/src/main/java/com/internal/projectmgmt/exception/AppException.java`
- [X] T029 [P] [US3] Tạo `LoadingState.vue` reusable component (PrimeVue ProgressSpinner + text prop) tại `FE/src/components/common/LoadingState.vue`
- [X] T030 [P] [US3] Tạo `EmptyState.vue` reusable component (PrimeIcons icon + message prop) tại `FE/src/components/common/EmptyState.vue`
- [X] T031 [P] [US3] Tạo `ErrorState.vue` reusable component (error icon + message prop + retry emit) tại `FE/src/components/common/ErrorState.vue`
- [X] T032 [US3] Thêm Axios response error interceptor vào `FE/src/services/api.ts` — extract `error.response?.data?.message ?? "Không thể kết nối đến máy chủ"`, gọi PrimeVue `useToast()` với `severity: 'error'`, `life: 4000` — **NOTE**: T022 phải hoàn tất trước (Toast đã được đăng ký trong App.vue)
- [X] T033 [US3] Thêm health check call vào `FE/src/App.vue` `onMounted`: gọi `healthService.checkHealth()`, wrap trong `try/catch` để hiển thị `LoadingState.vue` trong khi chờ; lỗi được bắt bởi Axios interceptor (T032) tại `FE/src/App.vue`

**Checkpoint**: US3 hoàn tất — tất cả lỗi hiển thị toast, không có lỗi nào bị nuốt im lặng, loading state hoạt động

---

## Phase 6: Polish & Cross-Cutting

**Purpose**: Validate quickstart end-to-end và đảm bảo tích hợp hoàn chỉnh

- [X] T034 [P] Kiểm tra `repository` package có stub — nếu chưa có Java file sau T003, tạo `package-info.java` tại `BE/src/main/java/com/internal/projectmgmt/repository/package-info.java` (mapper và service đã có file từ US1/US3)
- [X] T035 Validate toàn bộ quickstart flow theo `specs/001-technical-foundation/quickstart.md`: tạo DB local → chạy BE (`mvn spring-boot:run -Plocal`) → `curl /api/binance/health` → chạy FE (`npm run dev`) → xác nhận layout hiển thị + menu 4 items → tắt BE → xác nhận toast lỗi xuất hiện

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: Không phụ thuộc — bắt đầu ngay; T001 ‖ T002
- **Phase 2 (Foundational)**: T003 cần T001; T008 cần T002; T004–T007 sau T003; T009–T010 sau T008 — **BLOCKS** tất cả user stories
- **Phase 3 (US1)**: Tất cả cần Phase 2 hoàn tất; T011 → T012; T019 → T020 → T021 → T022; T015 độc lập với T016
- **Phase 4 (US2)**: T025 cần T003; T026 cần T001 (pom.xml có springdoc dep)
- **Phase 5 (US3)**: T027/T028 cần T003 + T007; T029–T031 song song; T032 cần T015 + T022 (Toast đăng ký trước); T033 cần T032 + T016
- **Phase 6 (Polish)**: T035 cần tất cả phases hoàn tất

### Critical Dependency Chain (US3)

```
T022 (App.vue + <Toast/> registration)
  └─→ T032 (Axios interceptor — cần Toast đã sẵn sàng)
        └─→ T033 (health check call với loading state)

T016 (healthService)
  └─→ T033 (cần service để gọi health check)
```

### Parallel Opportunities

**Phase 1**: `T001 ‖ T002`

**Phase 2** (sau T001 → T003, và T002 → T008):
```
T004 ‖ T005 ‖ T006 ‖ T007    (BE — sau T003)
T009 ‖ T010                   (FE — sau T008)
```

**Phase 3 / US1** (sau Phase 2):
```
T011 → T012
T013 ‖ T014                   (BE configs — độc lập)
T015 ‖ T016 ‖ T017 ‖ T018 ‖ T019 ‖ T023 ‖ T024   (FE + infra)
T019 → T020 → T021 → T022
```

**Phase 4 / US2**: `T025 ‖ T026`

**Phase 5 / US3**:
```
T027 ‖ T028    (BE exceptions)
T029 ‖ T030 ‖ T031    (FE state components)
T022 → T032 → T033    (sequential: Toast → interceptor → health call)
```

---

## Implementation Strategy

**MVP = Phase 1 + Phase 2 + Phase 3 (US1) = 24 tasks**

Sau khi hoàn thành Phase 3:
- BE khởi động, `GET /api/binance/health` trả SUCCESS
- FE hiển thị layout với sidebar 4 menu items
- FE gọi health check (silent success)
- `docker-compose.yml` và `README.md` sẵn sàng
→ Developer mới có thể onboard và bắt đầu feature nghiệp vụ tiếp theo.

US2 và US3 hoàn thiện DX nhưng không block MVP.

---

## Dependency Graph (Story Completion Order)

```
Phase 1 (T001‖T002) → Phase 2 (T003–T010) → Phase 3/US1 (T011–T024)
                                            ↓                    ↓
                                     Phase 4/US2          Phase 5/US3
                                     (T025–T026)          (T027–T033)
                                            ↓                    ↓
                                     Phase 6/Polish (T034–T035)
```

---

## Kiểm Tra Format Checklist

Tất cả tasks tuân theo format: `- [ ] T### [P?] [Story?] Mô tả với đường dẫn file`

- **Tổng tasks: 35**
- Phase 1 (Setup): 2 tasks
- Phase 2 (Foundational): 8 tasks
- Phase 3 (US1 — MVP): 14 tasks
- Phase 4 (US2): 2 tasks
- Phase 5 (US3): 7 tasks
- Phase 6 (Polish): 2 tasks
- Tasks `[P]` (parallelizable): 24
- Tasks `[US1]`: 14 | `[US2]`: 2 | `[US3]`: 7

**Fixes applied vs v1**:
- **A1**: T004 scope tách biệt với T026 — T004 không include springdoc paths, T026 owns springdoc config
- **I1**: spec.md US1 Independent Test updated (Docker Compose reference removed, Maven command added)
- **I2**: T033 di chuyển sau T032 trong Phase 5; dependency note cho rõ T022 → T032 → T033 chain
- **U1**: T033 now wires `LoadingState.vue` vào App.vue health check call; T034 added for repository stub check
