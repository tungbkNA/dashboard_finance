package com.internal.projectmgmt.dto.monthlyrecord;

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
public class ProjectMonthRecordSummaryResponse {

    private UUID id;
    private UUID projectId;
    private String projectCode;
    private String projectName;
    private String monthKey;
    private boolean active;
    private String customerName;
    private BigDecimal g1SlsxTonTuSxHd;
    private BigDecimal g4DoanhThu;
    private BigDecimal g5DoanhThu;
    private BigDecimal g5TongSlnt;
    private BigDecimal g6SlsxTon;
    private OffsetDateTime updatedAt;
}
