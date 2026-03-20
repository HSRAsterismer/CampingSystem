package com.camping.system.repository;

import com.camping.system.entity.Reservation;
import com.camping.system.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {

    List<Reservation> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Reservation> findByIdAndUserId(Long id, Long userId);

    long countByUserIdAndCreatedAtAfter(Long userId, LocalDateTime createdAt);

    long countBySourceIpAndCreatedAtAfter(String sourceIp, LocalDateTime createdAt);

    boolean existsByUserIdAndSiteIdAndStartDateLessThanAndEndDateGreaterThanAndStatusIn(
            Long userId,
            Long siteId,
            LocalDate endDate,
            LocalDate startDate,
            Collection<ReservationStatus> statuses
    );

    List<Reservation> findBySiteIdAndStatusInAndStartDateLessThanAndEndDateGreaterThan(
            Long siteId,
            Collection<ReservationStatus> statuses,
            LocalDate endDate,
            LocalDate startDate
    );
}
