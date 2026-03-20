package com.camping.system.service;

import com.camping.system.common.AuthContext;
import com.camping.system.common.BusinessException;
import com.camping.system.dto.ObservationDtos;
import com.camping.system.entity.CampingSite;
import com.camping.system.entity.ObservationRecord;
import com.camping.system.entity.User;
import com.camping.system.enums.ObservationStatus;
import com.camping.system.enums.UserRole;
import com.camping.system.repository.CampingSiteRepository;
import com.camping.system.repository.ObservationRecordRepository;
import com.camping.system.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ObservationService {

    private final ObservationRecordRepository observationRecordRepository;
    private final CampingSiteRepository campingSiteRepository;
    private final UserRepository userRepository;

    public ObservationService(ObservationRecordRepository observationRecordRepository,
                              CampingSiteRepository campingSiteRepository,
                              UserRepository userRepository) {
        this.observationRecordRepository = observationRecordRepository;
        this.campingSiteRepository = campingSiteRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ObservationDtos.ObservationItem createObservation(ObservationDtos.CreateObservationRequest request) {
        AuthContext.AuthUser authUser = AuthContext.getRequiredUser();
        CampingSite site = campingSiteRepository.findById(request.siteId())
                .orElseThrow(() -> new BusinessException(404, "露营点不存在"));

        ObservationRecord record = new ObservationRecord();
        record.setSiteId(request.siteId());
        record.setObserverId(authUser.userId());
        record.setSpeciesName(request.speciesName().trim());
        record.setCategory(request.category().trim());
        record.setQuantity(request.quantity());
        record.setWeather(request.weather());
        record.setObservationTime(request.observationTime());
        record.setPhotoUrl(request.photoUrl());
        record.setCoordinates(request.coordinates());
        record.setHabitat(request.habitat());
        record.setRarityLevel(request.rarityLevel());
        record.setEnvironmentalScore(request.environmentalScore());
        record.setNotes(request.notes());
        record.setStatus(authUser.role() == UserRole.ADMIN || authUser.role() == UserRole.OBSERVER
                ? ObservationStatus.VERIFIED
                : ObservationStatus.SUBMITTED);
        record = observationRecordRepository.save(record);

        return new ObservationDtos.ObservationItem(
                record.getId(),
                record.getSiteId(),
                site.getName(),
                authUser.displayName(),
                record.getSpeciesName(),
                record.getCategory(),
                record.getQuantity(),
                record.getWeather(),
                record.getObservationTime(),
                record.getPhotoUrl(),
                record.getCoordinates(),
                record.getHabitat(),
                record.getRarityLevel(),
                record.getEnvironmentalScore(),
                record.getNotes(),
                record.getStatus(),
                record.getCreatedAt()
        );
    }

    public List<ObservationDtos.ObservationItem> listObservations() {
        Map<Long, CampingSite> siteMap = new HashMap<>();
        campingSiteRepository.findAll().forEach(item -> siteMap.put(item.getId(), item));
        Map<Long, User> userMap = new HashMap<>();
        userRepository.findAll().forEach(item -> userMap.put(item.getId(), item));

        return observationRecordRepository.findAllByOrderByObservationTimeDesc()
                .stream()
                .map(item -> {
                    CampingSite site = siteMap.get(item.getSiteId());
                    User user = userMap.get(item.getObserverId());
                    return new ObservationDtos.ObservationItem(
                            item.getId(),
                            item.getSiteId(),
                            site == null ? "未知营地" : site.getName(),
                            user == null ? "未知观察员" : user.getDisplayName(),
                            item.getSpeciesName(),
                            item.getCategory(),
                            item.getQuantity(),
                            item.getWeather(),
                            item.getObservationTime(),
                            item.getPhotoUrl(),
                            item.getCoordinates(),
                            item.getHabitat(),
                            item.getRarityLevel(),
                            item.getEnvironmentalScore(),
                            item.getNotes(),
                            item.getStatus(),
                            item.getCreatedAt()
                    );
                })
                .toList();
    }

    @Transactional
    public ObservationDtos.ObservationItem verifyObservation(Long id) {
        requireAdmin();
        ObservationRecord record = observationRecordRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "生态记录不存在"));
        record.setStatus(ObservationStatus.VERIFIED);
        record = observationRecordRepository.save(record);
        CampingSite site = campingSiteRepository.findById(record.getSiteId()).orElse(null);
        User user = userRepository.findById(record.getObserverId()).orElse(null);
        return new ObservationDtos.ObservationItem(
                record.getId(),
                record.getSiteId(),
                site == null ? "未知营地" : site.getName(),
                user == null ? "未知观察员" : user.getDisplayName(),
                record.getSpeciesName(),
                record.getCategory(),
                record.getQuantity(),
                record.getWeather(),
                record.getObservationTime(),
                record.getPhotoUrl(),
                record.getCoordinates(),
                record.getHabitat(),
                record.getRarityLevel(),
                record.getEnvironmentalScore(),
                record.getNotes(),
                record.getStatus(),
                record.getCreatedAt()
        );
    }

    private void requireAdmin() {
        if (!AuthContext.getRequiredUser().isAdmin()) {
            throw new BusinessException(403, "当前账号没有管理权限");
        }
    }
}
