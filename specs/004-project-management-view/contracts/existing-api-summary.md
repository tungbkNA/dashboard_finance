# API Contract: Existing Endpoints (Feature 003 — Used Unchanged)

**Feature**: 004-project-management-view  
**Date**: 2026-05-07  
**Source**: `ProjectMonthRecordController.java`

This feature reuses the following endpoints from Feature 003 without modification.

---

## GET /api/binance/project-monthly-records

**Purpose**: Load the project list for a given month (accordion card list).

**Request**:
```
GET /api/binance/project-monthly-records?monthKey=YYYY-MM
```

| Param | Required | Default | Notes |
|---|---|---|---|
| `monthKey` | No | current YearMonth | Format: `YYYY-MM` e.g. `2026-05` |

**Response 200**:
```json
{
  "code": "SUCCESS",
  "data": [
    {
      "id": "uuid",
      "projectId": "uuid",
      "projectCode": "AI_001",
      "projectName": "Dự án AI số 1",
      "monthKey": "2026-05",
      "active": true,
      "g4DoanhThu": 1500000000,
      "g5DoanhThu": 1200000000,
      "g5TongSlnt": 3.5,
      "updatedAt": "2026-05-07T10:00:00Z"
    }
  ]
}
```

**FE usage**: Called on mount and when month filter changes. Response populates the accordion card list.

---

## GET /api/binance/project-monthly-records/{id}

**Purpose**: Load full record detail for a project card (all ~40 fields + isFirstMonth).

**Request**:
```
GET /api/binance/project-monthly-records/{id}
```

**Response 200**:
```json
{
  "code": "SUCCESS",
  "data": {
    "id": "uuid",
    "projectId": "uuid",
    "projectCode": "AI_001",
    "projectName": "Dự án AI số 1",
    "monthKey": "2026-05",
    "active": true,
    "isFirstMonth": true,
    "createdAt": "...",
    "updatedAt": "...",
    "g1RaTon": null,
    "g1SlsxTonTuSxHd": null,
    "...": "...",
    "g6SlsxOsTonHt": null
  }
}
```

**Response 404**:
```json
{ "code": "MONTHLY_RECORD_NOT_FOUND", "message": "Bản ghi tháng không tồn tại" }
```

**FE usage**: Called when user first expands any group inside a project card. Cached in `detail` ref. Refreshed after each successful save.

---

## PUT /api/binance/project-monthly-records/{id}

**Purpose**: Update manual-entry fields; BE recalculates formula fields and cascades to next month.

**Request**:
```
PUT /api/binance/project-monthly-records/{id}
Content-Type: application/json

{
  "g1RaTon": 10.5,
  "g1SlsxTonTuSxHd": null,
  ...all ~40 manual fields...
}
```

**⚠️ Important**: All manual fields must be sent. A `null` value **clears** the field. The FE uses the merge strategy from research.md R1 to populate non-edited groups from the current `detail` record.

**Response 200**: Full `ProjectMonthRecordResponse` (same shape as GET /{id} response).

**Response 400**: `MONTHLY_RECORD_INACTIVE`
**Response 404**: `MONTHLY_RECORD_NOT_FOUND`

**FE usage**: Called from per-group Save handler. On success, `detail` ref is updated from the response. On failure (any HTTP error), error toast is shown and row stays in edit mode.
