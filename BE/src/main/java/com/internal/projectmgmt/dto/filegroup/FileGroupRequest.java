package com.internal.projectmgmt.dto.filegroup;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FileGroupRequest(
        @NotBlank(message = "Tên nhóm file không được để trống") @Size(max = 100, message = "Tên nhóm file tối đa 100 ký tự") String name,

        @Size(max = 255, message = "Mô tả tối đa 255 ký tự") String description) {
}
