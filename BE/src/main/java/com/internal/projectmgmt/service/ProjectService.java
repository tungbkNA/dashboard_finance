package com.internal.projectmgmt.service;

import com.internal.projectmgmt.dto.project.ProjectRequest;
import com.internal.projectmgmt.dto.project.ProjectResponse;
import com.internal.projectmgmt.entity.Customer;
import com.internal.projectmgmt.entity.Project;
import com.internal.projectmgmt.entity.ProjectType;
import com.internal.projectmgmt.exception.AppException;
import com.internal.projectmgmt.exception.ShrinkWarningException;
import com.internal.projectmgmt.mapper.ProjectMapper;
import com.internal.projectmgmt.repository.CustomerRepository;
import com.internal.projectmgmt.repository.ProjectRepository;
import com.internal.projectmgmt.repository.ProjectTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final CustomerRepository customerRepository;
    private final ProjectTypeRepository projectTypeRepository;
    private final ProjectMapper projectMapper;
    private final ProjectMonthRecordService projectMonthRecordService;

    @Transactional(readOnly = true)
    public List<ProjectResponse> findAll() {
        return projectRepository.findAllByDeletedFalse().stream()
                .map(projectMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse findById(UUID id) {
        Project project = projectRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AppException("PROJECT_NOT_FOUND", "Dự án không tồn tại"));
        return projectMapper.toResponse(project);
    }

    @Transactional
    public ProjectResponse create(ProjectRequest request) {
        if (projectRepository.existsByProjectCodeIgnoreCaseAndDeletedFalse(request.projectCode())) {
            throw new AppException("PROJECT_CODE_DUPLICATE", "Mã dự án đã tồn tại");
        }
        validateMonthRange(request.monthStart(), request.monthEnd());

        Customer customer = customerRepository.findByIdAndDeletedFalse(request.customerId())
                .orElseThrow(() -> new AppException("CUSTOMER_NOT_FOUND", "Khách hàng không tồn tại"));
        ProjectType projectType = projectTypeRepository.findByIdAndDeletedFalse(request.projectTypeId())
                .orElseThrow(() -> new AppException("PROJECT_TYPE_NOT_FOUND", "Loại dự án không tồn tại"));

        Project project = projectMapper.toEntity(request, customer, projectType);
        Project saved = projectRepository.save(project);
        projectMonthRecordService.generateRecordsForProject(saved);
        return projectMapper.toResponse(saved);
    }

    @Transactional
    public ProjectResponse update(UUID id, ProjectRequest request, boolean confirmShrink) {
        Project project = projectRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AppException("PROJECT_NOT_FOUND", "Dự án không tồn tại"));

        if (projectRepository.existsByProjectCodeIgnoreCaseAndDeletedFalseAndIdNot(request.projectCode(), id)) {
            throw new AppException("PROJECT_CODE_DUPLICATE", "Mã dự án đã tồn tại");
        }
        validateMonthRange(request.monthStart(), request.monthEnd());

        // Detect month range change
        boolean rangeChanged = !project.getMonthStart().equals(request.monthStart())
                || !project.getMonthEnd().equals(request.monthEnd());

        if (rangeChanged) {
            List<String> shrinkMonths = projectMonthRecordService
                    .computeShrinkMonthKeys(id, request.monthStart(), request.monthEnd());
            if (!shrinkMonths.isEmpty()) {
                if (!confirmShrink) {
                    throw new ShrinkWarningException(shrinkMonths);
                }
                projectMonthRecordService.markInactiveForShrink(id, shrinkMonths);
            }
        }

        Customer customer = customerRepository.findByIdAndDeletedFalse(request.customerId())
                .orElseThrow(() -> new AppException("CUSTOMER_NOT_FOUND", "Khách hàng không tồn tại"));
        ProjectType projectType = projectTypeRepository.findByIdAndDeletedFalse(request.projectTypeId())
                .orElseThrow(() -> new AppException("PROJECT_TYPE_NOT_FOUND", "Loại dự án không tồn tại"));

        project.setProjectCode(request.projectCode());
        project.setProjectName(request.projectName());
        project.setCustomer(customer);
        project.setProjectType(projectType);
        project.setPrice(request.price());
        project.setStatusContract(request.statusContract());
        project.setStatusProject(request.statusProject());
        project.setMonthStart(request.monthStart());
        project.setMonthEnd(request.monthEnd());

        Project saved = projectRepository.save(project);

        // Generate new records if range expanded
        if (rangeChanged) {
            projectMonthRecordService.generateRecordsForProject(saved);
        }

        return projectMapper.toResponse(saved);
    }

    @Transactional
    public void softDelete(UUID id) {
        Project project = projectRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AppException("PROJECT_NOT_FOUND", "Dự án không tồn tại"));
        project.setDeleted(true);
        projectRepository.save(project);
    }

    // ---- helpers ----

    /**
     * Compares month strings in format mm/yyyy by converting to yyyymm integer.
     * Throws MONTH_RANGE_INVALID if monthEnd < monthStart.
     */
    private void validateMonthRange(String monthStart, String monthEnd) {
        if (toYearMonth(monthEnd) < toYearMonth(monthStart)) {
            throw new AppException("MONTH_RANGE_INVALID",
                    "Tháng kết thúc phải >= tháng bắt đầu");
        }
    }

    private int toYearMonth(String monthYear) {
        // format: mm/yyyy → yyyymm as integer
        String[] parts = monthYear.split("/");
        return Integer.parseInt(parts[1]) * 100 + Integer.parseInt(parts[0]);
    }
}
