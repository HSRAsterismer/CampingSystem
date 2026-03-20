package com.camping.system.controller;

import com.camping.system.common.ApiResponse;
import com.camping.system.dto.SiteDtos;
import com.camping.system.enums.SiteStatus;
import com.camping.system.service.CampingSiteService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/sites")
public class CampingSiteController {

    private final CampingSiteService campingSiteService;

    public CampingSiteController(CampingSiteService campingSiteService) {
        this.campingSiteService = campingSiteService;
    }

    @GetMapping
    public ApiResponse<List<SiteDtos.SiteItem>> listSites(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) SiteStatus status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
    ) {
        return ApiResponse.success(campingSiteService.listSites(new SiteDtos.SiteQuery(keyword, city, status, startDate, endDate)));
    }

    @PostMapping
    public ApiResponse<SiteDtos.SiteItem> createSite(@Valid @RequestBody SiteDtos.SaveSiteRequest request) {
        return ApiResponse.success("露营点创建成功", campingSiteService.createSite(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<SiteDtos.SiteItem> updateSite(@PathVariable Long id, @Valid @RequestBody SiteDtos.SaveSiteRequest request) {
        return ApiResponse.success("露营点更新成功", campingSiteService.updateSite(id, request));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<SiteDtos.SiteItem> updateStatus(@PathVariable Long id, @RequestParam SiteStatus status) {
        return ApiResponse.success("露营点状态更新成功", campingSiteService.updateSiteStatus(id, status));
    }
}
