package com.camping.system.repository;

import com.camping.system.entity.PaymentOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Long> {

    Optional<PaymentOrder> findByReservationId(Long reservationId);

    List<PaymentOrder> findByUserIdOrderByCreatedAtDesc(Long userId);
}
