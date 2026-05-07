package com.internal.projectmgmt.repository;

import com.internal.projectmgmt.entity.ProjectMonthRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectMonthRecordRepository extends JpaRepository<ProjectMonthRecord, UUID> {

    List<ProjectMonthRecord> findByMonthKeyAndActiveTrue(String monthKey);

    Optional<ProjectMonthRecord> findByProjectIdAndMonthKeyAndActiveTrue(UUID projectId, String monthKey);

    Optional<ProjectMonthRecord> findByProjectIdAndMonthKey(UUID projectId, String monthKey);

    List<ProjectMonthRecord> findByProjectIdOrderByMonthKeyAsc(UUID projectId);

    List<ProjectMonthRecord> findByProjectIdAndActiveTrue(UUID projectId);

    Optional<ProjectMonthRecord> findFirstByProjectIdOrderByMonthKeyAsc(UUID projectId);
}
