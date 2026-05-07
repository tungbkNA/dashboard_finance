# API Contract: PUT /api/binance/project-monthly-records/{id}

**Feature**: 005-cross-month-recalculation
**Version**: v2 (extended from Feature 003)
**Method**: PUT
**Path**: `/api/binance/project-monthly-records/{id}`

---

## Changes from v1 (Feature 003)

- **Response** `data` object: thêm trường `affectedMonths: int` (số tháng bị ảnh hưởng bởi propagation, không gồm tháng gốc).
- **Behavior**: Sau khi save tháng gốc, nếu G6 thay đổi → tự động propagate chain sang T+1, T+2, ... theo điều kiện dừng.
- **Backward compatible**: `affectedMonths = 0` là giá trị mặc định khi không có propagation.

---

## Request

### Path Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `id` | `UUID` | Yes | ID của `ProjectMonthRecord` cần cập nhật |

### Request Body

Không thay đổi so với v1. Chỉ chứa các trường **nhập tay** (manual fields). Các trường công thức (formula fields) không được gửi lên.

```json
{
  "g1RaTon": 100.0000,
  "g1SlsxTonTuSxHd": 50.0000,
  "g1SlsxTonTuSxHtHd": 20.0000,
  "g1SlsxTonTuSxDdHd": 30.0000,
  "g1SlsxOsTon": 10.0000,
  "g1SlsxOsTonHt": 5.0000,
  "g2Headcount": 10.0000,
  "g2Ra": 200.0000,
  "..."  "..."
}
```

### Headers

| Header | Value | Required |
|--------|-------|----------|
| `Content-Type` | `application/json` | Yes |

---

## Response

### 200 OK — Thành công

```json
{
  "code": "SUCCESS",
  "message": "Cập nhật thành công",
  "data": {
    "id": "uuid",
    "projectId": "uuid",
    "projectCode": "PRJ-001",
    "projectName": "Dự án ABC",
    "monthKey": "2026-01",
    "active": true,
    "isFirstMonth": true,
    "price": 1500000.0000,
    "affectedMonths": 2,
    "createdAt": "2026-05-07T10:00:00+07:00",
    "updatedAt": "2026-05-07T14:00:00+07:00",
    "g1RaTon": 100.0000,
    "g1SlsxTonTuSxHd": 50.0000,
    "...": "... (all other fields)"
  }
}
```

#### Field `affectedMonths`

| Value | Meaning |
|-------|---------|
| `0` | Không có tháng nào được propagate (G6 không thay đổi, hoặc không có tháng kế tiếp) |
| `N > 0` | N tháng kế tiếp đã được cập nhật theo chuỗi propagation |

---

### 400 Bad Request — Bản ghi không active

```json
{
  "code": "MONTHLY_RECORD_INACTIVE",
  "message": "Bản ghi tháng này không còn hoạt động (inactive)",
  "data": null
}
```

### 404 Not Found — Không tìm thấy

```json
{
  "code": "MONTHLY_RECORD_NOT_FOUND",
  "message": "Bản ghi tháng không tồn tại",
  "data": null
}
```

### 422 Unprocessable Entity — Validation error

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Dữ liệu không hợp lệ",
  "data": null
}
```

### 500 Internal Server Error — Propagation failed (rolled back)

```json
{
  "code": "INTERNAL_ERROR",
  "message": "Đã xảy ra lỗi khi cập nhật. Dữ liệu không thay đổi.",
  "data": null
}
```

---

## Propagation Behavior (server-side, transparent to caller)

```
PUT /{id} với manual fields
  │
  ├─ 1. Load bản ghi (throw 404 nếu không tìm thấy)
  ├─ 2. Kiểm tra active (throw 400 nếu inactive)
  ├─ 3. Snapshot G6 values (before)
  ├─ 4. applyRequest() → update manual fields
  ├─ 5. calculateAndFill() → tính lại G6 + tất cả formula fields
  ├─ 6. Compare G6 after vs G6 before
  ├─ 7. Save origin record
  ├─ 8. If G6 changed:
  │       loop: T+1, T+2, ...
  │         ├─ Tìm bản ghi tháng kế tiếp
  │         ├─ Dừng nếu: không tồn tại / inactive / locked
  │         ├─ Cập nhật G1 từ G6 tháng trước
  │         ├─ calculateAndFill() tháng này
  │         ├─ Compare G6 mới vs cũ → Dừng nếu không thay đổi
  │         ├─ Save bản ghi tháng này
  │         └─ Ghi audit log
  └─ 9. Return response với affectedMonths count
```

Toàn bộ bước 1-9 nằm trong một `@Transactional`. Nếu bất kỳ bước nào lỗi, tất cả rollback.

---

## FE Behavior

### Trước khi gọi API (ConfirmDialog)

Nếu **bất kỳ trường nhập tay nào** trong group đang save có giá trị **non-null** trong `detail` đã tải về:
1. Hiển thị `<ConfirmDialog>` (PrimeVue) với message:
   > "Thay đổi này có thể sẽ làm thay đổi các nhóm giá trị trong các tháng khác"
2. User chọn **Xác nhận** → gọi `PUT /{id}`.
3. User chọn **Hủy** / đóng dialog → không gọi API, form không thay đổi.

Nếu tất cả trường nhập tay trong group là `null` → gọi API trực tiếp không cần confirm.

### Sau khi API trả về

- **Thành công, `affectedMonths > 0`**: Toast `"Lưu thành công. Đã cập nhật thêm {affectedMonths} tháng liên quan."`
- **Thành công, `affectedMonths === 0`**: Toast `"Lưu thành công."`
- **Lỗi**: Toast lỗi với message từ BE. **Không tự cập nhật local data**.

---

## Idempotency

Mỗi lần gọi `PUT /{id}`:
- BE tạo một `eventId` (UUID mới).
- Nếu G6 không thay đổi → không có audit log được ghi → chạy lại không tạo thêm record.
- Nếu G6 thay đổi → audit log ghi với `eventId`. Nếu event bị replay (same eventId), check `existsByEventId()` → bỏ qua.
