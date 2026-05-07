# API Contract: Project Monthly Records

**Feature**: 003-project-monthly-records  
**Base path**: `/api/binance/project-monthly-records`  
**Response wrapper**: `{ "code": "SUCCESS|ERROR_CODE", "message": "...", "data": {} }`

---

## GET /api/binance/project-monthly-records

Lấy danh sách bản ghi tháng, filter theo tháng hoặc dự án. Chỉ trả bản ghi `active = TRUE`.

### Query Parameters

| Param | Type | Required | Description |
|-------|------|----------|-------------|
| `monthKey` | String `YYYY-MM` | NO | Filter theo tháng. Mặc định tháng hiện tại khi FE không truyền |
| `projectId` | UUID | NO | Filter theo dự án cụ thể |

### Response 200

```json
{
  "code": "SUCCESS",
  "message": "Tải danh sách bản ghi thành công",
  "data": [
    {
      "id": "uuid",
      "projectId": "uuid",
      "projectCode": "PRJ-001",
      "projectName": "Tên dự án",
      "monthKey": "2026-01",
      "active": true,
      "g4DoanhThu": 1500000000,
      "g5DoanhThu": 1200000000,
      "g5TongSlnt": 120,
      "updatedAt": "2026-01-15T10:00:00+07:00"
    }
  ]
}
```

*Summary response — chỉ trả các trường dùng cho bảng tóm tắt.*

---

## GET /api/binance/project-monthly-records/{id}

Lấy chi tiết đầy đủ 6 nhóm thuộc tính của một bản ghi tháng.

### Path Parameters

| Param | Type | Description |
|-------|------|-------------|
| `id` | UUID | ID của bản ghi |

### Response 200

```json
{
  "code": "SUCCESS",
  "message": "Tải bản ghi tháng thành công",
  "data": {
    "id": "uuid",
    "projectId": "uuid",
    "projectCode": "PRJ-001",
    "projectName": "Tên dự án",
    "monthKey": "2026-01",
    "active": true,
    "isFirstMonth": false,

    "g1RaTon": null,
    "g1SlsxTonTuSxHd": null,
    "g1SlsxTonTuSxHtHd": null,
    "g1SlsxTonTuSxDdHd": null,
    "g1SlsxOsTon": null,
    "g1SlsxOsTonHt": null,

    "g2Headcount": null,
    "g2Ra": null,
    "g2SlsxTuSx": null,
    "g2SlsxOs": null,
    "g2LienKet": null,
    "g2TongSlsxDuKien": null,
    "g2SlsxTuSxHtTrongThang": null,
    "g2SlsxTuSxDd": null,
    "g2SlsxOsHt": null,
    "g2SlsxOsDd": null,
    "g2Cpbqtb": null,
    "g2TySuatLng": null,

    "g3Ra": null,
    "g3TongSlsxHd": null,
    "g3Ee": null,
    "g3SlsxTuSxHt": null,
    "g3SlsxTuSxDd": null,
    "g3SlsxOsDd": null,
    "g3SlsxOsTonHt": null,

    "g4TuSlsxTonHt": null,
    "g4TuSlsxTrongThang": null,
    "g4SlsxOsTon": null,
    "g4SlsxOsTrongThang": null,
    "g4Lk": null,
    "g4Tong": null,
    "g4DoanhThu": null,
    "g4TiSuatLngDuKien": null,
    "g4LngDuKien": null,

    "g5RaTuongUngSlnt": null,
    "g5NtSlsxTonHt": null,
    "g5NtSlsxTrongThang": null,
    "g5NtSlsxOsTon": null,
    "g5NtSlsxOsTrongThang": null,
    "g5TongSlnt": null,
    "g5DoanhThu": null,
    "g5TiSuatLng": null,
    "g5LngVnd": null,

    "g6RaTon": null,
    "g6SlsxTonHt": null,
    "g6SlsxTonDd": null,
    "g6SlsxOsTon": null,
    "g6SlsxOsTonHt": null,

    "createdAt": "2026-01-01T00:00:00+07:00",
    "updatedAt": "2026-01-15T10:00:00+07:00"
  }
}
```

> **Note**: Các trường công thức (g2TongSlsxDuKien, g3Ee, g4Tong, g4DoanhThu, g5TongSlnt, g5DoanhThu, g6_*) được backend tính nếu DB chưa có snapshot.  
> **Note**: `isFirstMonth = true` khi đây là bản ghi tháng đầu tiên của dự án — FE dùng để quyết định có cho nhập g1 fields hay không.

### Response 404

```json
{
  "code": "MONTHLY_RECORD_NOT_FOUND",
  "message": "Bản ghi tháng không tồn tại",
  "data": null
}
```

---

## PUT /api/binance/project-monthly-records/{id}

Cập nhật các trường nhập tay của bản ghi tháng. Backend tính lại công thức, lưu snapshot, và cascade Tồn cuối kỳ → Tồn đầu kỳ của tháng N+1 nếu tồn tại.

### Request Body

```json
{
  "g1RaTon": 100,
  "g1SlsxTonTuSxHd": 50,
  "g1SlsxTonTuSxHtHd": 30,
  "g1SlsxTonTuSxDdHd": 20,
  "g1SlsxOsTon": 10,
  "g1SlsxOsTonHt": 5,

  "g2Headcount": 10,
  "g2Ra": 200,
  "g2SlsxTuSx": 150,
  "g2SlsxOs": 30,
  "g2LienKet": 20,
  "g2SlsxTuSxHtTrongThang": 140,
  "g2SlsxTuSxDd": 10,
  "g2SlsxOsHt": 28,
  "g2SlsxOsDd": 2,
  "g2Cpbqtb": 5000000,
  "g2TySuatLng": 15.5,

  "g3Ra": 180,
  "g3TongSlsxHd": 160,
  "g3SlsxTuSxHt": 130,
  "g3SlsxTuSxDd": 30,
  "g3SlsxOsDd": 5,
  "g3SlsxOsTonHt": 10,

  "g4TuSlsxTonHt": 35,
  "g4TuSlsxTrongThang": 130,
  "g4SlsxOsTon": 10,
  "g4SlsxOsTrongThang": 28,
  "g4Lk": 20,
  "g4TiSuatLngDuKien": 12.0,
  "g4LngDuKien": 100000000,

  "g5RaTuongUngSlnt": 170,
  "g5NtSlsxTonHt": 30,
  "g5NtSlsxTrongThang": 120,
  "g5NtSlsxOsTon": 8,
  "g5NtSlsxOsTrongThang": 25,
  "g5TiSuatLng": 11.5,
  "g5LngVnd": 90000000
}
```

> **Note**: Các trường công thức (g2TongSlsxDuKien, g3Ee, g4Tong, g4DoanhThu, g5TongSlnt, g5DoanhThu, g6_*) bị **ignore** nếu có trong request — backend tự tính.  
> **Note**: g1 fields của tháng không phải tháng đầu tiên bị **ignore** (5 trường auto-populated từ g6 tháng trước).

### Response 200

```json
{
  "code": "SUCCESS",
  "message": "Đã lưu bản ghi tháng",
  "data": { /* full ProjectMonthRecordResponse như GET /{id} */ }
}
```

### Response 400 — bản ghi inactive

```json
{
  "code": "MONTHLY_RECORD_INACTIVE",
  "message": "Bản ghi tháng này không còn hoạt động (inactive)",
  "data": null
}
```

---

## Lưu ý tích hợp với Project API

Khi `PUT /api/binance/projects/{id}` cập nhật `monthEnd`:

- **Nếu mở rộng**: ProjectService sinh thêm bản ghi tháng mới (active=TRUE), trả response bình thường.
- **Nếu rút ngắn**: ProjectService trả response với warning code và yêu cầu confirm:

```json
{
  "code": "MONTH_RANGE_SHRINK_WARNING",
  "message": "Có 2 bản ghi tháng (2026-04, 2026-05) sẽ bị đánh dấu inactive. Dữ liệu không bị xóa.",
  "data": {
    "pendingInactiveMonths": ["2026-04", "2026-05"]
  }
}
```

- FE hiển thị confirm dialog, nếu user đồng ý gọi lại với header `X-Confirm-Shrink: true` hoặc query param `confirmShrink=true`.
