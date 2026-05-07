package com.internal.projectmgmt.mapper;

import com.internal.projectmgmt.dto.role.PermissionResponse;
import com.internal.projectmgmt.dto.role.RoleResponse;
import com.internal.projectmgmt.entity.Permission;
import com.internal.projectmgmt.entity.Role;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RoleMapper {

    public RoleResponse toResponse(Role role, long userCount) {
        return new RoleResponse(
                role.getId(),
                role.getRoleName(),
                role.getDescription(),
                role.isActive(),
                userCount,
                role.getCreatedAt());
    }

    public RoleResponse.RoleDetailResponse toDetailResponse(Role role, long userCount) {
        List<PermissionResponse> perms = role.getPermissions().stream()
                .map(this::toPermissionResponse)
                .sorted(java.util.Comparator.comparingInt(PermissionResponse::sortOrder))
                .collect(Collectors.toList());
        return new RoleResponse.RoleDetailResponse(
                role.getId(),
                role.getRoleName(),
                role.getDescription(),
                role.isActive(),
                userCount,
                role.getCreatedAt(),
                perms);
    }

    public PermissionResponse toPermissionResponse(Permission permission) {
        return new PermissionResponse(
                permission.getCode(),
                permission.getDisplayName(),
                permission.getParentCode(),
                permission.getType().name(),
                permission.getSortOrder());
    }
}
