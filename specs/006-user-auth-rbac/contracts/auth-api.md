# API Contract: Authentication

**Base path**: `/api/auth`  
**Auth required**: ❌ (public endpoints)  
**Source**: [spec.md FR-001 to FR-006](../spec.md), [research.md R-002](../research.md)

---

## POST /api/auth/login

Đăng nhập bằng username và password. Trả về JWT token và thông tin user.

**Request**:
```json
{
  "username": "admin",
  "password": "secret123"
}
```

| Field | Type | Constraints |
|---|---|---|
| `username` | string | required, 3–50 chars |
| `password` | string | required |

**Response 200 — Success**:
```json
{
  "code": "SUCCESS",
  "message": "Đăng nhập thành công",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "expiresAt": "2026-05-07T16:00:00Z",
    "user": {
      "id": "uuid",
      "username": "admin",
      "displayName": "Quản trị viên",
      "roleId": "uuid",
      "roleName": "SYSTEM_ADMIN",
      "permissions": ["VIEW_DASHBOARD", "MANAGE_USER", "MANAGE_ROLE", "MANAGE_PROJECT", "SYSTEM_SETTINGS"]
    }
  }
}
```

**Response 401 — Sai thông tin** (FR-002 — không tiết lộ tài khoản tồn tại hay không):
```json
{
  "code": "AUTH_INVALID_CREDENTIALS",
  "message": "Tên đăng nhập hoặc mật khẩu không đúng",
  "data": null
}
```

**Response 403 — Tài khoản bị khóa** (FR-003):
```json
{
  "code": "AUTH_ACCOUNT_INACTIVE",
  "message": "Tài khoản đã bị vô hiệu hoá. Vui lòng liên hệ quản trị viên.",
  "data": null
}
```

**Response 403 — Role bị inactive**:
```json
{
  "code": "AUTH_ROLE_INACTIVE",
  "message": "Quyền truy cập của tài khoản đã bị thu hồi. Vui lòng liên hệ quản trị viên.",
  "data": null
}
```

**Notes**:
- Không phân biệt sai username hay sai password trong response 401 (chống enumeration attack).
- Token TTL: 8 giờ (configurable via `app.jwt.expiration-hours`).
- Token payload claims: `sub` (username), `userId`, `roleId`, `permissions` (array of codes), `exp`, `iat`.

---

## POST /api/auth/logout

Đăng xuất — server-side là no-op (stateless JWT). Client phải xóa token.

**Auth required**: ✅ (Bearer token)

**Response 200**:
```json
{
  "code": "SUCCESS",
  "message": "Đăng xuất thành công",
  "data": null
}
```

---

## GET /api/auth/me

Lấy thông tin user hiện tại từ token.

**Auth required**: ✅

**Response 200**:
```json
{
  "code": "SUCCESS",
  "message": "OK",
  "data": {
    "id": "uuid",
    "username": "admin",
    "displayName": "Quản trị viên",
    "email": "admin@internal.com",
    "roleId": "uuid",
    "roleName": "SYSTEM_ADMIN",
    "permissions": ["VIEW_DASHBOARD", "MANAGE_USER", "MANAGE_ROLE", "MANAGE_PROJECT", "SYSTEM_SETTINGS"]
  }
}
```

**Response 401** — token không hợp lệ hoặc hết hạn:
```json
{
  "code": "AUTH_TOKEN_EXPIRED",
  "message": "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.",
  "data": null
}
```
