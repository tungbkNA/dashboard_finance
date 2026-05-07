package com.internal.projectmgmt.service;

import com.internal.projectmgmt.entity.ProjectMonthRecord;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Pure stateless service — computes all 11 formula fields for a
 * ProjectMonthRecord.
 * No DB dependency, fully unit-testable without mocks.
 *
 * Snapshot contract:
 * - On the WRITE path (update): all formula fields are ALWAYS recalculated and
 * overwritten.
 * - On the READ path (findById, when snapshot may be missing): caller may
 * optionally
 * invoke this with null-check if they want dynamic calculation without
 * overwriting DB.
 * This service itself always sets the field — callers decide whether to
 * persist.
 */
@Service
public class MonthlyCalculationService {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    /**
     * Calculates and sets all 11 formula fields on the given record.
     * Null inputs are treated as ZERO in arithmetic (per FR-REC-028).
     * EE returns null when g3Ra is null or zero (per FR-REC-010).
     *
     * @param record       the record to fill (mutated in place)
     * @param projectPrice the project's unit price for revenue formulas
     */
    public void calculateAndFill(ProjectMonthRecord record, BigDecimal projectPrice) {
        BigDecimal price = nvl(projectPrice);

        // (1) g2_tong_slsx_du_kien = g2_slsx_tu_sx + g2_slsx_os + g2_lien_ket
        record.setG2TongSlsxDuKien(
                add(record.getG2SlsxTuSx(), record.getG2SlsxOs(), record.getG2LienKet()));

        // (2) g3_ee = (g3_tong_slsx_hd / g3_ra) * 100 — NULL when g3_ra <= 0 or null
        BigDecimal g3Ra = record.getG3Ra();
        if (g3Ra == null || g3Ra.compareTo(BigDecimal.ZERO) == 0) {
            record.setG3Ee(null);
        } else {
            record.setG3Ee(
                    nvl(record.getG3TongSlsxHd())
                            .divide(g3Ra, 10, RoundingMode.HALF_UP)
                            .multiply(HUNDRED)
                            .setScale(2, RoundingMode.HALF_UP));
        }

        // (3) g4_tong = sum of 5 g4 manual fields
        record.setG4Tong(add(
                record.getG4TuSlsxTonHt(),
                record.getG4TuSlsxTrongThang(),
                record.getG4SlsxOsTon(),
                record.getG4SlsxOsTrongThang(),
                record.getG4Lk()));

        // (4) g4_doanh_thu = g4_tong * price — round 0dp
        record.setG4DoanhThu(
                nvl(record.getG4Tong())
                        .multiply(price)
                        .setScale(0, RoundingMode.HALF_UP));

        // (5) g5_tong_slnt = sum of 4 g5_nt_* fields
        record.setG5TongSlnt(add(
                record.getG5NtSlsxTonHt(),
                record.getG5NtSlsxTrongThang(),
                record.getG5NtSlsxOsTon(),
                record.getG5NtSlsxOsTrongThang()));

        // (6) g5_doanh_thu = g5_tong_slnt * price — round 0dp
        record.setG5DoanhThu(
                nvl(record.getG5TongSlnt())
                        .multiply(price)
                        .setScale(0, RoundingMode.HALF_UP));

        // (7) g6_ra_ton = g1_ra_ton + g3_ra − g5_ra_tuong_ung_slnt
        record.setG6RaTon(
                add(record.getG1RaTon(), record.getG3Ra())
                        .subtract(nvl(record.getG5RaTuongUngSlnt())));

        // (8) g6_slsx_ton_ht = g1_slsx_ton_tu_sx_ht_hd + g3_slsx_tu_sx_ht −
        // g5_nt_slsx_ton_ht − g5_nt_slsx_os_trong_thang
        record.setG6SlsxTonHt(
                add(record.getG1SlsxTonTuSxHtHd(), record.getG3SlsxTuSxHt())
                        .subtract(nvl(record.getG5NtSlsxTonHt()))
                        .subtract(nvl(record.getG5NtSlsxOsTrongThang())));

        // (9) g6_slsx_ton_dd = g3_slsx_tu_sx_dd + g1_slsx_ton_tu_sx_dd_hd
        record.setG6SlsxTonDd(
                add(record.getG3SlsxTuSxDd(), record.getG1SlsxTonTuSxDdHd()));

        // (10) g6_slsx_os_ton = g1_slsx_os_ton + g3_slsx_os_dd
        record.setG6SlsxOsTon(
                add(record.getG1SlsxOsTon(), record.getG3SlsxOsDd()));

        // (11) g6_slsx_os_ton_ht = g1_slsx_os_ton_ht + g3_slsx_os_ton_ht −
        // g5_nt_slsx_os_ton − g5_nt_slsx_os_trong_thang
        record.setG6SlsxOsTonHt(
                add(record.getG1SlsxOsTonHt(), record.getG3SlsxOsTonHt())
                        .subtract(nvl(record.getG5NtSlsxOsTon()))
                        .subtract(nvl(record.getG5NtSlsxOsTrongThang())));

        // (12) g6_slsx_ton = g6_slsx_ton_ht + g6_slsx_ton_dd + g6_slsx_os_ton +
        // g6_slsx_os_ton_ht
        // Cascades to next month's g1_slsx_ton_tu_sx_hd
        record.setG6SlsxTon(
                add(record.getG6SlsxTonHt(), record.getG6SlsxTonDd(),
                        record.getG6SlsxOsTon(), record.getG6SlsxOsTonHt()));
    }

    // ---- helpers ----

    /** Null-safe: returns ZERO when value is null. */
    private BigDecimal nvl(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    /** Variadic addition with null-as-zero semantics. */
    private BigDecimal add(BigDecimal... values) {
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal v : values) {
            sum = sum.add(nvl(v));
        }
        return sum;
    }
}
