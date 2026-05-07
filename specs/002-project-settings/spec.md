# Feature Specification: Cài Đặt Dự Án (Project Settings)

**Feature Branch**: `002-project-settings`
**Created**: 2026-05-07
**Status**: Draft
**Governing Constitution**: Hiến Chương Hệ Thống Quản Lý Dự Án Nội Bộ (v1.0.0)

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 – Quản Lý Danh Sách Dự Án (Priority: P1)

Là một người dùng hệ thống, tôi muốn có thể xem danh sách tất cả dự án, tạo dự án mới, chỉnh sửa và xóa dự án, để quản lý toàn bộ danh mục dự án của tổ chức trong một màn hình duy nhất.

**Why this priority**: Đây là chức năng cốt lõi của feature — không có danh sách dự án thì các chức năng cấu hình khác không có ý nghĩa. Tất cả các screen khác trong hệ thống đều phụ thuộc vào dữ liệu dự án.

**Independent Test**: Mở màn hình Cài đặt dự án → thấy danh sách dự án dạng bảng → tạo một dự án mới với đầy đủ thông tin → dự án xuất hiện trong bảng → chỉnh sửa tên dự án → thấy tên mới trong bảng → xóa dự án → dự án biến mất khỏi bảng.

**Acceptance Scenarios**:

1. **Given** người dùng mở màn hình Cài đặt dự án, **When** trang tải xong, **Then** hiển thị giao diện 3 tab ("Dự án", "Loại dự án", "Khách hàng") với tab "Dự án" được chọn mặc định, hiển thị bảng danh sách dự án với các cột: mã dự án, tên dự án, loại dự án, khách hàng, trạng thái hợp đồng, trạng thái dự án, tháng bắt đầu, tháng kết thúc.
2. **Given** người dùng bấm "Tạo dự án", **When** điền đầy đủ thông tin hợp lệ và xác nhận, **Then** dự án mới xuất hiện trong bảng và hệ thống thông báo tạo thành công.
3. **Given** người dùng chỉnh sửa một dự án, **When** thay đổi bất kỳ trường nào và lưu, **Then** thông tin mới hiển thị ngay trong bảng.
4. **Given** một dự án chưa có dữ liệu tháng, **When** người dùng xóa dự án và xác nhận, **Then** dự án bị xóa khỏi bảng.
5. **Given** một dự án đã có dữ liệu tháng, **When** người dùng bấm xóa, **Then** hệ thống hiển thị cảnh báo rõ ràng về dữ liệu liên quan và yêu cầu xác nhận bổ sung trước khi thực hiện soft delete.
6. **Given** người dùng nhập mã dự án đã tồn tại, **When** lưu form, **Then** hệ thống báo lỗi "Mã dự án đã tồn tại" và không tạo/cập nhật dự án.
7. **Given** người dùng nhập tháng kết thúc nhỏ hơn tháng bắt đầu, **When** lưu form, **Then** hệ thống báo lỗi validation ngay trên form.
8. **Given** người dùng nhập đơn giá âm, **When** lưu form, **Then** hệ thống báo lỗi "Đơn giá phải là số không âm".

---

### User Story 2 – Quản Lý Loại Dự Án (Priority: P2)

Là một người dùng hệ thống, tôi muốn quản lý danh sách các loại dự án dạng key-value, để có thể chọn loại dự án phù hợp khi tạo/sửa dự án.

**Why this priority**: Loại dự án là dữ liệu lookup cần có trước khi tạo dự án. Tuy nhiên, có thể hardcode tạm một số loại mặc định để US1 vẫn hoạt động được, nên US2 là P2.

**Independent Test**: Mở màn hình Cấu hình loại dự án → tạo loại mới với key và value → loại xuất hiện trong danh sách → mở form tạo dự án và thấy loại mới trong dropdown → xóa loại đang không được dùng → biến mất khỏi danh sách.

**Acceptance Scenarios**:

1. **Given** người dùng mở màn hình cấu hình loại dự án, **When** trang tải, **Then** hiển thị danh sách tất cả loại dự án với cột key và value.
2. **Given** người dùng tạo loại dự án mới, **When** nhập key và value hợp lệ và lưu, **Then** loại mới xuất hiện trong danh sách.
3. **Given** người dùng nhập key đã tồn tại, **When** lưu, **Then** hệ thống báo lỗi "Key loại dự án đã tồn tại".
4. **Given** loại dự án đang được ít nhất một dự án sử dụng, **When** người dùng cố xóa, **Then** hệ thống hiển thị cảnh báo "Loại dự án đang được sử dụng, không thể xóa" và không thực hiện xóa.
5. **Given** loại dự án chưa được dùng, **When** người dùng xóa và xác nhận, **Then** loại bị xóa khỏi danh sách.

---

### User Story 3 – Quản Lý Khách Hàng (Priority: P3)

Là một người dùng hệ thống, tôi muốn quản lý danh sách khách hàng với tên và mã, để có thể chọn khách hàng phù hợp khi tạo/sửa dự án.

**Why this priority**: Tương tự loại dự án, khách hàng là lookup data. Có thể tạm thời nhập tên thủ công, nên P3.

**Independent Test**: Mở màn hình cấu hình khách hàng → tạo khách hàng mới với tên và mã → khách hàng xuất hiện trong danh sách → mở form tạo dự án và thấy khách hàng mới trong dropdown → xóa khách hàng không được dùng → biến mất khỏi danh sách.

**Acceptance Scenarios**:

1. **Given** người dùng mở màn hình cấu hình khách hàng, **When** trang tải, **Then** hiển thị danh sách tất cả khách hàng với cột tên và mã.
2. **Given** người dùng tạo khách hàng mới, **When** nhập tên và mã hợp lệ và lưu, **Then** khách hàng mới xuất hiện trong danh sách.
3. **Given** người dùng nhập mã khách hàng đã tồn tại, **When** lưu, **Then** hệ thống báo lỗi "Mã khách hàng đã tồn tại".
4. **Given** khách hàng đang được ít nhất một dự án sử dụng, **When** người dùng cố xóa, **Then** hệ thống hiển thị cảnh báo "Khách hàng đang được sử dụng, không thể xóa" và không thực hiện xóa.
5. **Given** khách hàng chưa được dùng, **When** người dùng xóa và xác nhận, **Then** khách hàng bị xóa khỏi danh sách.

---

### Edge Cases

- Điều gì xảy ra nếu xóa dự án đã có dữ liệu tháng? → Không xóa vật lý; hiển thị cảnh báo + yêu cầu xác nhận bổ sung; thực hiện soft delete. Dự án ẩn hoàn toàn khỏi danh sách, không có chức năng restore trong feature này.
- Điều gì xảy ra nếu xóa loại dự án/khách hàng đang được dùng? → Hệ thống từ chối xóa và hiển thị thông báo rõ lý do.
- Điều gì xảy ra nếu nhập tháng sai định dạng (không phải mm/yyyy)? → Form validation tức thì, không cho submit.
- Điều gì xảy ra nếu người dùng bỏ trống trường bắt buộc? → Form validation highlight trường bị thiếu, không cho submit.
- Điều gì xảy ra với đơn giá có nhiều chữ số thập phân? → Không giới hạn số chữ số thập phân ở spec này; quyết định chính xác sẽ trong plan.
- Điều gì xảy ra nếu danh sách dự án rất dài? → Hiển thị phân trang hoặc scroll dài; quyết định trong plan.
- Điều gì xảy ra nếu người dùng đang sửa form và mất kết nối mạng? → Error toast từ Axios interceptor (đã có từ feature 001).

---

## Requirements *(mandatory)*

### Functional Requirements

**Quản lý Dự Án:**

- **FR-PRJ-001**: Hệ thống PHẢI cung cấp màn hình danh sách dự án hiển thị tất cả dự án dạng bảng với ít nhất các cột: mã dự án, tên dự án, loại dự án, khách hàng, trạng thái hợp đồng, trạng thái dự án, tháng bắt đầu, tháng kết thúc.
- **FR-PRJ-002**: Người dùng PHẢI có thể tạo dự án mới với đầy đủ 9 trường — tất cả đều bắt buộc (NOT NULL): tên dự án, mã dự án, khách hàng, loại dự án, đơn giá (mặc định `0`), trạng thái hợp đồng, trạng thái dự án, tháng bắt đầu hợp đồng, tháng kết thúc hợp đồng. Trường người đại diện (`representId`) KHÔNG hiển thị trên form trong feature này — được lưu là `null` cho đến khi feature quản lý nhân sự được xây dựng.
- **FR-PRJ-003**: Người dùng PHẢI có thể chỉnh sửa tất cả trường của một dự án đã tồn tại.
- **FR-PRJ-004**: Hệ thống PHẢI từ chối lưu nếu mã dự án vi phạm một trong các rule sau: (a) trùng với mã đã tồn tại (so sánh case-insensitive), (b) chứa ký tự ngoài `[A-Za-z0-9_-]`, (c) độ dài vượt quá 50 ký tự hoặc nhỏ hơn 1 ký tự.
- **FR-PRJ-005**: Hệ thống PHẢI từ chối lưu nếu tháng kết thúc hợp đồng nhỏ hơn tháng bắt đầu hợp đồng.
- **FR-PRJ-006**: Hệ thống PHẢI từ chối lưu nếu đơn giá là số âm. Giá trị mặc định là `0` khi người dùng không nhập.
- **FR-PRJ-007**: Trạng thái hợp đồng PHẢI là một trong: `NO_CONTRACT` (Chưa có hợp đồng — tương ứng `0` trong Constitution §8.2) hoặc `HAS_CONTRACT` (Có hợp đồng — tương ứng `1`). Java enum `StatusContract` được lưu dưới dạng chuỗi trong PostgreSQL; FE hiển thị nhãn người dùng là "Chưa có hợp đồng" / "Có hợp đồng".
- **FR-PRJ-008**: Trạng thái dự án PHẢI là một trong 5 giá trị: `OPEN`, `INPROGRESS`, `PENDING`, `DONE`, `CLOSE`.
- **FR-PRJ-009**: Khi xóa dự án, hệ thống PHẢI thực hiện soft delete (đánh dấu `deleted = true`) sau khi người dùng xác nhận — không có xóa vật lý trong feature này.
- **FR-PRJ-010**: Khi xóa dự án đã có dữ liệu tháng, hệ thống PHẢI hiển thị cảnh báo rõ ràng nêu lý do không thể xóa vật lý và thực hiện soft delete (đánh dấu `deleted = true`) sau khi người dùng xác nhận bổ sung. Dự án soft-deleted được ẩn hoàn toàn khỏi danh sách — không có chức năng restore trong feature này.
- **FR-PRJ-011**: Tháng bắt đầu và tháng kết thúc PHẢI theo định dạng `mm/yyyy` với các rule: (a) tháng là 2 chữ số có padding trong khoảng `01`–`12`, (b) năm là 4 chữ số `≥ 2000`, (c) regex tham chiếu: `^(0[1-9]|1[0-2])/[2-9][0-9]{3}$`. Ví dụ hợp lệ: `01/2026`, `12/2099`. Ví dụ không hợp lệ: `1/2026`, `00/2026`, `13/2026`.
- **FR-PRJ-012**: `statusProject` và `statusContract` PHẢI được phép sửa tự do bất kỳ lúc nào khi cập nhật dự án — không có ràng buộc transition trạng thái trong feature này.

**Quản lý Loại Dự Án:**

- **FR-PT-001**: Hệ thống PHẢI cung cấp màn hình quản lý loại dự án hiển thị danh sách tất cả loại dự án chưa bị xóa (`deleted = false`) với cột key và value.
- **FR-PT-002**: Người dùng PHẢI có thể tạo loại dự án mới với key và value. Loại dự án mới có `deleted = false` mặc định.
- **FR-PT-003**: Người dùng PHẢI có thể sửa key và value của một loại dự án.
- **FR-PT-004**: Hệ thống PHẢI từ chối lưu nếu key loại dự án vi phạm một trong các rule sau: (a) trùng với key đã tồn tại (so sánh case-insensitive), (b) chứa ký tự ngoài `[A-Za-z0-9_-]`, (c) độ dài vượt quá 50 ký tự hoặc nhỏ hơn 1 ký tự.
- **FR-PT-005**: Khi người dùng xóa loại dự án, hệ thống PHẢI thực hiện soft delete (đánh dấu `deleted = true`) sau khi xác nhận — không có xóa vật lý. Nếu loại dự án đang được ít nhất một dự án sử dụng, hiển thị cảnh báo bổ sung trước khi xác nhận.
- **FR-PT-006**: Loại dự án đã soft-deleted (`deleted = true`) ẩn khỏi danh sách quản lý và không xuất hiện trong dropdown chọn loại dự án khi tạo/sửa dự án mới.
- **FR-PT-007**: Dropdown chọn loại dự án trong form tạo/sửa dự án CHỈ hiển thị các loại dự án có `deleted = false`.
- **FR-PT-008**: Dự án đã tồn tại vẫn hiển thị đúng loại dự án của mình dù loại đó đã soft-deleted — tên/key được resolve từ DB, không bị ẩn.

**Quản lý Khách Hàng:**

- **FR-CUS-001**: Hệ thống PHẢI cung cấp màn hình quản lý khách hàng hiển thị danh sách tất cả khách hàng chưa bị xóa (`deleted = false`) với cột tên và mã.
- **FR-CUS-002**: Người dùng PHẢI có thể tạo khách hàng mới với tên và mã. Khách hàng mới có `deleted = false` mặc định.
- **FR-CUS-003**: Người dùng PHẢI có thể sửa tên và mã của một khách hàng.
- **FR-CUS-004**: Hệ thống PHẢI từ chối lưu nếu mã khách hàng vi phạm một trong các rule sau: (a) trùng với mã đã tồn tại (so sánh case-insensitive), (b) chứa ký tự ngoài `[A-Za-z0-9_-]`, (c) độ dài vượt quá 50 ký tự hoặc nhỏ hơn 1 ký tự.
- **FR-CUS-005**: Khi người dùng xóa khách hàng, hệ thống PHẢI thực hiện soft delete (đánh dấu `deleted = true`) sau khi xác nhận — không có xóa vật lý. Nếu khách hàng đang được ít nhất một dự án sử dụng, hiển thị cảnh báo bổ sung trước khi xác nhận.
- **FR-CUS-006**: Khách hàng đã soft-deleted (`deleted = true`) ẩn khỏi danh sách quản lý và không xuất hiện trong dropdown chọn khách hàng khi tạo/sửa dự án mới.
- **FR-CUS-007**: Dropdown chọn khách hàng trong form tạo/sửa dự án CHỈ hiển thị các khách hàng có `deleted = false`.
- **FR-CUS-008**: Dự án đã tồn tại vẫn hiển thị đúng tên/mã khách hàng của mình dù khách hàng đó đã soft-deleted — thông tin được resolve từ DB, không bị ẩn.

### Key Entities

- **Project (Dự Án)**: Thực thể trung tâm của feature. Thuộc tính: `id` (UUID), `projectCode` (NOT NULL, unique, `[A-Za-z0-9_-]`, max 50 chars, case-insensitive unique), `projectName` (NOT NULL), `representId` (UUID, nullable — ẩn khỏi UI, để null cho đến khi có feature nhân sự), `customerId` (NOT NULL, FK → Customer), `projectTypeKey` (NOT NULL, FK → ProjectType), `price` (NOT NULL, BigDecimal, ≥ 0, default `0`), `statusContract` (NOT NULL, `NO_CONTRACT` | `HAS_CONTRACT` — xem §8.2 Constitution), `statusProject` (NOT NULL, OPEN | INPROGRESS | PENDING | DONE | CLOSE), `monthStart` (NOT NULL, mm/yyyy), `monthEnd` (NOT NULL, mm/yyyy), `deleted` (NOT NULL, boolean default false), `createdAt`, `updatedAt`.

- **ProjectType (Loại Dự Án)**: Danh mục loại dự án dạng key-value. Thuộc tính: `id` (UUID), `key` (NOT NULL, unique, `[A-Za-z0-9_-]`, max 50 chars, case-insensitive unique), `value` (NOT NULL), `deleted` (NOT NULL, boolean, default `false`).

- **Customer (Khách Hàng)**: Danh mục khách hàng. Thuộc tính: `id` (UUID), `customerCode` (NOT NULL, unique, `[A-Za-z0-9_-]`, max 50 chars, case-insensitive unique), `customerName` (NOT NULL), `deleted` (NOT NULL, boolean, default `false`).

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Người dùng có thể tạo một dự án mới từ màn hình danh sách trong vòng 3 phút, bao gồm cả việc chọn loại dự án và khách hàng từ dropdown.
- **SC-002**: 100% các trường hợp vi phạm validation (mã trùng, ngày sai, giá âm) đều hiển thị thông báo lỗi rõ ràng tại vị trí trường bị lỗi — không có lỗi nào bị nuốt im lặng.
- **SC-003**: Thao tác tạo/sửa/xóa dự án cho kết quả trong vòng 2 giây trong điều kiện bình thường.
- **SC-004**: Hệ thống bảo vệ toàn vẹn dữ liệu: không một loại dự án hay khách hàng nào đang được dùng bị xóa thành công.
- **SC-005**: Người dùng không bao giờ mất dữ liệu tháng do xóa dự án — soft delete đảm bảo 100% dữ liệu tháng vẫn tồn tại trong DB sau khi "xóa" dự án.
- **SC-006**: Danh sách dự án, loại dự án, khách hàng tải xong và hiển thị trong vòng 2 giây.

---

## Assumptions

- Người đại diện (`representId`) ẩn khỏi form UI trong feature này — cột tồn tại trong DB (nullable UUID, không có FK constraint) và sẽ được hiển thị + ràng buộc FK khi feature quản lý nhân sự được xây dựng.
- Không có phân quyền/xác thực trong feature này — tất cả người dùng có thể xem, tạo, sửa, xóa.
- Không xử lý công thức tháng (opening balance, closing balance) trong feature này — chỉ lưu metadata dự án.
- "Có dữ liệu tháng" có nghĩa là tồn tại ít nhất một bản ghi trong bảng monthly data liên kết với dự án đó — bảng này chưa tồn tại trong feature này, nên logic kiểm tra sẽ luôn trả về "chưa có dữ liệu tháng" (xóa vật lý được phép) cho đến khi feature monthly data được xây dựng.
- Phân trang cho danh sách dự án: hiển thị tất cả (không phân trang) nếu số lượng nhỏ; thêm phân trang khi cần thiết trong plan.
- Màn hình `/project-settings` sử dụng layout 3 tab: tab 1 "Dự án", tab 2 "Loại dự án", tab 3 "Khách hàng". Không có route con — tất cả nằm trong cùng một Vue view (`ProjectSettingsView.vue`).
- Giao diện sử dụng Dialog (modal) cho form tạo/sửa và Confirm Dialog cho xóa — theo pattern constitution §5.3.
- Trường `monthStart` và `monthEnd` được lưu dạng chuỗi `mm/yyyy` trong database (không phải Date type) để đơn giản hóa.

---

## Clarifications

### Session 2026-05-07

- Q: Giá trị enum `statusProject` là gì? (Constitution §8.1 có INPROGRESS/PENDING/CLOSE; yêu cầu mới có Open/Inprogress/Done/Close) → A: Gộp tất cả — `OPEN, INPROGRESS, PENDING, DONE, CLOSE` (5 giá trị). Constitution §8.1 cần cập nhật để bổ sung `OPEN` và `DONE`.
- Q: Trường người đại diện (`representId`) hiển thị thế nào trên form UI khi bảng nhân sự chưa tồn tại? → A: Ẩn khỏi form hoàn toàn — cột tồn tại trong DB (nullable), sẽ hiển thị khi feature nhân sự được xây dựng.
- Q: Bố cục màn hình "Cài đặt dự án" cho 3 phần (dự án, loại dự án, khách hàng) là gì? → A: 1 trang `/project-settings` với 3 tab — "Dự án", "Loại dự án", "Khách hàng". Tab "Dự án" mặc định. Không có route con.
- Q: Sau khi soft delete dự án có dữ liệu tháng, dự án đó hiển thị thế nào trong danh sách? Có chức năng restore không? → A: Ẩn hoàn toàn khỏi danh sách. Không có chức năng restore trong feature này.
- Q: Trường nào bắt buộc khi tạo dự án? `price` mặc định là bao nhiêu? → A: Tất cả 9 trường bắt buộc (NOT NULL). `price` mặc định là `0`.
- Q: Khi Customer/ProjectType đang được dùng bị "xóa", hành vi là gì? Project cũ có còn hiển thị không? → A: Soft delete (`deleted = true`), không xóa vật lý. Dropdown mới chỉ hiện `deleted = false`. Project cũ vẫn resolve và hiển thị đúng customer/project type dù đã soft-deleted.
- Q: Validate định dạng mã dự án, mã khách hàng, key loại dự án như thế nào ngoài unique? → A: Chỉ chứa `[A-Za-z0-9_-]`, tối đa 50 ký tự, kiểm tra trùng case-insensitive.
- Q: Validate định dạng `mm/yyyy` cho monthStart/monthEnd: tháng và năm có giới hạn giá trị không? → A: Tháng `01`–`12` (padding bắt buộc), năm 4 chữ số `≥ 2000`. Regex: `^(0[1-9]|1[0-2])/[2-9][0-9]{3}$`.
- Q: Quy tắc xóa tổng hợp: khi nào xóa vật lý, khi nào soft delete? → A: Không có xóa vật lý nào trong toàn bộ feature. Tất cả (Project, Customer, ProjectType) đều dùng soft delete (`deleted = true`). Xóa có cảnh báo bổ sung nếu entity đang được sử dụng.
- Q: `statusProject` và `statusContract` có cho sửa sau khi tạo không? Có ràng buộc transition không? → A: Tự do sửa cả hai trường bất kỳ lúc nào. Không có ràng buộc transition trong feature này.
