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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestHeader;
import jakarta.validation.Validator;
import jakarta.validation.ConstraintViolation;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Set;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments/webhook")
@Tag(name = "Payment Webhook Controller", description = "Toss Payments 웹훅 수신 전용 컨트롤러")
public class PaymentWebhookController {

    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    @Value("${payment.toss.widget-secret-key}")
    private String widgetSecretKey;

    @Operation(summary = "Toss Payments 웹훅 수신", description = "Toss Payments 서버로부터 결제/취소 결과(PaymentKey, Status 등)를 수신하여 서버 상태를 동기화합니다.")
    @PostMapping
    public ResponseEntity<String> handleWebhook(
            @RequestBody String jsonBody,
            @RequestHeader("tosspayments-webhook-signature") String signature,
            @RequestHeader("tosspayments-webhook-transmission-time") String timestamp) throws JsonProcessingException {

        try {
            verifySignature(jsonBody, signature, timestamp);
        } catch (Exception e) {
            // 서명이 다르면 401 Unauthorized 반환하여 즉시 차단
            log.error("Webhook signature verification failed", e);
            return ResponseEntity.status(401).body("Invalid Signature");
        }

        PaymentWebhookDTO dto = objectMapper.readValue(jsonBody, PaymentWebhookDTO.class);

        Set<ConstraintViolation<PaymentWebhookDTO>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (ConstraintViolation<PaymentWebhookDTO> violation : violations) {
                sb.append(violation.getPropertyPath()).append(" ").append(violation.getMessage()).append("; ");
            }
            log.error("Webhook validation failed: {}", sb.toString());
            return ResponseEntity.badRequest().body("Validation failed: " + sb.toString());
        }

        log.info("Webhook received: orderId={}, status={}", dto.data().orderId(), dto.data().status());

        try {
            paymentService.processWebhook(dto);
        } catch (PaymentException e) {
            // 비즈니스 로직 오류(결제 없음 등)는 재시도해도 해결되지 않으므로 200 OK 반환하여 재시도 중단
            log.error("Webhook processing failed (Business Logic): {}", e.getMessage());
            return ResponseEntity.ok("Ignored: " + e.getMessage());
        } catch (Exception e) {
            // 시스템 오류(DB 접속 불가 등)는 재시도 필요하므로 500 반환
            log.error("Webhook processing failed (System Error)", e);
            return ResponseEntity.internalServerError().body("Internal Server Error");
        }

        return ResponseEntity.ok("Received");
    }

    private void verifySignature(String jsonBody, String signature, String timestamp) throws Exception {
        String payload = timestamp + "." + jsonBody;
        String calculatedSignature = hmacSha256(payload, widgetSecretKey);

        // Toss가 보낸 서명에, 내가 만든 암호문이 포함되어 있는지 확인
        if (!signature.contains("v1:" + calculatedSignature)) {
            throw new SecurityException("Signature verification failed");
        }
    }

    private String hmacSha256(String data, String key) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        return Base64.getEncoder().encodeToString(sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }
}
