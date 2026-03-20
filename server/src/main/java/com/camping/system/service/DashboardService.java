package com.camping.system.service;

import com.camping.system.dto.DashboardDtos;
import com.camping.system.entity.CampingSite;
import com.camping.system.entity.ObservationRecord;
import com.camping.system.entity.PaymentOrder;
import com.camping.system.entity.Reservation;
import com.camping.system.enums.ObservationStatus;
import com.camping.system.enums.PaymentStatus;
import com.camping.system.enums.ReservationStatus;
import com.camping.system.enums.RiskLevel;
import com.camping.system.enums.SiteStatus;
import com.camping.system.repository.CampingSiteRepository;
import com.camping.system.repository.ObservationRecordRepository;
import com.camping.system.repository.PaymentOrderRepository;
import com.camping.system.repository.ReservationRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class DashboardService {

    private final CampingSiteRepository campingSiteRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final ObservationRecordRepository observationRecordRepository;
    private final FraudControlService fraudControlService;
    private final CampingSiteService campingSiteService;

    public DashboardService(CampingSiteRepository campingSiteRepository,
                            ReservationRepository reservationRepository,
                            PaymentOrderRepository paymentOrderRepository,
                            ObservationRecordRepository observationRecordRepository,
                            FraudControlService fraudControlService,
                            CampingSiteService campingSiteService) {
        this.campingSiteRepository = campingSiteRepository;
        this.reservationRepository = reservationRepository;
        this.paymentOrderRepository = paymentOrderRepository;
        this.observationRecordRepository = observationRecordRepository;
        this.fraudControlService = fraudControlService;
        this.campingSiteService = campingSiteService;
    }

    public DashboardDtos.DashboardResponse getDashboard() {
        List<CampingSite> sites = campingSiteRepository.findAll();
        List<Reservation> reservations = reservationRepository.findAll();
        List<PaymentOrder> payments = paymentOrderRepository.findAll();
        List<ObservationRecord> observations = observationRecordRepository.findAll();
        List<DashboardDtos.FraudAlertItem> fraudAlerts = fraudControlService.listLatestAlerts();

        LocalDate today = LocalDate.now();
        DashboardDtos.OverviewStats overview = new DashboardDtos.OverviewStats(
                sites.size(),
                sites.stream().filter(item -> item.getStatus() == SiteStatus.OPEN).count(),
                reservations.size(),
                reservations.stream().filter(item -> item.getCreatedAt().toLocalDate().isEqual(today)).count(),
                reservations.stream().filter(item -> item.getStatus() == ReservationStatus.CONFIRMED).count(),
                payments.stream()
                        .filter(item -> item.getStatus() == PaymentStatus.PAID || item.getStatus() == PaymentStatus.SETTLED)
                        .map(PaymentOrder::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add),
                observations.size(),
                observations.stream().filter(item -> item.getStatus() == ObservationStatus.VERIFIED).count(),
                fraudAlerts.stream()
                        .filter(item -> item.riskLevel() == RiskLevel.MEDIUM || item.riskLevel() == RiskLevel.HIGH)
                        .filter(item -> item.createdAt().isAfter(LocalDateTime.now().minusHours(24)))
                        .count()
        );

        List<DashboardDtos.TrendPoint> trends = IntStream.rangeClosed(0, 6)
                .mapToObj(offset -> {
                    LocalDate date = today.minusDays(6L - offset);
                    long reservationCount = reservations.stream()
                            .filter(item -> item.getCreatedAt().toLocalDate().isEqual(date))
                            .count();
                    BigDecimal revenue = payments.stream()
                            .filter(item -> item.getPaidAt() != null && item.getPaidAt().toLocalDate().isEqual(date))
                            .map(PaymentOrder::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    long observationCount = observations.stream()
                            .filter(item -> item.getObservationTime().toLocalDate().isEqual(date))
                            .count();
                    return new DashboardDtos.TrendPoint(date, reservationCount, revenue, observationCount);
                })
                .toList();

        Map<Long, Long> reservationCountBySite = reservations.stream()
                .collect(Collectors.groupingBy(Reservation::getSiteId, Collectors.counting()));
        Map<Long, Long> observationCountBySite = observations.stream()
                .collect(Collectors.groupingBy(ObservationRecord::getSiteId, Collectors.counting()));

        List<DashboardDtos.SiteRankingItem> siteRankings = sites.stream()
                .map(site -> new DashboardDtos.SiteRankingItem(
                        site.getId(),
                        site.getName(),
                        site.getCity(),
                        reservationCountBySite.getOrDefault(site.getId(), 0L),
                        observationCountBySite.getOrDefault(site.getId(), 0L),
                        calculateOccupancyRate(site)
                ))
                .sorted(Comparator.comparing(DashboardDtos.SiteRankingItem::occupancyRate).reversed()
                        .thenComparing(DashboardDtos.SiteRankingItem::reservationCount).reversed())
                .limit(6)
                .toList();

        List<DashboardDtos.CategoryStatItem> observationCategories = observations.stream()
                .collect(Collectors.groupingBy(ObservationRecord::getCategory, Collectors.counting()))
                .entrySet()
                .stream()
                .map(entry -> new DashboardDtos.CategoryStatItem(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(DashboardDtos.CategoryStatItem::value).reversed())
                .toList();

        List<DashboardDtos.GeoBoardItem> geoBoard = sites.stream()
                .map(site -> new DashboardDtos.GeoBoardItem(
                        site.getId(),
                        site.getName(),
                        site.getCity(),
                        site.getLatitude(),
                        site.getLongitude(),
                        calculateOccupancyRate(site),
                        reservationCountBySite.getOrDefault(site.getId(), 0L),
                        observationCountBySite.getOrDefault(site.getId(), 0L),
                        site.getEcoIndex()
                ))
                .toList();

        return new DashboardDtos.DashboardResponse(
                overview,
                trends,
                siteRankings,
                observationCategories,
                fraudAlerts,
                geoBoard
        );
    }

    private BigDecimal calculateOccupancyRate(CampingSite site) {
        int reserved = campingSiteService.calculateReservedTents(site.getId(), LocalDate.now(), LocalDate.now().plusDays(1));
        if (site.getCapacity() == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(reserved)
                .divide(BigDecimal.valueOf(site.getCapacity()), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
