package com.camping.system.repository;

import com.camping.system.entity.ObservationRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ObservationRecordRepository extends JpaRepository<ObservationRecord, Long> {

    List<ObservationRecord> findAllByOrderByObservationTimeDesc();

    List<ObservationRecord> findByObserverIdOrderByObservationTimeDesc(Long observerId);
}
