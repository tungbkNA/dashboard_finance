package com.internal.projectmgmt.service;

import com.internal.projectmgmt.dto.projecttype.ProjectTypeRequest;
import com.internal.projectmgmt.dto.projecttype.ProjectTypeResponse;
import com.internal.projectmgmt.entity.ProjectType;
import com.internal.projectmgmt.exception.AppException;
import com.internal.projectmgmt.mapper.ProjectTypeMapper;
import com.internal.projectmgmt.repository.ProjectTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectTypeService {

    private final ProjectTypeRepository projectTypeRepository;
    private final ProjectTypeMapper projectTypeMapper;

    public List<ProjectTypeResponse> findAll() {
        return projectTypeRepository.findAllByDeletedFalse().stream()
                .map(projectTypeMapper::toResponse)
                .toList();
    }

    @Transactional
    public ProjectTypeResponse create(ProjectTypeRequest request) {
        if (projectTypeRepository.existsByKeyIgnoreCaseAndDeletedFalse(request.key())) {
            throw new AppException("PROJECT_TYPE_KEY_DUPLICATE", "Key loại dự án đã tồn tại");
        }
        ProjectType entity = projectTypeMapper.toEntity(request);
        return projectTypeMapper.toResponse(projectTypeRepository.save(entity));
    }

    @Transactional
    public ProjectTypeResponse update(UUID id, ProjectTypeRequest request) {
        ProjectType entity = projectTypeRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AppException("PROJECT_TYPE_NOT_FOUND", "Loại dự án không tồn tại"));

        if (!entity.getKey().equalsIgnoreCase(request.key())
                && projectTypeRepository.existsByKeyIgnoreCaseAndDeletedFalse(request.key())) {
            throw new AppException("PROJECT_TYPE_KEY_DUPLICATE", "Key loại dự án đã tồn tại");
        }

        entity.setKey(request.key());
        entity.setValue(request.value());
        return projectTypeMapper.toResponse(projectTypeRepository.save(entity));
    }

    /**
     * Two-step soft delete:
     * - confirmed=false: if in use → return usage info without deleting; if not in
     * use → soft delete
     * - confirmed=true: soft delete regardless
     *
     * @return DeleteResult with inUse flag and usageCount
     */
    @Transactional
    public DeleteResult softDelete(UUID id, boolean confirmed) {
        ProjectType entity = projectTypeRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AppException("PROJECT_TYPE_NOT_FOUND", "Loại dự án không tồn tại"));

        long usageCount = projectTypeRepository.countActiveProjectsByProjectTypeId(id);

        if (!confirmed && usageCount > 0) {
            return new DeleteResult(true, usageCount);
        }

        entity.setDeleted(true);
        projectTypeRepository.save(entity);
        return new DeleteResult(false, usageCount);
    }

    public record DeleteResult(boolean inUse, long usageCount) {
    }
}
