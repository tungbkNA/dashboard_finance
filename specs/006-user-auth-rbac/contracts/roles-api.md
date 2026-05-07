# API Contract: Role Management

**Base path**: `/api/roles`  
**Auth required**: ✅ (Bearer token)  
**Required permission**: `MANAGE_ROLE` (for write operations); `VIEW_DASHBOARD` (for read)  
**Source**: [spec.md FR-007 to FR-011](../spec.md), [data-model.md](../data-model.md)

---

## GET /api/roles

Lấy danh sách tất cả roles (kể cả inactive, không bao gồm deleted).

**Query params**: không

**Response 200**:
```json
{
  "code": "SUCCESS",
  "message": "OK",
  "data": [
    {
      "id": "uuid",
      "roleName": "SYSTEM_ADMIN",
      "description": "Quản trị viên hệ thống — toàn quyền",
      "active": true,
      "userCount": 1,
      "createdAt": "2026-05-07T08:00:00Z"
    }
  ]
}
```

---

## POST /api/roles

Tạo role mới.  
**Required permission**: `ROLE_CREATE`

**Request**:
```json
{
  "roleName": "Nhân viên",
  "description": "Quyền xem dashboard và dự án"
}
```

| Field | Type | Constraints |
|---|---|---|
| `roleName` | string | required, 1–100 chars, unique (case-insensitive) |
| `description` | string | optional, max 1000 chars |

**Response 201**:
```json
{
  "code": "SUCCESS",
  "message": "Tạo role thành công",
  "data": { "id": "uuid", "roleName": "Nhân viên", "description": "...", "active": true, "userCount": 0 }
}
```

**Response 409 — Tên đã tồn tại**:
```json
{
  "code": "ROLE_NAME_EXISTS",
  "message": "Tên role đã tồn tại",
  "data": null
}
```

---

## GET /api/roles/{id}

Lấy chi tiết một role bao gồm danh sách permission codes.

**Response 200**:
```json
{
  "code": "SUCCESS",
  "message": "OK",
  "data": {
    "id": "uuid",
    "roleName": "SYSTEM_ADMIN",
    "description": "...",
    "active": true,
    "userCount": 1,
    "permissions": ["VIEW_DASHBOARD", "MANAGE_USER", "MANAGE_ROLE", "MANAGE_PROJECT"]
  }
}
```

**Response 404**:
```json
{ "code": "ROLE_NOT_FOUND", "message": "Không tìm thấy role", "data": null }
```

---

## PUT /api/roles/{id}

Cập nhật tên và mô tả role.  
**Required permission**: `ROLE_EDIT`

**Request**:
```json
{
  "roleName": "Nhân viên cập nhật",
  "description": "Mô tả mới"
}
```

**Response 200**: giống GET /api/roles/{id} (không có `permissions`)

---

## DELETE /api/roles/{id}

Xóa mềm role (set `active = FALSE, deleted = TRUE`).  
**Required permission**: `ROLE_DEACTIVATE`

**Behavior** (FR-011):
- Nếu role **không có** user active → xóa ngay, response 200.
- Nếu role **có** user active → response 200 với `requiresConfirmation: true` + số lượng user bị ảnh hưởng. Client hiển thị cảnh báo và gọi lại với `?force=true`.

**Request** (lần 1 — check):
```
DELETE /api/roles/{id}
```

**Response 200 — Cần xác nhận** (có user bị ảnh hưởng):
```json
{
  "code": "ROLE_DELETE_REQUIRES_CONFIRMATION",
  "message": "Role này đang được sử dụng bởi 3 người dùng active. Tất cả họ sẽ không thể đăng nhập cho đến khi được gán role mới.",
  "data": { "affectedUserCount": 3, "requiresConfirmation": true }
}
```

**Request** (lần 2 — force confirm):
```
DELETE /api/roles/{id}?force=true
```

**Response 200 — Đã xóa**:
```json
{
  "code": "SUCCESS",
  "message": "Đã vô hiệu hoá role thành công",
  "data": null
}
```

---

## GET /api/roles/{id}/permissions

Lấy danh sách permission codes đang được gán cho role.

**Response 200**:
```json
{
  "code": "SUCCESS",
  "message": "OK",
  "data": ["VIEW_DASHBOARD", "MANAGE_PROJECT", "PROJECT_CREATE", "PROJECT_EDIT"]
}
```

---

## PUT /api/roles/{id}/permissions

Cập nhật toàn bộ danh sách permission của role (bulk replace).  
**Required permission**: `ROLE_ASSIGN_PERMISSIONS`

**Request**:
```json
{
  "permissions": ["VIEW_DASHBOARD", "MANAGE_PROJECT", "PROJECT_CREATE", "PROJECT_EDIT"]
}
```

**Response 200**:
```json
{
  "code": "SUCCESS",
  "message": "Đã cập nhật phân quyền thành công",
  "data": {
    "roleId": "uuid",
    "permissions": ["VIEW_DASHBOARD", "MANAGE_PROJECT", "PROJECT_CREATE", "PROJECT_EDIT"]
  }
}
```

**Response 400 — Permission code không hợp lệ**:
```json
{
  "code": "PERMISSION_INVALID",
  "message": "Permission code không tồn tại: INVALID_CODE",
  "data": null
}
```
