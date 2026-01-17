package com.eatsfine.eatsfine.domain.payment.service;

import com.eatsfine.eatsfine.domain.booking.entity.Booking;
import com.eatsfine.eatsfine.domain.booking.repository.BookingRepository;
import com.eatsfine.eatsfine.global.apiPayload.code.status.ErrorStatus;
import com.eatsfine.eatsfine.global.apiPayload.exception.GeneralException;
import com.eatsfine.eatsfine.domain.payment.dto.request.PaymentRequestDTO;
import com.eatsfine.eatsfine.domain.payment.dto.response.PaymentResponseDTO;
import com.eatsfine.eatsfine.domain.payment.entity.Payment;

import com.eatsfine.eatsfine.domain.payment.enums.PaymentStatus;
import com.eatsfine.eatsfine.domain.payment.enums.PaymentType;
import com.eatsfine.eatsfine.domain.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

        private final PaymentRepository paymentRepository;
        private final BookingRepository bookingRepository;

        @Transactional
        public PaymentResponseDTO.PaymentRequestResultDTO requestPayment(PaymentRequestDTO.RequestPaymentDTO dto) {
                Booking booking = bookingRepository.findById(dto.bookingId())
                                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

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
                                .paymentProvider(dto.provider())
                                .paymentStatus(PaymentStatus.PENDING)
                                .paymentType(PaymentType.DEPOSIT)
                                .requestedAt(LocalDateTime.now())
                                .build();

                Payment savedPayment = paymentRepository.save(payment);

                // 외부 결제 제공자 응답 모의 처리
                String tid = "T" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
                String nextRedirectUrl = "https://mock.api.kakaopay.com/online/v1/payment/ready/" + tid;

                return new PaymentResponseDTO.PaymentRequestResultDTO(
                                savedPayment.getId(),
                                booking.getId(),
                                savedPayment.getPaymentMethod(),
                                tid,
                                savedPayment.getAmount(),
                                savedPayment.getPaymentStatus(),
                                nextRedirectUrl,
                                savedPayment.getRequestedAt());
        }
}
