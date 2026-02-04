package com.eatsfine.eatsfine.domain.payment.controller;

import com.eatsfine.eatsfine.domain.payment.dto.request.PaymentWebhookDTO;
import com.eatsfine.eatsfine.domain.payment.exception.PaymentException;
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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.RequestHeader;
import jakarta.validation.Validator;
import jakarta.validation.ConstraintViolation;
import java.util.Set;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments/webhook")
@Tag(name = "Payment Webhook Controller", description = "Toss Payments 웹훅 수신 전용 컨트롤러")
public class PaymentWebhookController {

    private final PaymentService paymentService;
    private final com.eatsfine.eatsfine.domain.payment.service.TossPaymentService tossPaymentService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    @Operation(summary = "Toss Payments 웹훅 수신", description = "Toss Payments 서버로부터 결제/취소 결과(PaymentKey, Status 등)를 수신하여 서버 상태를 동기화합니다.")
    @PostMapping
    public ResponseEntity<String> handleWebhook(
            @RequestBody String jsonBody,
            @RequestHeader("tosspayments-webhook-signature") String signature,
            @RequestHeader("tosspayments-webhook-transmission-time") String timestamp) throws JsonProcessingException {

        try {
            tossPaymentService.verifyWebhookSignature(jsonBody, signature, timestamp);
        } catch (Exception e) {
            log.error("Webhook signature verification failed", e);
            return ResponseEntity.status(401).body("Invalid Signature");
        }

        PaymentWebhookDTO dto = objectMapper.readValue(jsonBody, PaymentWebhookDTO.class);

        if (hasValidationErrors(dto)) {
            return ResponseEntity.badRequest().body("Validation failed");
        }

        log.info("Webhook received: orderId={}, status={}", dto.data().orderId(), dto.data().status());

        try {
            paymentService.processWebhook(dto);
        } catch (PaymentException e) {
            log.error("Webhook processing failed (Business Logic): {}", e.getMessage());
            return ResponseEntity.ok("Ignored: " + e.getMessage());
        } catch (Exception e) {
            log.error("Webhook processing failed (System Error)", e);
            return ResponseEntity.internalServerError().body("Internal Server Error");
        }

        return ResponseEntity.ok("Received");
    }

    private boolean hasValidationErrors(PaymentWebhookDTO dto) {
        Set<ConstraintViolation<PaymentWebhookDTO>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (ConstraintViolation<PaymentWebhookDTO> violation : violations) {
                sb.append(violation.getPropertyPath()).append(" ").append(violation.getMessage()).append("; ");
            }
            log.error("Webhook validation failed: {}", sb.toString());
            return true;
        }
        return false;
    }
}
