# API Contract: FileGroup

**Module**: Sổ tay trung tâm  
**Base Path**: `/api/handbook/file-groups`  
**Auth**: Bearer JWT — requires `MANAGE_HANDBOOK` permission

---

## GET /api/handbook/file-groups

**Description**: Lấy danh sách tất cả nhóm file.

**Query Parameters**: none

**Response** `200 OK`:
```json
{
  "code": "SUCCESS",
  "message": "OK",
  "data": [
    {
      "id": "uuid",
      "name": "Kế hoạch tháng",
      "description": "Tài liệu kế hoạch hàng tháng",
      "active": true,
      "fileCount": 5,
      "createdAt": "2026-05-07T10:00:00+07:00",
      "updatedAt": "2026-05-07T10:00:00+07:00"
    }
  ]
}
```

**Notes**: Returns ALL groups (both active and inactive). `fileCount` is the number of FileRecords in the group.

---

## GET /api/handbook/file-groups/active

**Description**: Lấy danh sách nhóm file active (cho dropdown).

**Response** `200 OK`:
```json
{
  "code": "SUCCESS",
  "message": "OK",
  "data": [
    {
      "id": "uuid",
      "name": "Kế hoạch tháng"
    }
  ]
}
```

**Notes**: Lightweight response for populating dropdowns. Only returns `id` and `name`.

---

## POST /api/handbook/file-groups

**Description**: Tạo nhóm file mới.

**Request Body**:
```json
{
  "name": "Kế hoạch tháng",
  "description": "Tài liệu kế hoạch hàng tháng"
}
```

**Validation**:
- `name`: required, max 100 chars, unique (case-insensitive)
- `description`: optional, max 255 chars

**Response** `201 Created`:
```json
{
  "code": "SUCCESS",
  "message": "OK",
  "data": {
    "id": "uuid",
    "name": "Kế hoạch tháng",
    "description": "Tài liệu kế hoạch hàng tháng",
    "active": true,
    "fileCount": 0,
    "createdAt": "...",
    "updatedAt": "..."
  }
}
```

**Error** `400`:
```json
{ "code": "FILE_GROUP_NAME_DUPLICATE", "message": "Tên nhóm file đã tồn tại" }
```

---

## PUT /api/handbook/file-groups/{id}

**Description**: Cập nhật nhóm file.

**Request Body**:
```json
{
  "name": "Kế hoạch tháng (updated)",
  "description": "Mô tả mới",
  "active": false
}
```

**Validation**: Same as POST + `active` (boolean, required)

**Response** `200 OK`: Same shape as POST response.

**Error** `404`:
```json
{ "code": "FILE_GROUP_NOT_FOUND", "message": "Nhóm file không tồn tại" }
```

---

## DELETE /api/handbook/file-groups/{id}

**Description**: Xóa nhóm file (hard delete, chỉ khi không có file tham chiếu).

**Response** `200 OK`:
```json
{
  "code": "SUCCESS",
  "message": "Xóa nhóm file thành công",
  "data": null
}
```

**Error** `400`:
```json
{
  "code": "FILE_GROUP_IN_USE",
  "message": "Nhóm file đang được sử dụng bởi 5 bản ghi file",
  "data": { "fileCount": 5 }
}
```

**Error** `404`:
```json
{ "code": "FILE_GROUP_NOT_FOUND", "message": "Nhóm file không tồn tại" }
```
