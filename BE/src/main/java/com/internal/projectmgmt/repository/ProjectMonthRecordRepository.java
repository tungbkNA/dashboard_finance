package com.internal.projectmgmt.repository;

import com.internal.projectmgmt.entity.ProjectMonthRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
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

    @Query(value = "SELECT r FROM ProjectMonthRecord r" +
            " JOIN FETCH r.project p" +
            " JOIN FETCH p.customer" +
            " LEFT JOIN FETCH p.representUser" +
            " WHERE r.active = true" +
            " AND (:monthKey IS NULL OR r.monthKey = :monthKey)" +
            " AND (:keyword = '' OR LOWER(p.projectCode) LIKE LOWER(CONCAT('%', :keyword, '%'))" +
            "      OR LOWER(p.projectName) LIKE LOWER(CONCAT('%', :keyword, '%')))", countQuery = "SELECT COUNT(r) FROM ProjectMonthRecord r"
                    +
                    " JOIN r.project p" +
                    " WHERE r.active = true" +
                    " AND (:monthKey IS NULL OR r.monthKey = :monthKey)" +
                    " AND (:keyword = '' OR LOWER(p.projectCode) LIKE LOWER(CONCAT('%', :keyword, '%'))" +
                    "      OR LOWER(p.projectName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<ProjectMonthRecord> searchRecords(
            @Param("keyword") String keyword,
            @Param("monthKey") String monthKey,
            Pageable pageable);

    @Query("SELECT COALESCE(SUM(r.g2Ra), 0), COALESCE(SUM(r.g3Ra), 0)," +
            " COALESCE(SUM(r.g2TongSlsxDuKien), 0), COALESCE(SUM(r.g3TongSlsxHd), 0)," +
            " COALESCE(SUM(r.g2SlsxTuSxHtTrongThang), 0), COALESCE(SUM(r.g3SlsxTuSxHt), 0)," +
            " COALESCE(SUM(r.g2SlsxTuSxDd), 0), COALESCE(SUM(r.g3SlsxTuSxDd), 0)" +
            " FROM ProjectMonthRecord r WHERE r.monthKey = :monthKey AND r.active = true")
    Object[] aggregateG2G3(@Param("monthKey") String monthKey);

    @Query("SELECT COALESCE(SUM(r.g2TongSlsxDuKien), 0)," +
            " COALESCE(SUM(r.g5TongSlnt), 0)," +
            " COALESCE(SUM(r.g5RaTuongUngSlnt), 0)" +
            " FROM ProjectMonthRecord r WHERE r.monthKey = :monthKey AND r.active = true")
    Object[] aggregateMonthlyRevenue(@Param("monthKey") String monthKey);

    @Query("SELECT r.monthKey, COUNT(DISTINCT r.project.id)," +
            " COALESCE(SUM(r.g5DoanhThu), 0), COALESCE(SUM(r.g5TongSlnt), 0)" +
            " FROM ProjectMonthRecord r WHERE r.monthKey IN :monthKeys AND r.active = true" +
            " GROUP BY r.monthKey ORDER BY r.monthKey")
    List<Object[]> aggregateMonthlyG5(@Param("monthKeys") List<String> monthKeys);
}
