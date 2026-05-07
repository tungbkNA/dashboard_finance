# Feature Specification: Quản Lý Bản Ghi Dự Án Theo Tháng

**Feature Branch**: `003-project-monthly-records`
**Created**: 2026-05-07
**Status**: Draft

## Clarifications

### Session 2026-05-07

- Q: Màn hình nào hiển thị bản ghi tháng và cấu trúc điều hướng như thế nào? → A: Màn hình "Quản lý Các dự án" (route `/projects`) hiển thị danh sách bản ghi tháng của tất cả hợp đồng; mặc định filter theo tháng hiện tại; cho phép người dùng chọn tháng khác để filter.
- Q: Khi monthEnd/monthStart bị rút ngắn, hệ thống xử lý bản ghi ngoài khoảng mới như thế nào? → A: Không xóa cứng — hệ thống cảnh báo người dùng và đánh dấu các bản ghi ngoài khoảng mới là inactive (không hiển thị trong danh sách); dữ liệu vẫn được giữ trong DB.
- Q: Cấu trúc hiển thị bản ghi tháng trong danh sách như thế nào? → A: Bảng hiển thị các cột tóm tắt (mã DA, tên DA và một số trường key); click vào row mở form điền đủ 6 nhóm thuộc tính.
- Q: Khi RA = 0 hoặc null, công thức EE hiển thị như thế nào? → A: Hiển thị trống (null) — không hiển thị 0% hay "N/A".
- Q: Các cột nào hiển thị trực tiếp trên bảng danh sách bản ghi tháng? → A: Mã DA, Tên DA, Doanh thu kế hoạch (nhóm 4), Doanh thu nghiệm thu (nhóm 5), Tổng SLNT.
- Q: Giá trị số trống được hiểu là 0 hay null khi tính toán? → A: Trường số null khi tham gia công thức được hiểu là 0; UI vẫn hiển thị trống để phân biệt với giá trị 0 thực sự.
- Q: Quy tắc làm tròn tiền VNĐ? → A: Làm tròn 0 chữ số thập phân (số nguyên VNĐ).
- Q: Quy tắc làm tròn phần trăm (EE, tỷ suất LNG)? → A: Làm tròn 2 chữ số thập phân.
- Q: Tháng lưu theo format nào? → A: monthKey dạng YYYY-MM (ví dụ: 2026-01).
- Q: Trường công thức có lưu snapshot vào DB không? → A: Có — lưu snapshot khi tính; nếu DB chưa có thì backend tính lại khi trả về.
- Q: Khi đơn giá dự án thay đổi, doanh thu đã lưu có tự tính lại không? → A: Không — snapshot đã lưu giữ nguyên; chỉ tính lại khi DB chưa có snapshot.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Xem và nhập liệu bản ghi tháng trong màn Quản lý Dự án (Priority: P1)

Màn hình "Quản lý Các dự án" (`/projects`) hiển thị danh sách bản ghi tháng của tất cả hợp đồng, mặc định filter theo tháng hiện tại. Người dùng có thể chọn tháng khác để filter. Mỗi bản ghi tháng trong danh sách có thể được mở ra để xem/nhập đủ 6 nhóm thuộc tính. Các trường có công thức hiển thị giá trị tính toán (chỉ đọc); các trường không có công thức cho phép nhập tay.

**Why this priority**: Đây là màn hình trung tâm của feature — cho phép xem tổng hợp theo tháng và nhập liệu từng bản ghi. Không có màn hình này thì toàn bộ tính năng vô nghĩa.

**Independent Test**: Vào `/projects`, xác nhận mặc định hiển thị bản ghi tháng hiện tại của tất cả dự án. Chọn một tháng khác, danh sách cập nhật. Mở bản ghi của một dự án, nhập dữ liệu, lưu, kiểm tra các trường công thức hiển thị đúng.

**Acceptance Scenarios**:

1. **Given** người dùng vào màn hình `/projects`, **When** trang tải xong, **Then** bảng hiển thị danh sách bản ghi tháng hiện tại của tất cả dự án, mỗi row gồm các cột tóm tắt key.
2. **Given** bảng đang hiển thị, **When** người dùng chọn một tháng khác từ bộ lọc, **Then** bảng cập nhật hiển thị đúng bản ghi tháng đã chọn.
3. **Given** một row trong bảng, **When** người dùng click vào row đó, **Then** một form/dialog mở ra hiển thị đủ 6 nhóm thuộc tính của bản ghi tháng đó.
4. **Given** bản ghi tháng đang ở trạng thái chờ nhập liệu, **When** người dùng nhập các trường thủ công và lưu, **Then** dữ liệu được lưu thành công và các trường công thức tự động hiển thị giá trị tính toán mới nhất.
5. **Given** một trường có công thức, **When** người dùng cố nhập tay, **Then** trường đó không thể thao tác (disabled/read-only) và hiển thị giá trị do backend tính.
6. **Given** bản ghi tháng đầu tiên của dự án, **When** người dùng mở form, **Then** nhóm Tồn đầu kỳ cho phép nhập tay tất cả 6 trường.
7. **Given** bản ghi tháng thứ N (N > 1), **When** người dùng mở form, **Then** nhóm Tồn đầu kỳ hiển thị giá trị tự động lấy từ Tồn cuối kỳ tháng liền trước và không cho phép chỉnh sửa.

---

### User Story 2 - Tự động sinh bản ghi tháng khi tạo/cập nhật dự án (Priority: P2)

Khi một dự án được tạo với khoảng tháng xác định, hệ thống tự động sinh đủ các bản ghi tháng tương ứng. Khi khoảng tháng được mở rộng, hệ thống tự động bổ sung thêm bản ghi cho các tháng mới.

**Why this priority**: Không có bản ghi tháng thì không có gì để nhập liệu. Tuy nhiên việc tự động sinh là hệ quả của US1 — cần có US1 hoạt động trước mới validate được US2.

**Independent Test**: Tạo một dự án tháng 01/2026 – 03/2026, kiểm tra hệ thống tạo đúng 3 bản ghi tháng. Sau đó cập nhật tháng kết thúc thành 05/2026, kiểm tra hệ thống bổ sung 2 bản ghi mới (04/2026, 05/2026).

**Acceptance Scenarios**:

1. **Given** người dùng tạo dự án với monthStart = 01/2026 và monthEnd = 03/2026, **When** dự án được lưu thành công, **Then** hệ thống tự động tạo đúng 3 bản ghi tháng: 01/2026, 02/2026, 03/2026.
2. **Given** dự án có bản ghi tháng từ 01/2026 đến 03/2026, **When** người dùng cập nhật monthEnd thành 05/2026, **Then** hệ thống bổ sung thêm bản ghi 04/2026 và 05/2026, giữ nguyên dữ liệu 3 tháng cũ.
3. **Given** dự án vừa được tạo, **When** hệ thống sinh bản ghi tháng, **Then** tất cả các trường đều có giá trị null/mặc định (chờ người dùng nhập), ngoại trừ các trường công thức được tính từ giá trị hiện tại.
4. **Given** dự án có bản ghi tháng 01–05/2026, **When** người dùng cập nhật monthEnd xuống 03/2026, **Then** hệ thống hiển thị cảnh báo: "Các bản ghi tháng 04/2026 và 05/2026 sẽ bị đánh dấu inactive. Dữ liệu không bị xóa." và yêu cầu xác nhận.
5. **Given** người dùng xác nhận rút ngắn, **When** cập nhật được lưu, **Then** bản ghi 04/2026 và 05/2026 bị đánh dấu inactive và không còn hiển thị trong danh sách; dữ liệu vẫn tồn tại trong DB.

---

### User Story 3 - Chuỗi Tồn cuối kỳ → Tồn đầu kỳ giữa các tháng (Priority: P3)

Hệ thống đảm bảo tính liên tục dữ liệu: Tồn cuối kỳ của tháng N tự động trở thành Tồn đầu kỳ của tháng N+1 khi người dùng lưu tháng N.

**Why this priority**: Đây là nghiệp vụ quan trọng nhưng phụ thuộc vào US1 và US2 hoạt động đúng. Có thể test độc lập sau khi US1+US2 hoàn thành.

**Independent Test**: Nhập đầy đủ dữ liệu tháng 01/2026, lưu, sau đó mở tháng 02/2026 và kiểm tra nhóm Tồn đầu kỳ khớp với Tồn cuối kỳ của tháng 01/2026.

**Acceptance Scenarios**:

1. **Given** người dùng đã lưu dữ liệu tháng 01/2026 với Tồn cuối kỳ có giá trị, **When** người dùng mở bản ghi tháng 02/2026, **Then** nhóm Tồn đầu kỳ của 02/2026 hiển thị đúng 5 giá trị Tồn cuối kỳ từ tháng 01/2026 (RA TỒN, SLSX tồn hoàn thiện, SLSX tồn dở dang, SLSX OS tồn, SLSX OS tồn hoàn thiện).
2. **Given** tháng 02/2026 đang hiển thị Tồn đầu kỳ auto-populated, **When** người dùng cố chỉnh sửa các trường Tồn đầu kỳ của tháng 02/2026, **Then** các trường đó không cho phép chỉnh sửa.
3. **Given** người dùng cập nhật dữ liệu tháng 01/2026 khiến Tồn cuối kỳ thay đổi, **When** lưu thành công, **Then** Tồn đầu kỳ của tháng 02/2026 tự động cập nhật theo.

---

### Edge Cases

- Khi RA = 0 hoặc null trong nhóm Thực hiện SLSX đến NGÀY, công thức EE trả về null — ô hiển thị trống.
- Dự án chỉ có 1 tháng: Tồn đầu kỳ là nhập tay, Tồn cuối kỳ vẫn được tính theo công thức bình thường.
- Khi backend tính công thức mà một trong các trường đầu vào còn null: trả về null cho trường công thức đó thay vì báo lỗi.
- Bản ghi tháng mới tạo (chưa có dữ liệu): tất cả trường công thức trả về null/0 — không báo lỗi.

---

## Requirements *(mandatory)*

### Functional Requirements

#### Sinh bản ghi tháng

- **FR-REC-001**: Hệ thống PHẢI tự động sinh đủ bản ghi tháng cho mỗi tháng trong khoảng [monthStart, monthEnd] của dự án ngay khi dự án được tạo.
- **FR-REC-002**: Khi monthEnd của dự án được mở rộng (tăng), hệ thống PHẢI tự động bổ sung bản ghi cho các tháng mới trong khoảng bổ sung.
- **FR-REC-002b**: Khi monthEnd hoặc monthStart bị rút ngắn, hệ thống PHẢI hiển thị cảnh báo liệt kê các tháng sẽ bị inactive và yêu cầu người dùng xác nhận trước khi lưu. Sau khi xác nhận, các bản ghi ngoài khoảng mới bị đánh dấu inactive và không hiển thị trong danh sách — dữ liệu KHÔNG bị xóa khỏi DB.
- **FR-REC-003**: Mỗi bản ghi tháng được xác định duy nhất bởi cặp (projectId, monthKey) trong đó monthKey có dạng YYYY-MM (ví dụ: 2026-01).

#### Nhóm Tồn đầu kỳ

- **FR-REC-004**: Với bản ghi tháng đầu tiên của dự án, tất cả 6 trường nhóm Tồn đầu kỳ phải cho phép nhập tay.
- **FR-REC-005**: Với bản ghi tháng không phải tháng đầu, 5 trường Tồn đầu kỳ phải được tự động điền từ 5 trường Tồn cuối kỳ tương ứng của tháng liền trước và không cho phép chỉnh sửa.
  - Mapping: RA tồn ← Tồn cuối kỳ.RA TỒN; SLSX tồn tự SX hoàn thiện theo HĐ ← Tồn cuối kỳ.SLSX tồn hoàn thiện; SLSX tồn tự SX dở dang theo HĐ ← Tồn cuối kỳ.SLSX tồn dở dang; SLSX OS tồn ← Tồn cuối kỳ.SLSX OS tồn; SLSX OS tồn hoàn thiện ← Tồn cuối kỳ.SLSX OS tồn hoàn thiện.
  - Lưu ý: trường "SLSX tồn tự SX theo hợp đồng" trong Tồn đầu kỳ không có trường tương ứng trong Tồn cuối kỳ — nhập tay hoặc giữ nguyên giá trị tháng trước theo quy tắc nghiệp vụ.
- **FR-REC-006**: Khi dữ liệu Tồn cuối kỳ của tháng N thay đổi, Tồn đầu kỳ của tháng N+1 phải tự động cập nhật.

#### Nhóm Kế hoạch tháng

- **FR-REC-007**: Các trường nhập tay: Headcount tháng, RA, SLSX Tự SX, SLSX OS, Liên kết, SLSX tự SX hoàn thiện trong tháng, SLSX tự SX dở dang, SLSX OS hoàn thiện, SLSX OS dở dang, CPBQTB tháng, Tỷ suất LNG (%).
- **FR-REC-008**: Tổng SLSX dự kiến = SLSX Tự SX + SLSX OS + Liên kết (công thức — chỉ đọc).

#### Nhóm Thực hiện SLSX đến NGÀY

- **FR-REC-009**: Các trường nhập tay: RA, Tổng SLSX theo HĐ, SLSX tự SX hoàn thiện, SLSX tự SX dở dang, SLSX OS dở dang, SLSX OS tồn hoàn thiện.
- **FR-REC-010**: EE = Tổng SLSX theo HĐ / RA, hiển thị dạng phần trăm (công thức — chỉ đọc). Khi RA = 0 hoặc null, backend trả về null — ô hiển thị trống.

#### Nhóm Kế hoạch doanh thu

- **FR-REC-011**: Các trường nhập tay: Tự SLSX Tồn hoàn thiện, Tự SLSX trong tháng, SLSX OS tồn, SLSX OS trong tháng, LK, Tỉ suất LNG dự kiến, LNG dự kiến.
- **FR-REC-012**: Tổng = Tự SLSX Tồn hoàn thiện + Tự SLSX trong tháng + SLSX OS tồn + SLSX OS trong tháng + LK (công thức — chỉ đọc).
- **FR-REC-013**: Doanh thu = Tổng × đơn giá của dự án cha (công thức — chỉ đọc).

#### Nhóm Thực hiện nghiệm thu

- **FR-REC-014**: Các trường nhập tay: RA tương ứng SLNT, NT SLSX Tồn hoàn thiện, NT SLSX trong tháng, NT SLSX OS tồn, NT SLSX OS trong tháng, Tỉ suất LNG (%), LNG (VNĐ).
- **FR-REC-015**: Tổng SLNT = NT SLSX Tồn hoàn thiện + NT SLSX trong tháng + NT SLSX OS tồn + NT SLSX OS trong tháng (công thức — chỉ đọc).
- **FR-REC-016**: Doanh thu = Tổng SLNT × đơn giá của dự án cha (công thức — chỉ đọc).

#### Nhóm Tồn cuối kỳ (toàn bộ là công thức — chỉ đọc)

- **FR-REC-017**: RA TỒN = Tồn đầu kỳ.RA tồn + Thực hiện.RA - Nghiệm thu.RA tương ứng SLNT.
- **FR-REC-018**: SLSX tồn hoàn thiện = Tồn đầu kỳ.SLSX tồn tự SX hoàn thiện theo HĐ + Thực hiện.SLSX tự SX hoàn thiện - Nghiệm thu.NT SLSX Tồn hoàn thiện - Nghiệm thu.NT SLSX OS trong tháng.
- **FR-REC-019**: SLSX tồn dở dang = Thực hiện.SLSX tự SX dở dang + Tồn đầu kỳ.SLSX tồn tự SX dở dang theo HĐ.
- **FR-REC-020**: SLSX OS tồn = Tồn đầu kỳ.SLSX OS tồn + Thực hiện.SLSX OS dở dang.
- **FR-REC-021**: SLSX OS tồn hoàn thiện = Tồn đầu kỳ.SLSX OS tồn hoàn thiện + Thực hiện.SLSX OS tồn hoàn thiện - Nghiệm thu.NT SLSX OS tồn - Nghiệm thu.NT SLSX OS trong tháng.

#### Giao diện màn hình /projects

- **FR-REC-025**: Bảng danh sách bản ghi tháng PHẢI hiển thị ít nhất 5 cột sau trên mỗi row: Mã dự án, Tên dự án, Doanh thu kế hoạch (nhóm Kế hoạch doanh thu), Doanh thu nghiệm thu (nhóm Thực hiện nghiệm thu), Tổng SLNT.
- **FR-REC-026**: Bộ lọc tháng PHẢI mặc định theo tháng hiện tại khi trang tải. Người dùng có thể chọn tháng khác để xem bản ghi của tháng đó.
- **FR-REC-027**: Click vào một row trong bảng mở form/dialog điền đủ 6 nhóm thuộc tính của bản ghi tháng đó để xem và nhập liệu.

#### Quy tắc trường công thức

- **FR-REC-022**: Backend là nguồn sự thật của toàn bộ công thức. FE không tự tính công thức.
- **FR-REC-023**: Khi backend trả về bản ghi, nếu trường công thức đã có snapshot lưu trong DB thì trả về giá trị đó; nếu chưa có thì tính theo công thức và trả về. Khi đơn giá dự án thay đổi sau đó, snapshot doanh thu đã lưu giữ nguyên — không tự tính lại.
- **FR-REC-024**: Trường công thức trên giao diện phải ở trạng thái chỉ đọc, không cho người dùng nhập tay.

#### Quy tắc số học

- **FR-REC-028**: Trường số null khi tham gia tính công thức được hiểu là 0. UI hiển thị trống để phân biệt với giá trị 0 thực sự.
- **FR-REC-029**: Các trường tiền VNĐ (Doanh thu kế hoạch, Doanh thu nghiệm thu, LNG dự kiến, LNG VNĐ) được làm tròn 0 chữ số thập phân.
- **FR-REC-030**: Các trường phần trăm (EE, Tỷ suất LNG dự kiến, Tỷ suất LNG nghiệm thu) được làm tròn 2 chữ số thập phân.
- **FR-REC-031**: Bản ghi tháng inactive (do rút ngắn khoảng tháng) KHÔNG hiển thị trong bảng danh sách; có thể xem lại nếu người dùng mở rộng lại khoảng tháng.
- **FR-REC-032**: Khi người dùng mở form nhập liệu của một bản ghi tháng đã có ít nhất một trường nhập tay có giá trị (khác null), hệ thống PHẢI hiển thị hộp thoại xác nhận trước khi cho phép ghi đè. Thông điệp xác nhận phải rõ ràng rằng dữ liệu cũ sẽ bị thay thế.

### Key Entities

- **ProjectMonthRecord**: Bản ghi tháng của một dự án. Xác định duy nhất bởi (projectId, monthKey) với monthKey dạng YYYY-MM. Chứa tất cả các trường của 6 nhóm (bao gồm snapshot của trường công thức). Có trạng thái active/inactive. Liên kết với Project để lấy đơn giá cho công thức doanh thu.
- **Project**: Dự án cha — cung cấp monthStart, monthEnd, và price (đơn giá) cho các công thức tính doanh thu.

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Khi tạo dự án N tháng, hệ thống sinh đúng N bản ghi tháng trong vòng 2 giây.
- **SC-002**: Người dùng có thể nhập liệu và lưu một bản ghi tháng hoàn chỉnh trong dưới 5 phút.
- **SC-003**: Tất cả các trường công thức (ít nhất 13 trường) hiển thị đúng giá trị sau mỗi lần lưu dữ liệu đầu vào — tỉ lệ chính xác 100%.
- **SC-004**: Khi lưu tháng N, Tồn đầu kỳ của tháng N+1 tự động cập nhật mà không cần thao tác thêm từ người dùng.
- **SC-005**: Không có trường công thức nào cho phép nhập tay trên giao diện.

---

## Assumptions

- Đơn giá (price) của dự án có thể thay đổi, nhưng snapshot doanh thu đã lưu trong DB sẽ không tự tính lại — chỉ tính lại khi DB chưa có snapshot.
- Trường số null trong công thức được xử lý là 0; UI phân biệt null (trống) với 0 thực sự.
- Khoảng tháng của dự án không bao giờ vượt quá 120 tháng (10 năm).
- Tất cả các trường số trong bản ghi tháng là kiểu số nguyên hoặc số thực không âm, ngoại trừ khi có yêu cầu cụ thể khác.
- "SLSX tồn tự SX theo hợp đồng" trong Tồn đầu kỳ không có trường tương ứng trong Tồn cuối kỳ — trường này chỉ nhập tay cho tất cả các tháng (không auto-populate).
- LNG dự kiến (nhóm 4) và LNG (VNĐ) (nhóm 5) là trường nhập tay vì không có công thức được khai báo trong đặc tả.
- Feature này hiển thị bản ghi tháng trong màn hình "Quản lý Các dự án" (`/projects`), không phải trong trang chi tiết từng dự án.
- Màn hình `/projects` mặc định filter theo tháng hiện tại; người dùng có thể chọn tháng khác.
- Mỗi row trong danh sách đại diện cho một bản ghi tháng của một dự án; người dùng mở row đó để nhập/xem 6 nhóm thuộc tính.
- Tồn đầu kỳ của các tháng không phải tháng đầu tiên là **chỉ đọc** (không cho override) — "phải lấy từ Tồn cuối kỳ" được hiểu là ràng buộc cứng.
