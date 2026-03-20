package com.camping.system.dto;

import com.camping.system.enums.ReservationStatus;
import com.camping.system.enums.RiskLevel;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class ReservationDtos {

    private ReservationDtos() {
    }

    public record CreateReservationRequest(
            @NotNull(message = "露营点不能为空") Long siteId,
            @NotBlank(message = "联系人不能为空") String contactName,
            @NotBlank(message = "联系电话不能为空") String contactPhone,
            @NotNull(message = "开始日期不能为空") LocalDate startDate,
            @NotNull(message = "结束日期不能为空") LocalDate endDate,
            @NotNull(message = "同行人数不能为空")
            @Min(value = 1, message = "同行人数至少为 1")
            Integer guestCount,
            @NotNull(message = "帐篷数量不能为空")
            @Min(value = 1, message = "帐篷数量至少为 1")
            Integer tentCount,
            @Size(max = 255, message = "备注长度不能超过 255")
            String remark
    ) {
    }

    public record UpdateStatusRequest(
            @NotNull(message = "状态不能为空") ReservationStatus status
    ) {
    }

    public record ReservationItem(
            Long id,
            String reservationNo,
            Long siteId,
            String siteName,
            String city,
            LocalDate startDate,
            LocalDate endDate,
            Integer guestCount,
            Integer tentCount,
            BigDecimal totalAmount,
            String contactName,
            String contactPhone,
            ReservationStatus status,
            RiskLevel riskLevel,
            String riskTags,
            String remark,
            LocalDateTime paidAt,
            LocalDateTime createdAt
    ) {
    }
}
