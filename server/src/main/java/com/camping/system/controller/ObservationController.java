package com.camping.system.controller;

import com.camping.system.common.ApiResponse;
import com.camping.system.dto.ObservationDtos;
import com.camping.system.service.ObservationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/observations")
public class ObservationController {

    private final ObservationService observationService;

    public ObservationController(ObservationService observationService) {
        this.observationService = observationService;
    }

    @GetMapping
    public ApiResponse<List<ObservationDtos.ObservationItem>> listObservations() {
        return ApiResponse.success(observationService.listObservations());
    }

    @PostMapping
    public ApiResponse<ObservationDtos.ObservationItem> createObservation(@Valid @RequestBody ObservationDtos.CreateObservationRequest request) {
        return ApiResponse.success("生态记录提交成功", observationService.createObservation(request));
    }

    @PatchMapping("/{id}/verify")
    public ApiResponse<ObservationDtos.ObservationItem> verifyObservation(@PathVariable Long id) {
        return ApiResponse.success("生态记录已审核", observationService.verifyObservation(id));
    }
}
