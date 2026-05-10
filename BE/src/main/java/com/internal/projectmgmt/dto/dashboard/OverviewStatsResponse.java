package com.internal.projectmgmt.dto.dashboard;

import java.util.Map;

public record OverviewStatsResponse(
        long totalProjects,
        Map<String, Long> projectsByStatus,
        long hasContract,
        long noContract,
        long activeUsers,
        long inactiveUsers) {
}
