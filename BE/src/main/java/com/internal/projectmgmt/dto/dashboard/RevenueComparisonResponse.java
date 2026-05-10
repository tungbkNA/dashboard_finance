package com.internal.projectmgmt.dto.dashboard;

import java.math.BigDecimal;

public record RevenueComparisonResponse(
        String monthKey,
        BigDecimal g2Ra,
        BigDecimal g3Ra,
        BigDecimal g2TongSlsxDuKien,
        BigDecimal g3TongSlsxHd,
        BigDecimal g2SlsxTuSxHtTrongThang,
        BigDecimal g3SlsxTuSxHt,
        BigDecimal g2SlsxTuSxDd,
        BigDecimal g3SlsxTuSxDd) {
}
