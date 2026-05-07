package com.internal.projectmgmt.controller;

import com.internal.projectmgmt.dto.ApiResponse;
import com.internal.projectmgmt.dto.filegroup.FileGroupRequest;
import com.internal.projectmgmt.dto.filegroup.FileGroupResponse;
import com.internal.projectmgmt.dto.filegroup.FileGroupUpdateRequest;
import com.internal.projectmgmt.service.FileGroupService;
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
@RequestMapping("/api/handbook/file-groups")
@RequiredArgsConstructor
public class FileGroupController {

    private final FileGroupService fileGroupService;

    @GetMapping
    @PreAuthorize("hasAuthority('MANAGE_HANDBOOK')")
    public ResponseEntity<ApiResponse<List<FileGroupResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(fileGroupService.findAll()));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAuthority('MANAGE_HANDBOOK')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getActive() {
        return ResponseEntity.ok(ApiResponse.success(fileGroupService.findAllActive()));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_HANDBOOK')")
    public ResponseEntity<ApiResponse<FileGroupResponse>> create(@Valid @RequestBody FileGroupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(fileGroupService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_HANDBOOK')")
    public ResponseEntity<ApiResponse<FileGroupResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody FileGroupUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(fileGroupService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_HANDBOOK')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        fileGroupService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa nhóm file thành công", null));
    }
}
