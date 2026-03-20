package com.camping.system.entity;

import com.camping.system.enums.RiskLevel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "fraud_event")
public class FraudEvent extends BaseEntity {

    private Long userId;

    private Long reservationId;

    @Column(nullable = false, length = 50)
    private String eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RiskLevel riskLevel;

    @Column(nullable = false, length = 20)
    private String actionTaken;

    @Column(length = 100)
    private String sourceIp;

    @Column(columnDefinition = "TEXT")
    private String detail;
}
