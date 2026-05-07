# API Contract: FileRecord

**Module**: Sổ tay trung tâm  
**Base Path**: `/api/handbook/file-records`  
**Auth**: Bearer JWT — requires `MANAGE_HANDBOOK` permission

---

## GET /api/handbook/file-records

**Description**: Lấy danh sách file với tìm kiếm và lọc.

**Query Parameters**:

| Param | Type | Required | Description |
|-------|------|----------|-------------|
| keyword | string | no | Tìm kiếm theo tên file (case-insensitive, partial match) |
| groupId | UUID | no | Lọc theo nhóm file |
| includeInactive | boolean | no (default: false) | Bao gồm file thuộc nhóm Inactive |
| page | int | no (default: 0) | Số trang (0-based) |
| size | int | no (default: 20) | Số bản ghi mỗi trang |

**Response** `200 OK`:
```json
{
  "code": "SUCCESS",
  "message": "OK",
  "data": {
    "content": [
      {
        "id": "uuid",
        "fileName": "Báo cáo Q1 2026",
        "fileUrl": "https://drive.google.com/file/abc123",
        "groupId": "uuid",
        "groupName": "Báo cáo",
        "createdBy": "admin",
        "createdAt": "2026-05-07T10:00:00+07:00",
        "updatedAt": "2026-05-07T10:00:00+07:00"
      }
    ],
    "totalElements": 42,
    "totalPages": 3,
    "number": 0,
    "size": 20
  }
}
```

**Notes**: Uses Spring Data `Page` wrapper. Default sort: `createdAt DESC`.

---

## POST /api/handbook/file-records

**Description**: Tạo bản ghi file mới.

**Request Body**:
```json
{
  "fileName": "Báo cáo Q1 2026",
  "fileUrl": "https://drive.google.com/file/abc123",
  "groupId": "uuid-of-active-group"
}
```

**Validation**:
- `fileName`: required, max 200 chars
- `fileUrl`: required, must start with `http://` or `https://`, max 2048 chars
- `groupId`: required, must reference an active FileGroup

**Response** `201 Created`:
```json
{
  "code": "SUCCESS",
  "message": "OK",
  "data": {
    "id": "uuid",
    "fileName": "Báo cáo Q1 2026",
    "fileUrl": "https://drive.google.com/file/abc123",
    "groupId": "uuid",
    "groupName": "Báo cáo",
    "createdBy": "admin",
    "createdAt": "...",
    "updatedAt": "..."
  }
}
```

**Error** `400`:
```json
{ "code": "FILE_GROUP_NOT_FOUND", "message": "Nhóm file không tồn tại" }
```
```json
{ "code": "FILE_GROUP_INACTIVE", "message": "Nhóm file đang ngưng hoạt động" }
```

**Notes**: `createdBy` is automatically set from JWT `Authentication.getName()`.

---

## PUT /api/handbook/file-records/{id}

**Description**: Cập nhật bản ghi file.

**Request Body**:
```json
{
  "fileName": "Báo cáo Q1 2026 (revised)",
  "fileUrl": "https://drive.google.com/file/xyz789",
  "groupId": "uuid-of-active-group"
}
```

**Validation**: Same as POST.

**Response** `200 OK`: Same shape as POST response.

**Error** `404`:
```json
{ "code": "FILE_RECORD_NOT_FOUND", "message": "Bản ghi file không tồn tại" }
```

**Notes**: `createdBy` and `createdAt` are NOT updated on edit.

---

## DELETE /api/handbook/file-records/{id}

**Description**: Xóa bản ghi file (hard delete).

**Response** `200 OK`:
```json
{
  "code": "SUCCESS",
  "message": "Xóa bản ghi file thành công",
  "data": null
}
```

**Error** `404`:
```json
{ "code": "FILE_RECORD_NOT_FOUND", "message": "Bản ghi file không tồn tại" }
```
