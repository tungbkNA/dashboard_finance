package com.internal.projectmgmt.repository;

import com.internal.projectmgmt.entity.ProjectType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectTypeRepository extends JpaRepository<ProjectType, UUID> {

    List<ProjectType> findAllByDeletedFalse();

    Optional<ProjectType> findByIdAndDeletedFalse(UUID id);

    // Used by ProjectMapper to resolve type name for soft-deleted entities
    // (FR-PT-008)
    // Intentionally no deleted filter — inherited from JpaRepository.findById
    // (JpaRepository.findById already satisfies this; listed here for
    // documentation)

    boolean existsByKeyIgnoreCaseAndDeletedFalse(String key);

    @Query("SELECT COUNT(p) FROM Project p WHERE p.projectType.id = :projectTypeId AND p.deleted = false")
    long countActiveProjectsByProjectTypeId(UUID projectTypeId);
}
