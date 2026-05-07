# API Contract: Project Types

**Feature**: 002-project-settings
**Base URL**: `/api/binance/project-types`

---

## GET /api/binance/project-types

Returns all non-deleted project types.

**Response 200**:
```json
{
  "code": "SUCCESS",
  "message": "OK",
  "data": [
    { "id": "uuid", "key": "DEVELOPMENT", "value": "Phát triển phần mềm" }
  ]
}
```

---

## POST /api/binance/project-types

**Request Body**:
```json
{ "key": "DEVELOPMENT", "value": "Phát triển phần mềm" }
```

**Validation** (400): `key` required, `^[A-Za-z0-9_-]{1,50}$`; `value` required, max 255.

**Response 201**: created `ProjectTypeResponse` in `data`

**Response 422**:
```json
{ "code": "PROJECT_TYPE_KEY_DUPLICATE", "message": "Key loại dự án đã tồn tại", "data": null }
```

---

## PUT /api/binance/project-types/{id}

Same body/validation as POST.

**Response 200**: updated `ProjectTypeResponse` in `data`

**Response 404**: `{ "code": "PROJECT_TYPE_NOT_FOUND", ... }`

**Response 422**: `PROJECT_TYPE_KEY_DUPLICATE`

---

## DELETE /api/binance/project-types/{id}

Soft-delete a project type.

**Response 200**:
```json
{
  "code": "SUCCESS",
  "message": "Xóa loại dự án thành công",
  "data": { "inUse": false }
}
```

`inUse: true` when the project type was referenced by ≥ 1 project — FE uses this to show the warning dialog before calling delete. The deletion still occurs (soft delete) but the FE must confirm first.

> FE flow: (1) Call DELETE, (2) if `inUse: true` in response → show warning "Loại dự án đang được sử dụng. Xác nhận xóa?", (3) User confirms → call DELETE again with `?force=true` to actually soft-delete.

**Alternative flow** (simpler): FE calls `GET /api/binance/project-types/{id}/usage` first to check, then DELETE.

**Chosen approach**: Two-step — first call returns `inUse` flag without deleting; second call with `?confirmed=true` performs soft delete.

**Response 200 (first call, inUse=true)**:
```json
{ "code": "IN_USE_WARNING", "message": "Loại dự án đang được 3 dự án sử dụng", "data": { "inUse": true, "usageCount": 3 } }
```

**Response 200 (confirmed delete)**:
```json
{ "code": "SUCCESS", "message": "Xóa loại dự án thành công", "data": null }
```

**Response 404**: `PROJECT_TYPE_NOT_FOUND`
