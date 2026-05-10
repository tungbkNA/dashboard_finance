package com.internal.projectmgmt.controller;

import com.internal.projectmgmt.dto.ApiResponse;
import com.internal.projectmgmt.dto.customer.CustomerRequest;
import com.internal.projectmgmt.dto.customer.CustomerResponse;
import com.internal.projectmgmt.service.CustomerService;
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
@RequestMapping("/api/binance/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    @PreAuthorize("hasAuthority('PROJECT_SETTINGS_VIEW')")
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(customerService.findAll()));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PROJECT_SETTINGS_MANAGE')")
    public ResponseEntity<ApiResponse<CustomerResponse>> create(
            @Valid @RequestBody CustomerRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(customerService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PROJECT_SETTINGS_MANAGE')")
    public ResponseEntity<ApiResponse<CustomerResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.ok(ApiResponse.success(customerService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PROJECT_SETTINGS_MANAGE')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> delete(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "false") boolean confirmed) {

        CustomerService.DeleteResult result = customerService.softDelete(id, confirmed);

        if (result.inUse()) {
            return ResponseEntity.ok(ApiResponse.of(
                    "IN_USE_WARNING",
                    "Khách hàng đang được " + result.usageCount() + " dự án sử dụng",
                    Map.of("inUse", true, "usageCount", result.usageCount())));
        }

        return ResponseEntity.ok(ApiResponse.success("Xóa khách hàng thành công", null));
    }
}
