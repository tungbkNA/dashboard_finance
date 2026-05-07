package com.internal.projectmgmt.dto.customer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CustomerRequest(

        @NotBlank(message = "Mã khách hàng không được để trống") @Pattern(regexp = "^[A-Za-z0-9_-]{1,50}$", message = "Mã khách hàng chỉ được chứa chữ cái, số, dấu gạch dưới hoặc gạch ngang (tối đa 50 ký tự)") String customerCode,

        @NotBlank(message = "Tên khách hàng không được để trống") @Size(max = 255, message = "Tên khách hàng tối đa 255 ký tự") String customerName) {
}
