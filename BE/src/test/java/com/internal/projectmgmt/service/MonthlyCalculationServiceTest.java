package com.internal.projectmgmt.service;

import com.internal.projectmgmt.entity.ProjectMonthRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class MonthlyCalculationServiceTest {

    private MonthlyCalculationService service;

    @BeforeEach
    void setUp() {
        service = new MonthlyCalculationService();
    }

    // ---- helpers ----

    private ProjectMonthRecord emptyRecord() {
        return ProjectMonthRecord.builder().build();
    }

    private BigDecimal bd(String s) {
        return new BigDecimal(s);
    }

    // ========== (1) g2_tong_slsx_du_kien ==========

    @Test
    @DisplayName("g2TongSlsxDuKien = g2SlsxTuSx + g2SlsxOs + g2LienKet")
    void g2_normal() {
        ProjectMonthRecord r = emptyRecord();
        r.setG2SlsxTuSx(bd("100"));
        r.setG2SlsxOs(bd("30"));
        r.setG2LienKet(bd("20"));
        service.calculateAndFill(r, BigDecimal.ZERO);
        assertThat(r.getG2TongSlsxDuKien()).isEqualByComparingTo(bd("150"));
    }

    @Test
    @DisplayName("g2TongSlsxDuKien = 0 when all inputs null")
    void g2_allNull() {
        ProjectMonthRecord r = emptyRecord();
        service.calculateAndFill(r, BigDecimal.ZERO);
        assertThat(r.getG2TongSlsxDuKien()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // ========== (2) g3_ee ==========

    @Test
    @DisplayName("g3Ee = (g3TongSlsxHd / g3Ra) * 100 rounded 2dp")
    void g3ee_normal() {
        ProjectMonthRecord r = emptyRecord();
        r.setG3Ra(bd("180"));
        r.setG3TongSlsxHd(bd("160"));
        service.calculateAndFill(r, BigDecimal.ZERO);
        // 160/180*100 = 88.888... → 88.89
        assertThat(r.getG3Ee()).isEqualByComparingTo(bd("88.89"));
    }

    @Test
    @DisplayName("g3Ee = null when g3Ra is null")
    void g3ee_raNull() {
        ProjectMonthRecord r = emptyRecord();
        r.setG3TongSlsxHd(bd("100"));
        service.calculateAndFill(r, BigDecimal.ZERO);
        assertThat(r.getG3Ee()).isNull();
    }

    @Test
    @DisplayName("g3Ee = null when g3Ra is zero")
    void g3ee_raZero() {
        ProjectMonthRecord r = emptyRecord();
        r.setG3Ra(BigDecimal.ZERO);
        r.setG3TongSlsxHd(bd("100"));
        service.calculateAndFill(r, BigDecimal.ZERO);
        assertThat(r.getG3Ee()).isNull();
    }

    // ========== (3) g4_tong ==========

    @Test
    @DisplayName("g4Tong = sum of 5 g4 manual fields")
    void g4tong_normal() {
        ProjectMonthRecord r = emptyRecord();
        r.setG4TuSlsxTonHt(bd("35"));
        r.setG4TuSlsxTrongThang(bd("130"));
        r.setG4SlsxOsTon(bd("10"));
        r.setG4SlsxOsTrongThang(bd("28"));
        r.setG4Lk(bd("20"));
        service.calculateAndFill(r, BigDecimal.ZERO);
        assertThat(r.getG4Tong()).isEqualByComparingTo(bd("223"));
    }

    @Test
    @DisplayName("g4Tong = 0 when all inputs null")
    void g4tong_allNull() {
        ProjectMonthRecord r = emptyRecord();
        service.calculateAndFill(r, BigDecimal.ZERO);
        assertThat(r.getG4Tong()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // ========== (4) g4_doanh_thu ==========

    @Test
    @DisplayName("g4DoanhThu = g4Tong * price, rounded 0dp")
    void g4doanhThu_normal() {
        ProjectMonthRecord r = emptyRecord();
        r.setG4TuSlsxTonHt(bd("100"));
        // g4Tong = 100, price = 1500000.5 → 150000050 → rounds to 150000050
        service.calculateAndFill(r, bd("1500000.5"));
        assertThat(r.getG4DoanhThu()).isEqualByComparingTo(bd("150000050"));
    }

    @Test
    @DisplayName("g4DoanhThu rounds 0 decimal places (VNĐ)")
    void g4doanhThu_roundingZeroDP() {
        ProjectMonthRecord r = emptyRecord();
        r.setG4TuSlsxTrongThang(bd("3"));
        // g4Tong = 3, price = 1000000.3333 → 3000000.9999 → rounds to 3000001
        service.calculateAndFill(r, bd("1000000.3333"));
        assertThat(r.getG4DoanhThu().scale()).isZero();
        assertThat(r.getG4DoanhThu()).isEqualByComparingTo(bd("3000001"));
    }

    // ========== (5) g5_tong_slnt ==========

    @Test
    @DisplayName("g5TongSlnt = sum of 4 g5_nt_* fields")
    void g5tong_normal() {
        ProjectMonthRecord r = emptyRecord();
        r.setG5NtSlsxTonHt(bd("30"));
        r.setG5NtSlsxTrongThang(bd("120"));
        r.setG5NtSlsxOsTon(bd("8"));
        r.setG5NtSlsxOsTrongThang(bd("25"));
        service.calculateAndFill(r, BigDecimal.ZERO);
        assertThat(r.getG5TongSlnt()).isEqualByComparingTo(bd("183"));
    }

    // ========== (6) g5_doanh_thu ==========

    @Test
    @DisplayName("g5DoanhThu = g5TongSlnt * price, rounded 0dp")
    void g5doanhThu_normal() {
        ProjectMonthRecord r = emptyRecord();
        r.setG5NtSlsxTonHt(bd("10"));
        service.calculateAndFill(r, bd("2000000"));
        assertThat(r.getG5DoanhThu()).isEqualByComparingTo(bd("20000000"));
    }

    // ========== (7) g6_ra_ton ==========

    @Test
    @DisplayName("g6RaTon = g1RaTon + g3Ra − g5RaTuongUngSlnt")
    void g6raTon_normal() {
        ProjectMonthRecord r = emptyRecord();
        r.setG1RaTon(bd("100"));
        r.setG3Ra(bd("200"));
        r.setG5RaTuongUngSlnt(bd("150"));
        service.calculateAndFill(r, BigDecimal.ZERO);
        assertThat(r.getG6RaTon()).isEqualByComparingTo(bd("150"));
    }

    @Test
    @DisplayName("g6RaTon = 0 when all inputs null")
    void g6raTon_allNull() {
        ProjectMonthRecord r = emptyRecord();
        service.calculateAndFill(r, BigDecimal.ZERO);
        assertThat(r.getG6RaTon()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // ========== (8) g6_slsx_ton_ht ==========

    @Test
    @DisplayName("g6SlsxTonHt formula")
    void g6slsxTonHt_normal() {
        ProjectMonthRecord r = emptyRecord();
        r.setG1SlsxTonTuSxHtHd(bd("30"));
        r.setG3SlsxTuSxHt(bd("130"));
        r.setG5NtSlsxTonHt(bd("30"));
        r.setG5NtSlsxOsTrongThang(bd("25"));
        service.calculateAndFill(r, BigDecimal.ZERO);
        // 30 + 130 - 30 - 25 = 105
        assertThat(r.getG6SlsxTonHt()).isEqualByComparingTo(bd("105"));
    }

    // ========== (9) g6_slsx_ton_dd ==========

    @Test
    @DisplayName("g6SlsxTonDd = g3SlsxTuSxDd + g1SlsxTonTuSxDdHd")
    void g6slsxTonDd_normal() {
        ProjectMonthRecord r = emptyRecord();
        r.setG3SlsxTuSxDd(bd("10"));
        r.setG1SlsxTonTuSxDdHd(bd("20"));
        service.calculateAndFill(r, BigDecimal.ZERO);
        assertThat(r.getG6SlsxTonDd()).isEqualByComparingTo(bd("30"));
    }

    // ========== (10) g6_slsx_os_ton ==========

    @Test
    @DisplayName("g6SlsxOsTon = g1SlsxOsTon + g3SlsxOsDd")
    void g6slsxOsTon_normal() {
        ProjectMonthRecord r = emptyRecord();
        r.setG1SlsxOsTon(bd("10"));
        r.setG3SlsxOsDd(bd("5"));
        service.calculateAndFill(r, BigDecimal.ZERO);
        assertThat(r.getG6SlsxOsTon()).isEqualByComparingTo(bd("15"));
    }

    // ========== (11) g6_slsx_os_ton_ht ==========

    @Test
    @DisplayName("g6SlsxOsTonHt formula")
    void g6slsxOsTonHt_normal() {
        ProjectMonthRecord r = emptyRecord();
        r.setG1SlsxOsTonHt(bd("10"));
        r.setG3SlsxOsTonHt(bd("20"));
        r.setG5NtSlsxOsTon(bd("5"));
        r.setG5NtSlsxOsTrongThang(bd("7"));
        service.calculateAndFill(r, BigDecimal.ZERO);
        // 10 + 20 - 5 - 7 = 18
        assertThat(r.getG6SlsxOsTonHt()).isEqualByComparingTo(bd("18"));
    }

    @Test
    @DisplayName("g6SlsxOsTonHt = 0 when all inputs null")
    void g6slsxOsTonHt_allNull() {
        ProjectMonthRecord r = emptyRecord();
        service.calculateAndFill(r, BigDecimal.ZERO);
        assertThat(r.getG6SlsxOsTonHt()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
