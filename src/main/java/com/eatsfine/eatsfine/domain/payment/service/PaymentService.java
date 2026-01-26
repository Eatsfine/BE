package com.eatsfine.eatsfine.domain.payment.service;

import com.eatsfine.eatsfine.domain.booking.entity.Booking;
import com.eatsfine.eatsfine.domain.booking.repository.BookingRepository;
import com.eatsfine.eatsfine.domain.payment.dto.request.PaymentConfirmDTO;
import com.eatsfine.eatsfine.domain.payment.dto.request.PaymentRequestDTO;
import com.eatsfine.eatsfine.domain.payment.dto.response.PaymentResponseDTO;
import com.eatsfine.eatsfine.domain.payment.dto.response.TossPaymentResponse;
import com.eatsfine.eatsfine.domain.payment.entity.Payment;
import com.eatsfine.eatsfine.domain.payment.enums.PaymentMethod;
import com.eatsfine.eatsfine.domain.payment.enums.PaymentProvider;
import com.eatsfine.eatsfine.domain.payment.enums.PaymentStatus;
import com.eatsfine.eatsfine.domain.payment.enums.PaymentType;
import com.eatsfine.eatsfine.domain.payment.repository.PaymentRepository;
import com.eatsfine.eatsfine.global.apiPayload.code.status.ErrorStatus;
import com.eatsfine.eatsfine.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final RestClient tossPaymentClient;

    @Transactional
    public PaymentResponseDTO.PaymentRequestResultDTO requestPayment(PaymentRequestDTO.RequestPaymentDTO dto) {
        Booking booking = bookingRepository.findById(dto.bookingId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.BOOKING_NOT_FOUND));

        // 주문 ID 생성
        String orderId = UUID.randomUUID().toString();

        // 예약금 검증
        if (booking.getDepositAmount() == null || booking.getDepositAmount() <= 0) {
            throw new GeneralException(ErrorStatus.PAYMENT_INVALID_DEPOSIT);
        }

        Payment payment = Payment.builder()
                .booking(booking)
                .orderId(orderId)
                .amount(booking.getDepositAmount())
                .paymentStatus(PaymentStatus.PENDING)
                .paymentType(PaymentType.DEPOSIT)
                .requestedAt(LocalDateTime.now())
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        return new PaymentResponseDTO.PaymentRequestResultDTO(
                savedPayment.getId(),
                booking.getId(),
                savedPayment.getOrderId(),
                savedPayment.getAmount(),
                savedPayment.getRequestedAt());
    }

    @Transactional
    public PaymentResponseDTO.PaymentRequestResultDTO confirmPayment(PaymentConfirmDTO dto) {
        Payment payment = paymentRepository.findByOrderId(dto.orderId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.PAYMENT_NOT_FOUND));

        if (!payment.getAmount().equals(dto.amount())) {
            throw new GeneralException(ErrorStatus.PAYMENT_INVALID_AMOUNT);
        }

        // 토스 API 호출 
        TossPaymentResponse response = tossPaymentClient.post()
                .uri("/v1/payments/confirm")
                .body(dto)
                .retrieve()
                .body(TossPaymentResponse.class);

        if (response == null || !"DONE".equals(response.status())) {
             throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR); 
        }

        // Provider 파싱 
        PaymentProvider provider = null;
        if (response.easyPay() != null) {
            String providerCode = response.easyPay().provider();
            if ("토스페이".equals(providerCode)) {
                provider = PaymentProvider.TOSS;
            } else if ("카카오페이".equals(providerCode)) {
                provider = PaymentProvider.KAKAOPAY;
            }
        }

        payment.completePayment(
                response.approvedAt() != null ? response.approvedAt().toLocalDateTime() : LocalDateTime.now(),
                PaymentMethod.SIMPLE_PAYMENT,
                response.paymentKey(),
                provider
        );

        // 예약 상태 확정으로 변경
        Booking booking = payment.getBooking();
        booking.confirm();

        return new PaymentResponseDTO.PaymentRequestResultDTO(
                payment.getId(),
                payment.getBooking().getId(),
                payment.getOrderId(),
                payment.getAmount(),
                payment.getRequestedAt());
    }
}
