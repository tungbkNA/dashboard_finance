package com.internal.projectmgmt.repository;

import com.internal.projectmgmt.entity.FileRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface FileRecordRepository extends JpaRepository<FileRecord, UUID> {

    long countByFileGroupId(UUID fileGroupId);

    @Query(value = """
            SELECT fr FROM FileRecord fr JOIN FETCH fr.fileGroup fg
            WHERE (:keyword = '' OR LOWER(fr.fileName) LIKE LOWER(CONCAT('%', :keyword, '%')))
            AND (:groupId IS NULL OR fg.id = :groupId)
            AND (:includeInactive = true OR fg.active = true)
            """, countQuery = """
            SELECT COUNT(fr) FROM FileRecord fr JOIN fr.fileGroup fg
            WHERE (:keyword = '' OR LOWER(fr.fileName) LIKE LOWER(CONCAT('%', :keyword, '%')))
            AND (:groupId IS NULL OR fg.id = :groupId)
            AND (:includeInactive = true OR fg.active = true)
            """)
    Page<FileRecord> findAllWithFilters(
            @Param("keyword") String keyword,
            @Param("groupId") UUID groupId,
            @Param("includeInactive") boolean includeInactive,
            Pageable pageable);
}
