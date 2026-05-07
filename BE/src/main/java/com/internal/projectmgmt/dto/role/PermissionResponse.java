package com.internal.projectmgmt.dto.role;

public record PermissionResponse(
        String code,
        String displayName,
        String parentCode,
        String type,
        int sortOrder) {
}
