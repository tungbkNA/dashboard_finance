# API Contract: Customers

**Feature**: 002-project-settings
**Base URL**: `/api/binance/customers`

---

## GET /api/binance/customers

Returns all non-deleted customers.

**Response 200**:
```json
{
  "code": "SUCCESS",
  "message": "OK",
  "data": [
    { "id": "uuid", "customerCode": "CUST-001", "customerName": "Công ty ABC" }
  ]
}
```

---

## POST /api/binance/customers

**Request Body**:
```json
{ "customerCode": "CUST-001", "customerName": "Công ty ABC" }
```

**Validation** (400): `customerCode` required, `^[A-Za-z0-9_-]{1,50}$`; `customerName` required, max 255.

**Response 201**: created `CustomerResponse` in `data`

**Response 422**:
```json
{ "code": "CUSTOMER_CODE_DUPLICATE", "message": "Mã khách hàng đã tồn tại", "data": null }
```

---

## PUT /api/binance/customers/{id}

Same body/validation as POST.

**Response 200**: updated `CustomerResponse` in `data`

**Response 404**: `{ "code": "CUSTOMER_NOT_FOUND", ... }`

**Response 422**: `CUSTOMER_CODE_DUPLICATE`

---

## DELETE /api/binance/customers/{id}

Soft-delete a customer. Two-step pattern (same as project-types):

**First call** (no params): checks usage, soft-deletes if not in use; returns `IN_USE_WARNING` if in use.

**Response 200 (not in use)**:
```json
{ "code": "SUCCESS", "message": "Xóa khách hàng thành công", "data": null }
```

**Response 200 (in use)**:
```json
{ "code": "IN_USE_WARNING", "message": "Khách hàng đang được 2 dự án sử dụng", "data": { "inUse": true, "usageCount": 2 } }
```

**Second call** with `?confirmed=true`: performs soft delete regardless.

**Response 200 (confirmed)**:
```json
{ "code": "SUCCESS", "message": "Xóa khách hàng thành công", "data": null }
```

**Response 404**: `CUSTOMER_NOT_FOUND`
