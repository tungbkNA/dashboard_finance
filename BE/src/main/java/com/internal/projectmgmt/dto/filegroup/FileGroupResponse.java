package com.internal.projectmgmt.dto.filegroup;

import java.time.OffsetDateTime;
import java.util.UUID;

public record FileGroupResponse(
        UUID id,
        String name,
        String description,
        boolean active,
        long fileCount,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}
