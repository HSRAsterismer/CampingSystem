package com.camping.system.service;

import com.camping.system.dto.DashboardDtos;
import com.camping.system.entity.FraudEvent;
import com.camping.system.enums.ReservationStatus;
import com.camping.system.enums.RiskLevel;
import com.camping.system.repository.FraudEventRepository;
import com.camping.system.repository.ReservationRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class FraudControlService {

    private static final List<ReservationStatus> ACTIVE_RESERVATIONS = List.of(
            ReservationStatus.PENDING_PAYMENT,
            ReservationStatus.CONFIRMED
    );

    private final ReservationRepository reservationRepository;
    private final FraudEventRepository fraudEventRepository;

    public FraudControlService(ReservationRepository reservationRepository, FraudEventRepository fraudEventRepository) {
        this.reservationRepository = reservationRepository;
        this.fraudEventRepository = fraudEventRepository;
    }

    public RiskDecision evaluate(Long userId,
                                 Long siteId,
                                 LocalDate startDate,
                                 LocalDate endDate,
                                 Integer guestCount,
                                 Integer tentCount,
                                 BigDecimal totalAmount,
                                 String sourceIp) {
        String normalizedIp = normalizeIp(sourceIp);
        List<String> tags = new ArrayList<>();
        RiskLevel riskLevel = RiskLevel.LOW;

        boolean duplicated = reservationRepository.existsByUserIdAndSiteIdAndStartDateLessThanAndEndDateGreaterThanAndStatusIn(
                userId,
                siteId,
                endDate,
                startDate,
                ACTIVE_RESERVATIONS
        );
        if (duplicated) {
            tags.add("duplicate_reservation");
            return new RiskDecision(RiskLevel.HIGH, true, "检测到重复预订占位行为，系统已自动拦截", String.join(",", tags));
        }

        LocalDateTime windowStart = LocalDateTime.now().minusMinutes(10);
        long userAttempts = reservationRepository.countByUserIdAndCreatedAtAfter(userId, windowStart)
                + fraudEventRepository.countByUserIdAndCreatedAtAfter(userId, windowStart);
        if (userAttempts >= 3) {
            tags.add("user_rate_limit");
            return new RiskDecision(RiskLevel.HIGH, true, "短时间内提交过多预订请求，请稍后再试", String.join(",", tags));
        }

        long ipAttempts = reservationRepository.countBySourceIpAndCreatedAtAfter(normalizedIp, windowStart)
                + fraudEventRepository.countBySourceIpAndCreatedAtAfter(normalizedIp, windowStart);
        if (ipAttempts >= 5) {
            tags.add("ip_burst");
            return new RiskDecision(RiskLevel.HIGH, true, "当前网络环境请求过于频繁，系统已触发防刷单限制", String.join(",", tags));
        }

        if (guestCount != null && guestCount >= 8) {
            riskLevel = RiskLevel.MEDIUM;
            tags.add("large_group");
        }
        if (tentCount != null && tentCount >= 4) {
            riskLevel = RiskLevel.MEDIUM;
            tags.add("multi_tent");
        }
        if (totalAmount != null && totalAmount.compareTo(BigDecimal.valueOf(2500)) > 0) {
            riskLevel = RiskLevel.MEDIUM;
            tags.add("high_amount");
        }
        if (isWeekend(startDate) && tentCount != null && tentCount >= 3) {
            riskLevel = RiskLevel.MEDIUM;
            tags.add("weekend_peak");
        }

        String message = riskLevel == RiskLevel.MEDIUM ? "订单已被标记为中风险，将进入重点监测" : "订单通过风险校验";
        return new RiskDecision(riskLevel, false, message, String.join(",", tags));
    }

    public FraudEvent recordEvent(Long userId,
                                  Long reservationId,
                                  String sourceIp,
                                  String eventType,
                                  RiskLevel riskLevel,
                                  String actionTaken,
                                  String detail) {
        FraudEvent fraudEvent = new FraudEvent();
        fraudEvent.setUserId(userId);
        fraudEvent.setReservationId(reservationId);
        fraudEvent.setSourceIp(normalizeIp(sourceIp));
        fraudEvent.setEventType(eventType);
        fraudEvent.setRiskLevel(riskLevel);
        fraudEvent.setActionTaken(actionTaken);
        fraudEvent.setDetail(detail);
        return fraudEventRepository.save(fraudEvent);
    }

    public List<DashboardDtos.FraudAlertItem> listLatestAlerts() {
        return fraudEventRepository.findTop20ByOrderByCreatedAtDesc()
                .stream()
                .map(item -> new DashboardDtos.FraudAlertItem(
                        item.getId(),
                        item.getEventType(),
                        item.getRiskLevel(),
                        item.getActionTaken(),
                        item.getDetail(),
                        item.getSourceIp(),
                        item.getCreatedAt()
                ))
                .toList();
    }

    private boolean isWeekend(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }

    private String normalizeIp(String sourceIp) {
        return sourceIp == null || sourceIp.isBlank() ? "127.0.0.1" : sourceIp.trim();
    }

    public record RiskDecision(
            RiskLevel riskLevel,
            boolean blocked,
            String message,
            String riskTags
    ) {
    }
}
