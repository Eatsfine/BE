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
import org.springframework.data.domain.PageRequest;
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

        /**
         * Create and persist a pending deposit payment for a booking.
         *
         * Validates the booking and its deposit amount, generates an order ID, saves a PENDING deposit payment, and returns the saved payment details.
         *
         * @param dto the request DTO containing the booking ID for which to create the payment
         * @return a PaymentRequestResultDTO with the saved payment's id, booking id, orderId, amount, and requestedAt
         * @throws PaymentException if the booking is not found or the booking's deposit amount is null or not greater than zero
         */
        @Transactional
        public PaymentResponseDTO.PaymentRequestResultDTO requestPayment(PaymentRequestDTO.RequestPaymentDTO dto) {
                Booking booking = bookingRepository.findById(dto.bookingId())
                                .orElseThrow(() -> new PaymentException(PaymentErrorStatus._BOOKING_NOT_FOUND));

                // 주문 ID 생성
                String orderId = UUID.randomUUID().toString();

                // 예약금 검증
                if (booking.getDepositAmount() == null || booking.getDepositAmount() <= 0) {
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

        /**
         * Confirms a pending payment by validating the requested amount, verifying the provider response, and completing the payment record.
         *
         * @param dto confirmation data containing the orderId and amount to validate
         * @return a PaymentSuccessResultDTO containing the payment id, status, approval time, order id, amount, method, provider, and receipt URL
         * @throws PaymentException if the payment is not found or the provided amount does not match the recorded amount
         * @throws GeneralException if the external payment provider cannot be confirmed or an internal error occurs during confirmation
         */
        @Transactional(noRollbackFor = GeneralException.class)
        public PaymentResponseDTO.PaymentSuccessResultDTO confirmPayment(PaymentConfirmDTO dto) {
                Payment payment = paymentRepository.findByOrderId(dto.orderId())
                                .orElseThrow(() -> new PaymentException(PaymentErrorStatus._PAYMENT_NOT_FOUND));

                if (!payment.getAmount().equals(dto.amount())) {
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

                return new PaymentResponseDTO.PaymentSuccessResultDTO(
                                payment.getId(),
                                payment.getPaymentStatus().name(),
                                payment.getApprovedAt(),
                                payment.getOrderId(),
                                payment.getAmount(),
                                payment.getPaymentMethod() != null ? payment.getPaymentMethod().name() : null,
                                payment.getPaymentProvider() != null ? payment.getPaymentProvider().name() : null,
                                payment.getReceiptUrl());
        }

        /**
         * Cancel a Toss payment and update the corresponding local Payment to canceled.
         *
         * @param paymentKey the external payment key used by the payment provider
         * @param dto        the cancellation request payload sent to the Toss API
         * @return           a result DTO containing the payment id, order id, payment key, current payment status, and cancellation timestamp
         * @throws PaymentException if no local Payment exists for the given paymentKey
         * @throws GeneralException if the Toss cancel API call fails or returns a non-canceled status
         */
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

        /**
         * Retrieve a paginated list of a user's payments, optionally filtered by payment status.
         *
         * @param userId the ID of the user whose payments are requested
         * @param page   1-based page number; defaults to 1 when null or <= 0
         * @param limit  number of items per page; defaults to 10 when null
         * @param status optional payment status name (case-insensitive) to filter results; must match a PaymentStatus enum value
         * @throws GeneralException if the provided status does not match any PaymentStatus (_BAD_REQUEST)
         * @return a PaymentListResponseDTO containing a list of payment history entries and pagination metadata
         */
        @Transactional(readOnly = true)
        public PaymentResponseDTO.PaymentListResponseDTO getPaymentList(Long userId, Integer page, Integer limit,
                        String status) {
                // limit 기본값 처리 (만약 null이면 10)
                int size = (limit != null) ? limit : 10;
                // page 기본값 처리 (만약 null이면 1, 0보다 작으면 1로 보정). Spring Data는 0-based index이므로 -1
                int pageNumber = (page != null && page > 0) ? page - 1 : 0;

                Pageable pageable = PageRequest.of(pageNumber, size);

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
                                                payment.getPaymentProvider() != null
                                                                ? payment.getPaymentProvider().name()
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