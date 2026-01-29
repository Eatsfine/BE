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
import com.eatsfine.eatsfine.domain.payment.exception.PaymentException;
import com.eatsfine.eatsfine.domain.payment.status.PaymentErrorStatus;
import com.eatsfine.eatsfine.global.apiPayload.code.status.ErrorStatus;
import com.eatsfine.eatsfine.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

        private final PaymentRepository paymentRepository;
        private final BookingRepository bookingRepository;
        private final RestClient tossPaymentClient;

        @Transactional
        public PaymentResponseDTO.PaymentRequestResultDTO requestPayment(PaymentRequestDTO.RequestPaymentDTO dto) {
                Booking booking = bookingRepository.findById(dto.bookingId())
                                .orElseThrow(() -> new PaymentException(PaymentErrorStatus._BOOKING_NOT_FOUND));

                // 주문 ID 생성
                String orderId = UUID.randomUUID().toString();

                // 예약금 검증
                if (booking.getDepositAmount() == null || booking.getDepositAmount().compareTo(BigDecimal.ZERO) <= 0) {
                        throw new PaymentException(PaymentErrorStatus._PAYMENT_INVALID_DEPOSIT);
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

        @Transactional(noRollbackFor = GeneralException.class)
        public PaymentResponseDTO.PaymentRequestResultDTO confirmPayment(PaymentConfirmDTO dto) {
                Payment payment = paymentRepository.findByOrderId(dto.orderId())
                                .orElseThrow(() -> new PaymentException(PaymentErrorStatus._PAYMENT_NOT_FOUND));

                if (payment.getAmount().compareTo(dto.amount()) != 0) {
                        payment.failPayment();
                        throw new PaymentException(PaymentErrorStatus._PAYMENT_INVALID_AMOUNT);
                }
                // 토스 API 호출
                TossPaymentResponse response;
                try {
                        response = tossPaymentClient.post()
                                        .uri("/v1/payments/confirm")
                                        .body(dto)
                                        .retrieve()
                                        .body(TossPaymentResponse.class);

                        if (response == null || !"DONE".equals(response.status())) {
                                log.error("Toss Payment Confirmation Failed: Status is not DONE");
                                payment.failPayment();
                                throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
                        }
                } catch (Exception e) {
                        log.error("Toss Payment API Error", e);
                        payment.failPayment();
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
                                response.approvedAt() != null ? response.approvedAt().toLocalDateTime()
                                                : LocalDateTime.now(),
                                PaymentMethod.SIMPLE_PAYMENT,
                                response.paymentKey(),
                                provider,
                                response.receipt() != null ? response.receipt().url() : null);

                log.info("Payment confirmed for OrderID: {}", dto.orderId());

                // 예약 확정 처리
                payment.getBooking().confirm();

                return new PaymentResponseDTO.PaymentRequestResultDTO(
                                payment.getId(),
                                payment.getBooking().getId(),
                                payment.getOrderId(),
                                payment.getAmount(),
                                payment.getRequestedAt());
        }

        @Transactional(noRollbackFor = GeneralException.class)
        public PaymentResponseDTO.CancelPaymentResultDTO cancelPayment(String paymentKey,
                        PaymentRequestDTO.CancelPaymentDTO dto) {
                Payment payment = paymentRepository.findByPaymentKey(paymentKey)
                                .orElseThrow(() -> new PaymentException(PaymentErrorStatus._PAYMENT_NOT_FOUND));

                // 토스 결제 취소 API 호출
                TossPaymentResponse response;
                try {
                        response = tossPaymentClient.post()
                                        .uri("/v1/payments/" + paymentKey + "/cancel")
                                        .body(dto)
                                        .retrieve()
                                        .body(TossPaymentResponse.class);

                        if (response == null || !"CANCELED".equals(response.status())) {
                                log.error("Toss Payment Cancel Failed: {}", response);
                                throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
                        }
                } catch (Exception e) {
                        log.error("Toss Payment Cancel API Error", e);
                        throw new GeneralException(ErrorStatus._INTERNAL_SERVER_ERROR);
                }

                payment.cancelPayment();

                return new PaymentResponseDTO.CancelPaymentResultDTO(
                                payment.getId(),
                                payment.getOrderId(),
                                payment.getPaymentKey(),
                                payment.getPaymentStatus().name(),
                                LocalDateTime.now());
        }

        @Transactional(readOnly = true)
        public PaymentResponseDTO.PaymentListResponseDTO getPaymentList(Long userId, Integer page, Integer limit,
                        String status) {
                // limit 기본값 처리 (만약 null이면 10)
                int size = (limit != null) ? limit : 10;
                // page 기본값 처리 (만약 null이면 1, 0보다 작으면 1로 보정). Spring Data는 0-based index이므로 -1
                int pageNumber = (page != null && page > 0) ? page - 1 : 0;

                Pageable pageable = org.springframework.data.domain.PageRequest.of(pageNumber, size);

                Page<Payment> paymentPage;
                if (status != null && !status.isEmpty()) {
                        PaymentStatus paymentStatus;
                        try {
                                paymentStatus = PaymentStatus.valueOf(status.toUpperCase());
                        } catch (IllegalArgumentException e) {
                                // 유효하지 않은 status가 들어오면 BadRequest 예외 발생
                                throw new GeneralException(ErrorStatus._BAD_REQUEST);
                        }
                        paymentPage = paymentRepository.findAllByBooking_User_IdAndPaymentStatus(userId, paymentStatus,
                                        pageable);
                } else {
                        paymentPage = paymentRepository.findAllByBooking_User_Id(userId, pageable);
                }

                List<PaymentResponseDTO.PaymentHistoryResultDTO> payments = paymentPage.getContent().stream()
                                .map(payment -> new PaymentResponseDTO.PaymentHistoryResultDTO(
                                                payment.getId(),
                                                payment.getBooking().getId(),
                                                payment.getBooking().getStore().getStoreName(),
                                                payment.getAmount(),
                                                payment.getPaymentType().name(),
                                                payment.getPaymentMethod() != null ? payment.getPaymentMethod().name()
                                                                : null,
                                                payment.getPaymentProvider() != null ? payment.getPaymentProvider().name()
                                                                : null,
                                                payment.getPaymentStatus().name(),
                                                payment.getApprovedAt()))
                                .collect(Collectors.toList());

                PaymentResponseDTO.PaginationDTO pagination = new PaymentResponseDTO.PaginationDTO(
                                paymentPage.getNumber() + 1, // 0-based -> 1-based
                                paymentPage.getTotalPages(),
                                paymentPage.getTotalElements());

                return new PaymentResponseDTO.PaymentListResponseDTO(payments, pagination);
        }

        @Transactional(readOnly = true)
        public PaymentResponseDTO.PaymentDetailResultDTO getPaymentDetail(Long paymentId) {
                Payment payment = paymentRepository.findById(paymentId)
                                .orElseThrow(() -> new PaymentException(PaymentErrorStatus._PAYMENT_NOT_FOUND));

                return new PaymentResponseDTO.PaymentDetailResultDTO(
                                payment.getId(),
                                payment.getBooking().getId(),
                                payment.getBooking().getStore().getStoreName(),
                                payment.getPaymentMethod() != null ? payment.getPaymentMethod().name() : null,
                                payment.getPaymentProvider() != null ? payment.getPaymentProvider().name() : null,
                                payment.getAmount(),
                                payment.getPaymentType().name(),
                                payment.getPaymentStatus().name(),
                                payment.getRequestedAt(),
                                payment.getApprovedAt(),
                                payment.getReceiptUrl(),
                                null // 환불 상세 정보는 현재 null 처리
                );
        }
}
