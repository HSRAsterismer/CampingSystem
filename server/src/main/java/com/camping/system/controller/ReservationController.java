package com.camping.system.controller;

import com.camping.system.common.ApiResponse;
import com.camping.system.dto.ReservationDtos;
import com.camping.system.service.ReservationService;
import jakarta.servlet.http.HttpServletRequest;
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
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public ApiResponse<List<ReservationDtos.ReservationItem>> listReservations() {
        return ApiResponse.success(reservationService.listReservations());
    }

    @PostMapping
    public ApiResponse<ReservationDtos.ReservationItem> createReservation(@Valid @RequestBody ReservationDtos.CreateReservationRequest request,
                                                                          HttpServletRequest httpServletRequest) {
        return ApiResponse.success(
                "预订提交成功",
                reservationService.createReservation(request, extractClientIp(httpServletRequest))
        );
    }

    @PatchMapping("/{id}/cancel")
    public ApiResponse<ReservationDtos.ReservationItem> cancelReservation(@PathVariable Long id) {
        return ApiResponse.success("预订已取消", reservationService.cancelReservation(id));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<ReservationDtos.ReservationItem> updateStatus(@PathVariable Long id,
                                                                     @Valid @RequestBody ReservationDtos.UpdateStatusRequest request) {
        return ApiResponse.success("预订状态更新成功", reservationService.updateStatus(id, request));
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
