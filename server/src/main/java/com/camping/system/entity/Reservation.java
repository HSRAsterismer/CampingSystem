package com.camping.system.entity;

import com.camping.system.enums.ReservationStatus;
import com.camping.system.enums.RiskLevel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "reservation_order")
public class Reservation extends BaseEntity {

    @Column(nullable = false, unique = true, length = 32)
    private String reservationNo;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long siteId;

    @Column(nullable = false, length = 50)
    private String contactName;

    @Column(nullable = false, length = 20)
    private String contactPhone;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private Integer guestCount;

    @Column(nullable = false)
    private Integer tentCount;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RiskLevel riskLevel = RiskLevel.LOW;

    @Column(length = 255)
    private String riskTags;

    @Column(length = 100)
    private String sourceIp;

    @Column(length = 255)
    private String remark;

    private LocalDateTime paidAt;

    private LocalDateTime cancelledAt;
}
