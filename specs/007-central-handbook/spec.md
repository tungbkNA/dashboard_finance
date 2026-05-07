# Feature Specification: Sổ tay trung tâm

**Feature Branch**: `007-central-handbook`  
**Created**: 2026-05-07  
**Status**: Draft  
**Input**: User description: "Xây dựng Module 'Sổ tay trung tâm' để quản lý tài liệu và liên kết. Cho phép người dùng lưu trữ tên file, link file và phân loại chúng theo nhóm để tra cứu nhanh."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Quản lý nhóm file (Priority: P1)

Người quản trị hệ thống cần tạo, chỉnh sửa và xóa các nhóm tài liệu (ví dụ: "Hợp đồng", "Báo cáo", "Hướng dẫn") để phân loại file một cách có tổ chức. Mỗi nhóm có tên, mô tả ngắn và trạng thái hoạt động (Active/Inactive).

**Why this priority**: Nhóm file là nền tảng dữ liệu cho toàn bộ module — phải có nhóm trước khi gắn file vào nhóm.

**Independent Test**: Có thể test hoàn chỉnh bằng cách truy cập màn hình "Quản lý nhóm file", thêm/sửa/xóa nhóm và xác nhận dữ liệu hiển thị đúng trong bảng.

**Acceptance Scenarios**:

1. **Given** người dùng có quyền truy cập module "Sổ tay trung tâm", **When** họ mở màn hình "Quản lý nhóm file", **Then** hệ thống hiển thị danh sách tất cả nhóm file dưới dạng bảng.
2. **Given** người dùng đang ở màn hình "Quản lý nhóm file", **When** họ nhấn nút "Thêm mới" và điền tên nhóm + mô tả rồi nhấn "Lưu", **Then** nhóm mới được tạo thành công, hiển thị Toast thông báo và bảng cập nhật.
3. **Given** một nhóm file đã tồn tại, **When** người dùng nhấn nút sửa, thay đổi thông tin và nhấn "Lưu", **Then** thông tin nhóm được cập nhật và hiển thị lại trong bảng.
4. **Given** một nhóm file đã tồn tại và không có file nào thuộc nhóm này, **When** người dùng nhấn nút xóa và xác nhận, **Then** nhóm bị xóa khỏi hệ thống.
5. **Given** một nhóm file đang có file thuộc về, **When** người dùng nhấn nút xóa, **Then** hệ thống từ chối xóa và hiển thị thông báo lỗi rằng nhóm đang được sử dụng.

---

### User Story 2 - Quản lý bản ghi file (Priority: P1)

Người dùng cần thêm, chỉnh sửa và xóa bản ghi file (gồm tên file, link URL và nhóm file). Khi xem danh sách, có thể click link để mở tài liệu trong tab mới của trình duyệt.

**Why this priority**: Đây là tính năng cốt lõi mà người dùng cuối sẽ tương tác nhiều nhất — lưu trữ và truy cập nhanh tài liệu.

**Independent Test**: Có thể test bằng cách tạo file mới với link URL, kiểm tra hiển thị trong bảng, click link mở tab mới, sửa/xóa bản ghi.

**Acceptance Scenarios**:

1. **Given** đã tồn tại ít nhất một nhóm file active, **When** người dùng mở màn hình "Danh mục file", **Then** hệ thống hiển thị danh sách file dưới dạng bảng với các cột: Tên file, Link file (clickable), Nhóm file, Ngày tạo, Người tạo.
2. **Given** người dùng đang ở màn hình "Danh mục file", **When** họ nhấn "Thêm mới", chọn nhóm file từ dropdown, nhập tên file và link URL rồi nhấn "Lưu", **Then** bản ghi được tạo, Toast thông báo thành công, bảng cập nhật.
3. **Given** một bản ghi file có link "https://example.com/doc.pdf", **When** người dùng click vào cột link, **Then** trình duyệt mở URL đó trong tab mới.
4. **Given** một bản ghi file đã tồn tại, **When** người dùng nhấn sửa, thay đổi tên/link/nhóm rồi nhấn "Lưu", **Then** thông tin được cập nhật.
5. **Given** người dùng nhấn nút xóa một bản ghi file, **When** dialog xác nhận hiện ra và người dùng chọn "Xác nhận", **Then** bản ghi bị xóa, Toast thông báo và bảng cập nhật.

---

### User Story 3 - Tìm kiếm và lọc file (Priority: P2)

Người dùng cần tìm kiếm file theo tên và lọc theo nhóm file để tra cứu nhanh trong danh sách lớn.

**Why this priority**: Nâng cao trải nghiệm người dùng khi dữ liệu file tăng lên, nhưng không bắt buộc cho MVP ban đầu.

**Independent Test**: Tạo nhiều file thuộc các nhóm khác nhau, sau đó sử dụng ô tìm kiếm và dropdown lọc nhóm để xác nhận kết quả hiển thị đúng.

**Acceptance Scenarios**:

1. **Given** danh sách file đang hiển thị, **When** người dùng nhập từ khóa vào ô tìm kiếm, **Then** bảng chỉ hiển thị các file có tên chứa từ khóa đó (tìm kiếm không phân biệt hoa/thường).
2. **Given** danh sách file đang hiển thị, **When** người dùng chọn một nhóm file từ dropdown lọc, **Then** bảng chỉ hiển thị các file thuộc nhóm đã chọn.
3. **Given** người dùng đã áp dụng cả tìm kiếm lẫn lọc nhóm, **When** họ xóa bộ lọc (clear), **Then** bảng hiển thị lại toàn bộ danh sách file.

---

### Edge Cases

- Khi người dùng nhập link file không bắt đầu bằng `http://` hoặc `https://`, hệ thống hiển thị lỗi validation tại form.
- Khi xóa nhóm file đang được tham chiếu bởi bản ghi file, hệ thống từ chối và thông báo lỗi.
- Khi không có nhóm file nào active, dropdown nhóm trên form tạo file hiển thị trống và người dùng không thể lưu file mà chưa chọn nhóm.
- Khi danh sách file rỗng, bảng hiển thị thông báo "Chưa có dữ liệu".
- Khi nhóm file chuyển sang Inactive, các file thuộc nhóm đó bị ẩn khỏi danh sách mặc định; người dùng bật toggle để xem lại.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Hệ thống PHẢI cho phép người dùng tạo nhóm file với các trường: tên nhóm (bắt buộc, tối đa 100 ký tự), mô tả (không bắt buộc, tối đa 255 ký tự), trạng thái (Active/Inactive, mặc định Active).
- **FR-002**: Hệ thống PHẢI cho phép sửa tên nhóm, mô tả và trạng thái của nhóm file.
- **FR-003**: Hệ thống PHẢI cho phép xóa nhóm file, với điều kiện nhóm không có bản ghi file nào tham chiếu đến. Khi từ chối xóa, thông báo lỗi PHẢI hiển thị số lượng file đang tham chiếu.
- **FR-004**: Hệ thống PHẢI hiển thị danh sách nhóm file dưới dạng bảng với các cột: Tên nhóm, Mô tả, Trạng thái, Số file.
- **FR-005**: Hệ thống PHẢI cho phép tạo bản ghi file với các trường: tên file (bắt buộc, tối đa 200 ký tự), link file (bắt buộc, phải bắt đầu bằng `http://` hoặc `https://`), nhóm file (bắt buộc, chọn từ danh sách nhóm active).
- **FR-006**: Hệ thống PHẢI tự động ghi nhận ngày tạo và người tạo khi thêm bản ghi file mới.
- **FR-007**: Hệ thống PHẢI cho phép sửa tên file, link file và nhóm file của bản ghi.
- **FR-008**: Hệ thống PHẢI cho phép xóa bản ghi file sau khi người dùng xác nhận qua dialog.
- **FR-009**: Hệ thống PHẢI hỗ trợ tìm kiếm bản ghi file theo tên (không phân biệt hoa/thường).
- **FR-010**: Hệ thống PHẢI hỗ trợ lọc bản ghi file theo nhóm file qua dropdown.
- **FR-011**: Link file trong bảng PHẢI mở trong tab mới khi người dùng click.
- **FR-012**: Hệ thống PHẢI hiển thị thông báo Toast khi thao tác thêm/sửa/xóa thành công hoặc thất bại.
- **FR-013**: Hệ thống PHẢI có menu "Sổ tay trung tâm" trên sidebar với 2 mục con: "Quản lý nhóm file" và "Danh mục file".
- **FR-014**: Tên nhóm file PHẢI là duy nhất trong hệ thống.
- **FR-015**: Toàn bộ API và màn hình của module PHẢI yêu cầu permission `MANAGE_HANDBOOK`. Người dùng không có permission này sẽ không thấy menu và không truy cập được API.
- **FR-016**: Màn hình "Danh mục file" mặc định chỉ hiển thị file thuộc nhóm Active. PHẢI có toggle (ví dụ: checkbox "Hiển thị cả nhóm ngưng hoạt động") để người dùng xem lại các file thuộc nhóm Inactive.
- **FR-017**: Migration PHẢI tạo dữ liệu seed cho các nhóm file mặc định: "Kế hoạch tháng", "Báo cáo", "Quy trình" (trạng thái Active).

### Key Entities

- **Nhóm file (FileGroup)**: Đại diện cho một danh mục phân loại tài liệu. Gồm tên nhóm (unique), mô tả, trạng thái hoạt động. Quan hệ 1-N với Bản ghi file.
- **Bản ghi file (FileRecord)**: Đại diện cho một tài liệu/liên kết được lưu trữ. Gồm tên file, link URL, nhóm file (thuộc đúng một FileGroup — quan hệ N-1, mỗi file chỉ thuộc 1 nhóm duy nhất), ngày tạo, người tạo.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Người dùng có thể tạo nhóm file và bản ghi file hoàn chỉnh trong vòng 1 phút.
- **SC-002**: Người dùng tìm thấy file cần tìm trong danh sách 100+ bản ghi trong vòng 10 giây bằng tính năng tìm kiếm/lọc.
- **SC-003**: 100% link file mở đúng URL trong tab mới khi click.
- **SC-004**: Mọi thao tác CRUD đều có phản hồi (Toast notification) trong vòng 2 giây.
- **SC-005**: Không thể xóa nhóm file đang được sử dụng — hệ thống từ chối 100% các trường hợp này.

## Clarifications

### Session 2026-05-07

- Q: Mô hình permission nào phù hợp cho module này? → A: Một permission duy nhất `MANAGE_HANDBOOK` cho toàn bộ CRUD của cả nhóm file lẫn bản ghi file.
- Q: Khi nhóm file bị chuyển sang Inactive, các bản ghi file thuộc nhóm đó sẽ thế nào? → A: Ẩn khỏi danh sách mặc định, có toggle để xem lại.
- Q: Mức độ validation URL cho link file? → A: Chỉ kiểm tra format bắt đầu bằng `http://` hoặc `https://`, không verify link sống.
- Q: Link file có bắt buộc bắt đầu bằng http/https? → A: Có, xác nhận validate format URL (đã phản ánh tại FR-005).
- Q: Khi xóa nhóm file còn chứa file bên trong? → A: Từ chối xóa, yêu cầu chuyển file sang nhóm khác trước (đã phản ánh tại FR-003).
- Q: Một file có thể thuộc nhiều nhóm không? → A: Không, mỗi file thuộc đúng 1 nhóm (quan hệ N-1).
- Q: Dữ liệu ban đầu cho các nhóm file? → A: Seed data qua migration với các nhóm gợi ý: "Kế hoạch tháng", "Báo cáo", "Quy trình".

## Assumptions

- Module "Sổ tay trung tâm" sử dụng hệ thống xác thực và phân quyền đã có (Feature 006 - User Auth & RBAC). Toàn bộ module được bảo vệ bởi một permission duy nhất: `MANAGE_HANDBOOK`.
- "Link file" là URL bên ngoài (ví dụ: Google Drive, SharePoint, nội bộ) — hệ thống chỉ lưu URL, không upload file.
- Tông màu đỏ chủ đạo và icon PrimeIcons sẽ tuân theo design system hiện tại của ứng dụng.
- Danh sách file sử dụng phân trang phía server khi dữ liệu lớn.
- Migration database dùng Flyway với version tiếp theo sau V9 (hiện tại).
- Người dùng truy cập qua trình duyệt desktop với kết nối internet ổn định.
- Mỗi bản ghi file chỉ thuộc đúng một nhóm file (không hỗ trợ multi-group).
