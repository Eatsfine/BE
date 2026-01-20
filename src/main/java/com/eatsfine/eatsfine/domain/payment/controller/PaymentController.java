package com.eatsfine.eatsfine.domain.payment.controller;

import com.eatsfine.eatsfine.domain.payment.dto.request.PaymentConfirmDTO;
import com.eatsfine.eatsfine.domain.payment.dto.request.PaymentRequestDTO;
import com.eatsfine.eatsfine.domain.payment.dto.response.PaymentResponseDTO;
import com.eatsfine.eatsfine.domain.payment.service.PaymentService;
import com.eatsfine.eatsfine.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Payment", description = "결제 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "결제 요청", description = "예약 ID를 받아 주문 ID를 생성하고 결제 정보를 초기화합니다.")
    @PostMapping("/request")
    public ApiResponse<PaymentResponseDTO.PaymentRequestResultDTO> requestPayment(
            @RequestBody @Valid PaymentRequestDTO.RequestPaymentDTO dto) {
        return ApiResponse.onSuccess(paymentService.requestPayment(dto));
    }

    @Operation(summary = "결제 승인", description = "토스페이먼츠 결제 승인을 요청합니다.")
    @PostMapping("/confirm")
    public ApiResponse<PaymentResponseDTO.PaymentRequestResultDTO> confirmPayment(
            @RequestBody @Valid PaymentConfirmDTO dto) {
        return ApiResponse.onSuccess(paymentService.confirmPayment(dto));
    }

    @Operation(summary = "결제 취소", description = "결제 키를 받아 결제를 취소합니다.")
    @PostMapping("/{paymentKey}/cancel")
    public ApiResponse<PaymentResponseDTO.CancelPaymentResultDTO> cancelPayment(
            @PathVariable String paymentKey,
            @RequestBody @Valid PaymentRequestDTO.CancelPaymentDTO dto) {
        return ApiResponse.onSuccess(paymentService.cancelPayment(paymentKey, dto));
    }
}
