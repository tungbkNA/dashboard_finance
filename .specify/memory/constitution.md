# Hệ Thống Dashboard Binance – Hiến Chương

## 1. Phạm Vi & Thẩm Quyền
Hiến chương này quản lý **TẤT CẢ các đặc tả và triển khai** trong Hệ Thống Dashboard Binance, bao gồm:

- Dịch vụ Backend
- Ứng dụng Frontend
- Hợp đồng API
- Hành vi UI
- Mô hình dữ liệu

Nếu bất kỳ đặc tả nào xung đột với hiến chương này, **tài liệu này sẽ được ưu tiên**.

---

## 2. Nguyên Tắc Cốt Lõi

### 2.1 Phát Triển Hướng Đặc Tả
- Đặc tả là **nguồn sự thật duy nhất**
- Không có quy tắc nghiệp vụ nào được tồn tại **chỉ trong code**
- Code phải tham chiếu đến ít nhất một file đặc tả

### 2.2 Phân Tách Trách Nhiệm FE & BE
- **Backend**:
  - Quy tắc nghiệp vụ
  - Xác thực dữ liệu
  - Chuyển đổi trạng thái
- **Frontend**:
  - Tương tác người dùng
  - Xác thực UI (cơ bản)
  - Chỉ logic hiển thị

Frontend **không bao giờ được giả định hành vi backend** mà không được định nghĩa trong đặc tả.

---

## 3. Kiến Trúc Dựa Trên Module
Các module:
- Dashboard Thống Kê
- Cài Đặt Dự Án + Tham Chiếu Dự Án - Nhân Sự
- Quản Lý Các Dự Án
- Cài Đặt Nhân Sự

Mỗi module:
- Sở hữu các API và hành vi UI của nó
- Không được dựa vào chi tiết nội bộ của các module khác

---

## 4. Hiến Chương API

### 4.1 Một API = Một Đặc Tả
Mỗi API phải có chính xác một file đặc tả.

### 4.2 Quy Ước Đặt Tên
URL dựa trên tài nguyên theo chuẩn RESTful:
- GET /api/binance
- POST /api/binance
- POST /api/binance/{id}
- POST /api/binance/{id}

### 4.3 Định Dạng Response Chuẩn
```json
{
  "code": "SUCCESS | ERROR_CODE",
  "message": "Thông báo có thể đọc được",
  "data": {}
}
```

### 4.4 Xử Lý Lỗi
- Lỗi phải được hiển thị rõ ràng
- Không có fallback im lặng
- Lỗi xác thực phải được ghi chép

---

## 5. Hiến Chương Dữ Liệu

### 5.1 Định Danh
- Tất cả ID là UUID
- Không có khóa chính tổng hợp được hiển thị cho FE

### 5.2 Các Trường Cốt Lõi Của Dự Án
- id
- projectCode (duy nhất)
- projectName
- projectType
- status
- customer
- representId (id của nhân sự)
- price
- status_contract
- status_project
- month_start (mm/yyyy)
- month_end (mm/yyyy)

Các trường dẫn xuất phải được ghi chép.

---

## 6. Trạng Thái & Vòng Đời

### 6.1 Trạng Thái Dự Án - status_project
- INPROGRESS
- PENDING
- CLOSE
### 6.1 Trạng Thái Dự Án - status_contract
- 0 (Chưa có hợp đồng)
- 1 (có hợp đồng)

Chuyển đổi ngầm định bị cấm.

---

## 7. Hiến Chương UI

### 7.1 Thiết Kế Dựa Trên Màn Hình
- UI được tổ chức theo màn hình
- Một màn hình ánh xạ đến một tập API chính

### 7.2 Tìm Kiếm & Lọc
- Tìm kiếm mặc định là nhẹ
- Bộ lọc nâng cao có thể thu gọn
- FE không được gửi các bộ lọc không sử dụng

### 7.3 Hành Vi UI
- Các hành động phá hủy yêu cầu xác nhận
- Trạng thái loading là bắt buộc
- Lỗi phải thân thiện với người dùng

---

## 8. Cấp Bậc Đặc Tả

Tất cả các đặc tả phải khai báo hiến chương quản lý của chúng.

Ví dụ:
```
Hiến Chương Quản Lý:
- Hiến Chương Hệ Thống
- Hiến Chương Dashboard Binance
```

---

## 9. Khả Năng Truy Vết Code

### Ví Dụ Backend
```java
/**
 * THAM CHIẾU ĐẶC TẢ:
 * - specs/license-management/apis/assign-license.md
 */
```

### Ví Dụ Frontend
```ts
/**
 * THAM CHIẾU ĐẶC TẢ:
 * - specs/license-management/ui/list-ui.md
 * - specs/license-management/apis/list-licenses.md
 */
```

---

## 10. Quản Lý Thay Đổi

### 10.1 Thay Đổi Quy Tắc Nghiệp Vụ
- Cập nhật đặc tả trước
- Sau đó cập nhật code

### 10.2 Thay Đổi Chỉ UI
- Cho phép thay đổi code
- Cập nhật đặc tả là tùy chọn

---

## 11. Ngoài Phạm Vi
- Chi tiết bố cục UI
- Chi tiết triển khai cơ sở dữ liệu
- Các mẫu code đặc thù framework

---

## 12. Quy Tắc Cuối Cùng
Nếu có điều gì không rõ ràng:
- Không được đoán
- Thêm TODO
- Làm rõ trước khi triển khai
