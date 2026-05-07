package com.internal.projectmgmt.repository;

import com.internal.projectmgmt.entity.FileGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface FileGroupRepository extends JpaRepository<FileGroup, UUID> {

    List<FileGroup> findByActiveTrue();

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);

    @Query("SELECT COUNT(fr) FROM FileRecord fr WHERE fr.fileGroup.id = :groupId")
    long countFileRecordsByGroupId(UUID groupId);
}
