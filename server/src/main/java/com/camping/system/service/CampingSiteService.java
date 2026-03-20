package com.camping.system.service;

import com.camping.system.common.AuthContext;
import com.camping.system.common.BusinessException;
import com.camping.system.dto.SiteDtos;
import com.camping.system.entity.CampingSite;
import com.camping.system.entity.Reservation;
import com.camping.system.enums.ReservationStatus;
import com.camping.system.enums.SiteStatus;
import com.camping.system.repository.CampingSiteRepository;
import com.camping.system.repository.ReservationRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class CampingSiteService {

    private static final List<ReservationStatus> ACTIVE_RESERVATIONS = List.of(
            ReservationStatus.PENDING_PAYMENT,
            ReservationStatus.CONFIRMED
    );

    private final CampingSiteRepository campingSiteRepository;
    private final ReservationRepository reservationRepository;

    public CampingSiteService(CampingSiteRepository campingSiteRepository, ReservationRepository reservationRepository) {
        this.campingSiteRepository = campingSiteRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<SiteDtos.SiteItem> listSites(SiteDtos.SiteQuery query) {
        LocalDate startDate = query.startDate() == null ? LocalDate.now() : query.startDate();
        LocalDate endDate = query.endDate() == null || !query.endDate().isAfter(startDate)
                ? startDate.plusDays(1)
                : query.endDate();

        Specification<CampingSite> specification = (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (query.keyword() != null && !query.keyword().isBlank()) {
                String keyword = "%" + query.keyword().trim() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(root.get("name"), keyword),
                        criteriaBuilder.like(root.get("city"), keyword),
                        criteriaBuilder.like(root.get("address"), keyword),
                        criteriaBuilder.like(root.get("tags"), keyword)
                ));
            }
            if (query.city() != null && !query.city().isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("city"), query.city().trim()));
            }
            if (query.status() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), query.status()));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };

        Sort sort = Sort.by(Sort.Direction.DESC, "ecoIndex")
                .and(Sort.by(Sort.Direction.DESC, "scenicLevel"))
                .and(Sort.by(Sort.Direction.ASC, "basePrice"));
        return campingSiteRepository.findAll(specification, sort)
                .stream()
                .map(site -> toItem(site, startDate, endDate))
                .toList();
    }

    @Transactional
    public SiteDtos.SiteItem createSite(SiteDtos.SaveSiteRequest request) {
        requireAdmin();
        CampingSite site = new CampingSite();
        fillSite(site, request);
        return toItem(campingSiteRepository.save(site), LocalDate.now(), LocalDate.now().plusDays(1));
    }

    @Transactional
    public SiteDtos.SiteItem updateSite(Long id, SiteDtos.SaveSiteRequest request) {
        requireAdmin();
        CampingSite site = findEntity(id);
        fillSite(site, request);
        return toItem(campingSiteRepository.save(site), LocalDate.now(), LocalDate.now().plusDays(1));
    }

    @Transactional
    public SiteDtos.SiteItem updateSiteStatus(Long id, SiteStatus status) {
        requireAdmin();
        CampingSite site = findEntity(id);
        site.setStatus(status);
        return toItem(campingSiteRepository.save(site), LocalDate.now(), LocalDate.now().plusDays(1));
    }

    public CampingSite findEntity(Long id) {
        return campingSiteRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "露营点不存在"));
    }

    public int calculateReservedTents(Long siteId, LocalDate startDate, LocalDate endDate) {
        return reservationRepository.findBySiteIdAndStatusInAndStartDateLessThanAndEndDateGreaterThan(
                        siteId,
                        ACTIVE_RESERVATIONS,
                        endDate,
                        startDate
                )
                .stream()
                .map(Reservation::getTentCount)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
    }

    private SiteDtos.SiteItem toItem(CampingSite site, LocalDate startDate, LocalDate endDate) {
        int reservedTents = calculateReservedTents(site.getId(), startDate, endDate);
        int availableTents = Math.max(site.getCapacity() - reservedTents, 0);
        BigDecimal occupancyRate = site.getCapacity() == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(reservedTents)
                .divide(BigDecimal.valueOf(site.getCapacity()), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

        return new SiteDtos.SiteItem(
                site.getId(),
                site.getCode(),
                site.getName(),
                site.getProvince(),
                site.getCity(),
                site.getAddress(),
                site.getLatitude(),
                site.getLongitude(),
                site.getCapacity(),
                availableTents,
                site.getScenicLevel(),
                site.getEcoIndex(),
                site.getBasePrice(),
                site.getStatus(),
                occupancyRate,
                splitText(site.getFacilities()),
                splitText(site.getTags()),
                site.getDescription()
        );
    }

    private void fillSite(CampingSite site, SiteDtos.SaveSiteRequest request) {
        site.setCode(request.code().trim());
        site.setName(request.name().trim());
        site.setProvince(request.province().trim());
        site.setCity(request.city().trim());
        site.setAddress(request.address().trim());
        site.setLatitude(request.latitude());
        site.setLongitude(request.longitude());
        site.setCapacity(request.capacity());
        site.setBasePrice(request.basePrice());
        site.setStatus(request.status());
        site.setScenicLevel(request.scenicLevel());
        site.setEcoIndex(request.ecoIndex());
        site.setFacilities(joinText(request.facilities()));
        site.setTags(joinText(request.tags()));
        site.setDescription(request.description());
    }

    private void requireAdmin() {
        if (!AuthContext.getRequiredUser().isAdmin()) {
            throw new BusinessException(403, "当前账号没有管理权限");
        }
    }

    private List<String> splitText(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return List.of(text.split(",")).stream().map(String::trim).filter(item -> !item.isBlank()).toList();
    }

    private String joinText(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        return String.join(",", values.stream().map(String::trim).filter(item -> !item.isBlank()).distinct().toList());
    }
}
