# Tasks: Quản Lý Người Dùng, Phân Quyền và Đăng Nhập

**Input**: Design documents from `/specs/006-user-auth-rbac/`
**Prerequisites**: [plan.md](plan.md), [spec.md](spec.md), [research.md](research.md), [data-model.md](data-model.md), [contracts/](contracts/)

**Tests**: Unit tests required for JWT logic, AuthService, UserService (per plan.md constitution requirement 4.4).

**Organization**: Grouped by user story — each story is independently testable and deliverable.

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Flyway migrations + Spring Security dependency + Maven dependencies added before any feature code.

- [X] T001 Add `spring-boot-starter-security`, `jjwt-api 0.12.6`, `jjwt-impl 0.12.6` (runtime), `jjwt-jackson 0.12.6` (runtime) to `BE/pom.xml`
- [X] T002 Add `app.jwt.secret`, `app.jwt.expiration-hours`, `app.admin.initial-password` properties to `BE/src/main/resources/application.yml`
- [X] T003 Create Flyway migration `BE/src/main/resources/db/migration/V6__user_role_permission_schema.sql`: bảng `role`, `app_user`, `permission`, `role_permission`; UPDATE orphan `project.represent_id` → NULL; ALTER TABLE project ADD FK `fk_project_represent_user` REFERENCES `app_user(id)` ON DELETE SET NULL
- [X] T004 Create Flyway migration `BE/src/main/resources/db/migration/V7__seed_permissions_and_roles.sql`: seed 15 permission rows + 1 role `SYSTEM_ADMIN` với tất cả permissions (theo data-model.md)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Spring Security config + JWT infrastructure + core entities phải hoàn thành trước khi bắt đầu bất kỳ user story nào.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [X] T005 [P] Create entity `BE/src/main/java/com/internal/projectmgmt/entity/Permission.java`: fields `code` (PK String), `displayName`, `parentCode` (nullable FK self-ref), `type` (enum SCREEN/ACTION), `sortOrder`
- [X] T006 [P] Create entity `BE/src/main/java/com/internal/projectmgmt/entity/Role.java`: fields `id` (UUID), `roleName`, `description`, `active`, `deleted`, timestamps; `@ManyToMany` to `Permission` via `role_permission`
- [X] T007 Create entity `BE/src/main/java/com/internal/projectmgmt/entity/AppUser.java`: fields `id` (UUID), `username`, `email`, `displayName`, `passwordHash`, `active`, `deleted`, timestamps; `@ManyToOne` to `Role` (FK `role_id`) — implements `UserDetails` for Spring Security
- [X] T008 [P] Create `BE/src/main/java/com/internal/projectmgmt/repository/AppUserRepository.java`: `findByUsername(String)`, `existsByUsername(String)`, `existsByEmail(String)`
- [X] T009 [P] Create `BE/src/main/java/com/internal/projectmgmt/repository/RoleRepository.java`: `findByDeletedFalse()`, `findByIdAndDeletedFalse(UUID)`, `existsByRoleNameIgnoreCaseAndDeletedFalse(String)`
- [X] T010 Create `BE/src/main/java/com/internal/projectmgmt/config/SecurityConfig.java`: `SecurityFilterChain` với `SessionCreationPolicy.STATELESS`, CSRF disabled, `/api/auth/login` permitAll, tất cả route còn lại authenticated; `BCryptPasswordEncoder` bean; `AuthenticationManager` bean; đăng ký `JwtAuthenticationFilter` (trước UsernamePasswordAuthenticationFilter) — **PHẢI làm trước T011 để tránh lock-out**
- [X] T011 Create `BE/src/main/java/com/internal/projectmgmt/config/JwtAuthenticationFilter.java`: `OncePerRequestFilter`; đọc `Authorization: Bearer` header; validate token qua `JwtService`; set `SecurityContextHolder` với `UsernamePasswordAuthenticationToken` chứa authorities từ token claims
- [X] T012 Create `BE/src/main/java/com/internal/projectmgmt/service/JwtService.java`: `generateToken(AppUser)` nhúng claims `userId`, `roleId`, `permissions[]`; `validateToken(String)`; `extractUsername(String)`; `extractPermissions(String)`; dùng HMAC-SHA256 key từ `app.jwt.secret`
- [X] T013 Create `BE/src/main/java/com/internal/projectmgmt/seed/AdminUserSeeder.java`: `ApplicationRunner`; đọc `app.admin.initial-password`; nếu user `admin` chưa tồn tại thì tạo với role `SYSTEM_ADMIN` và BCrypt-hashed password; idempotent (skip nếu đã tồn tại)

**Checkpoint**: Foundation complete — Spring Security running, JWT working, DB schema up, admin user seeded.

---

## Phase 3: User Story 1 — Đăng Nhập và Bảo Vệ Màn Hình (Priority: P1) 🎯 MVP

**Goal**: Người dùng phải đăng nhập trước khi vào bất kỳ màn hình nào. Đăng nhập thành công → Dashboard. Token hết hạn hoặc đăng xuất → Login.

**Independent Test**: `curl POST /api/auth/login` với đúng credentials → 200 + token. Truy cập `GET /api/users` không có token → 401. Truy cập FE route `/` khi chưa đăng nhập → redirect `/login`.

### Implementation — User Story 1

- [X] T014 [US1] Create DTO `BE/src/main/java/com/internal/projectmgmt/dto/auth/LoginRequest.java`: `username`, `password` (validation: @NotBlank)
- [X] T015 [P] [US1] Create DTO `BE/src/main/java/com/internal/projectmgmt/dto/auth/LoginResponse.java`: `token`, `expiresAt`, nested `user` object với `id`, `username`, `displayName`, `roleId`, `roleName`, `permissions[]`
- [X] T016 [US1] Create `BE/src/main/java/com/internal/projectmgmt/service/AuthService.java`: `login(LoginRequest)` → load user by username, check active (throw `AUTH_ACCOUNT_INACTIVE`), check role active (throw `AUTH_ROLE_INACTIVE`), verify password BCrypt, generate JWT, return `LoginResponse`; sai credentials → throw `AUTH_INVALID_CREDENTIALS` (không tiết lộ tài khoản tồn tại)
- [X] T017 [US1] Create `BE/src/main/java/com/internal/projectmgmt/controller/AuthController.java`: `POST /api/auth/login` (public); `POST /api/auth/logout` (authenticated, no-op); `GET /api/auth/me` (authenticated, trả UserInfo từ token); tất cả wrap trong `ApiResponse<T>`
- [X] T018 [P] [US1] Write unit test `BE/src/test/java/com/internal/projectmgmt/service/JwtServiceTest.java`: generate token → validate → extract claims; tampered token → invalid; expired token → invalid
- [X] T019 [P] [US1] Write unit test `BE/src/test/java/com/internal/projectmgmt/service/AuthServiceTest.java`: đúng credentials → LoginResponse; sai password → AUTH_INVALID_CREDENTIALS; user inactive → AUTH_ACCOUNT_INACTIVE; role inactive → AUTH_ROLE_INACTIVE
- [X] T020 [US1] Create FE type `FE/src/types/auth.ts`: `LoginRequest`, `LoginResponse`, `AuthUser` (id, username, displayName, roleId, roleName, permissions[])
- [X] T021 [US1] Create `FE/src/stores/authStore.ts` (Pinia): state `token`, `user`; actions `login(credentials)`, `logout()`, `initFromStorage()`; getter `isAuthenticated` (token tồn tại + chưa expired, kiểm tra bằng `atob` decode `exp` claim); persist token vào `localStorage`
- [X] T022 [US1] Create `FE/src/services/authService.ts`: `login(req: LoginRequest)`, `logout()`, `getMe()` — wrap axios calls tới `/api/auth/*`
- [X] T023 [US1] Update `FE/src/router/index.ts`: thêm `meta: { requiresAuth: true }` trên tất cả route hiện có; thêm route `/login` (name: 'login') trỏ đến `LoginView`; thêm global `beforeEach` guard — chưa đăng nhập + requiresAuth → redirect login; đã đăng nhập + vào /login → redirect dashboard
- [X] T024 [US1] Create `FE/src/views/auth/LoginView.vue`: layout full-screen centered; nhúng `LoginForm`
- [X] T025 [US1] Create `FE/src/components/auth/LoginForm.vue`: PrimeVue `InputText`, `Password`, `Button`; submit gọi `authStore.login()`; hiển thị lỗi từ API (AUTH_INVALID_CREDENTIALS, AUTH_ACCOUNT_INACTIVE, AUTH_ROLE_INACTIVE); loading state; tông màu đỏ per constitution
- [X] T026 [US1] Update `FE/src/services/api.ts` (hoặc axios instance hiện tại): thêm request interceptor inject `Authorization: Bearer <token>` từ `authStore`; thêm response interceptor bắt 401 → `authStore.logout()` → redirect `/login?expired=1`; hiển thị toast "Phiên đăng nhập đã hết hạn" khi `?expired=1`
- [X] T027 [US1] Update AppLayout/AppHeader (hoặc tương đương) để hiển thị tên user đăng nhập và nút Đăng xuất gọi `authStore.logout()`

**Checkpoint**: US1 fully functional. Login/logout works. All protected routes redirect to /login when unauthenticated. 401 auto-redirects.

---

## Phase 4: User Story 2 — Quản Lý Role và Phân Quyền (Priority: P2)

**Goal**: Admin tạo/sửa/xóa mềm Role và gán Permission qua giao diện cây. Role inactive → tất cả user thuộc role đó không thể login.

**Independent Test**: Tạo Role mới qua POST `/api/roles` → 201. GET `/api/permissions` → cây 15 nodes. PUT `/api/roles/{id}/permissions` → cập nhật permissions. DELETE `/api/roles/{id}` với role có user active → response `requiresConfirmation: true`; DELETE với `?force=true` → 200.

### Implementation — User Story 2

- [X] T028 [US2] Create DTO `BE/src/main/java/com/internal/projectmgmt/dto/role/RoleRequest.java`: `roleName` (@NotBlank, max 100), `description` (optional, max 1000)
- [X] T029 [P] [US2] Create DTO `BE/src/main/java/com/internal/projectmgmt/dto/role/RoleResponse.java`: `id`, `roleName`, `description`, `active`, `userCount`, `createdAt`; `RoleDetailResponse` extends với `permissions[]`
- [X] T030 [P] [US2] Create DTO `BE/src/main/java/com/internal/projectmgmt/dto/role/PermissionResponse.java`: `code`, `displayName`, `parentCode`, `type`, `sortOrder`
- [X] T031 [P] [US2] Create DTO `BE/src/main/java/com/internal/projectmgmt/dto/role/UpdateRolePermissionsRequest.java`: `permissions` (List<String>, @NotNull)
- [X] T032 [P] [US2] Create `BE/src/main/java/com/internal/projectmgmt/mapper/RoleMapper.java`: `toResponse(Role, long userCount)`, `toDetailResponse(Role, long userCount)`, `toPermissionResponse(Permission)`
- [X] T033 [US2] Create `BE/src/main/java/com/internal/projectmgmt/service/RoleService.java`: `listAll()`, `getById(UUID)`, `create(RoleRequest)`, `update(UUID, RoleRequest)`, `softDelete(UUID, boolean force)` — nếu có user active và !force trả `ROLE_DELETE_REQUIRES_CONFIRMATION`; `getPermissions(UUID)`, `updatePermissions(UUID, List<String>)` bulk-replace trong transaction
- [X] T034 [US2] Create `BE/src/main/java/com/internal/projectmgmt/controller/RoleController.java`: `GET /api/roles`, `POST /api/roles`, `GET /api/roles/{id}`, `PUT /api/roles/{id}`, `DELETE /api/roles/{id}` (với `?force=true`), `GET /api/roles/{id}/permissions`, `PUT /api/roles/{id}/permissions`; `@PreAuthorize` theo từng endpoint; wrap `ApiResponse<T>`
- [X] T035 [US2] Create `BE/src/main/java/com/internal/projectmgmt/controller/PermissionController.java`: `GET /api/permissions` (trả List<PermissionResponse> phẳng); `@PreAuthorize("hasAuthority('MANAGE_ROLE')")`
- [X] T036 [US2] Create FE type `FE/src/types/role.ts`: `Role`, `RoleDetail`, `Permission`, `UpdateRolePermissionsRequest`
- [X] T037 [US2] Create `FE/src/services/roleService.ts`: `listRoles()`, `getRole(id)`, `createRole(req)`, `updateRole(id, req)`, `deleteRole(id, force?)`, `getRolePermissions(id)`, `updateRolePermissions(id, permissions)`, `listPermissions()`
- [X] T038 [US2] Create `FE/src/views/admin/roles/RoleListView.vue`: DataTable danh sách roles (roleName, description, active, userCount); toolbar Tạo mới; actions Sửa, Phân quyền, Xóa; loading/empty/error state; route: `/admin/roles`
- [X] T039 [US2] Create `FE/src/components/admin/roles/RoleDialog.vue`: Dialog Tạo/Sửa role — InputText roleName, Textarea description; validate; gọi `roleService.createRole/updateRole`; toast thành công/lỗi
- [X] T040 [US2] Create `FE/src/components/admin/roles/PermissionTreeDialog.vue`: Dialog Phân quyền — fetch `GET /api/permissions` → build cây từ `parentCode`; PrimeVue `Tree` với checkbox selection; fetch current permissions từ `GET /api/roles/{id}/permissions` để pre-select; nút Lưu gọi `updateRolePermissions`; toast kết quả
- [X] T041 [US2] Implement 2-step delete confirm trong `RoleListView.vue`: DELETE lần 1 → nếu `ROLE_DELETE_REQUIRES_CONFIRMATION` hiển thị `ConfirmDialog` cảnh báo số user bị ảnh hưởng → nếu xác nhận gọi DELETE `?force=true`; toast kết quả
- [X] T042 [US2] Update `FE/src/router/index.ts`: thêm route `/admin/roles` → `RoleListView` với `meta: { requiresAuth: true, permission: 'MANAGE_ROLE' }`; thêm 403 guard trong `beforeEach` nếu user thiếu permission

**Checkpoint**: US2 fully functional. Role CRUD works. Permission tree UI works. Soft-delete with 2-step confirm works.

---

## Phase 5: User Story 3 — Quản Lý Người Dùng (Priority: P3)

**Goal**: Admin tạo/sửa/vô hiệu hoá User. BCrypt password. User inactive → không thể login.

**Independent Test**: POST `/api/users` → 201. Đăng nhập bằng user vừa tạo → 200. DELETE `/api/users/{id}` → 200. Đăng nhập bằng user đã deactivate → 403 AUTH_ACCOUNT_INACTIVE.

### Implementation — User Story 3

- [X] T043 [US3] Create DTO `BE/src/main/java/com/internal/projectmgmt/dto/user/UserRequest.java`: `username` (@NotBlank, @Size(3,50), @Pattern `[a-zA-Z0-9_.-]+`), `email` (@NotBlank, @Email), `displayName` (@NotBlank, max 255), `password` (@NotBlank, @Size(min=8) — chỉ khi tạo mới), `roleId` (@NotNull)
- [X] T044 [P] [US3] Create DTO `BE/src/main/java/com/internal/projectmgmt/dto/user/UserResponse.java`: `id`, `username`, `email`, `displayName`, `roleId`, `roleName`, `active`, `createdAt` — **không có** `passwordHash`
- [X] T045 [P] [US3] Create DTO `BE/src/main/java/com/internal/projectmgmt/dto/user/UpdateUserRequest.java`: `email` (optional), `displayName` (optional), `roleId` (optional), `active` (optional)
- [X] T046 [P] [US3] Create DTO `BE/src/main/java/com/internal/projectmgmt/dto/user/ResetPasswordRequest.java`: `newPassword` (@NotBlank, @Size(min=8))
- [X] T047 [P] [US3] Create `BE/src/main/java/com/internal/projectmgmt/mapper/UserMapper.java`: `toResponse(AppUser)` — map tất cả fields trừ passwordHash
- [X] T048 [US3] Create `BE/src/main/java/com/internal/projectmgmt/service/UserService.java`: `listAll()`, `getById(UUID)`, `create(UserRequest)` — BCrypt hash password, check username/email unique; `update(UUID, UpdateUserRequest)`; `resetPassword(UUID, ResetPasswordRequest)`; `softDelete(UUID, UUID currentUserId)` — không cho xóa chính mình; validate `roleId` phải active + not deleted khi tạo/sửa
- [X] T049 [US3] Create `BE/src/main/java/com/internal/projectmgmt/controller/UserController.java`: `GET /api/users` (với `?active=true` filter), `POST /api/users`, `GET /api/users/{id}`, `PUT /api/users/{id}`, `PUT /api/users/{id}/password`, `DELETE /api/users/{id}`; `@PreAuthorize` theo permission; wrap `ApiResponse<T>`
- [X] T050 [P] [US3] Write unit test `BE/src/test/java/com/internal/projectmgmt/service/UserServiceTest.java`: tạo user → password được BCrypt hash (không lưu plain text); username duplicate → USER_USERNAME_EXISTS; email duplicate → USER_EMAIL_EXISTS; role inactive → ROLE_NOT_FOUND_OR_INACTIVE; xóa chính mình → USER_CANNOT_DELETE_SELF
- [X] T051 [US3] Create FE type `FE/src/types/user.ts`: `AppUser`, `UserRequest`, `UpdateUserRequest`, `ResetPasswordRequest`
- [X] T052 [US3] Create `FE/src/services/userService.ts`: `listUsers(active?)`, `getUser(id)`, `createUser(req)`, `updateUser(id, req)`, `resetPassword(id, req)`, `deleteUser(id)`
- [X] T053 [US3] Create `FE/src/views/admin/users/UserListView.vue`: DataTable danh sách users (username, displayName, email, roleName, active); toolbar Tạo mới; actions Sửa, Đặt lại MK, Vô hiệu hoá; ConfirmDialog trước khi vô hiệu hoá; loading/empty/error state; route: `/admin/users`
- [X] T054 [US3] Create `FE/src/components/admin/users/UserDialog.vue`: Dialog Tạo/Sửa user — InputText username (disabled khi edit), email, displayName; Password (chỉ hiện khi tạo); Dropdown role (fetch `/api/roles?active=true`); validate; gọi service; toast kết quả
- [X] T055 [US3] Update `FE/src/router/index.ts`: thêm route `/admin/users` → `UserListView` với `meta: { requiresAuth: true, permission: 'MANAGE_USER' }`

**Checkpoint**: US3 fully functional. User CRUD works. New user can immediately login. Deactivated user cannot login.

---

## Phase 6: User Story 4 — Cập Nhật Trường "Người Đại Diện" Dự Án (Priority: P4)

**Goal**: Trường `representId` trong Project chuyển từ text tự do sang FK đến `app_user`. Dropdown chỉ hiện User active. Dữ liệu cũ không resolve được → null.

**Independent Test**: Mở form Cài đặt Dự án → trường Người đại diện là Dropdown các User active. Chọn User → lưu → mở lại → đúng User được chọn. Dự án cũ có `represent_id = NULL` → trường hiển thị trống, không lỗi.

### Implementation — User Story 4

- [X] T056 [US4] Update entity `BE/src/main/java/com/internal/projectmgmt/entity/Project.java`: thay field `UUID representId` bằng `@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "represent_id") AppUser representUser` (nullable)
- [X] T057 [US4] Update DTO `BE/src/main/java/com/internal/projectmgmt/dto/project/ProjectRequest.java`: đổi field `representId` sang `UUID representUserId`; validator: nếu không null thì User phải tồn tại và active
- [X] T058 [US4] Update DTO `BE/src/main/java/com/internal/projectmgmt/dto/project/ProjectResponse.java`: thêm `representUserId` (UUID) và `representUserName` (String) — lấy từ `representUser.displayName`; giữ backward compat nếu null
- [X] T059 [US4] Update `BE/src/main/java/com/internal/projectmgmt/mapper/ProjectMapper.java`: map `representUser` → `representUserId` + `representUserName` khi null-safe
- [X] T060 [US4] Update `BE/src/main/java/com/internal/projectmgmt/service/ProjectService.java`: khi create/update project với `representUserId` không null, load `AppUser` entity và set `representUser`; validate user active
- [X] T061 [US4] Update FE type `FE/src/types/project-settings.ts`: thêm `representUserId: string | null`, `representUserName: string | null` vào `ProjectResponse` và `representUserId: string | null` vào `ProjectRequest`
- [X] T062 [US4] Update FE project form `FE/src/components/project-settings/ProjectTab.vue`: thay InputText "Người đại diện" bằng `Dropdown` PrimeVue; options fetch từ `GET /api/users?active=true`; option label = `displayName`, option value = `id`; nếu current `representUserId` null → unselected (không lỗi)

**Checkpoint**: US4 fully functional. Represent field is a user dropdown. Old null data shows empty correctly.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Bảo mật, test coverage, error handling nhất quán.

- [X] T063 [P] Write unit test `BE/src/test/java/com/internal/projectmgmt/controller/AuthControllerTest.java` (`@WebMvcTest` + `@WithMockUser`): POST `/api/auth/login` → 200; POST với sai credentials → 401; GET `/api/auth/me` không có token → 401
- [X] T064 [P] Update existing BE controller tests bị ảnh hưởng bởi Spring Security (thêm `@WithMockUser` hoặc mock `SecurityContext` — per research.md R-011)
- [X] T065 Verify `AdminUserSeeder` idempotent: chạy app 2 lần → chỉ có 1 user `admin` trong DB; log đúng khi skip
- [X] T066 [P] Verify không có mật khẩu plain text trong response: kiểm tra tất cả UserResponse serialization không có field `passwordHash`; kiểm tra `GET /api/users`, `GET /api/users/{id}`, `POST /api/users` response
- [X] T067 [P] Verify FE route guard: truy cập `/admin/users` khi không có permission `MANAGE_USER` → redirect 403 hoặc Dashboard; truy cập khi chưa đăng nhập → redirect `/login`
- [X] T068 Run `mvn test` trong `BE/` — tất cả tests pass
- [X] T069 Manual smoke test per [quickstart.md](quickstart.md): login → verify token in localStorage → call protected API → logout → verify redirect

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: No dependencies — start immediately
- **Phase 2 (Foundation)**: Depends on Phase 1 completion — **BLOCKS all user stories**
- **Phase 3 (US1 — Login)**: Depends on Phase 2 — **MUST complete before US2/US3 (FE auth infrastructure)**
- **Phase 4 (US2 — Role Mgmt)**: Depends on Phase 2 + Phase 3 FE auth store (T021, T026)
- **Phase 5 (US3 — User Mgmt)**: Depends on Phase 2 + Phase 3 FE auth store + Phase 4 BE roles (RoleService needed for user-role assignment)
- **Phase 6 (US4 — Represent Field)**: Depends on Phase 5 (AppUser entity must exist)
- **Phase 7 (Polish)**: Depends on all Phases 1–6

### User Story Dependencies

```
Phase 1 (Setup)
    └── Phase 2 (Foundation: Security + JWT + Entities)
            ├── Phase 3 (US1: Login/Logout/Guard) 🎯 MVP
            │       └── Phase 4 (US2: Role Management) [needs FE authStore from US1]
            │               └── Phase 5 (US3: User Management) [needs Role from US2]
            │                       └── Phase 6 (US4: Represent dropdown) [needs AppUser from US3]
            │
            └── (All phases above → Phase 7: Polish)
```

### Parallel Opportunities Within Each Phase

**Phase 2**: T005, T006 (Permission + Role entities) can run in parallel; T008, T009 (repositories) can run in parallel after entities done.

**Phase 3**: After T016 (AuthService) done — T018, T019 (unit tests) can run in parallel; After T021 (authStore) done — T022, T023 (FE service + router) can run in parallel; T024, T025, T026, T027 can run in parallel.

**Phase 4**: T028, T029, T030, T031, T032 (DTOs + mapper) all parallel; T038, T039, T040 (FE views + dialogs) parallel after T037 (roleService).

**Phase 5**: T043, T044, T045, T046, T047 (DTOs + mapper) all parallel; T053, T054 (FE views + dialogs) parallel after T052 (userService).

**Phase 7**: T063, T064, T065, T066, T067 all fully parallel.

---

## Implementation Strategy

**MVP scope (P1 only — Phases 1–3)**:  
Delivers login/logout + route guard. System is secured. No user management UI yet — admin uses seed account. ~27 tasks (T001–T027).

**Full delivery (Phases 1–7)**:  
Complete auth + RBAC + user management + represent field. ~69 tasks (T001–T069).

**Recommended incremental delivery**:
1. Ship Phase 1–3 (US1 MVP) → system is secured
2. Ship Phase 4 (US2) → admin can manage roles
3. Ship Phase 5 (US3) → admin can manage users
4. Ship Phase 6 (US4) → represent field linked to real users
5. Ship Phase 7 (Polish) → tests + hardening
