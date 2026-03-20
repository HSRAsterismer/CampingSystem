package com.camping.system.controller;

import com.camping.system.common.ApiResponse;
import com.camping.system.dto.PaymentDtos;
import com.camping.system.service.PaymentService;
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
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    public ApiResponse<List<PaymentDtos.PaymentItem>> listPayments() {
        return ApiResponse.success(paymentService.listPayments());
    }

    @PostMapping("/pay")
    public ApiResponse<PaymentDtos.PaymentItem> pay(@Valid @RequestBody PaymentDtos.PayRequest request) {
        return ApiResponse.success("支付成功", paymentService.payReservation(request));
    }

    @PatchMapping("/{id}/settle")
    public ApiResponse<PaymentDtos.PaymentItem> settle(@PathVariable Long id,
                                                       @Valid @RequestBody PaymentDtos.SettleRequest request) {
        return ApiResponse.success("结算完成", paymentService.settlePayment(id, request));
    }
}
