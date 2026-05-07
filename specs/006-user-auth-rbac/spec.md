# Feature Specification: Quản Lý Người Dùng, Phân Quyền và Đăng Nhập

**Feature Branch**: `006-user-auth-rbac`
**Created**: 2026-05-07
**Status**: Draft
**Input**: User description: "Xây dựng feature 006: Quản lý Người dùng, Phân quyền (Role-based Access Control) và Đăng nhập/Đăng xuất."

---

## Background

Hệ thống hiện tại không có cơ chế xác thực hay phân quyền — mọi người đều có thể truy cập toàn bộ dữ liệu và thao tác. Feature này bổ sung:

1. **Xác thực (Authentication)**: Người dùng đăng nhập bằng tên đăng nhập và mật khẩu, nhận token phiên làm việc, đăng xuất để huỷ token.
2. **Phân quyền (Authorization)**: Mỗi user được gán một Role. Mỗi Role có tập hợp quyền (Permissions) xác định màn hình và thao tác được phép.
3. **Liên kết dữ liệu**: Trường "Người đại diện" của dự án (hiện là text tự do) được chuyển thành tham chiếu đến bảng User.

---

## Clarifications

### Session 2026-05-07

- Q: Thuật toán mã hóa mật khẩu là gì? → A: BCrypt.
- Q: Dữ liệu Permission được seed như thế nào? → A: Seed sẵn qua Flyway migration, ví dụ: `VIEW_DASHBOARD`, `MANAGE_PROJECT`, `MANAGE_USER`, `MANAGE_ROLE`, `SYSTEM_SETTINGS`.
- Q: Hành vi khi Role chuyển sang Inactive với User đang dùng Role đó? → A: User không thể đăng nhập và nhận thông báo lỗi rõ ràng; trước khi xóa mềm hệ thống hiển thị cảnh báo số User bị ảnh hưởng.
- Q: Hành vi migration trường "Người đại diện" với dữ liệu cũ không resolve được? → A: Gán null — không có auto-match tên cũ sang User mới.

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Đăng Nhập và Bảo Vệ Màn Hình (Priority: P1) 🎯 MVP

Người dùng truy cập hệ thống và được yêu cầu đăng nhập trước khi xem bất kỳ nội dung nào. Sau khi đăng nhập thành công, hệ thống chuyển hướng đến Dashboard. Khi token hết hạn hoặc người dùng đăng xuất, hệ thống yêu cầu đăng nhập lại.

**Why this priority**: Không có xác thực thì mọi tính năng bảo mật khác đều vô nghĩa. Đây là điều kiện tiên quyết cho toàn bộ feature.

**Independent Test**: Truy cập URL bất kỳ khi chưa đăng nhập → bị chuyển về trang Login. Đăng nhập đúng thông tin → chuyển về Dashboard. Đăng xuất → bị chuyển về trang Login. Truy cập URL protected khi không có token → bị chuyển về Login.

**Acceptance Scenarios**:

1. **Given** người dùng chưa đăng nhập, **When** truy cập bất kỳ route protected nào, **Then** hệ thống redirect về trang Login.
2. **Given** người dùng ở trang Login, **When** nhập đúng tên đăng nhập và mật khẩu, **Then** nhận token phiên và được redirect về Dashboard.
3. **Given** người dùng nhập sai tên đăng nhập hoặc mật khẩu, **When** nhấn Đăng nhập, **Then** hệ thống hiển thị thông báo lỗi rõ ràng, không tiết lộ thông tin nào về tài khoản.
4. **Given** người dùng đang đăng nhập, **When** nhấn Đăng xuất, **Then** token bị huỷ, người dùng bị redirect về trang Login, không thể truy cập API bằng token cũ.
5. **Given** token của người dùng đã hết hạn, **When** thực hiện bất kỳ thao tác nào, **Then** hệ thống tự động redirect về trang Login và hiển thị thông báo phiên hết hạn.
6. **Given** người dùng đăng nhập thành công, **When** truy cập route mà Role của họ không có quyền, **Then** hệ thống redirect về trang 403 hoặc Dashboard thay vì hiển thị dữ liệu.

---

### User Story 2 - Quản Lý Role và Phân Quyền (Priority: P2)

Quản trị viên tạo các Role (ví dụ: "Quản trị viên", "Nhân viên", "Xem báo cáo"), gán tập hợp quyền cho từng Role thông qua giao diện dạng cây. Mỗi quyền đại diện cho một màn hình hoặc nhóm thao tác. Role có thể bị tắt (inactive) mà không xóa hẳn.

**Why this priority**: Role là nền tảng của mô hình phân quyền. Quản lý User (US3) phụ thuộc vào danh sách Role đang active.

**Independent Test**: Tạo Role mới với tên và mô tả → lưu thành công. Mở dialog Phân quyền → chọn một số quyền trong cây → lưu → mở lại dialog xác nhận các quyền đã được tích. Tắt Role → Role không còn hiển thị trong danh sách chọn khi tạo User.

**Acceptance Scenarios**:

1. **Given** admin ở màn hình Quản lý Role, **When** nhấn Tạo mới và điền tên + mô tả, **Then** Role được tạo với trạng thái Active.
2. **Given** admin nhấn icon Phân quyền trên một Role, **When** giao diện cây quyền mở ra, **Then** toàn bộ cây quyền hiển thị với trạng thái đã chọn/chưa chọn đúng theo cấu hình hiện tại của Role đó.
3. **Given** admin đang trong dialog Phân quyền, **When** tích/bỏ tích các quyền và nhấn Lưu, **Then** cấu hình quyền của Role được cập nhật, người dùng đang sử dụng Role đó nhận quyền mới khi token được làm mới.
4. **Given** admin nhấn Xóa mềm một Role, **When** xác nhận, **Then** Role chuyển trạng thái thành Inactive, không còn khả dụng khi tạo/sửa User; User đang dùng Role này không bị xoá nhưng **không thể đăng nhập** cho đến khi được admin gán Role Active mới.
5. **Given** admin nhấn Sửa một Role, **When** cập nhật tên hoặc mô tả, **Then** thay đổi được lưu lại.
6. **Given** admin cố xóa mềm Role đang có User active sử dụng, **When** nhấn xác nhận, **Then** hệ thống hiển thị cảnh báo số User bị ảnh hưởng; sau khi xác nhận lần hai, Role chuyển Inactive và toàn bộ User thuộc Role đó không thể đăng nhập cho đến khi admin gán Role Active mới.

---

### User Story 3 - Quản Lý Người Dùng (Priority: P3)

Quản trị viên tạo, sửa, xem danh sách người dùng trong hệ thống. Mỗi người dùng bắt buộc phải được gán một Role. Mật khẩu được lưu trữ an toàn — không thể đọc lại, chỉ có thể đặt lại. Người dùng có thể bị vô hiệu hoá (inactive).

**Why this priority**: Sau khi có Role, cần quản lý User để hoàn thiện vòng đời xác thực/phân quyền.

**Independent Test**: Tạo User mới với username, email, mật khẩu, gán Role → đăng nhập bằng tài khoản đó thành công. Sửa thông tin User → thay đổi được lưu. Vô hiệu hoá User → đăng nhập bằng tài khoản đó thất bại với thông báo tài khoản bị khóa.

**Acceptance Scenarios**:

1. **Given** admin ở màn hình Quản lý User, **When** nhấn Tạo mới và điền đầy đủ thông tin (username, email, mật khẩu, Role), **Then** User được tạo và có thể đăng nhập ngay.
2. **Given** admin tạo User mới, **When** nhập mật khẩu, **Then** mật khẩu được mã hóa trước khi lưu — không có API nào trả về mật khẩu dạng plain text.
3. **Given** admin nhấn Sửa một User, **When** thay đổi Role, **Then** thay đổi có hiệu lực từ lần đăng nhập tiếp theo của User đó.
4. **Given** admin nhấn Vô hiệu hoá User, **When** xác nhận, **Then** User không thể đăng nhập, hệ thống hiển thị thông báo tài khoản bị khóa thay vì lỗi đăng nhập sai.
5. **Given** admin tạo User, **When** nhập username hoặc email đã tồn tại, **Then** hệ thống báo lỗi trùng lặp.
6. **Given** admin tạo User không nhập mật khẩu, **When** nhấn Lưu, **Then** form validation hiển thị lỗi bắt buộc.

---

### User Story 4 - Cập Nhật Trường "Người Đại Diện" Dự Án (Priority: P4)

Trường "Người đại diện" của dự án (hiện là text tự do) được chuyển thành dropdown chọn từ danh sách User đang active trong hệ thống. Dữ liệu dự án hiện có cần được di chuyển không phá vỡ.

**Why this priority**: Đây là thay đổi cải tiến dữ liệu phụ thuộc vào US3 (User đã tồn tại). Có thể thực hiện sau khi hệ thống User hoạt động.

**Independent Test**: Mở form Cài đặt Dự án → trường "Người đại diện" hiển thị dạng dropdown → chọn một User từ danh sách → lưu → mở lại form → User đã chọn vẫn được hiển thị đúng.

**Acceptance Scenarios**:

1. **Given** admin mở form Cài đặt Dự án, **When** nhìn vào trường "Người đại diện", **Then** trường hiển thị dạng dropdown với danh sách các User đang active.
2. **Given** admin chọn một User từ dropdown, **When** lưu dự án, **Then** dự án được lưu với tham chiếu đến User đó.
3. **Given** dự án hiện có có giá trị text "Người đại diện" từ trước, **When** mở form sửa, **Then** trường hiển thị trống (null) — migration đã gán null cho tất cả giá trị cũ không resolve được, không có auto-match, không gây lỗi hệ thống.
4. **Given** một User bị vô hiệu hoá sau khi đã được gán vào dự án, **When** mở form sửa dự án, **Then** User đó vẫn hiển thị trong trường (không mất dữ liệu), nhưng dropdown chỉ cho phép chọn User đang active.

---

### Edge Cases

- Điều gì xảy ra khi database có nhiều User cùng username? → Username phải là unique constraint.
- Điều gì xảy ra khi token bị giả mạo hoặc sửa đổi? → Backend từ chối với 401, frontend redirect Login.
- Điều gì xảy ra khi toàn bộ admin bị vô hiệu hoá? → Cần ít nhất một tài khoản admin seed mặc định không thể bị xoá.
- Điều gì xảy ra khi người dùng cố truy cập API trực tiếp mà không qua frontend? → JWT middleware chặn với 401.
- Điều gì xảy ra khi session đang mở bị revoke từ phía admin? → Trong phạm vi feature này, token hiện tại vẫn hợp lệ đến khi hết hạn tự nhiên (stateless JWT). Revoke ngay lập tức là ngoài scope.

---

## Requirements *(mandatory)*

### Functional Requirements

#### Authentication
- **FR-001**: Hệ thống PHẢI cung cấp endpoint đăng nhập nhận username + password, trả về token phiên nếu thông tin đúng.
- **FR-002**: Hệ thống PHẢI từ chối đăng nhập với thông báo lỗi chung (không tiết lộ tài khoản có tồn tại không) khi thông tin sai.
- **FR-003**: Hệ thống PHẢI từ chối đăng nhập với thông báo "tài khoản bị khóa" khi User có trạng thái inactive.
- **FR-004**: Token phiên PHẢI có thời hạn. Sau khi hết hạn, mọi request phải bị từ chối với mã lỗi phiên hết hạn.
- **FR-005**: Hệ thống PHẢI cung cấp endpoint đăng xuất — phía client xoá token; phía server không cần blacklist (stateless).
- **FR-006**: Tất cả các endpoint nghiệp vụ (trừ endpoint đăng nhập) PHẢI yêu cầu token hợp lệ trong header request.

#### Role Management
- **FR-007**: Hệ thống PHẢI hỗ trợ CRUD đầy đủ cho Role (tạo, đọc, cập nhật, xóa mềm).
- **FR-008**: Role PHẢI có trạng thái active/inactive. Xóa là xóa mềm — dữ liệu không bị xoá vật lý. Khi Role chuyển sang Inactive, tất cả User thuộc Role đó **không thể đăng nhập** và nhận thông báo lỗi rõ ràng (phân biệt với lỗi sai mật khẩu) cho đến khi được gán Role Active mới.
- **FR-009**: Hệ thống PHẢI cung cấp danh sách quyền (Permissions) dưới dạng cây phân cấp (màn hình → nhóm thao tác → thao tác cụ thể). Dữ liệu Permission được seed sẵn qua Flyway migration; ví dụ tối thiểu: `VIEW_DASHBOARD`, `MANAGE_PROJECT`, `MANAGE_USER`, `MANAGE_ROLE`, `SYSTEM_SETTINGS`.
- **FR-010**: Hệ thống PHẢI hỗ trợ gán/thu hồi nhiều Permission cho một Role trong một lần thao tác.
- **FR-011**: Role đang có User sử dụng không thể bị xóa vật lý — chỉ có thể chuyển sang Inactive (xóa mềm). Hệ thống PHẢI hiển thị cảnh báo số lượng User bị ảnh hưởng và yêu cầu xác nhận hai bước trước khi thực hiện.

#### User Management
- **FR-012**: Hệ thống PHẢI hỗ trợ CRUD đầy đủ cho User.
- **FR-013**: Mỗi User PHẢI được gán đúng một Role tại mọi thời điểm (bắt buộc, không thể null).
- **FR-014**: Mật khẩu PHẢI được mã hóa bằng **BCrypt** trước khi lưu vào cơ sở dữ liệu. Mật khẩu dạng plain text không bao giờ được lưu hoặc trả về qua API.
- **FR-015**: Username và Email PHẢI là duy nhất trong toàn hệ thống.
- **FR-016**: User có trạng thái active/inactive. Inactive User không thể đăng nhập.
- **FR-017**: Phải có ít nhất một User với quyền quản trị được tạo sẵn (seed) khi khởi tạo hệ thống.

#### API Security
- **FR-018**: Hệ thống PHẢI kiểm tra quyền (Permission) của User dựa trên Role khi truy cập các endpoint được bảo vệ.
- **FR-019**: Request không có token hoặc token sai PHẢI bị từ chối với mã lỗi 401 Unauthorized.
- **FR-020**: Request có token hợp lệ nhưng không đủ quyền PHẢI bị từ chối với mã lỗi 403 Forbidden.

#### Project Representative Update
- **FR-021**: Trường "Người đại diện" của dự án PHẢI được chuyển từ text tự do sang tham chiếu đến User.
- **FR-022**: Dropdown "Người đại diện" CHỈ hiển thị User đang active.
- **FR-023**: Dữ liệu dự án hiện tại KHÔNG bị mất hay hỏng khi migration. Các dự án chưa có Người đại diện hoặc có giá trị text không resolve được sẽ được gán **null** trong Flyway migration. Không có auto-match tên text cũ sang User mới.

### Key Entities

- **User**: Tài khoản người dùng. Thuộc tính: username (unique), email (unique), mật khẩu (mã hóa), trạng thái (active/inactive), tên hiển thị, Role (bắt buộc).
- **Role**: Nhóm quyền được đặt tên. Thuộc tính: tên, mô tả, trạng thái (active/inactive), danh sách Permissions.
- **Permission**: Quyền truy cập cụ thể. Thuộc tính: mã quyền, tên hiển thị, nhóm cha (để tạo cây phân cấp), loại (màn hình / thao tác). Dữ liệu tĩnh, seed qua Flyway. Bộ quyền tối thiểu: `VIEW_DASHBOARD`, `MANAGE_PROJECT`, `MANAGE_USER`, `MANAGE_ROLE`, `SYSTEM_SETTINGS`.
- **Role_Permission**: Bảng trung gian liên kết Role với nhiều Permission.
- **Project** (cập nhật): Thêm tham chiếu đến User cho trường người đại diện (thay thế text tự do `represent_id`).

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Người dùng chưa đăng nhập không thể truy cập bất kỳ dữ liệu nghiệp vụ nào — 100% các route protected đều redirect về Login khi không có token.
- **SC-002**: Quản trị viên có thể tạo Role và gán quyền trong vòng 2 phút sử dụng giao diện cây.
- **SC-003**: Quản trị viên có thể tạo User mới và User đó có thể đăng nhập ngay lập tức sau khi tạo.
- **SC-004**: Không có mật khẩu plain text nào được lưu trong cơ sở dữ liệu hoặc xuất hiện trong response API.
- **SC-005**: Mọi request không hợp lệ hoặc không có quyền đều nhận được mã lỗi HTTP đúng (401 hoặc 403) trong vòng 500ms.
- **SC-006**: Dữ liệu dự án hiện tại không bị mất sau khi migration trường "Người đại diện".

---

## Assumptions

- Mô hình phân quyền là **flat RBAC**: một User có một Role, một Role có nhiều Permission — không có kế thừa Role hay phân cấp User.
- Token phiên là **stateless** (không lưu trên server). Revoke tức thời không được hỗ trợ trong phiên bản này.
- Thời hạn token: **8 giờ** cho access token — phù hợp với một ngày làm việc, không cần refresh token trong v1.
- Danh sách Permission là **dữ liệu tĩnh** được seed vào DB khi khởi tạo, không có UI CRUD cho Permission.
- Seed account mặc định: **username `admin`**, mật khẩu cấu hình qua biến môi trường (không hard-code trong code nguồn).
- Trường `represent_id` trong bảng `project` hiện là UUID nhưng không có FK — migration sẽ thêm FK trỏ đến bảng User.
- Phạm vi **chỉ bao gồm web app này** — không có SSO, OAuth2, hay tích hợp với hệ thống xác thực bên ngoài.
- Bảo mật mật khẩu: sử dụng **BCrypt** để mã hóa mật khẩu một chiều.
- Mobile/tablet là **ngoài phạm vi** — tối ưu cho desktop browser.
- Audit log cho User (ai đã thay đổi gì, lúc nào) là **ngoài phạm vi** của feature này.
