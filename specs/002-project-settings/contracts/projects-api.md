# API Contract: Projects

**Feature**: 002-project-settings
**Base URL**: `/api/binance/projects`
**Response Wrapper**: `{ "code": "...", "message": "...", "data": {} }`

---

## GET /api/binance/projects

Returns all non-deleted projects.

**Response 200**:
```json
{
  "code": "SUCCESS",
  "message": "OK",
  "data": [
    {
      "id": "uuid",
      "projectCode": "PRJ-001",
      "projectName": "Tên dự án",
      "customerId": "uuid",
      "customerName": "Tên khách hàng",
      "projectTypeId": "uuid",
      "projectTypeName": "Tên loại dự án",
      "price": 1000000.0000,
      "statusContract": "HAS_CONTRACT",
      "statusProject": "OPEN",
      "monthStart": "01/2026",
      "monthEnd": "12/2026",
      "createdAt": "2026-05-07T10:00:00Z",
      "updatedAt": "2026-05-07T10:00:00Z"
    }
  ]
}
```

---

## GET /api/binance/projects/{id}

Returns a single project by id.

**Path param**: `id` (UUID)

**Response 200**: single `ProjectResponse` object in `data`

**Response 404**:
```json
{ "code": "PROJECT_NOT_FOUND", "message": "Dự án không tồn tại", "data": null }
```

---

## POST /api/binance/projects

Create a new project.

**Request Body**:
```json
{
  "projectCode": "PRJ-001",
  "projectName": "Tên dự án",
  "customerId": "uuid",
  "projectTypeId": "uuid",
  "price": 1000000,
  "statusContract": "HAS_CONTRACT",
  "statusProject": "OPEN",
  "monthStart": "01/2026",
  "monthEnd": "12/2026"
}
```

**Validation Rules** (400 on violation):
- `projectCode`: required, `^[A-Za-z0-9_-]{1,50}$`
- `projectName`: required, max 255
- `customerId`: required, must reference non-deleted customer
- `projectTypeId`: required, must reference non-deleted project_type
- `price`: required, ≥ 0, default 0
- `statusContract`: required, one of `NO_CONTRACT | HAS_CONTRACT`
- `statusProject`: required, one of `OPEN | INPROGRESS | PENDING | DONE | CLOSE`
- `monthStart`: required, `^(0[1-9]|1[0-2])/[2-9][0-9]{3}$`
- `monthEnd`: required, `^(0[1-9]|1[0-2])/[2-9][0-9]{3}$`, ≥ monthStart

**Response 201**: created `ProjectResponse` in `data`

**Response 400**:
```json
{ "code": "VALIDATION_ERROR", "message": "Mã dự án không được để trống", "data": null }
```

**Response 422**:
```json
{ "code": "PROJECT_CODE_DUPLICATE", "message": "Mã dự án đã tồn tại", "data": null }
```

---

## PUT /api/binance/projects/{id}

Update an existing project. Same request body and validation as POST.

**Response 200**: updated `ProjectResponse` in `data`

**Response 404**: `PROJECT_NOT_FOUND`

**Response 422**: `PROJECT_CODE_DUPLICATE` (if code changed to an existing one)

---

## DELETE /api/binance/projects/{id}

Soft-delete a project (`deleted = true`).

**Response 200**:
```json
{ "code": "SUCCESS", "message": "Xóa dự án thành công", "data": null }
```

**Response 404**: `PROJECT_NOT_FOUND`

> Note: Always soft deletes. If project has monthly data (future feature), a `hasMonthlyData: true` field will be included in the response to prompt FE to show extra warning. Currently always `false`.
