package com.internal.projectmgmt.service;

import com.internal.projectmgmt.dto.filegroup.FileGroupRequest;
import com.internal.projectmgmt.dto.filegroup.FileGroupResponse;
import com.internal.projectmgmt.dto.filegroup.FileGroupUpdateRequest;
import com.internal.projectmgmt.entity.FileGroup;
import com.internal.projectmgmt.exception.AppException;
import com.internal.projectmgmt.mapper.FileGroupMapper;
import com.internal.projectmgmt.repository.FileGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileGroupService {

    private final FileGroupRepository fileGroupRepository;
    private final FileGroupMapper fileGroupMapper;

    @Transactional(readOnly = true)
    public List<FileGroupResponse> findAll() {
        return fileGroupRepository.findAll().stream()
                .map(g -> fileGroupMapper.toResponse(g, fileGroupRepository.countFileRecordsByGroupId(g.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> findAllActive() {
        return fileGroupRepository.findByActiveTrue().stream()
                .map(g -> Map.<String, Object>of("id", g.getId(), "name", g.getName()))
                .toList();
    }

    @Transactional
    public FileGroupResponse create(FileGroupRequest request) {
        if (fileGroupRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw new AppException("FILE_GROUP_NAME_DUPLICATE", "Tên nhóm file đã tồn tại");
        }
        FileGroup entity = fileGroupMapper.toEntity(request);
        entity = fileGroupRepository.save(entity);
        return fileGroupMapper.toResponse(entity, 0);
    }

    @Transactional
    public FileGroupResponse update(UUID id, FileGroupUpdateRequest request) {
        FileGroup entity = fileGroupRepository.findById(id)
                .orElseThrow(() -> new AppException("FILE_GROUP_NOT_FOUND", "Nhóm file không tồn tại"));

        if (fileGroupRepository.existsByNameIgnoreCaseAndIdNot(request.name().trim(), id)) {
            throw new AppException("FILE_GROUP_NAME_DUPLICATE", "Tên nhóm file đã tồn tại");
        }

        entity.setName(request.name().trim());
        entity.setDescription(request.description());
        entity.setActive(request.active());
        entity = fileGroupRepository.save(entity);

        long fileCount = fileGroupRepository.countFileRecordsByGroupId(id);
        return fileGroupMapper.toResponse(entity, fileCount);
    }

    @Transactional
    public void delete(UUID id) {
        FileGroup entity = fileGroupRepository.findById(id)
                .orElseThrow(() -> new AppException("FILE_GROUP_NOT_FOUND", "Nhóm file không tồn tại"));

        long fileCount = fileGroupRepository.countFileRecordsByGroupId(id);
        if (fileCount > 0) {
            throw new AppException("FILE_GROUP_IN_USE",
                    "Nhóm file đang được sử dụng bởi " + fileCount + " bản ghi file");
        }

        fileGroupRepository.delete(entity);
    }
}
