package com.eatsfine.eatsfine.domain.payment.service;

import com.eatsfine.eatsfine.domain.payment.dto.request.PaymentConfirmDTO;
import com.eatsfine.eatsfine.domain.payment.dto.request.PaymentRequestDTO;
import com.eatsfine.eatsfine.domain.payment.dto.response.TossPaymentResponse;
import com.eatsfine.eatsfine.global.apiPayload.code.status.ErrorStatus;
import com.eatsfine.eatsfine.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class TossPaymentService {

    private final RestClient tossPaymentClient;

    public TossPaymentResponse confirm(PaymentConfirmDTO dto) {
        try {
            return tossPaymentClient.post()
                    .uri("/v1/payments/confirm")
                    .body(dto)
                    .retrieve()
                    .body(TossPaymentResponse.class);
        } catch (Exception e) {
            log.error("Toss Payment API Error", e);
            throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
        }
    }

    public TossPaymentResponse cancel(String paymentKey, PaymentRequestDTO.CancelPaymentDTO dto) {
        try {
            return tossPaymentClient.post()
                    .uri("/v1/payments/{paymentKey}/cancel", paymentKey)
                    .body(dto)
                    .retrieve()
                    .body(TossPaymentResponse.class);
        } catch (Exception e) {
            log.error("Toss Payment Cancel API Error", e);
            throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
        }
    }
}
