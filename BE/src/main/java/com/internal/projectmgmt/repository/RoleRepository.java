package com.internal.projectmgmt.repository;

import com.internal.projectmgmt.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    List<Role> findByDeletedFalse();

    Optional<Role> findByIdAndDeletedFalse(UUID id);

    boolean existsByRoleNameIgnoreCaseAndDeletedFalse(String roleName);

    boolean existsByRoleNameIgnoreCaseAndDeletedFalseAndIdNot(String roleName, UUID id);

    @Query("SELECT r FROM Role r WHERE r.roleName = :roleName AND r.deleted = false")
    Optional<Role> findByRoleNameAndDeletedFalse(String roleName);

    @Query("SELECT COUNT(u) FROM AppUser u WHERE u.role.id = :roleId AND u.deleted = false AND u.active = true")
    long countActiveUsersByRoleId(UUID roleId);
}
