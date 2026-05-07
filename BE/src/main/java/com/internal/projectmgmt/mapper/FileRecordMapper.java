package com.internal.projectmgmt.mapper;

import com.internal.projectmgmt.dto.filerecord.FileRecordResponse;
import com.internal.projectmgmt.entity.FileGroup;
import com.internal.projectmgmt.entity.FileRecord;
import org.springframework.stereotype.Component;

@Component
public class FileRecordMapper {

    public FileRecord toEntity(String fileName, String fileUrl, FileGroup fileGroup, String createdBy) {
        return FileRecord.builder()
                .fileName(fileName)
                .fileUrl(fileUrl)
                .fileGroup(fileGroup)
                .createdBy(createdBy)
                .build();
    }

    public FileRecordResponse toResponse(FileRecord entity) {
        return new FileRecordResponse(
                entity.getId(),
                entity.getFileName(),
                entity.getFileUrl(),
                entity.getFileGroup().getId(),
                entity.getFileGroup().getName(),
                entity.getCreatedBy(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
