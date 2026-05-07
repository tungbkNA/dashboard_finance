package com.internal.projectmgmt.dto.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RoleRequest(
        @NotBlank(message = "Tên role không được để trống") @Size(max = 100, message = "Tên role tối đa 100 ký tự") String roleName,

        @Size(max = 1000, message = "Mô tả tối đa 1000 ký tự") String description) {
}
