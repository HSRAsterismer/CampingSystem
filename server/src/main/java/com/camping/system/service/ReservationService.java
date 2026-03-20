package com.camping.system.service;

import com.camping.system.common.AuthContext;
import com.camping.system.common.BusinessException;
import com.camping.system.dto.ReservationDtos;
import com.camping.system.entity.CampingSite;
import com.camping.system.entity.Reservation;
import com.camping.system.enums.ReservationStatus;
import com.camping.system.enums.RiskLevel;
import com.camping.system.enums.SiteStatus;
import com.camping.system.repository.CampingSiteRepository;
import com.camping.system.repository.ReservationRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final CampingSiteRepository campingSiteRepository;
    private final CampingSiteService campingSiteService;
    private final FraudControlService fraudControlService;

    public ReservationService(ReservationRepository reservationRepository,
                              CampingSiteRepository campingSiteRepository,
                              CampingSiteService campingSiteService,
                              FraudControlService fraudControlService) {
        this.reservationRepository = reservationRepository;
        this.campingSiteRepository = campingSiteRepository;
        this.campingSiteService = campingSiteService;
        this.fraudControlService = fraudControlService;
    }

    @Transactional
    public ReservationDtos.ReservationItem createReservation(ReservationDtos.CreateReservationRequest request, String sourceIp) {
        AuthContext.AuthUser authUser = AuthContext.getRequiredUser();
        CampingSite site = campingSiteRepository.findById(request.siteId())
                .orElseThrow(() -> new BusinessException(404, "露营点不存在"));

        if (site.getStatus() != SiteStatus.OPEN) {
            throw new BusinessException(400, "当前露营点暂不可预订");
        }
        if (!request.endDate().isAfter(request.startDate())) {
            throw new BusinessException(400, "结束日期必须晚于开始日期");
        }
        if (request.startDate().isBefore(LocalDate.now())) {
            throw new BusinessException(400, "开始日期不能早于今天");
        }

        long stayDays = ChronoUnit.DAYS.between(request.startDate(), request.endDate());
        BigDecimal totalAmount = site.getBasePrice()
                .multiply(BigDecimal.valueOf(request.tentCount()))
                .multiply(BigDecimal.valueOf(stayDays));

        int reservedTents = campingSiteService.calculateReservedTents(site.getId(), request.startDate(), request.endDate());
        if (reservedTents + request.tentCount() > site.getCapacity()) {
            throw new BusinessException(400, "预订数量超过营地可用容量，请调整帐篷数量或更换日期");
        }

        FraudControlService.RiskDecision riskDecision = fraudControlService.evaluate(
                authUser.userId(),
                site.getId(),
                request.startDate(),
                request.endDate(),
                request.guestCount(),
                request.tentCount(),
                totalAmount,
                sourceIp
        );

        if (riskDecision.blocked()) {
            fraudControlService.recordEvent(
                    authUser.userId(),
                    null,
                    sourceIp,
                    "RESERVATION_BLOCK",
                    riskDecision.riskLevel(),
                    "BLOCK",
                    riskDecision.message() + "，标签：" + riskDecision.riskTags()
            );
            throw new BusinessException(429, riskDecision.message());
        }

        Reservation reservation = new Reservation();
        reservation.setReservationNo(generateReservationNo());
        reservation.setUserId(authUser.userId());
        reservation.setSiteId(site.getId());
        reservation.setContactName(request.contactName().trim());
        reservation.setContactPhone(request.contactPhone().trim());
        reservation.setStartDate(request.startDate());
        reservation.setEndDate(request.endDate());
        reservation.setGuestCount(request.guestCount());
        reservation.setTentCount(request.tentCount());
        reservation.setTotalAmount(totalAmount);
        reservation.setStatus(ReservationStatus.PENDING_PAYMENT);
        reservation.setRiskLevel(riskDecision.riskLevel());
        reservation.setRiskTags(riskDecision.riskTags());
        reservation.setSourceIp(sourceIp);
        reservation.setRemark(request.remark());
        reservation = reservationRepository.save(reservation);

        if (riskDecision.riskLevel() != RiskLevel.LOW) {
            fraudControlService.recordEvent(
                    authUser.userId(),
                    reservation.getId(),
                    sourceIp,
                    "RESERVATION_REVIEW",
                    riskDecision.riskLevel(),
                    "REVIEW",
                    "订单命中风控标签：" + riskDecision.riskTags()
            );
        }
        return toItem(reservation, site);
    }

    public List<ReservationDtos.ReservationItem> listReservations() {
        AuthContext.AuthUser authUser = AuthContext.getRequiredUser();
        List<Reservation> reservations = authUser.isAdmin()
                ? reservationRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                : reservationRepository.findByUserIdOrderByCreatedAtDesc(authUser.userId());
        Map<Long, CampingSite> siteMap = loadSiteMap();
        return reservations.stream().map(item -> toItem(item, siteMap.get(item.getSiteId()))).toList();
    }

    @Transactional
    public ReservationDtos.ReservationItem cancelReservation(Long id) {
        AuthContext.AuthUser authUser = AuthContext.getRequiredUser();
        Reservation reservation = authUser.isAdmin()
                ? reservationRepository.findById(id).orElseThrow(() -> new BusinessException(404, "预订记录不存在"))
                : reservationRepository.findByIdAndUserId(id, authUser.userId())
                .orElseThrow(() -> new BusinessException(404, "预订记录不存在"));

        if (reservation.getStatus() != ReservationStatus.PENDING_PAYMENT
                && reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new BusinessException(400, "当前状态不允许取消");
        }
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setCancelledAt(LocalDateTime.now());
        reservation = reservationRepository.save(reservation);
        return toItem(reservation, campingSiteService.findEntity(reservation.getSiteId()));
    }

    @Transactional
    public ReservationDtos.ReservationItem updateStatus(Long id, ReservationDtos.UpdateStatusRequest request) {
        requireAdmin();
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "预订记录不存在"));
        reservation.setStatus(request.status());
        if (request.status() == ReservationStatus.CANCELLED) {
            reservation.setCancelledAt(LocalDateTime.now());
        }
        reservation = reservationRepository.save(reservation);
        return toItem(reservation, campingSiteService.findEntity(reservation.getSiteId()));
    }

    private Map<Long, CampingSite> loadSiteMap() {
        Map<Long, CampingSite> siteMap = new HashMap<>();
        campingSiteRepository.findAll().forEach(site -> siteMap.put(site.getId(), site));
        return siteMap;
    }

    private ReservationDtos.ReservationItem toItem(Reservation reservation, CampingSite site) {
        return new ReservationDtos.ReservationItem(
                reservation.getId(),
                reservation.getReservationNo(),
                reservation.getSiteId(),
                site == null ? "未知营地" : site.getName(),
                site == null ? "" : site.getCity(),
                reservation.getStartDate(),
                reservation.getEndDate(),
                reservation.getGuestCount(),
                reservation.getTentCount(),
                reservation.getTotalAmount(),
                reservation.getContactName(),
                reservation.getContactPhone(),
                reservation.getStatus(),
                reservation.getRiskLevel(),
                reservation.getRiskTags(),
                reservation.getRemark(),
                reservation.getPaidAt(),
                reservation.getCreatedAt()
        );
    }

    private String generateReservationNo() {
        return "RSV" + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(100, 999);
    }

    private void requireAdmin() {
        if (!AuthContext.getRequiredUser().isAdmin()) {
            throw new BusinessException(403, "当前账号没有管理权限");
        }
    }
}
