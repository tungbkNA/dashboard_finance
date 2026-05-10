package com.internal.projectmgmt.controller;

import com.internal.projectmgmt.dto.ApiResponse;
import com.internal.projectmgmt.dto.projecttype.ProjectTypeRequest;
import com.internal.projectmgmt.dto.projecttype.ProjectTypeResponse;
import com.internal.projectmgmt.service.ProjectTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/binance/project-types")
@RequiredArgsConstructor
public class ProjectTypeController {

    private final ProjectTypeService projectTypeService;

    @GetMapping
    @PreAuthorize("hasAuthority('PROJECT_SETTINGS_VIEW')")
    public ResponseEntity<ApiResponse<List<ProjectTypeResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(projectTypeService.findAll()));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PROJECT_SETTINGS_MANAGE')")
    public ResponseEntity<ApiResponse<ProjectTypeResponse>> create(
            @Valid @RequestBody ProjectTypeRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(projectTypeService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PROJECT_SETTINGS_MANAGE')")
    public ResponseEntity<ApiResponse<ProjectTypeResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody ProjectTypeRequest request) {
        return ResponseEntity.ok(ApiResponse.success(projectTypeService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PROJECT_SETTINGS_MANAGE')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> delete(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "false") boolean confirmed) {

        ProjectTypeService.DeleteResult result = projectTypeService.softDelete(id, confirmed);

        if (result.inUse()) {
            return ResponseEntity.ok(ApiResponse.of(
                    "IN_USE_WARNING",
                    "Loại dự án đang được " + result.usageCount() + " dự án sử dụng",
                    Map.of("inUse", true, "usageCount", result.usageCount())));
        }

        return ResponseEntity.ok(ApiResponse.success("Xóa loại dự án thành công", null));
    }
}
