# Data Model: Quản Lý Người Dùng, Phân Quyền và Đăng Nhập

**Date**: 2026-05-07
**Source**: [spec.md](spec.md), [research.md](research.md)

---

## Entity Overview

```
app_user ──────────────── role ─────────────── role_permission ─── permission
   │                        │                                           │
   │ (FK represent_id)      │ (FK role_id)                              │
   ▼                        ▼                                           │
 project                app_user                                    (tree via
                                                                   parent_code)
```

---

## Entity: `role`

**Table**: `role`  
**Description**: Nhóm quyền được đặt tên. Tạo/sửa/xóa mềm bởi admin.

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `id` | UUID | PK, DEFAULT gen_random_uuid() | |
| `role_name` | VARCHAR(100) | NOT NULL, UNIQUE (partial — where deleted=false) | Tên hiển thị |
| `description` | TEXT | NULL | Mô tả tùy chọn |
| `active` | BOOLEAN | NOT NULL DEFAULT TRUE | FALSE = Inactive, không thể dùng |
| `deleted` | BOOLEAN | NOT NULL DEFAULT FALSE | Xóa mềm |
| `created_at` | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | |
| `updated_at` | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | |

**Indices**:
- `idx_role_name_lower` — `LOWER(role_name)` WHERE `deleted = FALSE` (unique)

**Business rules**:
- Khi `active = FALSE`: tất cả User thuộc Role này không thể đăng nhập.
- Không xóa vật lý nếu có User đang dùng — chỉ `deleted = TRUE, active = FALSE`.
- Xóa mềm yêu cầu xác nhận 2 bước nếu có User active đang sử dụng.

---

## Entity: `app_user`

**Table**: `app_user` (không dùng `user` — reserved word trong PostgreSQL)  
**Description**: Tài khoản người dùng của hệ thống.

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `id` | UUID | PK, DEFAULT gen_random_uuid() | |
| `username` | VARCHAR(50) | NOT NULL, UNIQUE | Dùng để đăng nhập |
| `email` | VARCHAR(255) | NOT NULL, UNIQUE | |
| `display_name` | VARCHAR(255) | NOT NULL | Tên hiển thị trong UI |
| `password_hash` | VARCHAR(255) | NOT NULL | BCrypt hash — không bao giờ trả về qua API |
| `role_id` | UUID | NOT NULL, FK → role(id) | Bắt buộc; 1 user = 1 role |
| `active` | BOOLEAN | NOT NULL DEFAULT TRUE | FALSE = không thể đăng nhập |
| `deleted` | BOOLEAN | NOT NULL DEFAULT FALSE | Xóa mềm |
| `created_at` | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | |
| `updated_at` | TIMESTAMPTZ | NOT NULL DEFAULT NOW() | |

**Indices**:
- `idx_app_user_username` — `username` (unique)
- `idx_app_user_email` — `email` (unique)
- `idx_app_user_role_id` — `role_id`

**Constraints**:
```sql
CONSTRAINT fk_app_user_role FOREIGN KEY (role_id) REFERENCES role(id)
```

**Business rules**:
- `active = FALSE`: login thất bại với thông báo riêng (phân biệt với sai mật khẩu).
- `role_id` không thể NULL — phải luôn có Role.
- Admin seed user (`username = 'admin'`) được tạo qua `AdminUserSeeder` (ApplicationRunner) khi khởi động lần đầu.

---

## Entity: `permission`

**Table**: `permission`  
**Description**: Quyền truy cập tĩnh — seed via Flyway, không có CRUD UI.

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `code` | VARCHAR(100) | PK | Mã quyền, e.g. `VIEW_DASHBOARD` |
| `display_name` | VARCHAR(255) | NOT NULL | Tên hiển thị trong cây phân quyền |
| `parent_code` | VARCHAR(100) | NULL, FK → permission(code) | NULL = root node |
| `type` | VARCHAR(20) | NOT NULL CHECK IN ('SCREEN','ACTION') | |
| `sort_order` | INTEGER | NOT NULL DEFAULT 0 | Thứ tự hiển thị trong cây |

**Note**: PK là `code` (not UUID) — Permission là lookup data, code phải ổn định và readable trong JWT claims.

**Full seed data** (see [research.md R-010](research.md#r-010-permission-tree-minimum-set)):

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

---

## Entity: `role_permission`

**Table**: `role_permission`  
**Description**: Bảng trung gian M-N giữa Role và Permission.

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `role_id` | UUID | NOT NULL, FK → role(id) | |
| `permission_code` | VARCHAR(100) | NOT NULL, FK → permission(code) | |

**Primary key**: `(role_id, permission_code)`

**Business rules**:
- Cập nhật permissions của một Role là **bulk replace**: xóa tất cả row cũ theo `role_id`, insert danh sách mới — trong một transaction.
- Permission codes được đưa vào JWT claims khi user đăng nhập → không cần DB lookup trên mỗi request.

---

## Entity update: `project`

**Change type**: Thêm FK constraint trên cột `represent_id` đã tồn tại.

**Migration SQL** (V6):
```sql
-- Bước 1: Set null cho các giá trị orphan (UUID không trỏ đến app_user nào)
UPDATE project
SET represent_id = NULL
WHERE represent_id IS NOT NULL
  AND represent_id NOT IN (SELECT id FROM app_user);

-- Bước 2: Thêm FK constraint
ALTER TABLE project
  ADD CONSTRAINT fk_project_represent_user
  FOREIGN KEY (represent_id)
  REFERENCES app_user(id)
  ON DELETE SET NULL;
```

**Entity change** (`Project.java`): thêm `@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "represent_id") private AppUser representUser;`

---

## Flyway Migration Plan

| Version | File | Description |
|---|---|---|
| V6 | `V6__user_role_permission_schema.sql` | Tạo bảng `role`, `app_user`, `permission`, `role_permission`; thêm FK trên `project.represent_id` |
| V7 | `V7__seed_permissions_and_roles.sql` | Seed 15 permissions + 1 role `SYSTEM_ADMIN` (tất cả permissions); admin user được seed bởi `AdminUserSeeder` |

---

## Validation Rules

| Field | Rule |
|---|---|
| `app_user.username` | 3–50 ký tự, chỉ `[a-zA-Z0-9_.-]`, unique |
| `app_user.email` | Valid email format, unique |
| `app_user.password` (request) | Tối thiểu 8 ký tự — validate ở BE trước khi hash |
| `app_user.display_name` | 1–255 ký tự, not blank |
| `app_user.role_id` | Not null; role phải active và không deleted |
| `role.role_name` | 1–100 ký tự, not blank, unique (case-insensitive) |
| `role.description` | Tùy chọn, max 1000 ký tự |

---

## State Transitions

### AppUser.active
```
TRUE (default) ──[admin deactivate]──► FALSE
FALSE ──[admin reactivate]──► TRUE
```
- `active = FALSE`: login → 403 với message "Tài khoản bị vô hiệu hoá"

### Role.active
```
TRUE (default) ──[admin soft-delete / deactivate]──► FALSE
```
- `active = FALSE` → tất cả user thuộc role không thể đăng nhập
- Không có reactivate trong v1 (admin phải gán role khác cho user)

### JWT Token
```
[Login success] ──► Token issued (8h TTL)
[Token expired] ──► 401 Unauthorized
[Logout] ──► Client xóa token (server không blacklist)
```
