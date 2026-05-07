# Research: Feature 005 — Cross-Month Recalculation

**Date**: 2026-05-07
**Resolved by**: `/speckit.plan` workflow

---

## Decision 1: Kiến trúc xử lý propagation

**Decision**: Xử lý đồng bộ trong cùng một `@Transactional` qua `CrossMonthPropagationService`, không dùng async message queue.

**Rationale**:
- Spec FR-009 yêu cầu toàn bộ chuỗi save + propagate phải nằm trong một giao dịch duy nhất.
- Async event (Kafka, Spring `@Async`) vi phạm yêu cầu này vì không thể tham gia transaction hiện tại một cách an toàn.
- Spring `ApplicationEventPublisher` với `@TransactionalEventListener(phase = BEFORE_COMMIT)` có thể dùng đồng bộ, nhưng thêm độ phức tạp không cần thiết giai đoạn đầu.
- Chuỗi propagation tối đa 24 tháng → vòng lặp while đơn giản trong service là đủ.

**Alternatives considered**:
- **Spring ApplicationEvent (synchronous)**: Phù hợp nhưng thêm boilerplate. Dành cho giai đoạn sau khi cần decouple.
- **Kafka/RabbitMQ (async)**: Không phù hợp vì vi phạm FR-009 (single transaction).
- **Recursive call**: Dễ gây stack overflow với contract dài; vòng lặp while an toàn hơn.

---

## Decision 2: Phát hiện thay đổi G6 (changed field detection)

**Decision**: Snapshot G6 values trước khi `applyRequest()` + `calculateAndFill()`. So sánh với G6 values sau khi tính xong.

**Rationale**:
- 6 G6 fields (`g6RaTon`, `g6SlsxTonHt`, `g6SlsxTonDd`, `g6SlsxOsTon`, `g6SlsxOsTonHt`, `g6SlsxTon`) đều là formula fields được tính lại mỗi lần write.
- Không cần JPA `@DynamicUpdate` hay Hibernate dirty checking — so sánh trực tiếp `BigDecimal` bằng `compareTo()` là đủ.
- Snapshot được lấy từ entity trước khi `applyRequest()` modify các manual fields.

**Alternatives considered**:
- **Hibernate `@DynamicUpdate` + dirty check**: Phức tạp hơn, không controllable với formula fields.
- **Compare old DB record with new DB record after save**: Cần thêm 1 query DB. Không cần thiết vì có thể snapshot trong memory.

---

## Decision 3: Stopping conditions cho propagation chain

**Decision**: Propagation dừng khi thỏa MỘT trong các điều kiện:
1. Không tìm thấy bản ghi cho tháng kế tiếp (`repo.findByProjectIdAndMonthKey` → empty).
2. Bản ghi tháng kế tiếp có `active = false`.
3. Bản ghi tháng kế tiếp có `locked = true` (FR-014).
4. Sau khi cập nhật G1 và tính lại G6 của tháng T+1, G6 không thay đổi so với giá trị trước đó (FR-007).

**Rationale**: Đây là 4 stopping conditions từ spec. Điều kiện 4 giúp tránh cập nhật không cần thiết và đảm bảo idempotency tự nhiên.

---

## Decision 4: Idempotency

**Decision**: Hai lớp idempotency:
1. **Natural idempotency**: Nếu G6 không thay đổi sau khi propagate, chain dừng tự nhiên → chạy lại cùng request với cùng data không tạo thêm thay đổi.
2. **Explicit idempotency via `eventId`**: Mỗi lần gọi `update()` tạo một `eventId` (UUID server-side). `FieldChangeAuditLog` lưu `event_id`. Trước khi ghi audit, check `auditLogRepository.existsByEventId(eventId)`. Nếu đã tồn tại → bỏ qua (replay protection).

**Rationale**: Spec FR-013 yêu cầu idempotent replay. Natural idempotency xử lý 99% trường hợp; explicit eventId xử lý edge case replay từ external caller.

---

## Decision 5: Audit log scope

**Decision**: Chỉ log các thay đổi G6-level tại tháng gốc và G1-level thay đổi tại các tháng bị ảnh hưởng. Không log tất cả 50+ fields.

**Rationale**:
- Spec FR-010 yêu cầu: project_id, month_key, field_name, old_value, new_value, triggered_by_month_key, timestamp.
- Logging tất cả fields tạo noise và chiếm nhiều DB storage.
- Chỉ G6 (origin) và G1 (cascaded) là fields có ý nghĩa propagation.

---

## Decision 6: Response shape sau update

**Decision**: Thêm `affectedMonths: int` vào `ProjectMonthRecordResponse` (BE) và `ProjectMonthRecordDetail` (FE).

**Rationale**:
- Giữ một endpoint duy nhất `PUT /{id}` — không thêm endpoint mới.
- `affectedMonths = 0` khi không có propagation (backward compatible).
- FE đọc `affectedMonths` để quyết định toast message.

---

## Decision 7: FE ConfirmDialog trigger condition

**Decision**: Hiển thị ConfirmDialog khi **bất kỳ trường nhập tay nào trong group đang save có giá trị non-null** trong DB (tức là `detail[field] !== null`).

**Rationale**:
- Spec FR-001: "khi user cố ghi đè một trường nhập tay đã có giá trị không null".
- Spec SC-003 (AS-3 acceptance): Nếu trường chưa có dữ liệu → lưu trực tiếp.
- Group-level save: nếu ít nhất 1 trường trong group đã có data → show warning.

---

## Decision 8: `locked` field scope

**Decision**: Chỉ thêm `locked BOOLEAN NOT NULL DEFAULT FALSE` vào `project_monthly_record`. Không thêm endpoint lock/unlock trong feature này.

**Rationale**: Spec Q6 answer: add `locked` column + BE logic, defer UI. Cần field để:
- Stopping condition trong propagation chain.
- Price recalculation (chỉ unlock months).
- UI lock/unlock là feature riêng.

---

## Unresolved / Deferred

- **Price change recalculation endpoint**: Spec Q5 answer (locked months excluded) — design field `locked` đã có; endpoint thực tế của price recalculation cần feature riêng.
- **UI lock/unlock**: Defer per Q6.
- **FE testing framework**: Vitest (per plan template) — không yêu cầu tạo FE tests trong feature này (spec chỉ yêu cầu BE tests).
