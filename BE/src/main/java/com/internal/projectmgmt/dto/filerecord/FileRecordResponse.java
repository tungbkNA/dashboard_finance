package com.internal.projectmgmt.dto.filerecord;

import java.time.OffsetDateTime;
import java.util.UUID;

public record FileRecordResponse(
        UUID id,
        String fileName,
        String fileUrl,
        UUID groupId,
        String groupName,
        String createdBy,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}
