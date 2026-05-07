# Implementation Plan: Quản Lý Người Dùng, Phân Quyền và Đăng Nhập

**Branch**: `006-user-auth-rbac` | **Date**: 2026-05-07 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/006-user-auth-rbac/spec.md`

## Summary

Bổ sung toàn bộ lớp xác thực và phân quyền cho hệ thống hiện đang không có bảo vệ. Backend cài thêm Spring Security + stateless JWT (JJWT). Frontend bổ sung màn hình Login, navigation guard, Pinia auth store và axios interceptor. Cơ sở dữ liệu thêm 4 bảng mới (`app_user`, `role`, `permission`, `role_permission`) và thêm FK trên `project.represent_id`. Danh sách Permission được seed qua Flyway; tài khoản admin được tạo qua ApplicationRunner từ biến môi trường.

## Technical Context

**Backend Language/Version**: Java 21 / Spring Boot 3.4.5  
**Frontend Language/Version**: TypeScript 5.x / Vue 3.5.x + Vite 6.x  
**Primary Dependencies (BE)**: Spring Boot 3.4.5, Maven, PostgreSQL 15+, Flyway, Spring Data JPA, Lombok, **spring-boot-starter-security**, **jjwt-api 0.12.x**, **jjwt-impl 0.12.x**, **jjwt-jackson 0.12.x**  
**Primary Dependencies (FE)**: Vue Router 4, Pinia, PrimeVue 4.3.x Aura, PrimeIcons — **không cần package mới**, JWT decode thủ công qua `atob()` — xem research.md R-005  
**Storage**: PostgreSQL 15+  
**Testing (BE)**: JUnit 5 / Mockito (unit); Spring Boot Test / Testcontainers (integration)  
**Testing (FE)**: Vitest (đã cấu hình trong dự án)  
**Project Type**: web-service (BE) + web-app (FE)  
**Performance Goals**: API auth response ≤ 500ms (SC-005); token validation không gọi DB (stateless)  
**Constraints**: Stateless JWT 8 giờ; BCrypt password hashing; flat RBAC (1 user = 1 role); Permission là dữ liệu tĩnh; admin seed qua ApplicationRunner + env var  
**Scale/Scope**: Nội bộ, ~10–50 user đồng thời; không cần horizontal JWT scaling trong v1

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Gate | Status | Notes |
|------|--------|-------|
| BE là nguồn sự thật (auth logic, JWT validation, permission check) | ✅ PASS | Toàn bộ auth ở BE; FE chỉ lưu token và điều hướng |
| All IDs là UUID | ✅ PASS (justified deviation) | `app_user.id`, `role.id` đều UUID. `permission` dùng `code` VARCHAR PK — documented trong Complexity Tracking |
| Package structure đầy đủ (controller/service/repository/dto/entity/mapper/exception/config) | ✅ PASS | SecurityConfig + JwtFilter trong `config/`; auth DTOs; UserService; UserRepository |
| REST API convention `GET/POST/PUT/DELETE /api/{resource}` | ✅ PASS | `/api/auth/*`, `/api/users/*`, `/api/roles/*`, `/api/permissions` |
| ApiResponse wrapper trên tất cả endpoint | ✅ PASS | Endpoint login trả `ApiResponse<LoginResponse>` |
| Flyway cho database migration | ✅ PASS | V6 + V7 migration |
| BigDecimal cho trường tài chính | ✅ PASS (N/A) | Feature này không có trường tài chính |
| Unit test cho logic quan trọng | ✅ PASS (required) | JwtService, UserService, AuthController cần test |
| PrimeVue components imported locally per SFC | ✅ PASS | LoginForm, UserList, RoleList đều local import |
| Pinia cho shared state | ✅ PASS | `authStore` lưu token + user info |
| BE/ và FE/ directory separation | ✅ PASS | Không thay đổi cấu trúc thư mục gốc |

**Gate result: TẤT CẢ PASS — tiến hành Phase 0.**

## Project Structure

### Documentation (this feature)

```text
specs/006-user-auth-rbac/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
│   ├── auth-api.md
│   ├── roles-api.md
│   ├── users-api.md
│   └── permissions-api.md
└── tasks.md             # Phase 2 output (/speckit.tasks — NOT created by /speckit.plan)
```

### Source Code

```text
BE/
├── src/main/java/com/internal/projectmgmt/
│   ├── config/
│   │   ├── SecurityConfig.java          # SecurityFilterChain, BCryptPasswordEncoder
│   │   └── JwtAuthenticationFilter.java # OncePerRequestFilter
│   ├── controller/
│   │   ├── AuthController.java
│   │   ├── UserController.java
│   │   └── RoleController.java
│   ├── service/
│   │   ├── AuthService.java
│   │   ├── JwtService.java
│   │   ├── UserService.java
│   │   └── RoleService.java
│   ├── repository/
│   │   ├── AppUserRepository.java
│   │   └── RoleRepository.java
│   ├── entity/
│   │   ├── AppUser.java
│   │   ├── Role.java
│   │   └── Permission.java
│   ├── dto/
│   │   ├── auth/LoginRequest.java
│   │   ├── auth/LoginResponse.java
│   │   ├── user/UserRequest.java
│   │   ├── user/UserResponse.java
│   │   ├── role/RoleRequest.java
│   │   └── role/RoleResponse.java
│   ├── mapper/
│   │   ├── UserMapper.java
│   │   └── RoleMapper.java
│   └── seed/
│       └── AdminUserSeeder.java         # ApplicationRunner — seed admin từ env var
├── src/main/resources/db/migration/
│   ├── V6__user_role_permission_schema.sql
│   └── V7__seed_permissions_and_roles.sql
└── src/test/java/com/internal/projectmgmt/
    ├── service/JwtServiceTest.java
    ├── service/AuthServiceTest.java
    ├── service/UserServiceTest.java
    └── controller/AuthControllerTest.java

FE/
├── src/
│   ├── views/
│   │   ├── auth/LoginView.vue
│   │   ├── admin/users/UserListView.vue
│   │   └── admin/roles/RoleListView.vue
│   ├── components/
│   │   ├── auth/LoginForm.vue
│   │   ├── admin/users/UserDialog.vue
│   │   └── admin/roles/
│   │       ├── RoleDialog.vue
│   │       └── PermissionTreeDialog.vue
│   ├── stores/
│   │   └── authStore.ts
│   ├── services/
│   │   ├── authService.ts
│   │   ├── userService.ts
│   │   └── roleService.ts
│   ├── router/
│   │   └── index.ts                     # thêm navigation guard + route meta
│   └── types/
│       ├── auth.ts
│       ├── user.ts
│       └── role.ts
```

**Structure Decision**: Web application (BE/ + FE/ layout per constitution). SecurityConfig và JwtAuthenticationFilter vào `config/` package; AdminUserSeeder vào `seed/` sub-package (không cần thêm tầng mới).

## Complexity Tracking

| Deviation | Justification | Trade-off |
|-----------|---------------|-----------|
| `permission.code` dùng VARCHAR PK thay vì UUID | Mã permission (e.g., `VIEW_DASHBOARD`) phải readable trong JWT claims và FE permission check; nếu dùng UUID thì cần string-code lookup trên mỗi token validation | Không thể dùng sequence hay UUID generation tự động — code phải được define thủ công theo spec |
