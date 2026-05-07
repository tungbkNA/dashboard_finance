package com.internal.projectmgmt.repository;

import com.internal.projectmgmt.entity.FieldChangeAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FieldChangeAuditLogRepository extends JpaRepository<FieldChangeAuditLog, UUID> {

    boolean existsByEventId(String eventId);
}
