package com.internal.projectmgmt.dto.user;

import jakarta.validation.constraints.*;

import java.util.UUID;

public record UserRequest(
                @NotBlank(message = "Tên đăng nhập không được để trống") @Size(min = 3, max = 50, message = "Tên đăng nhập từ 3 đến 50 ký tự") @Pattern(regexp = "^[a-zA-Z0-9_.\\-]+$", message = "Tên đăng nhập chỉ được chứa chữ cái, số, dấu gạch dưới, chấm hoặc gạch ngang") String username,

                @NotBlank(message = "Email không được để trống") @Email(message = "Email không hợp lệ") String email,

                @NotBlank(message = "Tên hiển thị không được để trống") @Size(max = 255, message = "Tên hiển thị tối đa 255 ký tự") String displayName,

                @NotBlank(message = "Mật khẩu không được để trống") @Size(min = 8, message = "Mật khẩu tối thiểu 8 ký tự") @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).{8,}$", message = "Mật khẩu phải có ít nhất 8 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt") String password,

                @NotNull(message = "Role không được để trống") UUID roleId,

                @Pattern(regexp = "^0\\d{8}$", message = "Số điện thoại phải gồm 9 chữ số bắt đầu bằng số 0") String phone,

                @Pattern(regexp = "^(PM|PU)$", message = "Chức vụ phải là PM hoặc PU") String position,

                @Size(max = 50, message = "Mã nhân viên tối đa 50 ký tự") String employeeCode) {
}
