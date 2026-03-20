package com.camping.system.entity;

import com.camping.system.enums.ObservationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "observation_record")
public class ObservationRecord extends BaseEntity {

    @Column(nullable = false)
    private Long siteId;

    @Column(nullable = false)
    private Long observerId;

    @Column(nullable = false, length = 100)
    private String speciesName;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false)
    private Integer quantity;

    @Column(length = 50)
    private String weather;

    @Column(nullable = false)
    private LocalDateTime observationTime;

    @Column(length = 255)
    private String photoUrl;

    @Column(length = 100)
    private String coordinates;

    @Column(length = 100)
    private String habitat;

    @Column(length = 30)
    private String rarityLevel;

    private Integer environmentalScore;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ObservationStatus status;
}
