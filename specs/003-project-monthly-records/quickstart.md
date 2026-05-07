# Quickstart: Quản Lý Bản Ghi Dự Án Theo Tháng

**Feature**: 003-project-monthly-records  
**Date**: 2026-05-07

---

## Prerequisites

- PostgreSQL đang chạy, DB `dashboard_finance` tồn tại
- BE đã build được (feature 001 + 002 deployed)
- FE node_modules đã install

---

## 1. Chạy Backend

```powershell
cd BE
mvn spring-boot:run
```

Flyway sẽ tự động chạy `V3__project_monthly_records_schema.sql` khi khởi động.  
BE chạy tại `http://localhost:8080`.

---

## 2. Chạy Frontend

```powershell
cd FE
npm run dev
```

FE chạy tại `http://localhost:5173`.

---

## 3. Test flow thủ công

### Bước 1 — Tạo dự án có khoảng tháng

Vào **Cài đặt dự án** → tab **Dự án** → **Tạo dự án** với:
- monthStart = `01/2026`
- monthEnd = `03/2026`

→ Sau khi lưu, hệ thống tự sinh 3 bản ghi tháng: `2026-01`, `2026-02`, `2026-03`.

### Bước 2 — Xem danh sách bản ghi tháng

Vào **Quản lý Các dự án** (`/projects`):
- Bảng hiển thị bản ghi tháng hiện tại (tháng hiện tại theo ngày hệ thống)
- Dự án vừa tạo xuất hiện trong danh sách với các cột: Mã DA, Tên DA, Doanh thu KH, Doanh thu NT, Tổng SLNT
- Tất cả giá trị ban đầu là trống (chưa nhập liệu)

### Bước 3 — Nhập liệu tháng 01/2026

Chọn tháng `2026-01` từ bộ lọc (nếu tháng hiện tại không phải 01/2026).  
Click vào row của dự án → dialog 6 nhóm mở ra.

Nhập các trường thủ công, ví dụ:
- Nhóm 1 (Tồn đầu kỳ): `g1_ra_ton = 100`, `g1_slsx_os_ton = 20`, ...
- Nhóm 2 (KH tháng): `g2_slsx_tu_sx = 150`, `g2_slsx_os = 30`, `g2_lien_ket = 20`, ...
- Nhóm 3 (TH SLSX): `g3_ra = 180`, `g3_tong_slsx_hd = 160`, ...

Bấm **Lưu**.

**Kết quả mong đợi**:
- Trường công thức hiển thị giá trị được tính: `g2_tong_slsx_du_kien = 200`, `g3_ee = 88.89%`, ...
- Nhóm 6 (Tồn cuối kỳ) hiển thị 5 trường công thức với giá trị.

### Bước 4 — Kiểm tra cascade sang tháng 02/2026

Chọn tháng `2026-02` từ bộ lọc.  
Click vào row dự án → dialog mở ra.

**Kết quả mong đợi**:
- Nhóm 1 (Tồn đầu kỳ) tự động điền 5 trường từ Tồn cuối kỳ tháng 01/2026.
- Trường `g1_slsx_ton_tu_sx_hd` vẫn trống (phải nhập tay).
- 5 trường còn lại của Nhóm 1 hiển thị read-only, không cho sửa.

### Bước 5 — Mở rộng khoảng tháng

Vào **Cài đặt dự án** → sửa dự án, đổi monthEnd thành `05/2026` → Lưu.  
Quay lại **Quản lý Các dự án**, chọn tháng `2026-04` → dự án xuất hiện với row trống.

### Bước 6 — Rút ngắn khoảng tháng

Sửa dự án, đổi monthEnd thành `02/2026` → Lưu.

**Kết quả mong đợi**:
- Dialog cảnh báo: "Bản ghi tháng 2026-03 sẽ bị đánh dấu inactive. Dữ liệu không bị xóa."
- Bấm xác nhận → tháng 03/2026 biến mất khỏi danh sách (inactive).
- Chọn tháng `2026-03` từ bộ lọc → dự án không còn xuất hiện.

---

## 4. Chạy unit tests

```powershell
cd BE
mvn test -Dtest="MonthlyCalculationServiceTest" -q
```

**Kết quả mong đợi**: Tất cả test PASS, BUILD SUCCESS.

---

## 5. Kiểm tra API trực tiếp (Swagger)

Mở `http://localhost:8080/swagger-ui.html`:

```
GET  /api/binance/project-monthly-records?monthKey=2026-01
GET  /api/binance/project-monthly-records/{id}
PUT  /api/binance/project-monthly-records/{id}
```
