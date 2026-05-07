package com.internal.projectmgmt.dto.user;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserResponse(
                UUID id,
                String username,
                String email,
                String displayName,
                UUID roleId,
                String roleName,
                boolean active,
                String phone,
                String position,
                String employeeCode,
                OffsetDateTime createdAt) {
}
