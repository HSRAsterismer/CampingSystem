package com.camping.system.service;

import com.camping.system.common.AuthContext;
import com.camping.system.common.BusinessException;
import com.camping.system.dto.PaymentDtos;
import com.camping.system.entity.CampingSite;
import com.camping.system.entity.PaymentOrder;
import com.camping.system.entity.Reservation;
import com.camping.system.enums.PaymentStatus;
import com.camping.system.enums.ReservationStatus;
import com.camping.system.repository.CampingSiteRepository;
import com.camping.system.repository.PaymentOrderRepository;
import com.camping.system.repository.ReservationRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class PaymentService {

    private final PaymentOrderRepository paymentOrderRepository;
    private final ReservationRepository reservationRepository;
    private final CampingSiteRepository campingSiteRepository;

    public PaymentService(PaymentOrderRepository paymentOrderRepository,
                          ReservationRepository reservationRepository,
                          CampingSiteRepository campingSiteRepository) {
        this.paymentOrderRepository = paymentOrderRepository;
        this.reservationRepository = reservationRepository;
        this.campingSiteRepository = campingSiteRepository;
    }

    @Transactional
    public PaymentDtos.PaymentItem payReservation(PaymentDtos.PayRequest request) {
        AuthContext.AuthUser authUser = AuthContext.getRequiredUser();
        Reservation reservation = authUser.isAdmin()
                ? reservationRepository.findById(request.reservationId()).orElseThrow(() -> new BusinessException(404, "预订单不存在"))
                : reservationRepository.findByIdAndUserId(request.reservationId(), authUser.userId())
                .orElseThrow(() -> new BusinessException(404, "预订单不存在"));

        if (reservation.getStatus() == ReservationStatus.CANCELLED
                || reservation.getStatus() == ReservationStatus.REJECTED
                || reservation.getStatus() == ReservationStatus.COMPLETED) {
            throw new BusinessException(400, "当前预订单状态不允许支付");
        }

        PaymentOrder paymentOrder = paymentOrderRepository.findByReservationId(reservation.getId()).orElseGet(PaymentOrder::new);
        if (paymentOrder.getId() != null
                && (paymentOrder.getStatus() == PaymentStatus.PAID || paymentOrder.getStatus() == PaymentStatus.SETTLED)) {
            return toItem(paymentOrder, reservation, campingSiteRepository.findById(reservation.getSiteId()).orElse(null));
        }

        if (paymentOrder.getId() == null) {
            paymentOrder.setOrderNo(generateOrderNo());
            paymentOrder.setReservationId(reservation.getId());
            paymentOrder.setUserId(reservation.getUserId());
        }

        LocalDateTime now = LocalDateTime.now();
        paymentOrder.setChannel(request.channel());
        paymentOrder.setAmount(reservation.getTotalAmount());
        paymentOrder.setStatus(PaymentStatus.PAID);
        paymentOrder.setTransactionNo("TXN" + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(100, 999));
        paymentOrder.setPaidAt(now);
        paymentOrder.setOperatorName(authUser.displayName());
        paymentOrder = paymentOrderRepository.save(paymentOrder);

        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setPaidAt(now);
        reservationRepository.save(reservation);

        CampingSite site = campingSiteRepository.findById(reservation.getSiteId()).orElse(null);
        return toItem(paymentOrder, reservation, site);
    }

    public List<PaymentDtos.PaymentItem> listPayments() {
        AuthContext.AuthUser authUser = AuthContext.getRequiredUser();
        List<PaymentOrder> payments = authUser.isAdmin()
                ? paymentOrderRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                : paymentOrderRepository.findByUserIdOrderByCreatedAtDesc(authUser.userId());

        Map<Long, Reservation> reservationMap = new HashMap<>();
        reservationRepository.findAll().forEach(item -> reservationMap.put(item.getId(), item));
        Map<Long, CampingSite> siteMap = new HashMap<>();
        campingSiteRepository.findAll().forEach(item -> siteMap.put(item.getId(), item));

        return payments.stream()
                .map(item -> {
                    Reservation reservation = reservationMap.get(item.getReservationId());
                    CampingSite site = reservation == null ? null : siteMap.get(reservation.getSiteId());
                    return toItem(item, reservation, site);
                })
                .toList();
    }

    @Transactional
    public PaymentDtos.PaymentItem settlePayment(Long id, PaymentDtos.SettleRequest request) {
        requireAdmin();
        PaymentOrder paymentOrder = paymentOrderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "支付记录不存在"));
        if (paymentOrder.getStatus() == PaymentStatus.SETTLED) {
            Reservation reservation = reservationRepository.findById(paymentOrder.getReservationId()).orElse(null);
            CampingSite site = reservation == null ? null : campingSiteRepository.findById(reservation.getSiteId()).orElse(null);
            return toItem(paymentOrder, reservation, site);
        }
        if (paymentOrder.getStatus() != PaymentStatus.PAID) {
            throw new BusinessException(400, "只有已支付订单才能进行结算");
        }
        paymentOrder.setStatus(PaymentStatus.SETTLED);
        paymentOrder.setSettledAt(LocalDateTime.now());
        paymentOrder.setSettlementNote(request.note());
        paymentOrder.setOperatorName(AuthContext.getRequiredUser().displayName());
        paymentOrder = paymentOrderRepository.save(paymentOrder);

        Reservation reservation = reservationRepository.findById(paymentOrder.getReservationId()).orElse(null);
        CampingSite site = reservation == null ? null : campingSiteRepository.findById(reservation.getSiteId()).orElse(null);
        return toItem(paymentOrder, reservation, site);
    }

    private PaymentDtos.PaymentItem toItem(PaymentOrder paymentOrder, Reservation reservation, CampingSite site) {
        return new PaymentDtos.PaymentItem(
                paymentOrder.getId(),
                paymentOrder.getOrderNo(),
                paymentOrder.getReservationId(),
                reservation == null ? "-" : reservation.getReservationNo(),
                site == null ? "未知营地" : site.getName(),
                paymentOrder.getChannel(),
                paymentOrder.getAmount(),
                paymentOrder.getStatus(),
                paymentOrder.getTransactionNo(),
                paymentOrder.getOperatorName(),
                paymentOrder.getSettlementNote(),
                paymentOrder.getPaidAt(),
                paymentOrder.getSettledAt(),
                paymentOrder.getCreatedAt()
        );
    }

    private String generateOrderNo() {
        return "PAY" + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(100, 999);
    }

    private void requireAdmin() {
        if (!AuthContext.getRequiredUser().isAdmin()) {
            throw new BusinessException(403, "当前账号没有管理权限");
        }
    }
}
