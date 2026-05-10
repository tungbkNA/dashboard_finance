package com.internal.projectmgmt.repository;

import com.internal.projectmgmt.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PermissionRepository extends JpaRepository<Permission, String> {

    List<Permission> findAllByOrderBySortOrderAsc();

    @Query("SELECT p.code FROM Permission p ORDER BY p.sortOrder")
    List<String> findAllCodes();
}
