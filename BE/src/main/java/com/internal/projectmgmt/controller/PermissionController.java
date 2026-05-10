package com.internal.projectmgmt.controller;

import com.internal.projectmgmt.dto.ApiResponse;
import com.internal.projectmgmt.dto.role.PermissionResponse;
import com.internal.projectmgmt.entity.Permission;
import com.internal.projectmgmt.mapper.RoleMapper;
import com.internal.projectmgmt.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionRepository permissionRepository;
    private final RoleMapper roleMapper;

    @GetMapping
    @PreAuthorize("hasAuthority('PERMISSION_VIEW')")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> listAll() {
        List<PermissionResponse> perms = permissionRepository.findAllByOrderBySortOrderAsc()
                .stream()
                .map(roleMapper::toPermissionResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(perms));
    }
}
