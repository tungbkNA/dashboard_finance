package com.internal.projectmgmt.dto.monthlyrecord;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class ProjectMonthRecordRequest {

    // Nhóm 1: Tồn đầu kỳ (manual for first month; g1 except g1SlsxTonTuSxHd are
    // auto for non-first months)
    private BigDecimal g1RaTon;
    private BigDecimal g1SlsxTonTuSxHd;
    private BigDecimal g1SlsxTonTuSxHtHd;
    private BigDecimal g1SlsxTonTuSxDdHd;
    private BigDecimal g1SlsxOsTon;
    private BigDecimal g1SlsxOsTonHt;

    // Nhóm 2: Kế hoạch tháng (manual fields only — g2TongSlsxDuKien is formula,
    // excluded)
    private BigDecimal g2Headcount;
    private BigDecimal g2Ra;
    private BigDecimal g2SlsxTuSx;
    private BigDecimal g2SlsxOs;
    private BigDecimal g2LienKet;
    private BigDecimal g2SlsxTuSxHtTrongThang;
    private BigDecimal g2SlsxTuSxDd;
    private BigDecimal g2SlsxOsHt;
    private BigDecimal g2SlsxOsDd;
    private BigDecimal g2Cpbqtb;
    private BigDecimal g2TySuatLng;

    // Nhóm 3: Thực hiện SLSX đến NGÀY (manual fields only — g3Ee is formula,
    // excluded)
    private BigDecimal g3Ra;
    private BigDecimal g3TongSlsxHd;
    private BigDecimal g3SlsxTuSxHt;
    private BigDecimal g3SlsxTuSxDd;
    private BigDecimal g3SlsxOsDd;
    private BigDecimal g3SlsxOsTonHt;

    // Nhóm 4: Kế hoạch doanh thu (manual fields only — g4Tong, g4DoanhThu are
    // formulas, excluded)
    private BigDecimal g4TuSlsxTonHt;
    private BigDecimal g4TuSlsxTrongThang;
    private BigDecimal g4SlsxOsTon;
    private BigDecimal g4SlsxOsTrongThang;
    private BigDecimal g4Lk;
    private BigDecimal g4TiSuatLngDuKien;
    private BigDecimal g4LngDuKien;

    // Nhóm 5: Thực hiện nghiệm thu (manual fields only — g5TongSlnt, g5DoanhThu are
    // formulas, excluded)
    private BigDecimal g5RaTuongUngSlnt;
    private BigDecimal g5NtSlsxTonHt;
    private BigDecimal g5NtSlsxTrongThang;
    private BigDecimal g5NtSlsxOsTon;
    private BigDecimal g5NtSlsxOsTrongThang;
    private BigDecimal g5TiSuatLng;
    private BigDecimal g5LngVnd;
}
