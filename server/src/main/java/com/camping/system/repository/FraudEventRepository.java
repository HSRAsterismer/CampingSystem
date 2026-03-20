package com.camping.system.repository;

import com.camping.system.entity.FraudEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface FraudEventRepository extends JpaRepository<FraudEvent, Long> {

    long countByUserIdAndCreatedAtAfter(Long userId, LocalDateTime createdAt);

    long countBySourceIpAndCreatedAtAfter(String sourceIp, LocalDateTime createdAt);

    List<FraudEvent> findTop20ByOrderByCreatedAtDesc();
}
