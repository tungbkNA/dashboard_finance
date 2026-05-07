# Research: Quản Lý Người Dùng, Phân Quyền và Đăng Nhập

**Date**: 2026-05-07
**Status**: Complete — all NEEDS CLARIFICATION resolved

---

## R-001: JWT Library cho Spring Boot 3.x

**Decision**: JJWT (io.jsonwebtoken) phiên bản 0.12.x  
**Rationale**: JJWT là thư viện JWT thuần Java phổ biến nhất, tương thích Spring Boot 3.x / Java 21, không cần Spring Security OAuth2 Resource Server (tránh over-engineering). API builder fluent, type-safe. Phiên bản 0.12.x có breaking change so với 0.11.x (sử dụng `Jwts.builder()` mới) — cần dùng đúng phiên bản.  
**Alternatives considered**:
- `spring-boot-starter-oauth2-resource-server`: quá phức tạp cho internal app, yêu cầu issuer URL.
- `nimbus-jose-jwt`: tốt nhưng ít documentation hơn cho Spring context.
- Spring Security built-in token: không hỗ trợ JWT stateless tốt.

**Maven dependencies**:
```xml
<dependency>
  <groupId>io.jsonwebtoken</groupId>
  <artifactId>jjwt-api</artifactId>
  <version>0.12.6</version>
</dependency>
<dependency>
  <groupId>io.jsonwebtoken</groupId>
  <artifactId>jjwt-impl</artifactId>
  <version>0.12.6</version>
  <scope>runtime</scope>
</dependency>
<dependency>
  <groupId>io.jsonwebtoken</groupId>
  <artifactId>jjwt-jackson</artifactId>
  <version>0.12.6</version>
  <scope>runtime</scope>
</dependency>
```

---

## R-002: Spring Security Stateless JWT Pattern

**Decision**: `SecurityFilterChain` với `SessionCreationPolicy.STATELESS` + custom `OncePerRequestFilter` (JwtAuthenticationFilter) đặt trước `UsernamePasswordAuthenticationFilter`.  
**Rationale**: Pattern chuẩn nhất cho REST API stateless. Không dùng `WebSecurityConfigurerAdapter` (deprecated từ Spring Security 5.7). `JwtAuthenticationFilter` đọc header `Authorization: Bearer <token>`, validate, và set `SecurityContextHolder`. Không có session server-side.  
**Configuration notes**:
- CSRF disable (stateless API không cần).
- Endpoint `/api/auth/login` là `permitAll()`.
- Tất cả route còn lại: `authenticated()`.
- Permission-level guard: dùng `@PreAuthorize("hasAuthority('MANAGE_USER')")` trên controller method — Spring Security sẽ load GrantedAuthority từ token claims.

---

## R-003: BCrypt Password Hashing

**Decision**: `BCryptPasswordEncoder` built-in của Spring Security, strength mặc định 10.  
**Rationale**: BCrypt đã có trong `spring-boot-starter-security`, không cần dependency bổ sung. Strength 10 cân bằng tốt giữa bảo mật và performance (hash ~100ms). API `passwordEncoder.encode(plain)` và `passwordEncoder.matches(plain, hash)` đủ dùng.  
**Security note**: BCrypt tự động tạo salt ngẫu nhiên — không cần quản lý salt thủ công.

---

## R-004: Admin Seed Account Strategy

**Decision**: ApplicationRunner `AdminUserSeeder` đọc mật khẩu từ `${app.admin.initial-password}` (Spring `@Value`), kiểm tra xem user `admin` đã tồn tại chưa trước khi tạo.  
**Rationale**: Flyway migration là SQL thuần — không thể đọc biến môi trường Spring hay chạy BCrypt. ApplicationRunner chạy sau khi context khởi động, có đầy đủ Spring beans, và có thể safely hash password trước khi lưu.  
**Behavior**:
- Nếu user `admin` chưa tồn tại → tạo với role `SYSTEM_ADMIN` và hash password từ env var.
- Nếu đã tồn tại → skip (idempotent).
- Nếu env var không được set → log warning và skip (không throw exception để không block startup).

---

## R-005: Frontend JWT Handling (No New Package)

**Decision**: Decode JWT claims thủ công bằng `atob()` + `JSON.parse()` (không cần package `jose` hay `jwt-decode`).  
**Rationale**: Frontend chỉ cần đọc `exp` (expiry) và `sub` (username) từ payload — không cần verify signature (verification là trách nhiệm của backend). `atob(payload.split('.')[1])` đủ dùng.  
**Auth store pattern (Pinia)**:
```typescript
// stores/authStore.ts
interface AuthState {
  token: string | null
  user: { id: string; username: string; displayName: string; permissions: string[] } | null
}
```
Token lưu trong `localStorage` (persistent across page refresh). Logout xóa `localStorage` và reset store.

---

## R-006: Vue Router Navigation Guard

**Decision**: Global `beforeEach` guard trên `router/index.ts` kiểm tra `authStore.token` và route meta `requiresAuth: true`.  
**Rationale**: Global guard đơn giản hơn per-route guard khi có nhiều protected routes. Meta `requiresAuth` cho phép đánh dấu từng route riêng thay vì maintain danh sách whitelist.  
**Pattern**:
```typescript
router.beforeEach((to, _from) => {
  const auth = useAuthStore()
  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.name === 'login' && auth.isAuthenticated) {
    return { name: 'dashboard' }
  }
})
```
`isAuthenticated` kiểm tra token tồn tại VÀ chưa hết hạn (decode exp từ JWT payload).

---

## R-007: Axios Interceptor cho Token + 401 Handling

**Decision**: Request interceptor inject `Authorization: Bearer <token>` từ store; Response interceptor bắt 401 và redirect Login.  
**Pattern**:
```typescript
// Khi token hết hạn, BE trả 401 → FE redirect đến /login với thông báo
api.interceptors.response.use(
  (res) => res,
  (error) => {
    if (error.response?.status === 401) {
      authStore.logout()
      router.push({ name: 'login', query: { expired: '1' } })
    }
    return Promise.reject(error)
  }
)
```

---

## R-008: PostgreSQL Reserved Word — "user" Table

**Decision**: Đặt tên bảng là `app_user` (không phải `user`).  
**Rationale**: `user` là reserved word trong PostgreSQL — cần quote mỗi lần query. `app_user` rõ ràng hơn và tránh lỗi phát sinh trong native queries hay Flyway.

---

## R-009: represent_id Migration Strategy

**Decision**: V6 migration thêm FK constraint `REFERENCES app_user(id)` trên cột `represent_id` hiện có. Giá trị cũ (UUID không trỏ đến user nào) sẽ được set `NULL` trước khi thêm FK.  
**Rationale**: `represent_id` hiện là `UUID NULL` không có FK. Trước khi thêm FK, phải xử lý các giá trị orphan. Migration an toàn:
```sql
UPDATE project SET represent_id = NULL
WHERE represent_id IS NOT NULL
  AND represent_id NOT IN (SELECT id FROM app_user);

ALTER TABLE project
  ADD CONSTRAINT fk_project_represent_user
  FOREIGN KEY (represent_id) REFERENCES app_user(id) ON DELETE SET NULL;
```
`ON DELETE SET NULL` đảm bảo xóa user không phá vỡ dự án.

---

## R-010: Permission Tree Minimum Set

**Decision**: Seed 5 permissions gốc (SCREEN level) + 10 action permissions trong V7 migration.  
**Minimal permission set**:

| Code | Display Name | Type | Parent |
|---|---|---|---|
| `VIEW_DASHBOARD` | Xem Dashboard | SCREEN | null |
| `MANAGE_PROJECT` | Quản lý Dự án | SCREEN | null |
| `MANAGE_USER` | Quản lý Người dùng | SCREEN | null |
| `MANAGE_ROLE` | Quản lý Phân quyền | SCREEN | null |
| `SYSTEM_SETTINGS` | Cài đặt Hệ thống | SCREEN | null |
| `PROJECT_CREATE` | Tạo dự án | ACTION | `MANAGE_PROJECT` |
| `PROJECT_EDIT` | Sửa dự án | ACTION | `MANAGE_PROJECT` |
| `PROJECT_DELETE` | Xóa dự án | ACTION | `MANAGE_PROJECT` |
| `USER_CREATE` | Tạo người dùng | ACTION | `MANAGE_USER` |
| `USER_EDIT` | Sửa người dùng | ACTION | `MANAGE_USER` |
| `USER_DEACTIVATE` | Vô hiệu hóa người dùng | ACTION | `MANAGE_USER` |
| `ROLE_CREATE` | Tạo role | ACTION | `MANAGE_ROLE` |
| `ROLE_EDIT` | Sửa role | ACTION | `MANAGE_ROLE` |
| `ROLE_DEACTIVATE` | Vô hiệu hóa role | ACTION | `MANAGE_ROLE` |
| `ROLE_ASSIGN_PERMISSIONS` | Gán quyền cho role | ACTION | `MANAGE_ROLE` |

**Seed role**: `SYSTEM_ADMIN` — gán tất cả permissions trên.

---

## R-011: Tích Hợp Spring Security với Codebase Hiện Tại

**Issue identified**: Codebase hiện tại không có Spring Security. Khi thêm `spring-boot-starter-security`, toàn bộ endpoint sẽ tự động bị bảo vệ (HTTP Basic Auth by default). Cần cấu hình `SecurityFilterChain` ngay.  
**Deployment order**: SecurityConfig phải được implement trước khi thêm dependency vào pom.xml (để tránh break existing endpoints trong development).  
**Test impact**: Existing controller tests dùng `@WebMvcTest` sẽ cần thêm `@WithMockUser` hoặc disable security trong test context.
