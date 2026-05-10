package com.internal.projectmgmt.dto.role;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record RoleResponse(
                UUID id,
                String roleCode,
                String roleName,
                String description,
                boolean active,
                long userCount,
                OffsetDateTime createdAt) {

        public record RoleDetailResponse(
                        UUID id,
                        String roleCode,
                        String roleName,
                        String description,
                        boolean active,
                        long userCount,
                        OffsetDateTime createdAt,
                        List<PermissionResponse> permissions) {
        }
}
