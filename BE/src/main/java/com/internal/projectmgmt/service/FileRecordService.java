package com.internal.projectmgmt.service;

import com.internal.projectmgmt.dto.filerecord.FileRecordRequest;
import com.internal.projectmgmt.dto.filerecord.FileRecordResponse;
import com.internal.projectmgmt.entity.FileGroup;
import com.internal.projectmgmt.entity.FileRecord;
import com.internal.projectmgmt.exception.AppException;
import com.internal.projectmgmt.mapper.FileRecordMapper;
import com.internal.projectmgmt.repository.FileGroupRepository;
import com.internal.projectmgmt.repository.FileRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileRecordService {

    private final FileRecordRepository fileRecordRepository;
    private final FileGroupRepository fileGroupRepository;
    private final FileRecordMapper fileRecordMapper;

    @Transactional(readOnly = true)
    public Page<FileRecordResponse> findAll(String keyword, UUID groupId, boolean includeInactive, int page, int size) {
        String kw = (keyword == null || keyword.isBlank()) ? "" : keyword.trim();
        return fileRecordRepository.findAllWithFilters(kw, groupId, includeInactive,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(fileRecordMapper::toResponse);
    }

    @Transactional
    public FileRecordResponse create(FileRecordRequest request) {
        FileGroup group = findActiveGroup(request.groupId());

        String createdBy = getCurrentUsername();
        FileRecord entity = fileRecordMapper.toEntity(request.fileName(), request.fileUrl(), group, createdBy);
        entity = fileRecordRepository.save(entity);
        return fileRecordMapper.toResponse(entity);
    }

    @Transactional
    public FileRecordResponse update(UUID id, FileRecordRequest request) {
        FileRecord entity = fileRecordRepository.findById(id)
                .orElseThrow(() -> new AppException("FILE_RECORD_NOT_FOUND", "Bản ghi file không tồn tại"));

        FileGroup group = findActiveGroup(request.groupId());

        entity.setFileName(request.fileName());
        entity.setFileUrl(request.fileUrl());
        entity.setFileGroup(group);
        entity = fileRecordRepository.save(entity);
        return fileRecordMapper.toResponse(entity);
    }

    @Transactional
    public void delete(UUID id) {
        FileRecord entity = fileRecordRepository.findById(id)
                .orElseThrow(() -> new AppException("FILE_RECORD_NOT_FOUND", "Bản ghi file không tồn tại"));
        fileRecordRepository.delete(entity);
    }

    private FileGroup findActiveGroup(UUID groupId) {
        FileGroup group = fileGroupRepository.findById(groupId)
                .orElseThrow(() -> new AppException("FILE_GROUP_NOT_FOUND", "Nhóm file không tồn tại"));
        if (!group.isActive()) {
            throw new AppException("FILE_GROUP_INACTIVE", "Nhóm file đang ngưng hoạt động");
        }
        return group;
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }
}
