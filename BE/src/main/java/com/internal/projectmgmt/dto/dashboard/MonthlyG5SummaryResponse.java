package com.internal.projectmgmt.dto.dashboard;

import java.math.BigDecimal;
import java.util.List;

public record MonthlyG5SummaryResponse(List<MonthlyG5Entry> entries) {

    public record MonthlyG5Entry(
            String monthKey,
            long projectCount,
            BigDecimal g5DoanhThu,
            BigDecimal g5TongSlnt) {
    }
}
