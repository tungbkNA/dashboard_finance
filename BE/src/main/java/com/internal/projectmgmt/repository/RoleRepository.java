package com.internal.projectmgmt.repository;

import com.internal.projectmgmt.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query(value = "SELECT r FROM Role r" +
            " WHERE r.deleted = false" +
            " AND (:keyword = '' OR LOWER(r.roleCode) LIKE LOWER(CONCAT('%', :keyword, '%'))" +
            "      OR LOWER(r.roleName) LIKE LOWER(CONCAT('%', :keyword, '%')))", countQuery = "SELECT COUNT(r) FROM Role r"
                    +
                    " WHERE r.deleted = false" +
                    " AND (:keyword = '' OR LOWER(r.roleCode) LIKE LOWER(CONCAT('%', :keyword, '%'))" +
                    "      OR LOWER(r.roleName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Role> searchRoles(@Param("keyword") String keyword, Pageable pageable);
}
