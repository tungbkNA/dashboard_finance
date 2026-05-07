package com.internal.projectmgmt.controller;

import com.internal.projectmgmt.dto.ApiResponse;
import com.internal.projectmgmt.dto.HealthStatusDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/binance")
public class HealthController {

    @Value("${app.version}")
    private String appVersion;

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<HealthStatusDto>> health() {
        HealthStatusDto status = new HealthStatusDto("UP", "dashboard-finance", appVersion);
        return ResponseEntity.ok(ApiResponse.success("OK", status));
    }
}
