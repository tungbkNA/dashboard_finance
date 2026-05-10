package com.internal.projectmgmt.dto.dashboard;

import java.math.BigDecimal;

public record MonthlyRevenueResponse(
        String monthKey,
        BigDecimal g2TongSlsxDuKien,
        BigDecimal g5TongSlnt,
        BigDecimal g5RaTuongUngSlnt) {
}
