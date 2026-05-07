package com.internal.projectmgmt.dto.role;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateRolePermissionsRequest(
        @NotNull(message = "Danh sách permissions không được null") List<String> permissions) {
}
