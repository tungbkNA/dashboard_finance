package com.internal.projectmgmt.repository;

import com.internal.projectmgmt.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PermissionRepository extends JpaRepository<Permission, String> {

    List<Permission> findAllByOrderBySortOrderAsc();
}
