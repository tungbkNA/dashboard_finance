<!--
SYNC IMPACT REPORT
Version change: (no prior version) → 1.0.0
Modified sections:
  - Phạm Vi & Thẩm Quyền: Đổi tên dự án từ "Dashboard Binance" → "Quản Lý Dự Án Nội Bộ"
  - Nguyên Tắc Cốt Lõi: Bổ sung quy tắc BE là nguồn sự thật cho nghiệp vụ
Added sections:
  - 3. Kiến Trúc Tổng Thể (BE/ + FE/ layout)
  - 4. Hiến Chương Backend (stack, layers, BigDecimal, tests)
  - 5. Hiến Chương Frontend (stack, PrimeVue, red theme, UI patterns)
  - 9. Quy Tắc Nghiệp Vụ (cross-month integrity, formula fields, warnings)
Removed sections: none
Templates checked:
  - .specify/templates/plan-template.md ✅ aligned (BE/FE paths documented)
  - .specify/templates/spec-template.md ✅ aligned (no principle conflicts)
  - .specify/templates/tasks-template.md ✅ aligned (web-app path convention matches)
Follow-up TODOs: none
-->

# Hệ Thống Quản Lý Dự Án Nội Bộ – Hiến Chương

## 1. Phạm Vi & Thẩm Quyền

Hiến chương này quản lý **TẤT CẢ các đặc tả và triển khai** trong Hệ Thống Quản Lý Dự Án Nội Bộ, bao gồm:

- Dịch vụ Backend (Spring Boot / Java 21)
- Ứng dụng Frontend (Vue 3 / TypeScript)
- Hợp đồng API
- Hành vi UI
- Mô hình dữ liệu
- Quy tắc nghiệp vụ và công thức tính toán

Nếu bất kỳ đặc tả nào xung đột với hiến chương này, **tài liệu này sẽ được ưu tiên**.

---

## 2. Nguyên Tắc Cốt Lõi

### 2.1 Phát Triển Hướng Đặc Tả

- Đặc tả là **nguồn sự thật duy nhất**
- Không có quy tắc nghiệp vụ nào được tồn tại **chỉ trong code**
- Code PHẢI tham chiếu đến ít nhất một file đặc tả
- Mọi thay đổi nghiệp vụ PHẢI cập nhật spec trước, sau đó mới code
- Mỗi feature nghiệp vụ PHẢI có thư mục riêng trong `specs/` — không gom toàn bộ hệ thống vào một spec lớn

### 2.2 Phân Tách Trách Nhiệm FE & BE

**Backend là nguồn sự thật** cho:
- Quy tắc nghiệp vụ và validation nghiệp vụ
- Công thức tính toán
- Xử lý transaction và nhất quán dữ liệu
- Audit log
- Cập nhật dữ liệu liên tháng

**Frontend chịu trách nhiệm**:
- Tương tác người dùng
- Validation UI cơ bản (required, format)
- Logic hiển thị

Frontend **KHÔNG ĐƯỢC tự quyết định các công thức nghiệp vụ quan trọng** và
**KHÔNG ĐƯỢC giả định hành vi backend** mà không được định nghĩa trong đặc tả.

---

## 3. Kiến Trúc Tổng Thể

Project PHẢI tách thành 2 thư mục chính ở root:

```text
<root>/
├── BE/    ← Backend (Spring Boot / Java 21)
└── FE/    ← Frontend (Vue 3 / TypeScript)
```

Không được đặt source code backend hoặc frontend ở ngoài hai thư mục này.

---

## 4. Hiến Chương Backend

### 4.1 Công Nghệ

| Thành phần | Quy định |
|---|---|
| Ngôn ngữ | Java 21 |
| Framework | Spring Boot |
| Build tool | Maven |
| Cơ sở dữ liệu | PostgreSQL |
| Giao tiếp | REST API |

### 4.2 Cấu Trúc Package

Cấu trúc package PHẢI bao gồm đủ các tầng sau:

```
controller / service / repository / dto / entity / mapper / validation / exception / config
```

Không được bỏ qua hay gộp chung các tầng này.

### 4.3 Kiểu Dữ Liệu Tài Chính

- Tiền, đơn giá, số lượng và phần trăm quan trọng PHẢI dùng `BigDecimal`
- Không dùng `double` hay `float` cho các trường tài chính

### 4.4 Kiểm Thử

- Logic tính toán quan trọng PHẢI có unit test
- Các cập nhật liên tháng PHẢI có integration test hoặc test transaction

### 4.5 Nhất Quán Dữ Liệu Liên Tháng

- Tồn cuối kỳ tháng T là tồn đầu kỳ tháng T+1
- Mọi cập nhật có thể ảnh hưởng tháng khác PHẢI đảm bảo tính toàn vẹn qua transaction hoặc cơ chế nhất quán rõ ràng
- Khi sửa dữ liệu một tháng có thể ảnh hưởng tháng khác, hệ thống PHẢI cảnh báo trước và chỉ cập nhật sau khi người dùng xác nhận

---

## 5. Hiến Chương Frontend

### 5.1 Công Nghệ

| Thành phần | Quy định |
|---|---|
| Framework | Vue 3 (Composition API) |
| Build tool | Vite |
| Ngôn ngữ | TypeScript |
| Router | Vue Router |
| State management | Pinia (khi cần state dùng chung) |
| UI library | PrimeVue |
| Icon | PrimeIcons |

### 5.2 Giao Diện

- Tông màu chủ đạo: **đỏ** (red)
- Giao diện PHẢI rõ ràng, dễ nhập liệu, dễ kiểm tra số liệu

### 5.3 Thành Phần UI Bắt Buộc

Mỗi màn hình nghiệp vụ PHẢI có đầy đủ:

| Thành phần | Mô tả |
|---|---|
| Table | Hiển thị danh sách dữ liệu |
| Form | Nhập liệu |
| Dialog xác nhận | Trước mọi hành động phá hủy hoặc ảnh hưởng tháng khác |
| Toast notification | Phản hồi thành công / lỗi |
| Loading state | Khi đang gọi API |
| Empty state | Khi không có dữ liệu |
| Error state | Khi gọi API thất bại |

### 5.4 Trường Công Thức & Trường Nhập Tay

- **Trường công thức** (giá trị do backend tính): hiển thị **read-only rõ ràng**, không cho người dùng sửa trực tiếp
- **Trường nhập tay**: dễ nhận biết; nếu đã có dữ liệu, PHẢI cảnh báo trước khi cho phép thay đổi

---

## 6. Hiến Chương API

### 6.1 Một API = Một Đặc Tả

Mỗi API phải có chính xác một file đặc tả.

### 6.2 Quy Ước Đặt Tên

URL dựa trên tài nguyên theo chuẩn RESTful:

```
GET    /api/{resource}
POST   /api/{resource}
GET    /api/{resource}/{id}
PUT    /api/{resource}/{id}
DELETE /api/{resource}/{id}
```

### 6.3 Định Dạng Response Chuẩn

```json
{
  "code": "SUCCESS | ERROR_CODE",
  "message": "Thông báo có thể đọc được",
  "data": {}
}
```

### 6.4 Xử Lý Lỗi

- Lỗi PHẢI được hiển thị rõ ràng
- Không có fallback im lặng
- Lỗi validation PHẢI được ghi chép

---

## 7. Hiến Chương Dữ Liệu

### 7.1 Định Danh

- Tất cả ID là UUID
- Không có khóa chính tổng hợp được hiển thị cho FE

### 7.2 Các Trường Cốt Lõi Của Dự Án

- id
- projectCode (duy nhất)
- projectName
- projectType
- customer
- representId (id của nhân sự)
- price
- status_contract
- status_project
- month_start (mm/yyyy)
- month_end (mm/yyyy)

Các trường dẫn xuất PHẢI được ghi chép trong đặc tả.

---

## 8. Trạng Thái & Vòng Đời

### 8.1 Trạng Thái Dự Án — status_project

- INPROGRESS
- PENDING
- CLOSE

### 8.2 Trạng Thái Hợp Đồng — status_contract

- `0` — Chưa có hợp đồng
- `1` — Có hợp đồng

Chuyển đổi trạng thái ngầm định bị cấm.

---

## 9. Quy Tắc Nghiệp Vụ

### 9.1 Nhất Quán Dữ Liệu Liên Tháng

- Tồn cuối kỳ tháng T = Tồn đầu kỳ tháng T+1 (bất biến)
- Khi người dùng sửa dữ liệu một tháng có thể kéo theo thay đổi tháng khác:
  1. Hệ thống PHẢI hiển thị cảnh báo rõ ràng nêu các tháng bị ảnh hưởng
  2. Chỉ thực hiện cập nhật sau khi người dùng bấm xác nhận
  3. Toàn bộ cập nhật PHẢI nằm trong một transaction

### 9.2 Trường Công Thức

- Các trường được tính từ công thức nghiệp vụ KHÔNG cho người dùng sửa trực tiếp
- Backend là nơi duy nhất thực thi công thức
- Frontend chỉ hiển thị kết quả nhận từ API

### 9.3 Trường Nhập Tay

- Trường nhập tay hiện có dữ liệu PHẢI hiển thị cảnh báo trước khi cho phép thay đổi
- Người dùng PHẢI xác nhận trước khi ghi đè

---

## 10. Hiến Chương UI

### 10.1 Thiết Kế Dựa Trên Màn Hình

- UI được tổ chức theo màn hình
- Một màn hình ánh xạ đến một tập API chính

### 10.2 Tìm Kiếm & Lọc

- Tìm kiếm mặc định là nhẹ
- Bộ lọc nâng cao có thể thu gọn
- FE không được gửi các bộ lọc không sử dụng

### 10.3 Hành Vi UI

- Hành động phá hủy và hành động ảnh hưởng liên tháng PHẢI yêu cầu xác nhận
- Trạng thái loading là bắt buộc khi chờ API
- Lỗi PHẢI thân thiện với người dùng
- Trường công thức PHẢI hiển thị read-only rõ ràng (ví dụ: màu nền khác, icon khóa)
- Trường nhập tay PHẢI dễ nhận biết

---

## 11. Cấp Bậc Đặc Tả

Tất cả các đặc tả PHẢI khai báo hiến chương quản lý của chúng.

Ví dụ:
```
Hiến Chương Quản Lý:
- Hiến Chương Hệ Thống Quản Lý Dự Án Nội Bộ
```

---

## 12. Khả Năng Truy Vết Code

### Ví Dụ Backend

```java
/**
 * THAM CHIẾU ĐẶC TẢ:
 * - specs/project-management/apis/update-project.md
 */
```

### Ví Dụ Frontend

```ts
/**
 * THAM CHIẾU ĐẶC TẢ:
 * - specs/project-management/ui/project-list-ui.md
 * - specs/project-management/apis/list-projects.md
 */
```

---

## 13. Quản Lý Thay Đổi

### 13.1 Thay Đổi Quy Tắc Nghiệp Vụ

- Cập nhật đặc tả trước
- Sau đó cập nhật code

### 13.2 Thay Đổi Chỉ UI

- Cho phép thay đổi code
- Cập nhật đặc tả là tùy chọn (nhưng khuyến nghị)

---

## 14. Ngoài Phạm Vi

- Chi tiết layout pixel-perfect của UI
- Cấu hình hạ tầng deployment
- Các mẫu code đặc thù framework không ảnh hưởng nghiệp vụ

---

## 15. Quy Tắc Cuối Cùng

Nếu có điều gì không rõ ràng:

- Không được đoán
- Thêm `TODO(<FIELD_NAME>): explanation` vào đặc tả
- Làm rõ với team trước khi triển khai

---

## Quản Trị

Hiến chương này PHẢI được cập nhật khi có thay đổi nguyên tắc lớn. Mọi amendment:

1. PHẢI ghi rõ lý do thay đổi
2. PHẢI cập nhật phiên bản theo Semantic Versioning
3. PHẢI cập nhật `LAST_AMENDED_DATE`

**Version**: 1.0.0 | **Ratified**: 2026-05-06 | **Last Amended**: 2026-05-06
