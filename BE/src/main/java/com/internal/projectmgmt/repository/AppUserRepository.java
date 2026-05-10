package com.internal.projectmgmt.repository;

import com.internal.projectmgmt.entity.AppUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {

    Optional<AppUser> findByUsernameAndDeletedFalse(String username);

    boolean existsByUsernameAndDeletedFalse(String username);

    boolean existsByEmailAndDeletedFalse(String email);

    boolean existsByEmailAndDeletedFalseAndIdNot(String email, UUID id);

    @Query(value = "SELECT u FROM AppUser u" +
            " JOIN FETCH u.role" +
            " WHERE u.deleted = false AND u.system = false" +
            " AND (:keyword = '' OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))" +
            "      OR LOWER(u.employeeCode) LIKE LOWER(CONCAT('%', :keyword, '%')))" +
            " AND (:position IS NULL OR u.position = :position)" +
            " AND (:roleId IS NULL OR u.role.id = :roleId)" +
            " AND (:active IS NULL OR u.active = :active)", countQuery = "SELECT COUNT(u) FROM AppUser u" +
                    " WHERE u.deleted = false AND u.system = false" +
                    " AND (:keyword = '' OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))" +
                    "      OR LOWER(u.employeeCode) LIKE LOWER(CONCAT('%', :keyword, '%')))" +
                    " AND (:position IS NULL OR u.position = :position)" +
                    " AND (:roleId IS NULL OR u.role.id = :roleId)" +
                    " AND (:active IS NULL OR u.active = :active)")
    Page<AppUser> searchUsers(
            @Param("keyword") String keyword,
            @Param("position") String position,
            @Param("roleId") UUID roleId,
            @Param("active") Boolean active,
            Pageable pageable);

    long countByDeletedFalseAndSystemFalseAndActiveTrue();

    long countByDeletedFalseAndSystemFalseAndActiveFalse();
}
