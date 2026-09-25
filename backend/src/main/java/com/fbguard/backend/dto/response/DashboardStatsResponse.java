package com.fbguard.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class DashboardStatsResponse {
    private String username;
    private long totalApps;
    private long licensedApps;
    private long blockedApps;
    private long friendCount;
    private Map<String, Long> appsByStatus;
    private List<DayCount> submissionsPerDay;

    @Data
    @Builder
    public static class DayCount {
        private String day;
        private long count;
    }
}
