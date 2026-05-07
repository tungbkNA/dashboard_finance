package com.internal.projectmgmt.service;

import com.internal.projectmgmt.dto.monthlyrecord.CrossMonthPropagationResult;
import com.internal.projectmgmt.entity.FieldChangeAuditLog;
import com.internal.projectmgmt.entity.ProjectMonthRecord;
import com.internal.projectmgmt.repository.FieldChangeAuditLogRepository;
import com.internal.projectmgmt.repository.ProjectMonthRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Propagates G6 → G1 cascade across consecutive months for a project.
 * Runs inside the caller's transaction (default REQUIRED propagation).
 *
 * G6→G1 mapping (5 fields per FR-008, g1SlsxTonTuSxHd intentionally excluded):
 * g6RaTon → g1RaTon
 * g6SlsxTonHt → g1SlsxTonTuSxHtHd
 * g6SlsxTonDd → g1SlsxTonTuSxDdHd
 * g6SlsxOsTon → g1SlsxOsTon
 * g6SlsxOsTonHt → g1SlsxOsTonHt
 */
@Service
@RequiredArgsConstructor
public class CrossMonthPropagationService {

    private final ProjectMonthRecordRepository repo;
    private final MonthlyCalculationService calculationService;
    private final FieldChangeAuditLogRepository auditLogRepository;

    /**
     * Propagates the closing G6 values of {@code origin} forward through
     * subsequent months until a stopping condition is reached.
     *
     * @param origin  the record that was just saved (G6 already recalculated)
     * @param eventId idempotency key for this update event
     * @return result containing all affected monthKeys
     */
    @Transactional
    public CrossMonthPropagationResult propagateFrom(ProjectMonthRecord origin, String eventId) {
        // T022: idempotency — skip if this event was already processed
        if (auditLogRepository.existsByEventId(eventId)) {
            return CrossMonthPropagationResult.builder().affectedMonthKeys(List.of()).build();
        }

        List<String> affected = new ArrayList<>();
        ProjectMonthRecord current = origin;

        while (true) {
            String nextMk = YearMonth.parse(current.getMonthKey()).plusMonths(1).toString();
            Optional<ProjectMonthRecord> nextOpt = repo.findByProjectIdAndMonthKey(current.getProject().getId(),
                    nextMk);

            if (nextOpt.isEmpty()) {
                break; // StoppedReason.NO_MORE_MONTHS
            }

            ProjectMonthRecord next = nextOpt.get();

            if (!next.isActive()) {
                break; // StoppedReason.INACTIVE_MONTH
            }

            if (next.isLocked()) {
                break; // StoppedReason.LOCKED_MONTH
            }

            BigDecimal[] g6Before = snapshotG6(next);
            applyG6ToG1(current, next);
            calculationService.calculateAndFill(next, next.getProject().getPrice());
            BigDecimal[] g6After = snapshotG6(next);

            if (g6Unchanged(g6Before, g6After)) {
                break; // StoppedReason.NO_CHANGE_DETECTED — G1 may have changed but chain stops here
            }

            repo.save(next);
            writeAuditLogs(origin, next, g6Before, g6After, eventId);
            affected.add(next.getMonthKey());
            current = next;
        }

        return CrossMonthPropagationResult.builder()
                .affectedMonthKeys(affected)
                .build();
    }

    // ---- helpers ----

    /**
     * Applies G6 closing values from {@code source} to G1 opening fields of
     * {@code target} (5-field mapping per FR-008).
     */
    void applyG6ToG1(ProjectMonthRecord source, ProjectMonthRecord target) {
        target.setG1RaTon(source.getG6RaTon());
        target.setG1SlsxTonTuSxHd(source.getG6SlsxTon());
        target.setG1SlsxTonTuSxHtHd(source.getG6SlsxTonHt());
        target.setG1SlsxTonTuSxDdHd(source.getG6SlsxTonDd());
        target.setG1SlsxOsTon(source.getG6SlsxOsTon());
        target.setG1SlsxOsTonHt(source.getG6SlsxOsTonHt());
    }

    /** Takes a snapshot of all 6 G6 fields from the given record. */
    BigDecimal[] snapshotG6(ProjectMonthRecord r) {
        return new BigDecimal[] {
                r.getG6RaTon(),
                r.getG6SlsxTonHt(),
                r.getG6SlsxTonDd(),
                r.getG6SlsxOsTon(),
                r.getG6SlsxOsTonHt(),
                r.getG6SlsxTon()
        };
    }

    /**
     * Returns true if all 6 G6 fields are unchanged.
     * Uses BigDecimal.compareTo() to ignore scale differences.
     */
    boolean g6Unchanged(BigDecimal[] before, BigDecimal[] after) {
        for (int i = 0; i < before.length; i++) {
            BigDecimal b = before[i] == null ? BigDecimal.ZERO : before[i];
            BigDecimal a = after[i] == null ? BigDecimal.ZERO : after[i];
            if (b.compareTo(a) != 0)
                return false;
        }
        return true;
    }

    /**
     * T021: Writes one FieldChangeAuditLog record per changed G6 field.
     */
    private void writeAuditLogs(ProjectMonthRecord origin, ProjectMonthRecord affected,
            BigDecimal[] g6Before, BigDecimal[] g6After, String eventId) {
        String[] fieldNames = {
                "g6RaTon", "g6SlsxTonHt", "g6SlsxTonDd", "g6SlsxOsTon", "g6SlsxOsTonHt", "g6SlsxTon"
        };
        for (int i = 0; i < fieldNames.length; i++) {
            BigDecimal before = g6Before[i];
            BigDecimal after = g6After[i];
            BigDecimal bNorm = before == null ? BigDecimal.ZERO : before;
            BigDecimal aNorm = after == null ? BigDecimal.ZERO : after;
            if (bNorm.compareTo(aNorm) != 0) {
                auditLogRepository.save(FieldChangeAuditLog.builder()
                        .projectId(affected.getProject().getId())
                        .monthKey(affected.getMonthKey())
                        .fieldName(fieldNames[i])
                        .oldValue(before == null ? null : before.toPlainString())
                        .newValue(after == null ? null : after.toPlainString())
                        .triggeredByMonthKey(origin.getMonthKey())
                        .eventId(eventId)
                        .build());
            }
        }
    }
}
