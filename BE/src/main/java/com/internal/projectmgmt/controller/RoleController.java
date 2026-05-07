package com.internal.projectmgmt.controller;

import com.internal.projectmgmt.dto.ApiResponse;
import com.internal.projectmgmt.dto.role.RoleRequest;
import com.internal.projectmgmt.dto.role.RoleResponse;
import com.internal.projectmgmt.dto.role.UpdateRolePermissionsRequest;
import com.internal.projectmgmt.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @PreAuthorize("hasAuthority('MANAGE_ROLE')")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> listAll() {
        return ResponseEntity.ok(ApiResponse.success(roleService.listAll()));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_CREATE')")
    public ResponseEntity<ApiResponse<RoleResponse>> create(@Valid @RequestBody RoleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(roleService.create(request)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_ROLE')")
    public ResponseEntity<ApiResponse<RoleResponse.RoleDetailResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(roleService.getById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_EDIT')")
    public ResponseEntity<ApiResponse<RoleResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody RoleRequest request) {
        return ResponseEntity.ok(ApiResponse.success(roleService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_DEACTIVATE')")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "false") boolean force) {
        roleService.softDelete(id, force);
        return ResponseEntity.ok(ApiResponse.success("Xóa role thành công", null));
    }

    @GetMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('MANAGE_ROLE')")
    public ResponseEntity<ApiResponse<RoleResponse.RoleDetailResponse>> getPermissions(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(roleService.getById(id)));
    }

    @PutMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('ROLE_ASSIGN_PERMISSIONS')")
    public ResponseEntity<ApiResponse<Void>> updatePermissions(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRolePermissionsRequest request) {
        roleService.updatePermissions(id, request.permissions());
        return ResponseEntity.ok(ApiResponse.success("Cập nhật phân quyền thành công", null));
    }
}
