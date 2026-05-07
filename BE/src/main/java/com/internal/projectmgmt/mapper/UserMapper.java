package com.internal.projectmgmt.mapper;

import com.internal.projectmgmt.dto.user.UserResponse;
import com.internal.projectmgmt.entity.AppUser;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(AppUser user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName(),
                user.getRole().getId(),
                user.getRole().getRoleName(),
                user.isActive(),
                user.getPhone(),
                user.getPosition(),
                user.getEmployeeCode(),
                user.getCreatedAt());
    }
}
