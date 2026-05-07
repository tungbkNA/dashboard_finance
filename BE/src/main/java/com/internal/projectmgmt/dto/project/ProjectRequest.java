package com.internal.projectmgmt.dto.project;

import com.internal.projectmgmt.entity.StatusContract;
import com.internal.projectmgmt.entity.StatusProject;
import com.internal.projectmgmt.validation.MonthYear;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record ProjectRequest(

        @NotBlank(message = "Mã dự án không được để trống") @Pattern(regexp = "^[A-Za-z0-9_-]{1,50}$", message = "Mã dự án chỉ được chứa chữ cái, số, dấu gạch dưới hoặc gạch ngang (tối đa 50 ký tự)") String projectCode,

        @NotBlank(message = "Tên dự án không được để trống") @Size(max = 255, message = "Tên dự án tối đa 255 ký tự") String projectName,

        @NotNull(message = "Khách hàng không được để trống") UUID customerId,

        @NotNull(message = "Loại dự án không được để trống") UUID projectTypeId,

        @NotNull(message = "Đơn giá không được để trống") @DecimalMin(value = "0", message = "Đơn giá phải là số không âm") BigDecimal price,

        @NotNull(message = "Trạng thái hợp đồng không được để trống") StatusContract statusContract,

        @NotNull(message = "Trạng thái dự án không được để trống") StatusProject statusProject,

        @NotBlank(message = "Tháng bắt đầu không được để trống") @MonthYear String monthStart,

        @NotBlank(message = "Tháng kết thúc không được để trống") @MonthYear String monthEnd) {
}
