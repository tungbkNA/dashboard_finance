package com.internal.projectmgmt.controller;

import com.internal.projectmgmt.dto.ApiResponse;
import com.internal.projectmgmt.dto.monthlyrecord.FieldMetadataResponse;
import com.internal.projectmgmt.dto.monthlyrecord.ProjectMonthRecordRequest;
import com.internal.projectmgmt.dto.monthlyrecord.ProjectMonthRecordResponse;
import com.internal.projectmgmt.dto.monthlyrecord.ProjectMonthRecordSummaryResponse;
import com.internal.projectmgmt.exception.AppException;
import com.internal.projectmgmt.service.ProjectMonthRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Tag(name = "Project Monthly Records", description = "Quản lý bản ghi tháng của dự án")
@RestController
@RequestMapping("/api/binance/project-monthly-records")
@RequiredArgsConstructor
public class ProjectMonthRecordController {

    private final ProjectMonthRecordService service;

    @Operation(summary = "Lấy metadata các trường", description = "Trả về phân loại trường công thức/nhập tay theo nhóm (tĩnh, không query DB)")
    @GetMapping("/field-metadata")
    @PreAuthorize("hasAuthority('MONTHLY_RECORD_VIEW')")
    public ResponseEntity<ApiResponse<FieldMetadataResponse>> getFieldMetadata() {
        return ResponseEntity.ok(ApiResponse.success(service.getFieldMetadata()));
    }

    @Operation(summary = "Lấy danh sách bản ghi tháng", description = "Trả về danh sách đầy đủ các bản ghi tháng active theo tháng chỉ định (mặc định: tháng hiện tại, định dạng YYYY-MM)")
    @GetMapping
    @PreAuthorize("hasAuthority('MONTHLY_RECORD_VIEW')")
    public ResponseEntity<ApiResponse<List<ProjectMonthRecordResponse>>> getAll(
            @RequestParam(required = false) String monthKey) {
        String mk = (monthKey != null && !monthKey.isBlank()) ? monthKey : YearMonth.now().toString();
        return ResponseEntity.ok(ApiResponse.success(service.findAllByMonthKey(mk)));
    }

    @Operation(summary = "Tìm kiếm bản ghi tháng có phân trang", description = "Tìm theo mã/tên dự án và tháng (YYYY-MM), phân trang")
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('MONTHLY_RECORD_VIEW')")
    public ResponseEntity<ApiResponse<Page<ProjectMonthRecordResponse>>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String monthKey,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(service.search(keyword, monthKey, page, size)));
    }

    @Operation(summary = "Lấy chi tiết bản ghi tháng", description = "Trả về toàn bộ trường của bản ghi tháng theo ID, bao gồm cả cờ isFirstMonth")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MONTHLY_RECORD_VIEW')")
    public ResponseEntity<ApiResponse<ProjectMonthRecordResponse>> getById(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(ApiResponse.success(service.findById(id)));
        } catch (AppException ex) {
            if ("MONTHLY_RECORD_NOT_FOUND".equals(ex.getCode())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(ex.getCode(), ex.getMessage()));
            }
            throw ex;
        }
    }

    @Operation(summary = "Cập nhật bản ghi tháng", description = "Nhận các trường nhập tay, tính toán lại công thức, lưu và cascades sang tháng kế tiếp. Trả về bản ghi đầy đủ sau khi cập nhật")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MONTHLY_RECORD_EDIT')")
    public ResponseEntity<ApiResponse<ProjectMonthRecordResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody ProjectMonthRecordRequest request) {
        try {
            return ResponseEntity.ok(ApiResponse.success(service.update(id, request)));
        } catch (AppException ex) {
            if ("MONTHLY_RECORD_NOT_FOUND".equals(ex.getCode())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(ex.getCode(), ex.getMessage()));
            }
            if ("MONTHLY_RECORD_INACTIVE".equals(ex.getCode())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error(ex.getCode(), ex.getMessage()));
            }
            if ("MONTHLY_RECORD_LOCKED".equals(ex.getCode())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error(ex.getCode(), ex.getMessage()));
            }
            throw ex;
        }
    }
}
