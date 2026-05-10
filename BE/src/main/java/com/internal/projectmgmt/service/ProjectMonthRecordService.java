package com.internal.projectmgmt.service;

import com.internal.projectmgmt.dto.monthlyrecord.CrossMonthPropagationResult;
import com.internal.projectmgmt.dto.monthlyrecord.FieldMetadataResponse;
import com.internal.projectmgmt.dto.monthlyrecord.ProjectMonthRecordRequest;
import com.internal.projectmgmt.dto.monthlyrecord.ProjectMonthRecordResponse;
import com.internal.projectmgmt.dto.monthlyrecord.ProjectMonthRecordSummaryResponse;
import com.internal.projectmgmt.entity.Project;
import com.internal.projectmgmt.entity.ProjectMonthRecord;
import com.internal.projectmgmt.exception.AppException;
import com.internal.projectmgmt.mapper.ProjectMonthRecordMapper;
import com.internal.projectmgmt.repository.ProjectMonthRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectMonthRecordService {

    private final ProjectMonthRecordRepository repo;
    private final ProjectMonthRecordMapper mapper;
    private final MonthlyCalculationService calculationService;
    private final CrossMonthPropagationService propagationService;

    // ===================== US1: CRUD =====================

    @Transactional(readOnly = true)
    public List<ProjectMonthRecordResponse> findAllByMonthKey(String monthKey) {
        return repo.findByMonthKeyAndActiveTrue(monthKey).stream()
                .map(r -> mapper.toResponse(r, isFirstMonth(r)))
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ProjectMonthRecordResponse> search(String keyword, String monthKey, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "project.projectCode"));
        String kw = keyword == null ? "" : keyword.trim();
        String mk = (monthKey == null || monthKey.isBlank()) ? null : monthKey.trim();
        Page<ProjectMonthRecord> result = repo.searchRecords(kw, mk, pageable);
        return result.map(r -> mapper.toResponse(r, isFirstMonth(r)));
    }

    @Transactional(readOnly = true)
    public ProjectMonthRecordResponse findById(UUID id) {
        ProjectMonthRecord record = repo.findById(id)
                .orElseThrow(() -> new AppException("MONTHLY_RECORD_NOT_FOUND", "Bản ghi tháng không tồn tại"));
        boolean isFirst = isFirstMonth(record);
        return mapper.toResponse(record, isFirst);
    }

    @Transactional
    public ProjectMonthRecordResponse update(UUID id, ProjectMonthRecordRequest request) {
        ProjectMonthRecord record = repo.findById(id)
                .orElseThrow(() -> new AppException("MONTHLY_RECORD_NOT_FOUND", "Bản ghi tháng không tồn tại"));

        if (!record.isActive()) {
            throw new AppException("MONTHLY_RECORD_INACTIVE", "Bản ghi tháng này không còn hoạt động (inactive)");
        }

        if (record.isLocked()) {
            throw new AppException("MONTHLY_RECORD_LOCKED", "Bản ghi tháng này đã bị khóa và không thể chỉnh sửa");
        }

        // Snapshot G6 before applying request
        BigDecimal[] g6Before = propagationService.snapshotG6(record);

        // Apply manual fields from request
        mapper.applyRequest(request, record);

        // Recalculate all formula fields (always on write path)
        calculationService.calculateAndFill(record, record.getProject().getPrice());

        repo.save(record);

        // Snapshot G6 after save and propagate if G6 changed
        BigDecimal[] g6After = propagationService.snapshotG6(record);
        String eventId = java.util.UUID.randomUUID().toString();
        CrossMonthPropagationResult propagation;
        if (propagationService.g6Unchanged(g6Before, g6After)) {
            propagation = CrossMonthPropagationResult.builder().affectedMonthKeys(List.of()).build();
        } else {
            propagation = propagationService.propagateFrom(record, eventId);
        }

        boolean isFirst = isFirstMonth(record);
        return mapper.toResponse(record, isFirst, propagation.getAffectedMonthKeys().size());
    }

    // ===================== US2: Generate / Inactive =====================

    @Transactional
    public void generateRecordsForProject(Project project) {
        YearMonth start = parseMonthYear(project.getMonthStart());
        YearMonth end = parseMonthYear(project.getMonthEnd());

        List<ProjectMonthRecord> toSave = new ArrayList<>();
        YearMonth current = start;
        while (!current.isAfter(end)) {
            String mk = current.toString();
            ProjectMonthRecord existing = repo.findByProjectIdAndMonthKey(project.getId(), mk).orElse(null);
            if (existing == null) {
                toSave.add(ProjectMonthRecord.builder()
                        .project(project)
                        .monthKey(mk)
                        .active(true)
                        .build());
            } else if (!existing.isActive()) {
                existing.setActive(true);
                toSave.add(existing);
            }
            // active + exists → skip
            current = current.plusMonths(1);
        }
        repo.saveAll(toSave);
    }

    @Transactional
    public void markInactiveForShrink(UUID projectId, List<String> monthKeys) {
        for (String mk : monthKeys) {
            repo.findByProjectIdAndMonthKey(projectId, mk).ifPresent(r -> {
                r.setActive(false);
                repo.save(r);
            });
        }
    }

    @Transactional
    public void softDeleteByProjectId(UUID projectId) {
        List<ProjectMonthRecord> records = repo.findByProjectIdOrderByMonthKeyAsc(projectId);
        for (ProjectMonthRecord r : records) {
            r.setActive(false);
        }
        repo.saveAll(records);
    }

    /**
     * Computes the list of monthKeys that are currently active for a project
     * but fall outside the new [newStart, newEnd] range.
     */
    public List<String> computeShrinkMonthKeys(UUID projectId, String newStart, String newEnd) {
        YearMonth start = parseMonthYear(newStart);
        YearMonth end = parseMonthYear(newEnd);
        return repo.findByProjectIdAndActiveTrue(projectId).stream()
                .map(ProjectMonthRecord::getMonthKey)
                .filter(mk -> {
                    YearMonth ym = YearMonth.parse(mk);
                    return ym.isBefore(start) || ym.isAfter(end);
                })
                .toList();
    }

    // ===================== US4: Field Metadata (static) =====================

    private boolean isFirstMonth(ProjectMonthRecord record) {
        YearMonth projectStart = parseMonthYear(record.getProject().getMonthStart());
        YearMonth recordMonth = YearMonth.parse(record.getMonthKey());
        return recordMonth.equals(projectStart);
    }

    // ===================== US5: Field Metadata (static) =====================

    public FieldMetadataResponse getFieldMetadata() {
        return FieldMetadataResponse.builder()
                .groups(List.of(
                        FieldMetadataResponse.GroupMetadata.builder()
                                .groupId("g1").groupName("Tồn đầu kỳ")
                                .manualFields(List.of(
                                        "g1RaTon", "g1SlsxTonTuSxHd", "g1SlsxTonTuSxHtHd",
                                        "g1SlsxTonTuSxDdHd", "g1SlsxOsTon", "g1SlsxOsTonHt"))
                                .formulaFields(List.of())
                                .cascadedFromPrevMonthFields(List.of(
                                        "g1RaTon", "g1SlsxTonTuSxHd", "g1SlsxTonTuSxHtHd",
                                        "g1SlsxTonTuSxDdHd", "g1SlsxOsTon", "g1SlsxOsTonHt"))
                                .build(),
                        FieldMetadataResponse.GroupMetadata.builder()
                                .groupId("g2").groupName("Kế hoạch tháng")
                                .manualFields(List.of(
                                        "g2Headcount", "g2Ra", "g2SlsxTuSx", "g2SlsxOs", "g2LienKet",
                                        "g2SlsxTuSxHtTrongThang", "g2SlsxTuSxDd", "g2SlsxOsHt",
                                        "g2SlsxOsDd", "g2Cpbqtb", "g2TySuatLng"))
                                .formulaFields(List.of("g2TongSlsxDuKien"))
                                .cascadedFromPrevMonthFields(List.of())
                                .build(),
                        FieldMetadataResponse.GroupMetadata.builder()
                                .groupId("g3").groupName("Thực hiện SLSX đến NGÀY")
                                .manualFields(List.of(
                                        "g3Ra", "g3TongSlsxHd", "g3SlsxTuSxHt",
                                        "g3SlsxTuSxDd", "g3SlsxOsDd", "g3SlsxOsTonHt"))
                                .formulaFields(List.of("g3Ee"))
                                .cascadedFromPrevMonthFields(List.of())
                                .build(),
                        FieldMetadataResponse.GroupMetadata.builder()
                                .groupId("g4").groupName("Kế hoạch doanh thu")
                                .manualFields(List.of(
                                        "g4TuSlsxTonHt", "g4TuSlsxTrongThang", "g4SlsxOsTon",
                                        "g4SlsxOsTrongThang", "g4Lk", "g4TiSuatLngDuKien", "g4LngDuKien"))
                                .formulaFields(List.of("g4Tong", "g4DoanhThu"))
                                .cascadedFromPrevMonthFields(List.of())
                                .build(),
                        FieldMetadataResponse.GroupMetadata.builder()
                                .groupId("g5").groupName("Thực hiện nghiệm thu")
                                .manualFields(List.of(
                                        "g5RaTuongUngSlnt", "g5NtSlsxTonHt", "g5NtSlsxTrongThang",
                                        "g5NtSlsxOsTon", "g5NtSlsxOsTrongThang", "g5TiSuatLng", "g5LngVnd"))
                                .formulaFields(List.of("g5TongSlnt", "g5DoanhThu"))
                                .cascadedFromPrevMonthFields(List.of())
                                .build(),
                        FieldMetadataResponse.GroupMetadata.builder()
                                .groupId("g6").groupName("Tồn cuối kỳ")
                                .manualFields(List.of())
                                .formulaFields(List.of(
                                        "g6RaTon", "g6SlsxTonHt", "g6SlsxTonDd",
                                        "g6SlsxOsTon", "g6SlsxOsTonHt", "g6SlsxTon"))
                                .cascadedFromPrevMonthFields(List.of())
                                .build()))
                .build();
    }

    // ---- helpers ----

    /**
     * Parses mm/yyyy format (used by Project.monthStart/monthEnd) into YearMonth.
     */
    private YearMonth parseMonthYear(String mmYyyy) {
        String[] parts = mmYyyy.split("/");
        int month = Integer.parseInt(parts[0]);
        int year = Integer.parseInt(parts[1]);
        return YearMonth.of(year, month);
    }
}
