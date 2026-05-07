package com.internal.projectmgmt.service;

import com.internal.projectmgmt.dto.filegroup.FileGroupRequest;
import com.internal.projectmgmt.dto.filegroup.FileGroupResponse;
import com.internal.projectmgmt.entity.FileGroup;
import com.internal.projectmgmt.exception.AppException;
import com.internal.projectmgmt.mapper.FileGroupMapper;
import com.internal.projectmgmt.repository.FileGroupRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileGroupServiceTest {

    @Mock
    private FileGroupRepository fileGroupRepository;
    @Mock
    private FileGroupMapper fileGroupMapper;

    @InjectMocks
    private FileGroupService fileGroupService;

    @Test
    void delete_withFilesInGroup_shouldThrowAppException() {
        UUID groupId = UUID.randomUUID();
        FileGroup group = FileGroup.builder().id(groupId).name("Test").build();

        when(fileGroupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(fileGroupRepository.countFileRecordsByGroupId(groupId)).thenReturn(3L);

        assertThatThrownBy(() -> fileGroupService.delete(groupId))
                .isInstanceOf(AppException.class)
                .satisfies(e -> {
                    assertThat(((AppException) e).getCode()).isEqualTo("FILE_GROUP_IN_USE");
                    assertThat(e.getMessage()).contains("3");
                });

        verify(fileGroupRepository, never()).delete(any());
    }

    @Test
    void delete_withEmptyGroup_shouldSucceed() {
        UUID groupId = UUID.randomUUID();
        FileGroup group = FileGroup.builder().id(groupId).name("Test").build();

        when(fileGroupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(fileGroupRepository.countFileRecordsByGroupId(groupId)).thenReturn(0L);

        fileGroupService.delete(groupId);

        verify(fileGroupRepository).delete(group);
    }

    @Test
    void delete_withNonExistentGroup_shouldThrowNotFound() {
        UUID groupId = UUID.randomUUID();
        when(fileGroupRepository.findById(groupId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> fileGroupService.delete(groupId))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getCode()).isEqualTo("FILE_GROUP_NOT_FOUND"));
    }

    @Test
    void create_withDuplicateName_shouldThrowAppException() {
        when(fileGroupRepository.existsByNameIgnoreCase("Duplicate")).thenReturn(true);

        FileGroupRequest request = new FileGroupRequest("Duplicate", "desc");
        assertThatThrownBy(() -> fileGroupService.create(request))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getCode()).isEqualTo("FILE_GROUP_NAME_DUPLICATE"));

        verify(fileGroupRepository, never()).save(any());
    }

    @Test
    void create_withUniqueName_shouldSucceed() {
        when(fileGroupRepository.existsByNameIgnoreCase("New Group")).thenReturn(false);

        FileGroup entity = FileGroup.builder()
                .id(UUID.randomUUID())
                .name("New Group")
                .description("desc")
                .active(true)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        when(fileGroupMapper.toEntity(any())).thenReturn(entity);
        when(fileGroupRepository.save(any())).thenReturn(entity);
        when(fileGroupMapper.toResponse(entity, 0)).thenReturn(
                new FileGroupResponse(entity.getId(), "New Group", "desc", true, 0,
                        entity.getCreatedAt(), entity.getUpdatedAt()));

        FileGroupResponse result = fileGroupService.create(new FileGroupRequest("New Group", "desc"));

        assertThat(result.name()).isEqualTo("New Group");
        verify(fileGroupRepository).save(any());
    }
}
