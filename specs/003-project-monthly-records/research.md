# Research: Quản Lý Bản Ghi Dự Án Theo Tháng

**Feature**: 003-project-monthly-records  
**Date**: 2026-05-07

---

## R1: Thiết kế bảng DB cho bản ghi tháng có nhiều trường số

**Decision**: Một bảng phẳng `project_monthly_record` với ~40+ cột BigDecimal, thêm cột `active BOOLEAN`.

**Rationale**:
- Không cần normalize thêm vì đây là bản ghi nghiệp vụ nguyên tử — mỗi tháng của mỗi dự án là một đơn vị dữ liệu.
- Bảng phẳng đơn giản hóa query, tránh JOIN phức tạp khi đọc/ghi.
- PostgreSQL xử lý tốt bảng nhiều cột với index đúng cách.

**Alternatives considered**:
- JSON column (JSONB) cho từng nhóm: linh hoạt nhưng mất type safety và khó index từng trường.
- 6 bảng con (1 bảng/nhóm): tăng phức tạp join, không mang lại lợi ích thực tế.

---

## R2: Lưu snapshot công thức hay tính động

**Decision**: Lưu snapshot vào DB khi save; nếu DB chưa có (null) thì backend tính động và trả về (không auto-save khi đọc).

**Rationale**:
- Snapshot đảm bảo tính ổn định: doanh thu đã lưu không thay đổi khi đơn giá dự án thay đổi sau này.
- Tính động khi null: tránh lưu rác khi chưa có đủ dữ liệu đầu vào.
- Pattern này phù hợp với `FR-REC-023`: "nếu có snapshot thì trả về snapshot, nếu không có thì tính theo công thức".

**Alternatives considered**:
- Chỉ tính động hoàn toàn: đơn giản hơn nhưng doanh thu thay đổi theo đơn giá hiện tại — vi phạm yêu cầu freeze snapshot.
- Chỉ lưu snapshot: phải recalculate và save lại khi dữ liệu đầu vào thay đổi — phức tạp hơn không cần thiết.

---

## R3: MonthlyCalculationService — pure function hay stateful

**Decision**: Pure service (stateless, không có DB dependency) — nhận `ProjectMonthRecord` object + `BigDecimal price` → trả về `ProjectMonthRecord` với các trường công thức đã điền.

**Rationale**:
- Pure function dễ unit test: không cần mock DB.
- Tách rõ ràng "tính toán" khỏi "lưu trữ" — `ProjectMonthRecordService` gọi calculation service rồi mới save.
- Dễ reuse: có thể gọi từ nhiều nơi (create, update, cross-month cascade).

**Alternatives considered**:
- Tính toán inline trong service: gộp logic vào một class, khó test riêng từng công thức.

---

## R4: Cascade Tồn cuối kỳ → Tồn đầu kỳ tháng sau

**Decision**: Khi save tháng N, nếu tồn tại bản ghi tháng N+1 và bản ghi đó chưa có dữ liệu tồn đầu kỳ tự nhập (tức tháng N+1 không phải tháng đầu tiên), thì tự động cập nhật 5 trường `g1_*` của tháng N+1 từ 5 trường `g6_*` của tháng N — trong cùng một `@Transactional`.

**Rationale**:
- Đảm bảo tính bất biến `Tồn cuối kỳ T = Tồn đầu kỳ T+1` theo `FR-REC-006`.
- Transaction đảm bảo không bao giờ có trạng thái inconsistent.
- Không cần cascade đệ quy nhiều tháng — chỉ cập nhật tháng N+1 ngay, tháng N+2 sẽ được cập nhật khi user save tháng N+1.

**Alternatives considered**:
- Cascade đệ quy toàn chuỗi: quá phức tạp, có thể lock nhiều row, người dùng cần confirm từng bước.
- Tính động khi đọc: không lưu tồn đầu kỳ vào DB — khó kiểm soát trạng thái, vi phạm snapshot pattern.

---

## R5: monthKey format và sorting

**Decision**: `VARCHAR(7)` format `YYYY-MM` (ví dụ `2026-01`). Lexicographic sort = chronological sort.

**Rationale**:
- YYYY-MM sort đúng thứ tự thời gian mà không cần parse.
- Dễ filter theo tháng với WHERE `month_key = '2026-01'`.
- Dễ sinh range: iterate từ `monthStart` đến `monthEnd` bằng `YearMonth.parse()` trong Java.
- Nhất quán với chuẩn ISO 8601 partial date.

**Alternatives considered**:
- Lưu riêng year (INT) + month (INT): query phức tạp hơn khi filter range.
- `LocalDate` đầu tháng (`DATE`): overkill cho bài toán này, dễ gây nhầm lẫn.

---

## R6: Inactive flag cho bản ghi ngoài khoảng tháng rút ngắn

**Decision**: Thêm cột `active BOOLEAN DEFAULT TRUE`. Khi rút ngắn khoảng tháng: set `active = FALSE` cho các tháng bị loại (không hard-delete). Bảng và query filter `WHERE active = TRUE` trong listing.

**Rationale**:
- Giữ audit trail — dữ liệu đã nhập không mất.
- Nếu người dùng mở rộng lại khoảng tháng, bản ghi inactive có thể reactivate thay vì tạo mới.
- Đơn giản hơn soft-delete với `deleted_at` timestamp vì không cần timestamp phục hồi.

**Alternatives considered**:
- Hard-delete: mất dữ liệu, không reversible.
- Soft-delete với `deleted_at`: phức tạp hơn cần thiết cho use case này.

---

## R7: API endpoint design cho bản ghi tháng

**Decision**:
```
GET    /api/binance/project-monthly-records?projectId=&monthKey=    → danh sách (filter tháng)
GET    /api/binance/project-monthly-records/{id}                    → chi tiết 1 bản ghi
PUT    /api/binance/project-monthly-records/{id}                    → cập nhật trường nhập tay
```

**Rationale**:
- GET list hỗ trợ filter `monthKey` để implement month-picker ở FE.
- Không cần POST (sinh tự động khi tạo/update project) và DELETE (inactive, không hard-delete).
- PUT nhận chỉ các trường nhập tay; backend tính lại công thức và lưu snapshot.

**Alternatives considered**:
- Nested under project: `/api/binance/projects/{projectId}/monthly-records` — hợp lý nhưng phức tạp routing FE hơn.

---

## R8: Xử lý rounding

**Decision**:
- Tiền VNĐ (doanh thu, LNG): `BigDecimal.setScale(0, RoundingMode.HALF_UP)`
- Phần trăm (EE, tỷ suất LNG): `BigDecimal.setScale(2, RoundingMode.HALF_UP)`
- Số lượng SLSX, RA, Headcount: giữ nguyên BigDecimal không làm tròn (người dùng nhập số nguyên)

**Rationale**: Nhất quán với `FR-REC-029` và `FR-REC-030`. `HALF_UP` là quy tắc làm tròn thông dụng trong tài chính.

---

## R9: Điều chỉnh ProjectService khi save project

**Decision**: `ProjectService.create()` và `ProjectService.update()` gọi `ProjectMonthRecordService.generateRecords(project)` sau khi save project. Nếu monthEnd mở rộng, chỉ generate tháng mới. Nếu rút ngắn: trả warning response, FE confirm, sau đó call endpoint riêng để mark inactive.

**Rationale**: Tách rõ ràng giữa "cập nhật metadata dự án" và "quản lý vòng đời bản ghi tháng". Dễ test từng phần.
