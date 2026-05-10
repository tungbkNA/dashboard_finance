package com.internal.projectmgmt.repository;

import com.internal.projectmgmt.entity.Project;
import com.internal.projectmgmt.entity.StatusContract;
import com.internal.projectmgmt.entity.StatusProject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    List<Project> findAllByDeletedFalse();

    Optional<Project> findByIdAndDeletedFalse(UUID id);

    boolean existsByProjectCodeIgnoreCaseAndDeletedFalse(String projectCode);

    boolean existsByProjectCodeIgnoreCaseAndDeletedFalseAndIdNot(String projectCode, UUID id);

    long countByDeletedFalse();

    long countByDeletedFalseAndStatusProject(StatusProject statusProject);

    long countByDeletedFalseAndStatusContract(StatusContract statusContract);

    @Query(value = "SELECT p FROM Project p" +
            " JOIN FETCH p.customer" +
            " JOIN FETCH p.projectType" +
            " LEFT JOIN FETCH p.representUser" +
            " WHERE p.deleted = false" +
            " AND (:keyword = '' OR LOWER(p.projectCode) LIKE LOWER(CONCAT('%', :keyword, '%'))" +
            "      OR LOWER(p.projectName) LIKE LOWER(CONCAT('%', :keyword, '%')))" +
            " AND (:projectTypeId IS NULL OR p.projectType.id = :projectTypeId)" +
            " AND (:customerId IS NULL OR p.customer.id = :customerId)" +
            " AND (:statusContract IS NULL OR CAST(p.statusContract AS string) = :statusContract)" +
            " AND (:statusProject IS NULL OR CAST(p.statusProject AS string) = :statusProject)", countQuery = "SELECT COUNT(p) FROM Project p"
                    +
                    " WHERE p.deleted = false" +
                    " AND (:keyword = '' OR LOWER(p.projectCode) LIKE LOWER(CONCAT('%', :keyword, '%'))" +
                    "      OR LOWER(p.projectName) LIKE LOWER(CONCAT('%', :keyword, '%')))" +
                    " AND (:projectTypeId IS NULL OR p.projectType.id = :projectTypeId)" +
                    " AND (:customerId IS NULL OR p.customer.id = :customerId)" +
                    " AND (:statusContract IS NULL OR CAST(p.statusContract AS string) = :statusContract)" +
                    " AND (:statusProject IS NULL OR CAST(p.statusProject AS string) = :statusProject)")
    Page<Project> searchProjects(
            @Param("keyword") String keyword,
            @Param("projectTypeId") UUID projectTypeId,
            @Param("customerId") UUID customerId,
            @Param("statusContract") String statusContract,
            @Param("statusProject") String statusProject,
            Pageable pageable);
}
