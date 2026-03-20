package com.camping.system.repository;

import com.camping.system.entity.CampingSite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CampingSiteRepository extends JpaRepository<CampingSite, Long>, JpaSpecificationExecutor<CampingSite> {
}
