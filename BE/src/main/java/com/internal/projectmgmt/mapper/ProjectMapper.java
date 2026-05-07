package com.internal.projectmgmt.mapper;

import com.internal.projectmgmt.dto.project.ProjectRequest;
import com.internal.projectmgmt.dto.project.ProjectResponse;
import com.internal.projectmgmt.entity.Customer;
import com.internal.projectmgmt.entity.Project;
import com.internal.projectmgmt.entity.ProjectType;
import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {

    public Project toEntity(ProjectRequest request, Customer customer, ProjectType projectType) {
        return Project.builder()
                .projectCode(request.projectCode())
                .projectName(request.projectName())
                .customer(customer)
                .projectType(projectType)
                .price(request.price())
                .statusContract(request.statusContract())
                .statusProject(request.statusProject())
                .monthStart(request.monthStart())
                .monthEnd(request.monthEnd())
                .build();
    }

    public ProjectResponse toResponse(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getProjectCode(),
                project.getProjectName(),
                project.getCustomer().getId(),
                project.getCustomer().getCustomerName(),
                project.getProjectType().getId(),
                project.getProjectType().getValue(),
                project.getPrice(),
                project.getStatusContract(),
                project.getStatusProject(),
                project.getMonthStart(),
                project.getMonthEnd(),
                project.getCreatedAt(),
                project.getUpdatedAt());
    }
}
