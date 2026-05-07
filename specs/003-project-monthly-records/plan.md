# Implementation Plan: Quản Lý Bản Ghi Dự Án Theo Tháng

**Branch**: `003-project-monthly-records` | **Date**: 2026-05-07 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `specs/003-project-monthly-records/spec.md`

## Summary

Xây dựng hệ thống quản lý bản ghi dự án theo tháng (ProjectMonthRecord). Mỗi dự án có một bản ghi cho từng tháng trong khoảng monthStart–monthEnd. Mỗi bản ghi chứa 6 nhóm thuộc tính: Tồn đầu kỳ, Kế hoạch tháng, Thực hiện SLSX đến NGÀY, Kế hoạch doanh thu, Thực hiện nghiệm thu, Tồn cuối kỳ. Nhóm Tồn cuối kỳ là toàn bộ công thức. Các nhóm khác hỗn hợp nhập tay + công thức.

Backend là nguồn sự thật duy nhất cho tất cả công thức. Frontend hiển thị bảng tóm tắt theo tháng tại `/projects` (filter tháng mặc định = tháng hiện tại), click row → form đầy đủ 6 nhóm.

## Technical Context

**Backend Language/Version**: Java 21 / Spring Boot 3.4.5  
**Frontend Language/Version**: TypeScript / Vue 3 + Vite 6.x  
**Primary Dependencies (BE)**: Spring Data JPA, Flyway, Lombok, Bean Validation, springdoc-openapi 2.8.6, JUnit 5, Mockito  
**Primary Dependencies (FE)**: PrimeVue 4.3.x, Axios 1.x, Vue Router 4.x, Pinia 2.x  
**Storage**: PostgreSQL 15  
**Testing (BE)**: JUnit 5 / Mockito — unit tests cho toàn bộ công thức trong MonthlyCalculationService  
**Testing (FE)**: N/A — FE không tự tính công thức, không cần unit test logic tính toán  
**Project Type**: web-service (BE) + web-app (FE)  
**Performance Goals**: Sinh N bản ghi tháng trong vòng 2 giây; filter danh sách theo tháng < 500ms  
**Constraints**:
- BigDecimal cho tất cả trường số (số lượng, đơn giá, doanh thu, tỷ suất)
- monthKey format YYYY-MM (VARCHAR 7)
- Formula fields lưu snapshot vào DB khi save; nếu chưa có thì tính động khi đọc
- FE không tự tính công thức nghiệp vụ
- Cross-month updates (Tồn cuối → Tồn đầu tháng sau) trong một transaction
- Inactive flag thay vì hard-delete khi rút ngắn khoảng tháng
- Rounding: VNĐ 0dp, % 2dp

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| Java 21 / Spring Boot | ✅ PASS | Đúng stack hiện có |
| Maven build tool | ✅ PASS | pom.xml đã có |
| PostgreSQL storage | ✅ PASS | DB đang chạy, Flyway đã cấu hình |
| Vue 3 + TypeScript + PrimeVue (FE) | ✅ PASS | Stack FE hiện có |
| BigDecimal cho tài chính | ✅ PASS | Áp dụng toàn bộ trường số |
| Controller/Service/Repository/DTO/Entity/Mapper layers | ✅ PASS | Thêm MonthlyCalculationService |
| Formula fields read-only (FE) | ✅ PASS | Backend là nguồn sự thật |
| Transaction cho cross-month updates | ✅ PASS | @Transactional bao toàn bộ cascade |
| Unit test cho logic công thức | ✅ PASS | MonthlyCalculationServiceTest |
| UUID IDs | ✅ PASS | Tất cả entity |
| REST API prefix `/api/binance/**` | ✅ PASS | CORS đã cover |
| Cảnh báo trước destructive ops | ✅ PASS | FE hiển thị confirm dialog khi rút ngắn tháng |

**Result**: ✅ GATE PASS — Không có vi phạm Hiến Chương.

## Project Structure

### Documentation (this feature)

```text
specs/003-project-monthly-records/
├── plan.md          ← this file
├── research.md      ← Phase 0
├── data-model.md    ← Phase 1
├── quickstart.md    ← Phase 1
└── contracts/
    └── project-monthly-records-api.md   ← Phase 1
```

### Source Code (new files for this feature)

```text
BE/src/main/java/com/internal/projectmgmt/
├── entity/
│   └── ProjectMonthRecord.java
├── repository/
│   └── ProjectMonthRecordRepository.java
├── dto/
│   └── monthlyrecord/
│       ├── ProjectMonthRecordRequest.java
│       └── ProjectMonthRecordResponse.java
├── mapper/
│   └── ProjectMonthRecordMapper.java
├── service/
│   ├── ProjectMonthRecordService.java     ← CRUD + generate logic
│   └── MonthlyCalculationService.java     ← pure formula computation
└── controller/
    └── ProjectMonthRecordController.java

BE/src/main/resources/db/migration/
└── V3__project_monthly_records_schema.sql

BE/src/test/java/com/internal/projectmgmt/service/
└── MonthlyCalculationServiceTest.java

FE/src/
├── types/
│   └── project-monthly-record.ts
├── services/
│   └── projectMonthlyRecordService.ts
└── views/
    └── ProjectManagementView.vue  ← (already exists, extend with month filter + table)
    + components/project-management/
        └── MonthlyRecordDialog.vue   ← 6-group detail form
```

## Complexity Tracking

*No constitution violations.*
