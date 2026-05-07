package com.internal.projectmgmt.mapper;

import com.internal.projectmgmt.dto.projecttype.ProjectTypeRequest;
import com.internal.projectmgmt.dto.projecttype.ProjectTypeResponse;
import com.internal.projectmgmt.entity.ProjectType;
import org.springframework.stereotype.Component;

@Component
public class ProjectTypeMapper {

    public ProjectType toEntity(ProjectTypeRequest request) {
        return ProjectType.builder()
                .key(request.key())
                .value(request.value())
                .build();
    }

    public ProjectTypeResponse toResponse(ProjectType entity) {
        return new ProjectTypeResponse(entity.getId(), entity.getKey(), entity.getValue());
    }
}
