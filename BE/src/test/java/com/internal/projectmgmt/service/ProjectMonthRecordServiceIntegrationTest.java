package com.internal.projectmgmt.service;

import com.internal.projectmgmt.entity.Project;
import com.internal.projectmgmt.entity.ProjectMonthRecord;
import com.internal.projectmgmt.mapper.ProjectMonthRecordMapper;
import com.internal.projectmgmt.repository.ProjectMonthRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration-scenario tests for ProjectMonthRecordService.
 * Validates cross-method behavior (cascade, generate, reactivate, shrink)
 * per constitution §4.4 cross-module integration requirements.
 *
 * Uses Mockito rather than @SpringBootTest because the project has no
 * in-memory DB dependency; full DB integration is performed via manual/CI
 * testing.
 */
@ExtendWith(MockitoExtension.class)
class ProjectMonthRecordServiceIntegrationTest {

    @Mock
    private ProjectMonthRecordRepository repo;
    @Mock
    private ProjectMonthRecordMapper mapper;
    @Mock
    private MonthlyCalculationService calculationService;

    @InjectMocks
    private ProjectMonthRecordService service;

    private Project project;

    @BeforeEach
    void setUp() {
        project = new Project();
        project.setId(UUID.randomUUID());
        project.setPrice(BigDecimal.valueOf(1_500_000));
        project.setMonthStart("01/2026");
        project.setMonthEnd("03/2026");
    }

    // ---- (a) GENERATE: 3 months → 3 records created ----

    @Test
    @DisplayName("generateRecordsForProject: creates 3 new records for 3-month project")
    void generate_createsThreeNewRecords() {
        // None exist yet
        when(repo.findByProjectIdAndMonthKey(any(), anyString())).thenReturn(Optional.empty());

        service.generateRecordsForProject(project);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ProjectMonthRecord>> captor = ArgumentCaptor.forClass(List.class);
        verify(repo).saveAll(captor.capture());

        List<ProjectMonthRecord> saved = captor.getValue();
        assertThat(saved).hasSize(3);
        assertThat(saved).extracting(ProjectMonthRecord::getMonthKey)
                .containsExactlyInAnyOrder("2026-01", "2026-02", "2026-03");
        assertThat(saved).allMatch(ProjectMonthRecord::isActive);
    }

    // ---- (b) REACTIVATE: inactive record reactivated (not duplicated) ----

    @Test
    @DisplayName("generateRecordsForProject: reactivates an inactive record instead of creating duplicate")
    void generate_reactivatesInactiveRecord() {
        ProjectMonthRecord inactive = ProjectMonthRecord.builder()
                .project(project).monthKey("2026-02").active(false).build();

        when(repo.findByProjectIdAndMonthKey(any(), eq("2026-01"))).thenReturn(Optional.empty());
        when(repo.findByProjectIdAndMonthKey(any(), eq("2026-02"))).thenReturn(Optional.of(inactive));
        when(repo.findByProjectIdAndMonthKey(any(), eq("2026-03"))).thenReturn(Optional.empty());

        service.generateRecordsForProject(project);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ProjectMonthRecord>> captor = ArgumentCaptor.forClass(List.class);
        verify(repo).saveAll(captor.capture());

        List<ProjectMonthRecord> saved = captor.getValue();
        // Should contain the reactivated 2026-02 + 2 new ones
        assertThat(saved).hasSize(3);
        boolean hasReactivated = saved.stream()
                .anyMatch(r -> "2026-02".equals(r.getMonthKey()) && r.isActive());
        assertThat(hasReactivated).isTrue();
    }

    // ---- (c) ACTIVE SKIP: active record is not added to saveAll ----

    @Test
    @DisplayName("generateRecordsForProject: skips records that are already active")
    void generate_skipsExistingActiveRecords() {
        ProjectMonthRecord active = ProjectMonthRecord.builder()
                .project(project).monthKey("2026-01").active(true).build();

        when(repo.findByProjectIdAndMonthKey(any(), eq("2026-01"))).thenReturn(Optional.of(active));
        when(repo.findByProjectIdAndMonthKey(any(), eq("2026-02"))).thenReturn(Optional.empty());
        when(repo.findByProjectIdAndMonthKey(any(), eq("2026-03"))).thenReturn(Optional.empty());

        service.generateRecordsForProject(project);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ProjectMonthRecord>> captor = ArgumentCaptor.forClass(List.class);
        verify(repo).saveAll(captor.capture());

        List<ProjectMonthRecord> saved = captor.getValue();
        // Only 2 new ones; active 2026-01 is not added
        assertThat(saved).hasSize(2);
        assertThat(saved).extracting(ProjectMonthRecord::getMonthKey)
                .doesNotContain("2026-01");
    }

    // ---- (d) SHRINK: markInactiveForShrink sets active=false for targeted months
    // ----

    @Test
    @DisplayName("markInactiveForShrink: targeted months are set to inactive")
    void shrink_marksTargetedRecordsInactive() {
        ProjectMonthRecord r1 = ProjectMonthRecord.builder()
                .project(project).monthKey("2026-01").active(true).build();
        ProjectMonthRecord r3 = ProjectMonthRecord.builder()
                .project(project).monthKey("2026-03").active(true).build();

        when(repo.findByProjectIdAndMonthKey(any(), eq("2026-01"))).thenReturn(Optional.of(r1));
        when(repo.findByProjectIdAndMonthKey(any(), eq("2026-03"))).thenReturn(Optional.of(r3));

        service.markInactiveForShrink(project.getId(), List.of("2026-01", "2026-03"));

        assertThat(r1.isActive()).isFalse();
        assertThat(r3.isActive()).isFalse();
        verify(repo, times(2)).save(any(ProjectMonthRecord.class));
    }

    // ---- (e) CASCADE: closing stock cascades to next month opening stock ----

    @Test
    @DisplayName("cascadeClosingToNextMonth: g6 fields are copied to g1 of next month")
    void cascade_copiesG6ToG1OfNextMonth() {
        ProjectMonthRecord current = ProjectMonthRecord.builder()
                .project(project).monthKey("2026-01").active(true).build();
        current.setG6RaTon(BigDecimal.valueOf(100));
        current.setG6SlsxTonHt(BigDecimal.valueOf(200));
        current.setG6SlsxTonDd(BigDecimal.valueOf(50));
        current.setG6SlsxOsTon(BigDecimal.valueOf(30));
        current.setG6SlsxOsTonHt(BigDecimal.valueOf(20));

        ProjectMonthRecord next = ProjectMonthRecord.builder()
                .project(project).monthKey("2026-02").active(true).build();

        // Stub findById for update() — needed to mock findById call
        when(repo.findById(any())).thenReturn(Optional.of(current));
        when(repo.findByProjectIdAndMonthKey(any(), eq("2026-02"))).thenReturn(Optional.of(next));
        doNothing().when(mapper).applyRequest(any(), any());
        when(mapper.toResponse(any(), anyBoolean())).thenReturn(null);

        service.update(current.getId(), null);

        // Verify cascade fields were set on next month
        assertThat(next.getG1RaTon()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(next.getG1SlsxTonTuSxHtHd()).isEqualByComparingTo(BigDecimal.valueOf(200));
        assertThat(next.getG1SlsxTonTuSxDdHd()).isEqualByComparingTo(BigDecimal.valueOf(50));
        assertThat(next.getG1SlsxOsTon()).isEqualByComparingTo(BigDecimal.valueOf(30));
        assertThat(next.getG1SlsxOsTonHt()).isEqualByComparingTo(BigDecimal.valueOf(20));

        // g1SlsxTonTuSxHd is NOT cascaded — should be null (untouched)
        assertThat(next.getG1SlsxTonTuSxHd()).isNull();
    }
}
