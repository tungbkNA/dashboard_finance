# Feature Specification: Kiểm Tra Ảnh Hưởng Liên Tháng & Cập Nhật Cascade

**Feature Branch**: `005-cross-month-recalculation`
**Created**: 2026-05-07
**Status**: Draft
**Input**: User description: "Xây dựng feature kiểm tra ảnh hưởng liên tháng và cập nhật các tháng bị ảnh hưởng bằng event khi dữ liệu của một tháng thay đổi."

---

## Background

Trong hệ thống quản lý dự án theo tháng, **Tồn cuối kỳ (Nhóm 6) của tháng T là nguồn dữ liệu cho Tồn đầu kỳ (Nhóm 1) của tháng T+1**. Mapping cụ thể:

| Nguồn (Tháng T – Nhóm 6) | Đích (Tháng T+1 – Nhóm 1) |
|---|---|
| `g6_ra_ton` | `g1_ra_ton` |
| `g6_slsx_ton_ht` | `g1_slsx_ton_tu_sx_ht_hd` |
| `g6_slsx_ton_dd` | `g1_slsx_ton_tu_sx_dd_hd` |
| `g6_slsx_os_ton` | `g1_slsx_os_ton` |
| `g6_slsx_os_ton_ht` | `g1_slsx_os_ton_ht` |

Khi user thay đổi một trường nhập tay ở tháng T, các trường Nhóm 6 của tháng T có thể thay đổi, dẫn đến thay đổi Nhóm 1 của tháng T+1, rồi lại ảnh hưởng Nhóm 6 của T+1, v.v. — tạo thành một chuỗi propagation.

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Cảnh Báo Trước Khi Ghi Đè Dữ Liệu Liên Tháng (Priority: P1)

Khi user chỉnh sửa một trường nhập tay trong nhóm thuộc tính của một bản ghi tháng và trường đó đã có dữ liệu, hệ thống hiển thị dialog cảnh báo: *"Thay đổi này có thể sẽ làm thay đổi các nhóm giá trị trong các tháng khác"*. User chọn xác nhận hoặc hủy. Nếu hủy, dữ liệu không thay đổi. Nếu xác nhận, hệ thống tiến hành lưu và kiểm tra propagation.

**Why this priority**: Đây là điểm vào duy nhất của toàn bộ cơ chế — không có cảnh báo này thì user không biết hành động của mình sẽ ảnh hưởng các tháng khác. Tất cả user story khác phụ thuộc vào luồng này.

**Independent Test**: Mở bản ghi tháng T đã có dữ liệu, sửa một trường nhập tay, nhấn Save → xác nhận dialog cảnh báo xuất hiện. Nhấn "Hủy" → dữ liệu không thay đổi. Thực hiện lại → nhấn "Xác nhận" → dữ liệu được lưu.

**Acceptance Scenarios**:

1. **Given** tháng T có bản ghi với ít nhất một trường nhập tay đã có giá trị, **When** user sửa trường đó và nhấn Save trong group, **Then** hệ thống hiển thị dialog cảnh báo với nội dung "Thay đổi này có thể sẽ làm thay đổi các nhóm giá trị trong các tháng khác" và hai lựa chọn "Xác nhận" / "Hủy".
2. **Given** dialog cảnh báo đang hiển thị, **When** user nhấn "Hủy", **Then** dialog đóng lại, trường quay về giá trị cũ, không có API call nào được thực hiện.
3. **Given** trường nhập tay chưa có dữ liệu (null), **When** user nhập giá trị mới và nhấn Save, **Then** hệ thống lưu trực tiếp mà không hiển thị cảnh báo.
4. **Given** dialog cảnh báo đang hiển thị, **When** user nhấn "Xác nhận", **Then** hệ thống tiến hành lưu thay đổi và bắt đầu kiểm tra propagation.
5. **Given** user đang ở trong dialog cảnh báo, **When** user đóng dialog bằng nút X hoặc click ngoài, **Then** hành vi tương đương "Hủy" — dữ liệu không thay đổi.

---

### User Story 2 - Cascade Recalculation Liên Tháng Sau Khi Xác Nhận (Priority: P2)

Sau khi user xác nhận lưu thay đổi tháng T, hệ thống tự động kiểm tra xem Tồn cuối kỳ (Nhóm 6) của tháng T có thay đổi không. Nếu có, hệ thống cập nhật Tồn đầu kỳ (Nhóm 1) của tháng T+1, tính lại Nhóm 6 của T+1, và tiếp tục cho đến khi không còn thay đổi hoặc đến tháng kết thúc hợp đồng. Toàn bộ chuỗi cập nhật phải là nguyên tử — tất cả thành công hoặc tất cả rollback.

**Why this priority**: Đây là cốt lõi của nghiệp vụ. Nếu không có propagation, dữ liệu liên tháng sẽ mất nhất quán sau mỗi lần chỉnh sửa.

**Independent Test**: Tạo dự án với 3 tháng dữ liệu liên tục. Sửa một trường nhập tay ở tháng 1 mà biết trước sẽ làm thay đổi g6. Xác nhận save. Kiểm tra tháng 2 và 3 đã được cập nhật đúng theo mapping g6→g1→g6.

**Acceptance Scenarios**:

1. **Given** user xác nhận lưu thay đổi tháng T, **When** thay đổi gây ra sự thay đổi trong ít nhất một trường Nhóm 6 của tháng T, **Then** hệ thống cập nhật 5 trường Nhóm 1 tương ứng của tháng T+1 (nếu bản ghi T+1 tồn tại và active).
2. **Given** Nhóm 1 của tháng T+1 đã được cập nhật, **When** hệ thống tính lại Nhóm 6 của tháng T+1, **Then** nếu bất kỳ trường Nhóm 6 nào thay đổi, hệ thống tiếp tục cập nhật Nhóm 1 của tháng T+2.
3. **Given** propagation đang chạy và đến tháng cuối cùng của hợp đồng (project.monthEnd), **When** hệ thống xử lý tháng đó xong, **Then** propagation dừng lại và không cố cập nhật tháng kế tiếp (không tồn tại).
4. **Given** propagation đang chạy và đến tháng M mà Nhóm 6 của M không thay đổi sau khi Nhóm 1 được cập nhật, **When** so sánh giá trị mới và cũ của Nhóm 6, **Then** propagation dừng sớm tại M và không cập nhật T+1 trở đi.
5. **Given** toàn bộ chuỗi propagation thực hiện thành công, **When** hệ thống hoàn tất, **Then** tất cả thay đổi được commit trong một giao dịch duy nhất.
6. **Given** một tháng trong chuỗi propagation bị lỗi (ví dụ: constraint violation, lỗi DB), **When** lỗi xảy ra, **Then** toàn bộ giao dịch rollback — không có tháng nào được cập nhật nửa vời.
7. **Given** user thay đổi một trường trong Nhóm 2, 3, 4, 5 (không ảnh hưởng trực tiếp Nhóm 6), **When** hệ thống tính lại Nhóm 6 và nhận thấy không có thay đổi, **Then** propagation dừng ngay và không cập nhật bất kỳ tháng nào khác.

---

### User Story 3 - Thông Báo Kết Quả & Audit Trail (Priority: P3)

Sau khi chuỗi cập nhật hoàn tất, user nhận được thông báo tường minh về kết quả: thành công với số tháng bị ảnh hưởng, hoặc lỗi với thông tin đủ để hiểu nguyên nhân. Mọi bước trong chuỗi cập nhật được ghi vào audit trail, cho phép truy vết thay đổi nào gây ra recalculation nào.

**Why this priority**: Transparency và traceability là yêu cầu nghiệp vụ quan trọng, nhưng có thể xây sau khi US1+US2 hoạt động ổn định.

**Independent Test**: Thực hiện một edit gây propagation 2 tháng. Kiểm tra toast thông báo "Đã cập nhật 2 tháng liên quan". Truy vấn audit log, xác nhận ghi nhận đúng project, tháng gốc, field, old/new value, và timestamp của từng bước.

**Acceptance Scenarios**:

1. **Given** propagation hoàn tất và cập nhật N tháng (N ≥ 1), **When** hệ thống hoàn tất, **Then** FE hiển thị toast thành công: "Lưu thành công. Đã cập nhật thêm N tháng liên quan."
2. **Given** propagation hoàn tất nhưng không có tháng nào bị ảnh hưởng (Nhóm 6 không thay đổi), **When** hệ thống hoàn tất, **Then** FE hiển thị toast thành công bình thường không đề cập đến "tháng liên quan".
3. **Given** propagation thất bại (rollback toàn bộ), **When** lỗi được trả về, **Then** FE hiển thị toast lỗi rõ ràng và không cập nhật bất kỳ trường nào trên màn hình.
4. **Given** một thay đổi dữ liệu được xác nhận, **When** giao dịch thành công, **Then** hệ thống ghi một bản ghi audit trail ghi nhận: project_id, month_key bắt đầu ảnh hưởng, tên field thay đổi, giá trị cũ, giá trị mới, timestamp, và danh sách các tháng bị ảnh hưởng.
5. **Given** một event được xử lý lại với cùng nội dung (idempotent test), **When** event được xử lý lần thứ 2, **Then** dữ liệu không thay đổi thêm và không tạo thêm bản ghi audit trùng lặp.

---

### Edge Cases

- Tháng T là tháng cuối cùng của hợp đồng (không có T+1): propagation không xảy ra, lưu bình thường.
- Tháng T+1 tồn tại nhưng có `active = false`: hệ thống bỏ qua tháng đó và dừng propagation (không nhảy sang T+2).
- Tháng T+1 tồn tại và active nhưng có `locked = true`: hệ thống bỏ qua tháng đó và dừng propagation — dữ liệu lịch sử được bảo toàn.
- Khi đơn giá (`price`) thay đổi: chỉ tính lại các tháng có `locked = false`; tháng đã khóa giữ nguyên.
- User sửa nhiều trường cùng lúc trong một group và save: hệ thống tính tổng hợp tất cả thay đổi trước khi propagate — chỉ một chuỗi propagation duy nhất được chạy.
- Giá trị mới giống giá trị cũ (user save mà không thực sự thay đổi): hệ thống phát hiện Nhóm 6 không thay đổi và không propagate.
- Dự án chỉ có 1 tháng (monthStart = monthEnd): không có tháng kế tiếp để propagate.
- Propagation gặp tháng có `g1_slsx_ton_tu_sx_hd` (trường nhập tay luôn nhập tay): trường này KHÔNG bị ghi đè bởi propagation — chỉ 5 trường được cascade.

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Hệ thống PHẢI hiển thị dialog cảnh báo khi user cố ghi đè một trường nhập tay đã có giá trị không null.
- **FR-002**: Hệ thống PHẢI cho phép user hủy thao tác từ dialog cảnh báo; sau khi hủy, không có thay đổi nào được lưu.
- **FR-003**: Hệ thống PHẢI lưu thay đổi chỉ sau khi user xác nhận từ dialog cảnh báo.
- **FR-004**: Sau khi lưu thành công, hệ thống PHẢI tính lại 6 trường Nhóm 6 (`g6_ra_ton`, `g6_slsx_ton_ht`, `g6_slsx_ton_dd`, `g6_slsx_os_ton`, `g6_slsx_os_ton_ht`, `g6_slsx_ton`) của tháng hiện tại.
- **FR-005**: Nếu bất kỳ trường Nhóm 6 nào của tháng T thay đổi so với giá trị đã lưu, hệ thống PHẢI cập nhật 5 trường Nhóm 1 tương ứng của tháng T+1 (nếu bản ghi T+1 tồn tại và active).
- **FR-006**: Sau khi cập nhật Nhóm 1 của tháng T+1, hệ thống PHẢI tính lại Nhóm 6 của tháng T+1 theo đúng công thức nghiệp vụ.
- **FR-007**: Hệ thống PHẢI tiếp tục propagation theo chuỗi cho đến khi: (a) đến tháng kết thúc hợp đồng (`project.monthEnd`), hoặc (b) Nhóm 6 của một tháng không thay đổi sau khi Nhóm 1 được cập nhật.
- **FR-008**: Trường `g1_slsx_ton_tu_sx_hd` KHÔNG được cập nhật trong quá trình propagation — đây là trường nhập tay độc lập ở mọi tháng.
- **FR-014**: Bảng `project_monthly_record` PHẢI có cột `locked` (boolean, default false). Hệ thống PHẢI bỏ qua (dừng propagation tại) bất kỳ tháng nào có `locked = true`, kể cả khi đơn giá thay đổi.
- **FR-015**: API và service layer PHẢI kiểm tra `locked` trước khi ghi đè bất kỳ trường nào của một tháng — tháng locked không bao giờ bị ghi đè bởi propagation.
- **FR-009**: Toàn bộ chuỗi lưu-và-propagation (từ tháng T đến tháng cuối bị ảnh hưởng) PHẢI thực thi trong một giao dịch duy nhất — tất cả thành công hoặc tất cả rollback.
- **FR-010**: Hệ thống PHẢI ghi audit trail cho mỗi sự kiện thay đổi, bao gồm: project_id, month_key (tháng gốc), field_name, old_value, new_value, triggered_by_month_key (tháng nào đã kích hoạt thay đổi này), và timestamp.
- **FR-011**: Sau khi propagation thành công, hệ thống PHẢI trả về số lượng tháng bị ảnh hưởng (ngoài tháng gốc) để FE hiển thị thông báo.
- **FR-012**: Nếu propagation thất bại, hệ thống PHẢI trả về lỗi rõ ràng và không lưu bất kỳ thay đổi nào.
- **FR-013**: Event/domain-event ghi nhận thay đổi PHẢI có thể xử lý idempotent — xử lý lại cùng event không tạo ra thay đổi thêm hay audit log trùng lặp.

### Key Entities

- **`MonthlyFieldChangedEvent`** (Domain Event): Ghi nhận một thay đổi trường nhập tay được xác nhận bởi user. Chứa: `projectId`, `originMonthKey`, `fieldName`, `oldValue`, `newValue`, `eventId` (UUID, để idempotency check), `occurredAt`.
- **`CrossMonthPropagationResult`**: Kết quả sau khi propagation chạy xong. Chứa: `affectedMonthKeys` (danh sách tháng đã được cập nhật), `stoppedReason` (`NO_MORE_MONTHS` | `NO_CHANGE_DETECTED` | `INACTIVE_MONTH` | `LOCKED_MONTH`).
- **`FieldChangeAuditLog`**: Bảng audit trail. Mỗi row là một field-level change trong chuỗi propagation. Chứa: `id`, `project_id`, `month_key`, `field_name`, `old_value`, `new_value`, `triggered_by_month_key`, `event_id` (FK về `MonthlyFieldChangedEvent`), `created_at`.
- **`locked` column** trên bảng `project_monthly_record`: kiểu `boolean NOT NULL DEFAULT false`. Khi `locked = true`, bản ghi tháng đó không được ghi đè bởi bất kỳ cơ chế propagation hay price-recalculation nào. UI khóa/mở khóa được defer sang giai đoạn sau.

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Tỷ lệ 100% các trường hợp user ghi đè dữ liệu đã có đều hiển thị dialog cảnh báo — không có lần nào dữ liệu bị ghi đè im lặng.
- **SC-002**: Sau khi xác nhận, tất cả các tháng trong chuỗi bị ảnh hưởng được cập nhật đúng và nhất quán — không có trạng thái "tháng này cập nhật, tháng sau chưa cập nhật".
- **SC-003**: Nếu có lỗi xảy ra ở bất kỳ bước nào trong chuỗi propagation, dữ liệu của tất cả các tháng (kể cả tháng gốc) giữ nguyên giá trị trước khi user thao tác.
- **SC-004**: Audit trail đủ chi tiết để từ một bản ghi audit có thể xác định được: ai thay đổi gì, tháng nào là nguồn, và tháng nào bị ảnh hưởng theo chuỗi.
- **SC-005**: Xử lý lại cùng một event (idempotent replay) không tạo ra thay đổi dữ liệu bổ sung hay bản ghi audit trùng lặp.
- **SC-006**: User nhận được thông báo thành công với số tháng bị ảnh hưởng trong vòng thời gian chờ tương đương với một thao tác lưu bình thường (không có độ trễ rõ rệt với chuỗi tối đa 24 tháng).
- **SC-007**: Giao diện không để user ở trạng thái mơ hồ — luôn có phản hồi rõ ràng (thành công / lỗi) sau khi xác nhận thao tác.

---

## Assumptions

- Hệ thống backend xử lý toàn bộ logic propagation; FE chỉ gửi request lưu và nhận kết quả (số tháng bị ảnh hưởng).
- Chuỗi propagation tối đa là độ dài hợp đồng (thường ≤ 24 tháng) — không có rủi ro vòng lặp vô tận.
- Backend đã có `MonthlyCalculationService` (từ Feature 003) với các công thức Nhóm 6 và có thể tái sử dụng.
- Chỉ 5 trường Nhóm 1 được cascade từ Nhóm 6 tháng trước (theo đặc tả Feature 003); `g1_slsx_ton_tu_sx_hd` luôn độc lập.
- "Event" trong yêu cầu này được hiểu là Domain Event nội bộ trong application layer (không phải message queue / Kafka); xử lý đồng bộ trong cùng transaction giai đoạn đầu.
- FE hiện tại đã có warning dialog từ Feature 004 (§9.3 constitution); feature này mở rộng luồng sau khi user xác nhận.
- Audit log được lưu trong cùng giao dịch với dữ liệu để đảm bảo nhất quán.
- Inactive months (`active = false`) trong chuỗi propagation làm dừng chuỗi — không nhảy qua.
- Locked months (`locked = true`) trong chuỗi propagation làm dừng chuỗi — dữ liệu lịch sử được bảo toàn. UI khóa/mở khóa defer sang giai đoạn sau.

---

## Clarifications

### Session 2026-05-07

- Q: Cơ chế xử lý event/propagation sau khi user xác nhận lưu → A: Đồng bộ trong cùng DB transaction (save + propagate trong 1 request), không dùng async queue giai đoạn đầu.
- Q: User có cần xem preview danh sách tháng bị ảnh hưởng trước khi xác nhận không → A: Không cần preview; chỉ hiển thị cảnh báo chung "có thể ảnh hưởng các tháng khác", sau khi save xong mới thông báo số tháng đã cập nhật.
- Q: Audit log cần lưu ở mức độ nào → A: Lưu chi tiết từng field thay đổi: projectId, monthKey, fieldName, oldValue, newValue, triggeredByMonthKey, timestamp. Audit log lưu trong cùng transaction với dữ liệu.
- Q: Khi recalculation chạy, có được ghi đè giá trị trường công thức đang lưu trong DB không → A: Có, recalculation luôn ghi đè trường công thức vì user không sửa trực tiếp được; giá trị DB chỉ là snapshot cũ cần được làm mới.
- Q: Khi đơn giá dự án thay đổi, hệ thống áp dụng lại cho tháng nào → A: Chỉ áp dụng cho các tháng chưa khóa (locked = false); tháng đã khóa giữ nguyên dữ liệu lịch sử.
- Q: Có cần cơ chế khóa tháng không → A: Thêm cột `locked` (boolean, default false) vào bảng `project_monthly_record` và bổ sung logic BE bỏ qua tháng locked khi propagation; chưa build UI khóa/mở khóa trong giai đoạn đầu.
