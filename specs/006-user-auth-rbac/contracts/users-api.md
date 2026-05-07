# API Contract: User Management

**Base path**: `/api/users`  
**Auth required**: ✅ (Bearer token)  
**Source**: [spec.md FR-012 to FR-017](../spec.md), [data-model.md](../data-model.md)

---

## GET /api/users

Lấy danh sách tất cả users (kể cả inactive, không bao gồm deleted).  
**Required permission**: `MANAGE_USER`

**Query params**:
- `active` (optional, boolean): lọc theo trạng thái — dùng cho dropdown "Người đại diện"

**Response 200**:
```json
{
  "code": "SUCCESS",
  "message": "OK",
  "data": [
    {
      "id": "uuid",
      "username": "admin",
      "email": "admin@internal.com",
      "displayName": "Quản trị viên",
      "roleId": "uuid",
      "roleName": "SYSTEM_ADMIN",
      "active": true,
      "createdAt": "2026-05-07T08:00:00Z"
    }
  ]
}
```

**Note**: `passwordHash` **không bao giờ** có trong response.

---

## POST /api/users

Tạo user mới.  
**Required permission**: `USER_CREATE`

**Request**:
```json
{
  "username": "nguyen.van.a",
  "email": "nguyen.van.a@company.com",
  "displayName": "Nguyễn Văn A",
  "password": "Password123!",
  "roleId": "uuid"
}
```

| Field | Type | Constraints |
|---|---|---|
| `username` | string | required, 3–50 chars, `[a-zA-Z0-9_.-]` only |
| `email` | string | required, valid email format |
| `displayName` | string | required, 1–255 chars |
| `password` | string | required, min 8 chars |
| `roleId` | UUID | required; role phải active và không deleted |

**Response 201**:
```json
{
  "code": "SUCCESS",
  "message": "Tạo người dùng thành công",
  "data": {
    "id": "uuid",
    "username": "nguyen.van.a",
    "email": "nguyen.van.a@company.com",
    "displayName": "Nguyễn Văn A",
    "roleId": "uuid",
    "roleName": "Nhân viên",
    "active": true
  }
}
```

**Response 409 — Trùng username hoặc email**:
```json
{
  "code": "USER_USERNAME_EXISTS",
  "message": "Tên đăng nhập đã tồn tại",
  "data": null
}
```
```json
{
  "code": "USER_EMAIL_EXISTS",
  "message": "Email đã tồn tại",
  "data": null
}
```

**Response 400 — Role không hợp lệ**:
```json
{
  "code": "ROLE_NOT_FOUND_OR_INACTIVE",
  "message": "Role không tồn tại hoặc đã bị vô hiệu hoá",
  "data": null
}
```

---

## GET /api/users/{id}

Lấy chi tiết một user.  
**Required permission**: `MANAGE_USER`

**Response 200**: giống mỗi item trong GET /api/users, không có `passwordHash`.

**Response 404**:
```json
{ "code": "USER_NOT_FOUND", "message": "Không tìm thấy người dùng", "data": null }
```

---

## PUT /api/users/{id}

Cập nhật thông tin user (không bao gồm password).  
**Required permission**: `USER_EDIT`

**Request**:
```json
{
  "email": "new.email@company.com",
  "displayName": "Nguyễn Văn B",
  "roleId": "uuid",
  "active": true
}
```

| Field | Type | Constraints |
|---|---|---|
| `email` | string | optional; valid email; unique |
| `displayName` | string | optional; 1–255 chars |
| `roleId` | UUID | optional; role phải active và không deleted |
| `active` | boolean | optional |

**Response 200**: thông tin user sau khi cập nhật.

**Note**: Thay đổi `roleId` có hiệu lực từ lần đăng nhập tiếp theo (token hiện tại không bị invalidate).

---

## PUT /api/users/{id}/password

Đặt lại mật khẩu của user.  
**Required permission**: `USER_EDIT`

**Request**:
```json
{
  "newPassword": "NewPassword456!"
}
```

| Field | Type | Constraints |
|---|---|---|
| `newPassword` | string | required, min 8 chars |

**Response 200**:
```json
{
  "code": "SUCCESS",
  "message": "Đã đặt lại mật khẩu thành công",
  "data": null
}
```

---

## DELETE /api/users/{id}

Vô hiệu hoá user (set `active = FALSE, deleted = TRUE`).  
**Required permission**: `USER_DEACTIVATE`

**Response 200**:
```json
{
  "code": "SUCCESS",
  "message": "Đã vô hiệu hoá người dùng thành công",
  "data": null
}
```

**Response 400 — Không thể xóa chính mình**:
```json
{
  "code": "USER_CANNOT_DELETE_SELF",
  "message": "Không thể vô hiệu hoá tài khoản của chính mình",
  "data": null
}
```
