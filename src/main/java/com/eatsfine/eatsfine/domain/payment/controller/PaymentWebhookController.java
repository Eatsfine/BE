package com.eatsfine.eatsfine.domain.payment.controller;

import com.eatsfine.eatsfine.domain.payment.dto.request.PaymentWebhookDTO;
import com.eatsfine.eatsfine.domain.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments/webhook")
@Tag(name = "Payment Webhook Controller", description = "Toss Payments 웹훅 수신 전용 컨트롤러")
public class PaymentWebhookController {

    private final PaymentService paymentService;

    @Operation(summary = "Toss Payments 웹훅 수신", description = "Toss Payments 서버로부터 결제/취소 결과(PaymentKey, Status 등)를 수신하여 서버 상태를 동기화합니다.")
    @PostMapping
    public ResponseEntity<String> handleWebhook(@RequestBody PaymentWebhookDTO dto) {
        log.info("Webhook received: orderId={}, status={}", dto.orderId(), dto.status());
        paymentService.processWebhook(dto);
        return ResponseEntity.ok("Received");
    }
}
