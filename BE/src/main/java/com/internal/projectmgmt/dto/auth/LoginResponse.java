package com.internal.projectmgmt.dto.auth;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record LoginResponse(
        String token,
        OffsetDateTime expiresAt,
        UserInfo user) {

    public record UserInfo(
            UUID id,
            String username,
            String displayName,
            UUID roleId,
            String roleName,
            List<String> permissions) {
    }
}
