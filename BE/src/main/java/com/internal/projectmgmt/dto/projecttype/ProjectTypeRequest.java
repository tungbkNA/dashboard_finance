package com.internal.projectmgmt.dto.projecttype;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProjectTypeRequest(

        @NotBlank(message = "Key không được để trống") @Pattern(regexp = "^[A-Za-z0-9_-]{1,50}$", message = "Key chỉ được chứa chữ cái, số, dấu gạch dưới hoặc gạch ngang (tối đa 50 ký tự)") String key,

        @NotBlank(message = "Value không được để trống") @Size(max = 255, message = "Value tối đa 255 ký tự") String value) {
}
