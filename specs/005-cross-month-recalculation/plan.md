# Implementation Plan: Feature 005 — Cross-Month Recalculation

**Branch**: `005-cross-month-recalculation` | **Date**: 2026-05-07 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `specs/005-cross-month-recalculation/spec.md`

## Summary

Xây dựng cơ chế **cập nhật cascade liên tháng** khi user thay đổi dữ liệu nhập tay của một bản ghi tháng. Sau khi user xác nhận lưu, hệ thống tự động tính lại toàn bộ chain T → T+1 → T+2 → ... trong một transaction duy nhất, ghi audit trail cho mỗi thay đổi, và trả về số tháng bị ảnh hưởng để FE hiển thị toast. FE bổ sung ConfirmDialog trước khi ghi đè dữ liệu đã tồn tại.

**Technical approach** (từ research.md):
- Xử lý **đồng bộ** trong cùng `@Transactional` — không dùng async queue.
- Thêm `CrossMonthPropagationService` với phương thức `propagateFrom(originRecord, eventId)`.
- Thêm cột `locked BOOLEAN` vào `project_monthly_record`; thêm bảng `field_change_audit_log`.
- Thêm `affectedMonths: int` vào `ProjectMonthRecordResponse`.
- FE: ConfirmDialog trước save nếu group có field non-null; Toast với affected count.

## Technical Context

**Backend Language/Version**: Java 21 / Spring Boot 3.4.5
**Frontend Language/Version**: TypeScript 5.x / Vue 3.5.x + Vite 6.x
**Primary Dependencies (BE)**: Spring Boot Starter Web/Data JPA/Validation, Maven, PostgreSQL, Flyway, Lombok
**Primary Dependencies (FE)**: Vue Router, Pinia, PrimeVue 4.3.x Aura, PrimeIcons
**Storage**: PostgreSQL 15+
**Testing (BE)**: JUnit 5 / Spring Boot Test (`@SpringBootTest` + H2 or PostgreSQL for integration)
**Testing (FE)**: Not in scope for this feature (spec only requires BE tests)
**Project Type**: web-service (BE) + web-app (FE)
**Performance Goals**: Propagation chain ≤ 24 tháng — không cần async; response trong vài trăm ms là chấp nhận được
**Constraints**: BigDecimal cho tất cả trường tài chính; formula fields read-only in FE; cross-month updates require user confirmation; locked months không bị ghi đè bởi propagation
**Scale/Scope**: Single-project propagation mỗi lần; tối đa 24 records per chain

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-checked after Phase 1 design.*

| Gate | Requirement (from constitution) | Status |
|------|---------------------------------|--------|
| §2.2 BE là nguồn sự thật | Tất cả công thức + propagation logic nằm trong BE service | ✅ PASS |
| §4.3 BigDecimal | Tất cả trường tài chính mới (`old_value`/`new_value` stored as TEXT nhưng parse từ BigDecimal) | ✅ PASS |
| §4.4 Kiểm thử | 5 test scenarios được yêu cầu trong spec + test có trong feature | ✅ PASS |
| §4.5 Nhất quán liên tháng | Toàn bộ propagation chain trong một `@Transactional` | ✅ PASS |
| §5.3 Dialog xác nhận | FE hiển thị ConfirmDialog trước khi ghi đè data đã có | ✅ PASS |
| §5.3 Toast notification | FE hiển thị toast kết quả với số tháng bị ảnh hưởng | ✅ PASS |
| §5.4 Trường công thức read-only | Formula fields vẫn read-only (không thay đổi) | ✅ PASS |
| §6.3 Response format | `ApiResponse<T>` wrapper vẫn được dùng | ✅ PASS |
| §9.3 Cảnh báo trước khi ghi đè | ConfirmDialog trigger khi field đã có data (FR-001) | ✅ PASS |

**No violations. No Complexity Tracking required.**

## Project Structure

### Documentation (this feature)

```text
specs/005-cross-month-recalculation/
├── plan.md              # This file
├── research.md          # ✅ Phase 0 output
├── data-model.md        # ✅ Phase 1 output
├── contracts/
│   └── update-monthly-record.md   # ✅ Phase 1 output
└── tasks.md             # Phase 2 output (via /speckit.tasks — NOT created here)
```

### Source Code Changes

```text
BE/
├── src/main/resources/db/migration/
│   └── V5__add_locked_and_audit_log.sql          [NEW]
├── src/main/java/com/internal/projectmgmt/
│   ├── entity/
│   │   ├── ProjectMonthRecord.java               [MODIFY: add locked field]
│   │   └── FieldChangeAuditLog.java              [NEW]
│   ├── repository/
│   │   └── FieldChangeAuditLogRepository.java    [NEW]
│   ├── dto/monthlyrecord/
│   │   ├── ProjectMonthRecordResponse.java       [MODIFY: add affectedMonths]
│   │   └── CrossMonthPropagationResult.java      [NEW]
│   ├── service/
│   │   ├── CrossMonthPropagationService.java     [NEW]
│   │   └── ProjectMonthRecordService.java        [MODIFY: use CrossMonthPropagationService]
│   └── mapper/
│       └── ProjectMonthRecordMapper.java         [MODIFY: map affectedMonths]
└── src/test/java/com/internal/projectmgmt/
    └── service/
        └── CrossMonthPropagationServiceTest.java [NEW: 5 test scenarios]

FE/src/
├── types/
│   └── project-monthly-record.ts                [MODIFY: add affectedMonths to ProjectMonthRecordDetail]
├── components/project-management/
│   └── ProjectCard.vue                          [MODIFY: ConfirmDialog + toast with affectedMonths]
└── views/
    └── ProjectManagementView.vue                [MODIFY: onSaved handler with propagation toast]
```

## Design Details

### BE: CrossMonthPropagationService — Core Algorithm

```java
/**
 * THAM CHIẾU ĐẶC TẢ: specs/005-cross-month-recalculation/plan.md
 */
@Service @RequiredArgsConstructor
public class CrossMonthPropagationService {

    // Called AFTER originRecord saved. Runs in caller's transaction (REQUIRED propagation).
    public CrossMonthPropagationResult propagateFrom(ProjectMonthRecord origin, String eventId) {
        List<String> affected = new ArrayList<>();
        ProjectMonthRecord current = origin;
        StoppedReason reason = NO_MORE_MONTHS;
        while (true) {
            String nextMk = YearMonth.parse(current.getMonthKey()).plusMonths(1).toString();
            Optional<ProjectMonthRecord> nextOpt = repo.findByProjectIdAndMonthKey(
                current.getProject().getId(), nextMk);
            if (nextOpt.isEmpty())     { reason = NO_MORE_MONTHS;    break; }
            ProjectMonthRecord next = nextOpt.get();
            if (!next.isActive())      { reason = INACTIVE_MONTH;    break; }
            if (next.isLocked())       { reason = LOCKED_MONTH;      break; }

            BigDecimal[] g6Before = snapshotG6(next);
            applyG6ToG1(current, next);              // cascade G6→G1 (5 fields, NOT g1SlsxTonTuSxHd)
            calculationService.calculateAndFill(next, next.getProject().getPrice());
            BigDecimal[] g6After = snapshotG6(next);

            if (g6Unchanged(g6Before, g6After)) { reason = NO_CHANGE_DETECTED; break; }

            writeAuditLogs(next, g6Before, g6After, origin.getMonthKey(), eventId);
            repo.save(next);
            affected.add(next.getMonthKey());
            current = next;
        }
        return new CrossMonthPropagationResult(affected, reason);
    }
}
```

**G6→G1 cascade mapping** (5 fields, FR-008 compliant — excludes `g1SlsxTonTuSxHd`):

| G6 source | G1 target |
|-----------|-----------|
| `g6RaTon` | `g1RaTon` |
| `g6SlsxTonHt` | `g1SlsxTonTuSxHtHd` |
| `g6SlsxTonDd` | `g1SlsxTonTuSxDdHd` |
| `g6SlsxOsTon` | `g1SlsxOsTon` |
| `g6SlsxOsTonHt` | `g1SlsxOsTonHt` |

> **Note**: `g6SlsxTon → g1SlsxTonTuSxHd` mapping exists in the current `cascadeClosingToNextMonth()` but conflicts with FR-008. The new `propagateFrom` will NOT include this mapping. Field metadata `cascadedFromPrevMonthFields` for G1 must be updated to remove `g1SlsxTonTuSxHd`. This is a reconciliation task.

---

### BE: `update()` modified flow

```
load record → check active → snapshot G6 before
→ applyRequest() → calculateAndFill()
→ save origin → compare G6 before/after
→ if changed: propagationService.propagateFrom(record, eventId)
→ return response with affectedMonths count
```

The existing `cascadeClosingToNextMonth(record)` is **replaced** by `propagationService.propagateFrom()`.

---

### FE: ConfirmDialog in `ProjectCard.vue`

- Use PrimeVue `useConfirm()` composable.
- Trigger: before group save, if `detail.value[field] !== null` for any manual field in that group.
- On confirm → call API → read `data.affectedMonths` → show appropriate toast.
- On cancel / close dialog → return without calling API.

---

## Test Scenarios (BE — CrossMonthPropagationServiceTest)

| # | Scenario | Expected |
|---|----------|----------|
| T1 | Sửa tháng đầu tiên → G6 thay đổi → 3 tháng liên tiếp đều có data | `affectedMonths = 2` (T+1, T+2 được cập nhật) |
| T2 | Sửa tháng giữa (T+1 của chuỗi) → chỉ T+2 bị ảnh hưởng | T trước không thay đổi; T+2 được cập nhật |
| T3 | Sửa trường G2 (không ảnh hưởng G6) | `affectedMonths = 0`; không record nào khác được cập nhật |
| T4 | G6 thay đổi nhưng DB constraint lỗi ở T+1 | `@Transactional` rollback; origin cũng không được save |
| T5 | Submit lại cùng data (idempotent) | `affectedMonths = 0` lần 2; không có audit log mới |
