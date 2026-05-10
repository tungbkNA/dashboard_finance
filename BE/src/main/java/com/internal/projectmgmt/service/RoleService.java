package com.internal.projectmgmt.service;

import com.internal.projectmgmt.dto.role.RoleRequest;
import com.internal.projectmgmt.dto.role.RoleResponse;
import com.internal.projectmgmt.entity.Permission;
import com.internal.projectmgmt.entity.Role;
import com.internal.projectmgmt.exception.AppException;
import com.internal.projectmgmt.mapper.RoleMapper;
import com.internal.projectmgmt.repository.PermissionRepository;
import com.internal.projectmgmt.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleMapper roleMapper;

    @Transactional(readOnly = true)
    public List<RoleResponse> listAll() {
        return roleRepository.findByDeletedFalse().stream()
                .map(r -> roleMapper.toResponse(r, roleRepository.countActiveUsersByRoleId(r.getId())))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RoleResponse.RoleDetailResponse getById(UUID id) {
        Role role = findActiveRole(id);
        return roleMapper.toDetailResponse(role, roleRepository.countActiveUsersByRoleId(id));
    }

    @Transactional
    public RoleResponse create(RoleRequest request) {
        if (roleRepository.existsByRoleNameIgnoreCaseAndDeletedFalse(request.roleName())) {
            throw new AppException("ROLE_NAME_DUPLICATE", "Tên role đã tồn tại");
        }
        Role role = Role.builder()
                .roleName(request.roleName())
                .roleCode(generateRoleCode(request.roleName()))
                .description(request.description())
                .active(true)
                .deleted(false)
                .build();
        role = roleRepository.save(role);
        return roleMapper.toResponse(role, 0);
    }

    @Transactional
    public RoleResponse update(UUID id, RoleRequest request) {
        Role role = findActiveRole(id);
        if (roleRepository.existsByRoleNameIgnoreCaseAndDeletedFalseAndIdNot(request.roleName(), id)) {
            throw new AppException("ROLE_NAME_DUPLICATE", "Tên role đã tồn tại");
        }
        role.setRoleName(request.roleName());
        role.setRoleCode(generateRoleCode(request.roleName()));
        role.setDescription(request.description());
        if (request.active() != null) {
            role.setActive(request.active());
        }
        role = roleRepository.save(role);
        return roleMapper.toResponse(role, roleRepository.countActiveUsersByRoleId(id));
    }

    @Transactional(readOnly = true)
    public Page<RoleResponse> search(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "roleName"));
        Page<Role> rolePage = roleRepository.searchRoles(keyword == null ? "" : keyword.trim(), pageable);
        return rolePage.map(r -> roleMapper.toResponse(r, roleRepository.countActiveUsersByRoleId(r.getId())));
    }

    private String generateRoleCode(String roleName) {
        return roleName.trim().toUpperCase().replaceAll("\\s+", "_");
    }

    @Transactional
    public void softDelete(UUID id, boolean force) {
        Role role = findActiveRole(id);
        long activeUsers = roleRepository.countActiveUsersByRoleId(id);
        if (activeUsers > 0 && !force) {
            throw new AppException("ROLE_DELETE_REQUIRES_CONFIRMATION",
                    "Role đang có " + activeUsers + " người dùng active. Xác nhận để tiếp tục.");
        }
        role.setDeleted(true);
        role.setActive(false);
        roleRepository.save(role);
    }

    @Transactional(readOnly = true)
    public List<RoleResponse.RoleDetailResponse> getPermissions(UUID id) {
        Role role = findActiveRole(id);
        return List.of(roleMapper.toDetailResponse(role, roleRepository.countActiveUsersByRoleId(id)));
    }

    @Transactional
    public void updatePermissions(UUID id, List<String> permissionCodes) {
        Role role = findActiveRole(id);
        Set<Permission> newPermissions = new HashSet<>(permissionRepository.findAllById(permissionCodes));
        role.setPermissions(newPermissions);
        roleRepository.save(role);
    }

    private Role findActiveRole(UUID id) {
        return roleRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AppException("ROLE_NOT_FOUND", "Role không tồn tại"));
    }
}
