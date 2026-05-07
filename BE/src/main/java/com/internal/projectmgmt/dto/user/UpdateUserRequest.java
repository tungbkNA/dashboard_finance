package com.internal.projectmgmt.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateUserRequest(
                @Email(message = "Email không hợp lệ") String email,

                @Size(max = 255, message = "Tên hiển thị tối đa 255 ký tự") String displayName,

                UUID roleId,

                Boolean active,

                @Pattern(regexp = "^0\\d{8}$", message = "Số điện thoại phải gồm 9 chữ số bắt đầu bằng số 0") String phone,

                @Pattern(regexp = "^(PM|PU)$", message = "Chức vụ phải là PM hoặc PU") String position,

                @Size(max = 50, message = "Mã nhân viên tối đa 50 ký tự") String employeeCode) {
}
