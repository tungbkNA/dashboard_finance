package com.internal.projectmgmt.dto.monthlyrecord;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMonthRecordResponse {

    private UUID id;
    private UUID projectId;
    private String projectCode;
    private String projectName;
    private String monthKey;
    private boolean active;
    @JsonProperty("isFirstMonth")
    private boolean isFirstMonth;
    private BigDecimal price;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    // Nhóm 1: Tồn đầu kỳ
    private BigDecimal g1RaTon;
    private BigDecimal g1SlsxTonTuSxHd;
    private BigDecimal g1SlsxTonTuSxHtHd;
    private BigDecimal g1SlsxTonTuSxDdHd;
    private BigDecimal g1SlsxOsTon;
    private BigDecimal g1SlsxOsTonHt;

    // Nhóm 2: Kế hoạch tháng
    private BigDecimal g2Headcount;
    private BigDecimal g2Ra;
    private BigDecimal g2SlsxTuSx;
    private BigDecimal g2SlsxOs;
    private BigDecimal g2LienKet;
    private BigDecimal g2TongSlsxDuKien; // formula
    private BigDecimal g2SlsxTuSxHtTrongThang;
    private BigDecimal g2SlsxTuSxDd;
    private BigDecimal g2SlsxOsHt;
    private BigDecimal g2SlsxOsDd;
    private BigDecimal g2Cpbqtb;
    private BigDecimal g2TySuatLng;

    // Nhóm 3: Thực hiện SLSX đến NGÀY
    private BigDecimal g3Ra;
    private BigDecimal g3TongSlsxHd;
    private BigDecimal g3Ee; // formula
    private BigDecimal g3SlsxTuSxHt;
    private BigDecimal g3SlsxTuSxDd;
    private BigDecimal g3SlsxOsDd;
    private BigDecimal g3SlsxOsTonHt;

    // Nhóm 4: Kế hoạch doanh thu
    private BigDecimal g4TuSlsxTonHt;
    private BigDecimal g4TuSlsxTrongThang;
    private BigDecimal g4SlsxOsTon;
    private BigDecimal g4SlsxOsTrongThang;
    private BigDecimal g4Lk;
    private BigDecimal g4Tong; // formula
    private BigDecimal g4DoanhThu; // formula
    private BigDecimal g4TiSuatLngDuKien;
    private BigDecimal g4LngDuKien;

    // Nhóm 5: Thực hiện nghiệm thu
    private BigDecimal g5RaTuongUngSlnt;
    private BigDecimal g5NtSlsxTonHt;
    private BigDecimal g5NtSlsxTrongThang;
    private BigDecimal g5NtSlsxOsTon;
    private BigDecimal g5NtSlsxOsTrongThang;
    private BigDecimal g5TongSlnt; // formula
    private BigDecimal g5DoanhThu; // formula
    private BigDecimal g5TiSuatLng;
    private BigDecimal g5LngVnd;

    // Nhóm 6: Tồn cuối kỳ (all formula)
    private BigDecimal g6RaTon;
    private BigDecimal g6SlsxTonHt;
    private BigDecimal g6SlsxTonDd;
    private BigDecimal g6SlsxOsTon;
    private BigDecimal g6SlsxOsTonHt;
    private BigDecimal g6SlsxTon; // formula: g6SlsxTonHt + g6SlsxTonDd + g6SlsxOsTon + g6SlsxOsTonHt

    // Feature 005: cross-month propagation result
    @Builder.Default
    private int affectedMonths = 0;
}
