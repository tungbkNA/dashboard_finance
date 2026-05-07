package com.internal.projectmgmt.controller;

import com.internal.projectmgmt.dto.ApiResponse;
import com.internal.projectmgmt.dto.filerecord.FileRecordRequest;
import com.internal.projectmgmt.dto.filerecord.FileRecordResponse;
import com.internal.projectmgmt.service.FileRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/handbook/file-records")
@RequiredArgsConstructor
public class FileRecordController {

    private final FileRecordService fileRecordService;

    @GetMapping
    @PreAuthorize("hasAuthority('MANAGE_HANDBOOK')")
    public ResponseEntity<ApiResponse<Page<FileRecordResponse>>> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UUID groupId,
            @RequestParam(defaultValue = "false") boolean includeInactive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                fileRecordService.findAll(keyword, groupId, includeInactive, page, size)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_HANDBOOK')")
    public ResponseEntity<ApiResponse<FileRecordResponse>> create(@Valid @RequestBody FileRecordRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(fileRecordService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_HANDBOOK')")
    public ResponseEntity<ApiResponse<FileRecordResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody FileRecordRequest request) {
        return ResponseEntity.ok(ApiResponse.success(fileRecordService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_HANDBOOK')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        fileRecordService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa bản ghi file thành công", null));
    }
}
