package com.internal.projectmgmt.mapper;

import com.internal.projectmgmt.dto.filegroup.FileGroupRequest;
import com.internal.projectmgmt.dto.filegroup.FileGroupResponse;
import com.internal.projectmgmt.entity.FileGroup;
import org.springframework.stereotype.Component;

@Component
public class FileGroupMapper {

    public FileGroup toEntity(FileGroupRequest request) {
        return FileGroup.builder()
                .name(request.name().trim())
                .description(request.description())
                .build();
    }

    public FileGroupResponse toResponse(FileGroup entity, long fileCount) {
        return new FileGroupResponse(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.isActive(),
                fileCount,
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
