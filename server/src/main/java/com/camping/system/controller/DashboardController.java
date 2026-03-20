package com.camping.system.controller;

import com.camping.system.common.ApiResponse;
import com.camping.system.dto.DashboardDtos;
import com.camping.system.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ApiResponse<DashboardDtos.DashboardResponse> getDashboard() {
        return ApiResponse.success(dashboardService.getDashboard());
    }
}
