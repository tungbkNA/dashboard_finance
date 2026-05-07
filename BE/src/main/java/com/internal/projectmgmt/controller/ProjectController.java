package com.internal.projectmgmt.controller;

import com.internal.projectmgmt.dto.ApiResponse;
import com.internal.projectmgmt.dto.project.ProjectRequest;
import com.internal.projectmgmt.dto.project.ProjectResponse;
import com.internal.projectmgmt.exception.ShrinkWarningException;
import com.internal.projectmgmt.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/binance/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(projectService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(projectService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProjectResponse>> create(
            @Valid @RequestBody ProjectRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(projectService.create(request)));
    }

    @PutMapping("/{id}")
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
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        projectService.softDelete(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa dự án thành công", null));
    }
}
