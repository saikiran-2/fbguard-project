package com.fbguard.backend.controller;

import com.fbguard.backend.dto.response.DashboardStatsResponse;
import com.fbguard.backend.dto.response.RiskBucketResponse;
import com.fbguard.backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/api/dashboard/stats")
    public DashboardStatsResponse stats(Authentication auth) {
        return dashboardService.getStats(auth.getName());
    }

    @GetMapping("/api/admin/dashboard/risk-distribution")
    public List<RiskBucketResponse> riskDistribution() {
        return dashboardService.getRiskDistribution();
    }
}
