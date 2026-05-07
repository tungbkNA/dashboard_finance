# Data Model: Feature 005 — Cross-Month Recalculation

**Date**: 2026-05-07
**Feature**: `005-cross-month-recalculation`

---

## Entities Affected / Added

### 1. `project_monthly_record` (modified)

Thêm một cột mới:

| Column | Type | Nullable | Default | Notes |
|--------|------|----------|---------|-------|
| `locked` | `BOOLEAN` | NOT NULL | `false` | Khi `true`, bản ghi tháng không bị ghi đè bởi propagation hoặc price recalculation |

**Java field** (`ProjectMonthRecord.java`):
```java
@Column(name = "locked", nullable = false)
@Builder.Default
private boolean locked = false;
```

---

### 2. `field_change_audit_log` (new table)

Lưu mỗi thay đổi field-level được thực hiện bởi propagation hoặc bởi user trực tiếp.

| Column | Type | Nullable | Default | Notes |
|--------|------|----------|---------|-------|
| `id` | `UUID` | NOT NULL | gen_random_uuid() | PK |
| `project_id` | `UUID` | NOT NULL | — | FK → project(id), NO CASCADE |
| `month_key` | `VARCHAR(7)` | NOT NULL | — | Tháng bị thay đổi (yyyy-MM) |
| `field_name` | `VARCHAR(100)` | NOT NULL | — | Tên Java field (camelCase) |
| `old_value` | `TEXT` | NULL | — | Giá trị cũ (serialize BigDecimal → String) |
| `new_value` | `TEXT` | NULL | — | Giá trị mới (serialize BigDecimal → String) |
| `triggered_by_month_key` | `VARCHAR(7)` | NOT NULL | — | Tháng gốc kích hoạt thay đổi này |
| `event_id` | `VARCHAR(36)` | NOT NULL | — | UUID của event (for idempotency check) |
| `created_at` | `TIMESTAMPTZ` | NOT NULL | NOW() | Thời điểm ghi log |

**Java Entity** (`FieldChangeAuditLog.java`):
```java
@Entity
@Table(name = "field_change_audit_log")
public class FieldChangeAuditLog {
    @Id @GeneratedValue(strategy = GenerationType.UUID) UUID id;
    @Column(name = "project_id", nullable = false) UUID projectId;
    @Column(name = "month_key", nullable = false, length = 7) String monthKey;
    @Column(name = "field_name", nullable = false, length = 100) String fieldName;
    @Column(name = "old_value") String oldValue;
    @Column(name = "new_value") String newValue;
    @Column(name = "triggered_by_month_key", nullable = false, length = 7) String triggeredByMonthKey;
    @Column(name = "event_id", nullable = false, length = 36) String eventId;
    @Column(name = "created_at", nullable = false, updatable = false) OffsetDateTime createdAt;
    @PrePersist void prePersist() { this.createdAt = OffsetDateTime.now(); }
}
```

**Indices**:
- `idx_fcal_event_id` ON `field_change_audit_log(event_id)` — idempotency lookup
- `idx_fcal_project_month` ON `field_change_audit_log(project_id, month_key)` — audit trail per month
- `idx_fcal_triggered_by` ON `field_change_audit_log(triggered_by_month_key)` — trace chain by origin

---

### 3. `CrossMonthPropagationResult` (new DTO, không có bảng)

Kết quả sau khi propagation chain hoàn thành.

```java
public class CrossMonthPropagationResult {
    List<String> affectedMonthKeys;   // Danh sách monthKey của các tháng bị cập nhật (không gồm origin)
    StoppedReason stoppedReason;      // Lý do dừng
    
    enum StoppedReason {
        NO_MORE_MONTHS,       // Không còn bản ghi tháng kế tiếp
        INACTIVE_MONTH,       // Tháng kế tiếp có active=false
        LOCKED_MONTH,         // Tháng kế tiếp có locked=true
        NO_CHANGE_DETECTED    // G6 không thay đổi sau khi propagate
    }
}
```

---

## Relationships

```text
Project (1) ──────────────── (N) ProjectMonthRecord
                                        │
                                        │ project_id + month_key + field_name
                                        ▼
                              FieldChangeAuditLog (N)
```

- `FieldChangeAuditLog.projectId` là plain UUID (không dùng JPA `@ManyToOne`) → tránh lazy loading issues trong audit writer.
- `FieldChangeAuditLog` không có FK constraint ON DELETE CASCADE để bảo tồn audit trail ngay cả khi project bị xóa.

---

## State Transitions

### ProjectMonthRecord.locked

```
false (default) ──[future UI lock action]──► true
true ──[future UI unlock action]──► false
```

- Feature 005 chỉ **đọc** `locked` (stopping condition); không **ghi** `locked` qua API trong feature này.
- Giá trị `locked = true` trong feature này chỉ có thể được set qua SQL trực tiếp (cho testing).

---

## Validation Rules

| Rule | Entity | Field | Constraint |
|------|--------|-------|------------|
| VR-01 | `FieldChangeAuditLog` | `project_id` | Non-null UUID |
| VR-02 | `FieldChangeAuditLog` | `month_key` | Format `yyyy-MM` |
| VR-03 | `FieldChangeAuditLog` | `event_id` | UUID format (36 chars) |
| VR-04 | `ProjectMonthRecord` | `locked` | Propagation skips when `true` |
| VR-05 | `CrossMonthPropagationResult` | `affectedMonthKeys` | Ordered list, không gồm origin monthKey |

---

## Migration Plan

| Version | File | Change |
|---------|------|--------|
| V5 | `V5__add_locked_and_audit_log.sql` | ADD COLUMN `locked` + CREATE TABLE `field_change_audit_log` + indices |
