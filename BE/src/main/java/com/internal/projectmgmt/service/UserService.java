package com.internal.projectmgmt.service;

import com.internal.projectmgmt.dto.user.ResetPasswordRequest;
import com.internal.projectmgmt.dto.user.UpdateUserRequest;
import com.internal.projectmgmt.dto.user.UserRequest;
import com.internal.projectmgmt.dto.user.UserResponse;
import com.internal.projectmgmt.entity.AppUser;
import com.internal.projectmgmt.entity.Role;
import com.internal.projectmgmt.exception.AppException;
import com.internal.projectmgmt.mapper.UserMapper;
import com.internal.projectmgmt.repository.AppUserRepository;
import com.internal.projectmgmt.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UserResponse> listAll(Boolean active) {
        List<AppUser> users = appUserRepository.findAll().stream()
                .filter(u -> !u.isDeleted())
                .filter(u -> !u.isSystem())
                .filter(u -> active == null || u.isActive() == active)
                .collect(Collectors.toList());
        return users.stream().map(userMapper::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponse getById(UUID id) {
        return userMapper.toResponse(findActiveUser(id));
    }

    @Transactional
    public UserResponse create(UserRequest request) {
        if (appUserRepository.existsByUsernameAndDeletedFalse(request.username())) {
            throw new AppException("USER_USERNAME_EXISTS", "Tên đăng nhập đã tồn tại");
        }
        if (appUserRepository.existsByEmailAndDeletedFalse(request.email())) {
            throw new AppException("USER_EMAIL_EXISTS", "Email đã được sử dụng");
        }
        Role role = findActiveRole(request.roleId());

        AppUser user = AppUser.builder()
                .username(request.username())
                .email(request.email())
                .displayName(request.displayName())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(role)
                .phone(request.phone())
                .position(request.position())
                .employeeCode(request.employeeCode())
                .active(true)
                .deleted(false)
                .build();
        return userMapper.toResponse(appUserRepository.save(user));
    }

    @Transactional
    public UserResponse update(UUID id, UpdateUserRequest request) {
        AppUser user = findActiveUser(id);
        if (user.isSystem()) {
            throw new AppException("USER_SYSTEM_PROTECTED", "Không thể chỉnh sửa tài khoản hệ thống");
        }

        if (request.email() != null && !request.email().equals(user.getEmail())) {
            if (appUserRepository.existsByEmailAndDeletedFalseAndIdNot(request.email(), id)) {
                throw new AppException("USER_EMAIL_EXISTS", "Email đã được sử dụng");
            }
            user.setEmail(request.email());
        }
        if (request.displayName() != null) {
            user.setDisplayName(request.displayName());
        }
        if (request.roleId() != null) {
            Role role = findActiveRole(request.roleId());
            user.setRole(role);
        }
        if (request.active() != null) {
            user.setActive(request.active());
        }
        user.setPhone(request.phone());
        user.setPosition(request.position());
        user.setEmployeeCode(request.employeeCode());
        return userMapper.toResponse(appUserRepository.save(user));
    }

    @Transactional
    public void resetPassword(UUID id, ResetPasswordRequest request) {
        AppUser user = findActiveUser(id);
        if (user.isSystem()) {
            throw new AppException("USER_SYSTEM_PROTECTED", "Không thể thay đổi mật khẩu tài khoản hệ thống");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        appUserRepository.save(user);
    }

    @Transactional
    public void softDelete(UUID id, UUID currentUserId) {
        if (id.equals(currentUserId)) {
            throw new AppException("USER_CANNOT_DELETE_SELF", "Không thể xóa tài khoản đang đăng nhập");
        }
        AppUser user = findActiveUser(id);
        if (user.isSystem()) {
            throw new AppException("USER_SYSTEM_PROTECTED", "Không thể xóa tài khoản hệ thống");
        }
        user.setDeleted(true);
        user.setActive(false);
        appUserRepository.save(user);
    }

    private AppUser findActiveUser(UUID id) {
        return appUserRepository.findById(id)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new AppException("USER_NOT_FOUND", "Người dùng không tồn tại"));
    }

    private Role findActiveRole(UUID roleId) {
        return roleRepository.findByIdAndDeletedFalse(roleId)
                .filter(Role::isActive)
                .orElseThrow(() -> new AppException("ROLE_NOT_FOUND_OR_INACTIVE",
                        "Role không tồn tại hoặc đã bị vô hiệu hoá"));
    }
}
