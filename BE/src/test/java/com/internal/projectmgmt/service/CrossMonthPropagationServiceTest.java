package com.internal.projectmgmt.service;

import com.internal.projectmgmt.dto.monthlyrecord.CrossMonthPropagationResult;
import com.internal.projectmgmt.entity.Project;
import com.internal.projectmgmt.entity.ProjectMonthRecord;
import com.internal.projectmgmt.repository.FieldChangeAuditLogRepository;
import com.internal.projectmgmt.repository.ProjectMonthRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * Unit tests for CrossMonthPropagationService.
 *
 * Tests T012–T015, T020 (idempotency — requires T022 impl), T026, T027.
 */
@ExtendWith(MockitoExtension.class)
class CrossMonthPropagationServiceTest {

    @Mock
    private ProjectMonthRecordRepository repo;

    @Mock
    private MonthlyCalculationService calculationService;

    @Mock
    private FieldChangeAuditLogRepository auditLogRepository;

    @InjectMocks
    private CrossMonthPropagationService service;

    private Project project;
    private static final String EVENT_ID = "test-event-001";

    @BeforeEach
    void setUp() {
        project = new Project();
        project.setId(UUID.randomUUID());
        project.setPrice(BigDecimal.valueOf(1_500_000));
        project.setMonthStart("01/2026");
        project.setMonthEnd("12/2026");

        // Default: eventId not yet processed (no idempotency skip)
        lenient().when(auditLogRepository.existsByEventId(anyString())).thenReturn(false);
    }

    // ---- builder helpers ----

    private ProjectMonthRecord buildRecord(String monthKey) {
        return ProjectMonthRecord.builder()
                .id(UUID.randomUUID())
                .project(project)
                .monthKey(monthKey)
                .active(true)
                .locked(false)
                .build();
    }

    private void setG6(ProjectMonthRecord r, BigDecimal raTon, BigDecimal tonHt,
            BigDecimal tonDd, BigDecimal osTon, BigDecimal osTonHt, BigDecimal slsxTon) {
        r.setG6RaTon(raTon);
        r.setG6SlsxTonHt(tonHt);
        r.setG6SlsxTonDd(tonDd);
        r.setG6SlsxOsTon(osTon);
        r.setG6SlsxOsTonHt(osTonHt);
        r.setG6SlsxTon(slsxTon);
    }

    /**
     * Simulates MonthlyCalculationService.calculateAndFill() setting G6 fields
     * based on the supplied values. Used with doAnswer to mock the side-effect.
     */
    private void simulateCalculate(ProjectMonthRecord next,
            BigDecimal g6RaTon, BigDecimal g6TonHt, BigDecimal g6TonDd,
            BigDecimal g6OsTon, BigDecimal g6OsTonHt, BigDecimal g6SlsxTon) {
        doAnswer(inv -> {
            ProjectMonthRecord r = inv.getArgument(0);
            setG6(r, g6RaTon, g6TonHt, g6TonDd, g6OsTon, g6OsTonHt, g6SlsxTon);
            return null;
        }).when(calculationService).calculateAndFill(eq(next), any(BigDecimal.class));
    }

    // ============================================================
    // T012: Edit first month → G6 changes → 2 subsequent months updated
    // ============================================================

    @Test
    @DisplayName("T012: propagateFrom first month → 2 downstream months updated (affectedMonths=2)")
    void t012_editFirstMonth_twoMonthsUpdated() {
        // Origin (Jan 2026) — already has G6 values set
        ProjectMonthRecord jan = buildRecord("2026-01");
        setG6(jan, bd("100"), bd("50"), bd("30"), bd("20"), bd("10"), bd("110"));

        // Feb 2026 — G6 currently null
        ProjectMonthRecord feb = buildRecord("2026-02");

        // Mar 2026 — G6 currently null
        ProjectMonthRecord mar = buildRecord("2026-03");

        // After calculation, Feb G6 changes (non-zero)
        simulateCalculate(feb, bd("90"), bd("45"), bd("25"), bd("15"), bd("8"), bd("93"));

        // After calculation, Mar G6 changes (non-zero)
        simulateCalculate(mar, bd("80"), bd("40"), bd("20"), bd("12"), bd("6"), bd("78"));

        when(repo.findByProjectIdAndMonthKey(project.getId(), "2026-02")).thenReturn(Optional.of(feb));
        when(repo.findByProjectIdAndMonthKey(project.getId(), "2026-03")).thenReturn(Optional.of(mar));
        when(repo.findByProjectIdAndMonthKey(project.getId(), "2026-04")).thenReturn(Optional.empty());

        CrossMonthPropagationResult result = service.propagateFrom(jan, EVENT_ID);

        assertThat(result.getAffectedMonthKeys()).containsExactly("2026-02", "2026-03");
        verify(repo).save(feb);
        verify(repo).save(mar);
        verify(repo, never()).save(jan);
    }

    // ============================================================
    // T013: Edit middle month → only later months affected, not earlier
    // ============================================================

    @Test
    @DisplayName("T013: propagateFrom middle month (Feb) → only Mar affected; Jan not touched")
    void t013_editMiddleMonth_onlyLaterAffected() {
        ProjectMonthRecord feb = buildRecord("2026-02");
        setG6(feb, bd("100"), bd("50"), bd("30"), bd("20"), bd("10"), bd("110"));

        ProjectMonthRecord mar = buildRecord("2026-03");
        simulateCalculate(mar, bd("90"), bd("45"), bd("25"), bd("15"), bd("8"), bd("93"));

        when(repo.findByProjectIdAndMonthKey(project.getId(), "2026-03")).thenReturn(Optional.of(mar));
        when(repo.findByProjectIdAndMonthKey(project.getId(), "2026-04")).thenReturn(Optional.empty());

        CrossMonthPropagationResult result = service.propagateFrom(feb, EVENT_ID);

        assertThat(result.getAffectedMonthKeys()).containsExactly("2026-03");
        verify(repo).save(mar);
        // Jan is never touched — propagation is strictly forward
        verify(repo, never()).findByProjectIdAndMonthKey(project.getId(), "2026-01");
    }

    // ============================================================
    // T014: G6 unchanged after applying G1 update → propagation stops
    // (NO_CHANGE_DETECTED)
    // ============================================================

    @Test
    @DisplayName("T014: G6 of next month unchanged after G1 update → chain stops, no months saved")
    void t014_g6Unchanged_noMonthsUpdated() {
        // Origin's G6 set to specific values
        ProjectMonthRecord jan = buildRecord("2026-01");
        setG6(jan, bd("100"), bd("50"), bd("30"), bd("20"), bd("10"), bd("110"));

        // Feb's G6 before applying Jan's G6 is already {0,0,0,0,0,0}
        ProjectMonthRecord feb = buildRecord("2026-02");
        // After calculation, Feb G6 stays {0,0,0,0,0,0} (unchanged from before)
        simulateCalculate(feb, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

        when(repo.findByProjectIdAndMonthKey(project.getId(), "2026-02")).thenReturn(Optional.of(feb));

        CrossMonthPropagationResult result = service.propagateFrom(jan, EVENT_ID);

        // G6 before (all null→ZERO) == G6 after (all ZERO) → NO_CHANGE_DETECTED
        assertThat(result.getAffectedMonthKeys()).isEmpty();
        verify(repo, never()).save(any(ProjectMonthRecord.class));
    }

    // ============================================================
    // T015: DB error at T+1 → exception propagates (caller's @Transactional rolls
    // back)
    // ============================================================

    @Test
    @DisplayName("T015: DB error when saving T+1 → RuntimeException propagates from propagateFrom")
    void t015_dbErrorAtNextMonth_exceptionPropagates() {
        ProjectMonthRecord jan = buildRecord("2026-01");
        setG6(jan, bd("100"), bd("50"), bd("30"), bd("20"), bd("10"), bd("110"));

        ProjectMonthRecord feb = buildRecord("2026-02");
        simulateCalculate(feb, bd("90"), bd("45"), bd("25"), bd("15"), bd("8"), bd("93"));

        when(repo.findByProjectIdAndMonthKey(project.getId(), "2026-02")).thenReturn(Optional.of(feb));
        when(repo.save(feb)).thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> service.propagateFrom(jan, EVENT_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB error");
    }

    // ============================================================
    // T026: locked=true stopping condition
    // ============================================================

    @Test
    @DisplayName("T026: propagation stops when next month is locked=true")
    void t026_lockedMonthStopsChain() {
        ProjectMonthRecord jan = buildRecord("2026-01");
        setG6(jan, bd("100"), bd("50"), bd("30"), bd("20"), bd("10"), bd("110"));

        ProjectMonthRecord feb = buildRecord("2026-02");
        feb.setLocked(true);

        when(repo.findByProjectIdAndMonthKey(project.getId(), "2026-02")).thenReturn(Optional.of(feb));

        CrossMonthPropagationResult result = service.propagateFrom(jan, EVENT_ID);

        assertThat(result.getAffectedMonthKeys()).isEmpty();
        verify(repo, never()).save(any(ProjectMonthRecord.class));
        verify(calculationService, never()).calculateAndFill(any(), any());
    }

    // ============================================================
    // T027: active=false stopping condition
    // ============================================================

    @Test
    @DisplayName("T027: propagation stops when next month is active=false")
    void t027_inactiveMonthStopsChain() {
        ProjectMonthRecord jan = buildRecord("2026-01");
        setG6(jan, bd("100"), bd("50"), bd("30"), bd("20"), bd("10"), bd("110"));

        ProjectMonthRecord feb = buildRecord("2026-02");
        feb.setActive(false);

        when(repo.findByProjectIdAndMonthKey(project.getId(), "2026-02")).thenReturn(Optional.of(feb));

        CrossMonthPropagationResult result = service.propagateFrom(jan, EVENT_ID);

        assertThat(result.getAffectedMonthKeys()).isEmpty();
        verify(repo, never()).save(any(ProjectMonthRecord.class));
        verify(calculationService, never()).calculateAndFill(any(), any());
    }

    // ============================================================
    // T020: Idempotent replay → affectedMonths=0, no new audit rows
    // ============================================================

    @Test
    @DisplayName("T020: replaying same eventId returns empty result without processing")
    void t020_idempotentReplay_returnsEmpty() {
        ProjectMonthRecord jan = buildRecord("2026-01");
        setG6(jan, bd("100"), bd("50"), bd("30"), bd("20"), bd("10"), bd("110"));

        // auditLogRepository already has this eventId
        when(auditLogRepository.existsByEventId(EVENT_ID)).thenReturn(true);

        CrossMonthPropagationResult result = service.propagateFrom(jan, EVENT_ID);

        assertThat(result.getAffectedMonthKeys()).isEmpty();
        verify(repo, never()).findByProjectIdAndMonthKey(any(), any());
        verify(repo, never()).save(any());
        verify(calculationService, never()).calculateAndFill(any(), any());
    }

    // ---- helper ----

    private BigDecimal bd(String s) {
        return new BigDecimal(s);
    }
}
