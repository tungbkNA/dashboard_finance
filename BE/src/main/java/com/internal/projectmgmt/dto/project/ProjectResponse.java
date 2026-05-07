package com.internal.projectmgmt.dto.project;

import com.internal.projectmgmt.entity.StatusContract;
import com.internal.projectmgmt.entity.StatusProject;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        String projectCode,
        String projectName,
        UUID customerId,
        String customerName,
        UUID projectTypeId,
        String projectTypeName,
        BigDecimal price,
        StatusContract statusContract,
        StatusProject statusProject,
        String monthStart,
        String monthEnd,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}
