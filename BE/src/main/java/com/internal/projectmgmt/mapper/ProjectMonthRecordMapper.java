package com.internal.projectmgmt.mapper;

import com.internal.projectmgmt.dto.monthlyrecord.ProjectMonthRecordRequest;
import com.internal.projectmgmt.dto.monthlyrecord.ProjectMonthRecordResponse;
import com.internal.projectmgmt.dto.monthlyrecord.ProjectMonthRecordSummaryResponse;
import com.internal.projectmgmt.entity.Project;
import com.internal.projectmgmt.entity.ProjectMonthRecord;
import org.springframework.stereotype.Component;

@Component
public class ProjectMonthRecordMapper {

    public ProjectMonthRecordSummaryResponse toSummaryResponse(ProjectMonthRecord r) {
        Project p = r.getProject();
        return ProjectMonthRecordSummaryResponse.builder()
                .id(r.getId())
                .projectId(p.getId())
                .projectCode(p.getProjectCode())
                .projectName(p.getProjectName())
                .monthKey(r.getMonthKey())
                .active(r.isActive())
                .customerName(p.getCustomer().getCustomerName())
                .g1SlsxTonTuSxHd(r.getG1SlsxTonTuSxHd())
                .g4DoanhThu(r.getG4DoanhThu())
                .g5DoanhThu(r.getG5DoanhThu())
                .g5TongSlnt(r.getG5TongSlnt())
                .g6SlsxTon(r.getG6SlsxTon())
                .updatedAt(r.getUpdatedAt())
                .build();
    }

    public ProjectMonthRecordResponse toResponse(ProjectMonthRecord r, boolean isFirstMonth) {
        return toResponse(r, isFirstMonth, 0);
    }

    public ProjectMonthRecordResponse toResponse(ProjectMonthRecord r, boolean isFirstMonth, int affectedMonths) {
        Project p = r.getProject();
        return ProjectMonthRecordResponse.builder()
                .id(r.getId())
                .projectId(p.getId())
                .projectCode(p.getProjectCode())
                .projectName(p.getProjectName())
                .monthKey(r.getMonthKey())
                .active(r.isActive())
                .isFirstMonth(isFirstMonth)
                .price(p.getPrice())
                .customerName(p.getCustomer() != null ? p.getCustomer().getCustomerName() : null)
                .representUserName(p.getRepresentUser() != null ? p.getRepresentUser().getDisplayName() : null)
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .affectedMonths(affectedMonths)
                // Nhóm 1
                .g1RaTon(r.getG1RaTon())
                .g1SlsxTonTuSxHd(r.getG1SlsxTonTuSxHd())
                .g1SlsxTonTuSxHtHd(r.getG1SlsxTonTuSxHtHd())
                .g1SlsxTonTuSxDdHd(r.getG1SlsxTonTuSxDdHd())
                .g1SlsxOsTon(r.getG1SlsxOsTon())
                .g1SlsxOsTonHt(r.getG1SlsxOsTonHt())
                // Nhóm 2
                .g2Headcount(r.getG2Headcount())
                .g2Ra(r.getG2Ra())
                .g2SlsxTuSx(r.getG2SlsxTuSx())
                .g2SlsxOs(r.getG2SlsxOs())
                .g2LienKet(r.getG2LienKet())
                .g2TongSlsxDuKien(r.getG2TongSlsxDuKien())
                .g2SlsxTuSxHtTrongThang(r.getG2SlsxTuSxHtTrongThang())
                .g2SlsxTuSxDd(r.getG2SlsxTuSxDd())
                .g2SlsxOsHt(r.getG2SlsxOsHt())
                .g2SlsxOsDd(r.getG2SlsxOsDd())
                .g2Cpbqtb(r.getG2Cpbqtb())
                .g2TySuatLng(r.getG2TySuatLng())
                // Nhóm 3
                .g3Ra(r.getG3Ra())
                .g3TongSlsxHd(r.getG3TongSlsxHd())
                .g3Ee(r.getG3Ee())
                .g3SlsxTuSxHt(r.getG3SlsxTuSxHt())
                .g3SlsxTuSxDd(r.getG3SlsxTuSxDd())
                .g3SlsxOsDd(r.getG3SlsxOsDd())
                .g3SlsxOsTonHt(r.getG3SlsxOsTonHt())
                // Nhóm 4
                .g4TuSlsxTonHt(r.getG4TuSlsxTonHt())
                .g4TuSlsxTrongThang(r.getG4TuSlsxTrongThang())
                .g4SlsxOsTon(r.getG4SlsxOsTon())
                .g4SlsxOsTrongThang(r.getG4SlsxOsTrongThang())
                .g4Lk(r.getG4Lk())
                .g4Tong(r.getG4Tong())
                .g4DoanhThu(r.getG4DoanhThu())
                .g4TiSuatLngDuKien(r.getG4TiSuatLngDuKien())
                .g4LngDuKien(r.getG4LngDuKien())
                // Nhóm 5
                .g5RaTuongUngSlnt(r.getG5RaTuongUngSlnt())
                .g5NtSlsxTonHt(r.getG5NtSlsxTonHt())
                .g5NtSlsxTrongThang(r.getG5NtSlsxTrongThang())
                .g5NtSlsxOsTon(r.getG5NtSlsxOsTon())
                .g5NtSlsxOsTrongThang(r.getG5NtSlsxOsTrongThang())
                .g5TongSlnt(r.getG5TongSlnt())
                .g5DoanhThu(r.getG5DoanhThu())
                .g5TiSuatLng(r.getG5TiSuatLng())
                .g5LngVnd(r.getG5LngVnd())
                // Nhóm 6
                .g6RaTon(r.getG6RaTon())
                .g6SlsxTonHt(r.getG6SlsxTonHt())
                .g6SlsxTonDd(r.getG6SlsxTonDd())
                .g6SlsxOsTon(r.getG6SlsxOsTon())
                .g6SlsxOsTonHt(r.getG6SlsxOsTonHt())
                .g6SlsxTon(r.getG6SlsxTon())
                .build();
    }

    /**
     * Copies only manual-input fields from request onto the entity.
     * Formula fields are NOT touched — they are computed by
     * MonthlyCalculationService.
     */
    public void applyRequest(ProjectMonthRecordRequest req, ProjectMonthRecord r) {
        // Nhóm 1
        r.setG1RaTon(req.getG1RaTon());
        r.setG1SlsxTonTuSxHd(req.getG1SlsxTonTuSxHd());
        r.setG1SlsxTonTuSxHtHd(req.getG1SlsxTonTuSxHtHd());
        r.setG1SlsxTonTuSxDdHd(req.getG1SlsxTonTuSxDdHd());
        r.setG1SlsxOsTon(req.getG1SlsxOsTon());
        r.setG1SlsxOsTonHt(req.getG1SlsxOsTonHt());
        // Nhóm 2
        r.setG2Headcount(req.getG2Headcount());
        r.setG2Ra(req.getG2Ra());
        r.setG2SlsxTuSx(req.getG2SlsxTuSx());
        r.setG2SlsxOs(req.getG2SlsxOs());
        r.setG2LienKet(req.getG2LienKet());
        r.setG2SlsxTuSxHtTrongThang(req.getG2SlsxTuSxHtTrongThang());
        r.setG2SlsxTuSxDd(req.getG2SlsxTuSxDd());
        r.setG2SlsxOsHt(req.getG2SlsxOsHt());
        r.setG2SlsxOsDd(req.getG2SlsxOsDd());
        r.setG2Cpbqtb(req.getG2Cpbqtb());
        r.setG2TySuatLng(req.getG2TySuatLng());
        // Nhóm 3
        r.setG3Ra(req.getG3Ra());
        r.setG3TongSlsxHd(req.getG3TongSlsxHd());
        r.setG3SlsxTuSxHt(req.getG3SlsxTuSxHt());
        r.setG3SlsxTuSxDd(req.getG3SlsxTuSxDd());
        r.setG3SlsxOsDd(req.getG3SlsxOsDd());
        r.setG3SlsxOsTonHt(req.getG3SlsxOsTonHt());
        // Nhóm 4
        r.setG4TuSlsxTonHt(req.getG4TuSlsxTonHt());
        r.setG4TuSlsxTrongThang(req.getG4TuSlsxTrongThang());
        r.setG4SlsxOsTon(req.getG4SlsxOsTon());
        r.setG4SlsxOsTrongThang(req.getG4SlsxOsTrongThang());
        r.setG4Lk(req.getG4Lk());
        r.setG4TiSuatLngDuKien(req.getG4TiSuatLngDuKien());
        r.setG4LngDuKien(req.getG4LngDuKien());
        // Nhóm 5
        r.setG5RaTuongUngSlnt(req.getG5RaTuongUngSlnt());
        r.setG5NtSlsxTonHt(req.getG5NtSlsxTonHt());
        r.setG5NtSlsxTrongThang(req.getG5NtSlsxTrongThang());
        r.setG5NtSlsxOsTon(req.getG5NtSlsxOsTon());
        r.setG5NtSlsxOsTrongThang(req.getG5NtSlsxOsTrongThang());
        r.setG5TiSuatLng(req.getG5TiSuatLng());
        r.setG5LngVnd(req.getG5LngVnd());
    }
}
