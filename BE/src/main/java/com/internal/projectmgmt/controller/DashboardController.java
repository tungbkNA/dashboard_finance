package com.internal.projectmgmt.controller;

import com.internal.projectmgmt.dto.ApiResponse;
import com.internal.projectmgmt.dto.dashboard.MonthlyG5SummaryResponse;
import com.internal.projectmgmt.dto.dashboard.MonthlyRevenueResponse;
import com.internal.projectmgmt.dto.dashboard.OverviewStatsResponse;
import com.internal.projectmgmt.dto.dashboard.RevenueComparisonResponse;
import com.internal.projectmgmt.entity.StatusContract;
import com.internal.projectmgmt.entity.StatusProject;
import com.internal.projectmgmt.repository.AppUserRepository;
import com.internal.projectmgmt.repository.ProjectMonthRecordRepository;
import com.internal.projectmgmt.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final ProjectMonthRecordRepository recordRepository;
    private final ProjectRepository projectRepository;
    private final AppUserRepository appUserRepository;

    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyy-MM");

    @GetMapping("/overview")
    @PreAuthorize("hasAuthority('DASHBOARD_VIEW')")
    public ResponseEntity<ApiResponse<OverviewStatsResponse>> overview() {
        long total = projectRepository.countByDeletedFalse();

        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (StatusProject sp : StatusProject.values()) {
            byStatus.put(sp.name(), projectRepository.countByDeletedFalseAndStatusProject(sp));
        }

        long hasContract = projectRepository.countByDeletedFalseAndStatusContract(StatusContract.HAS_CONTRACT);
        long noContract = projectRepository.countByDeletedFalseAndStatusContract(StatusContract.NO_CONTRACT);

        long activeUsers = appUserRepository.countByDeletedFalseAndSystemFalseAndActiveTrue();
        long inactiveUsers = appUserRepository.countByDeletedFalseAndSystemFalseAndActiveFalse();

        return ResponseEntity.ok(ApiResponse.success(
                new OverviewStatsResponse(total, byStatus, hasContract, noContract, activeUsers, inactiveUsers)));
    }

    @GetMapping("/revenue-comparison")
    @PreAuthorize("hasAuthority('DASHBOARD_VIEW')")
    public ResponseEntity<ApiResponse<RevenueComparisonResponse>> revenueComparison(
            @RequestParam(required = false) String monthKey) {

        if (monthKey == null || monthKey.isBlank()) {
            monthKey = YearMonth.now().minusMonths(1).format(MONTH_FMT);
        }

        Object[] row = recordRepository.aggregateG2G3(monthKey);
        // The query returns a single Object[] with 8 BigDecimal values
        Object[] vals = (row != null && row.length > 0 && row[0] instanceof Object[])
                ? (Object[]) row[0]
                : row;

        RevenueComparisonResponse response = new RevenueComparisonResponse(
                monthKey,
                toBd(vals, 0), toBd(vals, 1),
                toBd(vals, 2), toBd(vals, 3),
                toBd(vals, 4), toBd(vals, 5),
                toBd(vals, 6), toBd(vals, 7));

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/monthly-revenue")
    @PreAuthorize("hasAuthority('DASHBOARD_VIEW')")
    public ResponseEntity<ApiResponse<MonthlyRevenueResponse>> monthlyRevenue(
            @RequestParam(required = false) String monthKey) {

        if (monthKey == null || monthKey.isBlank()) {
            monthKey = YearMonth.now().minusMonths(1).format(MONTH_FMT);
        }

        Object[] row = recordRepository.aggregateMonthlyRevenue(monthKey);
        Object[] vals = (row != null && row.length > 0 && row[0] instanceof Object[])
                ? (Object[]) row[0]
                : row;

        MonthlyRevenueResponse response = new MonthlyRevenueResponse(
                monthKey,
                toBd(vals, 0), toBd(vals, 1), toBd(vals, 2));

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/monthly-g5-summary")
    @PreAuthorize("hasAuthority('DASHBOARD_VIEW')")
    public ResponseEntity<ApiResponse<MonthlyG5SummaryResponse>> monthlyG5Summary() {
        YearMonth current = YearMonth.now();
        List<String> monthKeys = new ArrayList<>();
        for (int i = 11; i >= 0; i--) {
            monthKeys.add(current.minusMonths(i).format(MONTH_FMT));
        }

        List<Object[]> rows = recordRepository.aggregateMonthlyG5(monthKeys);
        Map<String, Object[]> byMonth = new LinkedHashMap<>();
        for (Object[] row : rows) {
            byMonth.put((String) row[0], row);
        }

        List<MonthlyG5SummaryResponse.MonthlyG5Entry> entries = new ArrayList<>();
        for (String mk : monthKeys) {
            Object[] row = byMonth.get(mk);
            if (row != null) {
                entries.add(new MonthlyG5SummaryResponse.MonthlyG5Entry(
                        mk, ((Number) row[1]).longValue(), toBd(row, 2), toBd(row, 3)));
            } else {
                entries.add(new MonthlyG5SummaryResponse.MonthlyG5Entry(
                        mk, 0, BigDecimal.ZERO, BigDecimal.ZERO));
            }
        }

        return ResponseEntity.ok(ApiResponse.success(new MonthlyG5SummaryResponse(entries)));
    }

    private BigDecimal toBd(Object[] arr, int idx) {
        if (arr == null || idx >= arr.length || arr[idx] == null)
            return BigDecimal.ZERO;
        if (arr[idx] instanceof BigDecimal bd)
            return bd;
        return new BigDecimal(arr[idx].toString());
    }
}
