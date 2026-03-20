package com.camping.system.entity;

import com.camping.system.enums.PaymentChannel;
import com.camping.system.enums.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "payment_order")
public class PaymentOrder extends BaseEntity {

    @Column(nullable = false, unique = true, length = 32)
    private String orderNo;

    @Column(nullable = false)
    private Long reservationId;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentChannel channel;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(length = 64)
    private String transactionNo;

    private LocalDateTime paidAt;

    private LocalDateTime settledAt;

    @Column(length = 50)
    private String operatorName;

    @Column(length = 255)
    private String settlementNote;
}
