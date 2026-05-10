package com.internal.projectmgmt.controller;

import com.internal.projectmgmt.dto.ApiResponse;
import com.internal.projectmgmt.dto.project.ProjectImportResult;
import com.internal.projectmgmt.dto.project.ProjectRequest;
import com.internal.projectmgmt.dto.project.ProjectResponse;
import com.internal.projectmgmt.entity.StatusContract;
import com.internal.projectmgmt.entity.StatusProject;
import com.internal.projectmgmt.exception.ShrinkWarningException;
import com.internal.projectmgmt.service.ProjectImportService;
import com.internal.projectmgmt.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/binance/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final ProjectImportService projectImportService;

    @GetMapping
    @PreAuthorize("hasAuthority('PROJECT_VIEW')")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(projectService.findAll()));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('PROJECT_VIEW')")
    public ResponseEntity<ApiResponse<Page<ProjectResponse>>> search(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) UUID projectTypeId,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) StatusContract statusContract,
            @RequestParam(required = false) StatusProject statusProject,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<ProjectResponse> result = projectService.search(keyword, projectTypeId, customerId,
                statusContract, statusProject, page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PROJECT_VIEW')")
    public ResponseEntity<ApiResponse<ProjectResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(projectService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PROJECT_CREATE')")
    public ResponseEntity<ApiResponse<ProjectResponse>> create(
            @Valid @RequestBody ProjectRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(projectService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PROJECT_EDIT')")
    public ResponseEntity<ApiResponse<?>> update(
            @PathVariable UUID id,
            @Valid @RequestBody ProjectRequest request,
            @RequestParam(defaultValue = "false") boolean confirmShrink) {
        try {
            return ResponseEntity.ok(ApiResponse.success(projectService.update(id, request, confirmShrink)));
        } catch (ShrinkWarningException ex) {
            return ResponseEntity.ok(
                    ApiResponse.of("MONTH_RANGE_SHRINK_WARNING", ex.getMessage(), ex.getPendingInactiveMonths()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PROJECT_DELETE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        projectService.softDelete(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa dự án thành công", null));
    }

    @GetMapping("/import/template")
    @PreAuthorize("hasAuthority('PROJECT_IMPORT')")
    public ResponseEntity<byte[]> downloadTemplate() throws IOException {
        byte[] content = projectImportService.generateTemplate();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=mau_import_du_an.xlsx")
                .contentType(
                        MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(content);
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('PROJECT_IMPORT')")
    public ResponseEntity<ApiResponse<ProjectImportResult>> importExcel(
            @RequestParam("file") MultipartFile file) throws IOException {
        ProjectImportResult result = projectImportService.importExcel(file);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
