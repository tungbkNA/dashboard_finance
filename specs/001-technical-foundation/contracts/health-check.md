# API Contract: Health Check

**Spec Reference**: specs/001-technical-foundation/spec.md — FR-BE-002
**Feature**: 001-technical-foundation
**Date**: 2026-05-06

---

## Endpoint

```
GET /api/binance/health
```

## Authentication

Không yêu cầu. Endpoint công khai.

## Request

- **Body**: Không có
- **Query parameters**: Không có
- **Headers**: Không bắt buộc

## Response

### Success — HTTP 200

```json
{
  "code": "SUCCESS",
  "message": "OK",
  "data": {
    "status": "UP",
    "service": "dashboard-finance",
    "version": "1.0.0"
  }
}
```

| Field | Type | Description |
|---|---|---|
| `code` | string | Luôn là `"SUCCESS"` khi endpoint phản hồi được |
| `message` | string | Luôn là `"OK"` khi thành công |
| `data.status` | string | `"UP"` — backend đang hoạt động |
| `data.service` | string | `"dashboard-finance"` — giá trị cố định |
| `data.version` | string | Phiên bản từ `application.properties` (`app.version`) |

### Error — HTTP 500

Xảy ra khi có lỗi không mong đợi trong health controller.

```json
{
  "code": "INTERNAL_ERROR",
  "message": "Lỗi hệ thống nội bộ",
  "data": null
}
```

## Frontend Behavior

| Kết quả gọi API | Hành động UI |
|---|---|
| HTTP 200 (SUCCESS) | **Im lặng** — không hiển thị gì (silent success) |
| Network error / timeout | Hiển thị **toast notification** lỗi: `"Không thể kết nối đến máy chủ"` |
| HTTP 4xx / 5xx | Hiển thị **toast notification** lỗi với `message` từ response |

## Performance

- Response time: < 500ms trong điều kiện bình thường (SC-003)
- Không kiểm tra database connectivity trong iteration này

## Notes

- Endpoint này không có business logic
- Gọi khi ứng dụng frontend tải lần đầu (trong `App.vue` hoặc router `beforeEach`)
- Có thể mở rộng trong tương lai để kiểm tra DB connectivity, disk space, v.v. — cần spec riêng
