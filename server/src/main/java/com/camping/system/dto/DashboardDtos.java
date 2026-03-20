package com.camping.system.dto;

import com.camping.system.enums.RiskLevel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class DashboardDtos {

    private DashboardDtos() {
    }

    public record OverviewStats(
            long siteCount,
            long activeSiteCount,
            long reservationCount,
            long todayReservationCount,
            long confirmedReservationCount,
            BigDecimal totalRevenue,
            long observationCount,
            long verifiedObservationCount,
            long activeFraudAlerts
    ) {
    }

    public record TrendPoint(
            LocalDate date,
            long reservations,
            BigDecimal revenue,
            long observations
    ) {
    }

    public record SiteRankingItem(
            Long siteId,
            String siteName,
            String city,
            long reservationCount,
            long observationCount,
            BigDecimal occupancyRate
    ) {
    }

    public record CategoryStatItem(
            String name,
            long value
    ) {
    }

    public record FraudAlertItem(
            Long id,
            String eventType,
            RiskLevel riskLevel,
            String actionTaken,
            String detail,
            String sourceIp,
            LocalDateTime createdAt
    ) {
    }

    public record GeoBoardItem(
            Long siteId,
            String siteName,
            String city,
            BigDecimal latitude,
            BigDecimal longitude,
            BigDecimal occupancyRate,
            long reservationCount,
            long observationCount,
            int ecoIndex
    ) {
    }

    public record DashboardResponse(
            OverviewStats overview,
            List<TrendPoint> trends,
            List<SiteRankingItem> siteRankings,
            List<CategoryStatItem> observationCategories,
            List<FraudAlertItem> fraudAlerts,
            List<GeoBoardItem> geoBoard
    ) {
    }
}
