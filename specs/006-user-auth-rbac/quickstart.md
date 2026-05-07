# Quickstart: Feature 006 — User Auth & RBAC

**Target audience**: Developer bắt đầu implement feature 006

---

## Prerequisites

- Java 21, Maven 3.9+
- Node.js 20+, npm 10+
- PostgreSQL 15+ (local hoặc Docker)
- `.env` file hoặc biến môi trường đã set

---

## Environment Variables

Thêm vào `BE/src/main/resources/application.yml` (hoặc set qua OS env):

```yaml
app:
  jwt:
    secret: "${APP_JWT_SECRET}"          # Min 32 ký tự, random base64
    expiration-hours: 8
  admin:
    initial-password: "${APP_ADMIN_PASSWORD}"  # Mật khẩu tài khoản admin khi seed lần đầu
```

**Tạo `.env` local** (không commit vào git):
```
APP_JWT_SECRET=your-super-secret-key-at-least-32-characters-here
APP_ADMIN_PASSWORD=Admin@123456
```

---

## Backend Setup

```bash
cd BE

# Thêm dependencies mới vào pom.xml:
# - spring-boot-starter-security
# - jjwt-api 0.12.6
# - jjwt-impl 0.12.6 (runtime)
# - jjwt-jackson 0.12.6 (runtime)

# Chạy Flyway migration (V6 + V7 sẽ tự chạy khi start):
mvn spring-boot:run
```

**Flyway migrations sẽ:**
1. V6: Tạo bảng `role`, `app_user`, `permission`, `role_permission`; add FK trên `project.represent_id`
2. V7: Seed 15 permissions + role `SYSTEM_ADMIN`
3. `AdminUserSeeder` (ApplicationRunner): Tạo user `admin` với password từ `APP_ADMIN_PASSWORD` (nếu chưa tồn tại)

**Verify seed thành công:**
```sql
SELECT id, username, active FROM app_user WHERE username = 'admin';
SELECT count(*) FROM permission;  -- phải = 15
SELECT count(*) FROM role;        -- phải = 1 (SYSTEM_ADMIN)
```

---

## Frontend Setup

```bash
cd FE
npm install   # Không cần package mới trong v1

npm run dev
```

**Test thủ công luồng login:**
1. Truy cập `http://localhost:5173` (bất kỳ route) → redirect về `/login`
2. Đăng nhập: username `admin`, password từ `APP_ADMIN_PASSWORD`
3. Sau đăng nhập → redirect về Dashboard
4. Mở DevTools → Application → localStorage → xem token

---

## Kiểm Tra Nhanh Sau Implement

```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin@123456"}'

# Dùng token từ response trên để gọi API protected
TOKEN="eyJhbGci..."
curl http://localhost:8080/api/users \
  -H "Authorization: Bearer $TOKEN"

# Test 401 - không có token
curl http://localhost:8080/api/users
# Expected: 401 AUTH_TOKEN_EXPIRED hoặc AUTH_TOKEN_MISSING
```

---

## Thứ Tự Implement Đề Xuất

1. **V6 + V7 Flyway migration** → DB schema sẵn sàng
2. **Entities** (`Role`, `AppUser`, `Permission`) + Repositories
3. **SecurityConfig** (disable default auth, setup filter chain) — **QUAN TRỌNG**: làm trước khi add Spring Security dependency để tránh lock out
4. **JwtService** (generate + validate token)
5. **JwtAuthenticationFilter** (OncePerRequestFilter)
6. **AuthService + AuthController** (login endpoint)
7. **AdminUserSeeder** (ApplicationRunner)
8. **UserService + UserController** + **RoleService + RoleController**
9. **Unit tests**: JwtServiceTest, AuthServiceTest, UserServiceTest
10. **FE**: authStore (Pinia) + LoginView + router guard + axios interceptor
11. **FE**: UserListView + RoleListView + PermissionTreeDialog
12. **US4**: Cập nhật ProjectCard dropdown Người đại diện

---

## Lưu Ý Bảo Mật

- **KHÔNG** commit `APP_JWT_SECRET` hay `APP_ADMIN_PASSWORD` vào git.
- `APP_JWT_SECRET` phải tối thiểu 256-bit (32 bytes) cho HMAC-SHA256.
- Đổi password admin mặc định ngay sau deploy production lần đầu.
- Existing controller tests sẽ cần `@WithMockUser` sau khi thêm Spring Security (xem research.md R-011).
