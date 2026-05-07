package com.internal.projectmgmt.repository;

import com.internal.projectmgmt.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    List<Project> findAllByDeletedFalse();

    Optional<Project> findByIdAndDeletedFalse(UUID id);

    boolean existsByProjectCodeIgnoreCaseAndDeletedFalse(String projectCode);

    boolean existsByProjectCodeIgnoreCaseAndDeletedFalseAndIdNot(String projectCode, UUID id);
}
