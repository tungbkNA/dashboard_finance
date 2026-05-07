package com.internal.projectmgmt.dto.filerecord;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record FileRecordRequest(
        @NotBlank(message = "Tên file không được để trống") @Size(max = 200, message = "Tên file tối đa 200 ký tự") String fileName,

        @NotBlank(message = "Link file không được để trống") @Size(max = 2048, message = "Link file tối đa 2048 ký tự") @Pattern(regexp = "^https?://.*", message = "Link file phải bắt đầu bằng http:// hoặc https://") String fileUrl,

        @NotNull(message = "Nhóm file không được để trống") UUID groupId) {
}
