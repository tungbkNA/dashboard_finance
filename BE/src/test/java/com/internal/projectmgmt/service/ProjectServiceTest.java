package com.internal.projectmgmt.service;

import com.internal.projectmgmt.dto.project.ProjectRequest;
import com.internal.projectmgmt.entity.Customer;
import com.internal.projectmgmt.entity.Project;
import com.internal.projectmgmt.entity.ProjectType;
import com.internal.projectmgmt.entity.StatusContract;
import com.internal.projectmgmt.entity.StatusProject;
import com.internal.projectmgmt.exception.AppException;
import com.internal.projectmgmt.mapper.ProjectMapper;
import com.internal.projectmgmt.repository.CustomerRepository;
import com.internal.projectmgmt.repository.ProjectRepository;
import com.internal.projectmgmt.repository.ProjectTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private ProjectTypeRepository projectTypeRepository;
    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private ProjectService projectService;

    private ProjectRequest validRequest(String projectCode, String monthStart, String monthEnd) {
        return new ProjectRequest(
                projectCode,
                "Tên dự án",
                UUID.randomUUID(),
                UUID.randomUUID(),
                BigDecimal.ZERO,
                StatusContract.NO_CONTRACT,
                StatusProject.OPEN,
                monthStart,
                monthEnd);
    }

    // T012 — duplicate projectCode throws AppException
    @Test
    void create_shouldThrow_whenProjectCodeDuplicate() {
        ProjectRequest request = validRequest("DUP-001", "01/2026", "12/2026");
        when(projectRepository.existsByProjectCodeIgnoreCaseAndDeletedFalse("DUP-001"))
                .thenReturn(true);

        assertThatThrownBy(() -> projectService.create(request))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Mã dự án đã tồn tại")
                .extracting("code").isEqualTo("PROJECT_CODE_DUPLICATE");
    }

    // T013 — monthEnd < monthStart throws AppException
    @Test
    void create_shouldThrow_whenMonthEndBeforeMonthStart() {
        ProjectRequest request = validRequest("PRJ-001", "06/2026", "01/2026");
        when(projectRepository.existsByProjectCodeIgnoreCaseAndDeletedFalse("PRJ-001"))
                .thenReturn(false);

        assertThatThrownBy(() -> projectService.create(request))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Tháng kết thúc phải >= tháng bắt đầu")
                .extracting("code").isEqualTo("MONTH_RANGE_INVALID");
    }

    // monthEnd == monthStart should be valid (boundary)
    @Test
    void create_shouldNotThrow_whenMonthEndEqualsMonthStart() {
        ProjectRequest request = validRequest("PRJ-002", "06/2026", "06/2026");
        UUID customerId = request.customerId();
        UUID projectTypeId = request.projectTypeId();

        when(projectRepository.existsByProjectCodeIgnoreCaseAndDeletedFalse("PRJ-002"))
                .thenReturn(false);
        when(customerRepository.findByIdAndDeletedFalse(customerId))
                .thenReturn(Optional.of(Customer.builder().id(customerId).customerName("KH").build()));
        when(projectTypeRepository.findByIdAndDeletedFalse(projectTypeId))
                .thenReturn(Optional.of(ProjectType.builder().id(projectTypeId).value("Loại").build()));
        when(projectMapper.toEntity(any(), any(), any()))
                .thenReturn(Project.builder().build());
        when(projectRepository.save(any()))
                .thenReturn(Project.builder().build());
        when(projectMapper.toResponse(any())).thenReturn(null);

        // should not throw
        projectService.create(request);
    }
}
