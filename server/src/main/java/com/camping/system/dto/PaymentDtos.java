package com.camping.system.dto;

import com.camping.system.enums.PaymentChannel;
import com.camping.system.enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class PaymentDtos {

    private PaymentDtos() {
    }

    public record PayRequest(
            @NotNull(message = "预订单不能为空") Long reservationId,
            @NotNull(message = "支付方式不能为空") PaymentChannel channel
    ) {
    }

    public record SettleRequest(
            @Size(max = 255, message = "结算备注长度不能超过 255") String note
    ) {
    }

    public record PaymentItem(
            Long id,
            String orderNo,
            Long reservationId,
            String reservationNo,
            String siteName,
            PaymentChannel channel,
            BigDecimal amount,
            PaymentStatus status,
            String transactionNo,
            String operatorName,
            String settlementNote,
            LocalDateTime paidAt,
            LocalDateTime settledAt,
            LocalDateTime createdAt
    ) {
    }
}
