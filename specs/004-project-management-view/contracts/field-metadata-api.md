# API Contract: Field Metadata

**Endpoint**: `GET /api/binance/project-monthly-records/field-metadata`  
**Controller**: `ProjectMonthRecordController`  
**Feature**: 004-project-management-view  
**Status**: NEW (to be created)

---

## Purpose

Returns a static, hard-coded classification of all fields in `ProjectMonthRecord` into formula vs manual-entry per group. The FE calls this once on view mount and uses it to:
1. Show "Tự tính" badges on formula fields
2. Disable formula field inputs
3. Know which fields to include in the overwrite-warning check

---

## Request

```
GET /api/binance/project-monthly-records/field-metadata
```

No parameters, no request body.

---

## Response

**HTTP 200**

```json
{
  "code": "SUCCESS",
  "message": "OK",
  "data": {
    "groups": [
      {
        "groupId": "g1",
        "groupName": "Tồn đầu kỳ",
        "manualFields": [
          "g1RaTon", "g1SlsxTonTuSxHd", "g1SlsxTonTuSxHtHd",
          "g1SlsxTonTuSxDdHd", "g1SlsxOsTon", "g1SlsxOsTonHt"
        ],
        "formulaFields": [],
        "cascadedFromPrevMonthFields": [
          "g1RaTon", "g1SlsxTonTuSxHtHd", "g1SlsxTonTuSxDdHd",
          "g1SlsxOsTon", "g1SlsxOsTonHt"
        ]
      },
      {
        "groupId": "g2",
        "groupName": "Kế hoạch tháng",
        "manualFields": [
          "g2Headcount", "g2Ra", "g2SlsxTuSx", "g2SlsxOs", "g2LienKet",
          "g2SlsxTuSxHtTrongThang", "g2SlsxTuSxDd", "g2SlsxOsHt",
          "g2SlsxOsDd", "g2Cpbqtb", "g2TySuatLng"
        ],
        "formulaFields": ["g2TongSlsxDuKien"],
        "cascadedFromPrevMonthFields": []
      },
      {
        "groupId": "g3",
        "groupName": "Thực hiện SLSX đến NGÀY",
        "manualFields": [
          "g3Ra", "g3TongSlsxHd", "g3SlsxTuSxHt",
          "g3SlsxTuSxDd", "g3SlsxOsDd", "g3SlsxOsTonHt"
        ],
        "formulaFields": ["g3Ee"],
        "cascadedFromPrevMonthFields": []
      },
      {
        "groupId": "g4",
        "groupName": "Kế hoạch doanh thu",
        "manualFields": [
          "g4TuSlsxTonHt", "g4TuSlsxTrongThang", "g4SlsxOsTon",
          "g4SlsxOsTrongThang", "g4Lk", "g4TiSuatLngDuKien", "g4LngDuKien"
        ],
        "formulaFields": ["g4Tong", "g4DoanhThu"],
        "cascadedFromPrevMonthFields": []
      },
      {
        "groupId": "g5",
        "groupName": "Thực hiện nghiệm thu",
        "manualFields": [
          "g5RaTuongUngSlnt", "g5NtSlsxTonHt", "g5NtSlsxTrongThang",
          "g5NtSlsxOsTon", "g5NtSlsxOsTrongThang", "g5TiSuatLng", "g5LngVnd"
        ],
        "formulaFields": ["g5TongSlnt", "g5DoanhThu"],
        "cascadedFromPrevMonthFields": []
      },
      {
        "groupId": "g6",
        "groupName": "Tồn cuối kỳ",
        "manualFields": [],
        "formulaFields": [
          "g6RaTon", "g6SlsxTonHt", "g6SlsxTonDd",
          "g6SlsxOsTon", "g6SlsxOsTonHt"
        ],
        "cascadedFromPrevMonthFields": []
      }
    ]
  }
}
```

---

## Notes

- This is a **static** response. The BE method returns a hard-coded constant — no DB query.
- `cascadedFromPrevMonthFields` are a subset of `manualFields`. They are only editable when `isFirstMonth = true` on the record. When `isFirstMonth = false`, these fields are read-only (populated by cascade from the previous month's G6).
- The FE combines `formulaFields` and (when `isFirstMonth = false`) `cascadedFromPrevMonthFields` to determine the complete set of non-editable fields for a given record.
