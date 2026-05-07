package com.internal.projectmgmt.service;

import com.internal.projectmgmt.dto.projecttype.ProjectTypeRequest;
import com.internal.projectmgmt.entity.ProjectType;
import com.internal.projectmgmt.exception.AppException;
import com.internal.projectmgmt.mapper.ProjectTypeMapper;
import com.internal.projectmgmt.repository.ProjectTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectTypeServiceTest {

    @Mock
    private ProjectTypeRepository projectTypeRepository;
    @Mock
    private ProjectTypeMapper projectTypeMapper;

    @InjectMocks
    private ProjectTypeService projectTypeService;

    // T021 — duplicate key throws AppException
    @Test
    void create_shouldThrow_whenKeyDuplicate() {
        ProjectTypeRequest request = new ProjectTypeRequest("DUPLICATE", "Some value");
        when(projectTypeRepository.existsByKeyIgnoreCaseAndDeletedFalse("DUPLICATE"))
                .thenReturn(true);

        assertThatThrownBy(() -> projectTypeService.create(request))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Key loại dự án đã tồn tại")
                .extracting("code").isEqualTo("PROJECT_TYPE_KEY_DUPLICATE");
    }

    // T022 — softDelete in-use: returns inUse=true, does NOT soft delete
    @Test
    void softDelete_shouldReturnInUseWarning_whenInUse() {
        UUID id = UUID.randomUUID();
        ProjectType entity = ProjectType.builder().id(id).key("K").value("V").build();
        when(projectTypeRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));
        when(projectTypeRepository.countActiveProjectsByProjectTypeId(id)).thenReturn(3L);

        ProjectTypeService.DeleteResult result = projectTypeService.softDelete(id, false);

        assertThat(result.inUse()).isTrue();
        assertThat(result.usageCount()).isEqualTo(3L);
        // entity must NOT be saved (no soft delete without confirmation)
        verify(projectTypeRepository, never()).save(any());
    }

    // T022 — softDelete with confirmed=true: performs soft delete regardless of
    // usage
    @Test
    void softDelete_shouldSoftDelete_whenConfirmed() {
        UUID id = UUID.randomUUID();
        ProjectType entity = ProjectType.builder().id(id).key("K").value("V").build();
        when(projectTypeRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));
        when(projectTypeRepository.countActiveProjectsByProjectTypeId(id)).thenReturn(5L);
        when(projectTypeRepository.save(any())).thenReturn(entity);

        ProjectTypeService.DeleteResult result = projectTypeService.softDelete(id, true);

        assertThat(result.inUse()).isFalse();
        assertThat(entity.isDeleted()).isTrue();
        verify(projectTypeRepository).save(entity);
    }
}
