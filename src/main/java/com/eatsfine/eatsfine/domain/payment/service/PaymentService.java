package com.eatsfine.eatsfine.domain.payment.service;

import com.eatsfine.eatsfine.domain.booking.entity.Booking;
import com.eatsfine.eatsfine.domain.booking.repository.BookingRepository;
import com.eatsfine.eatsfine.domain.payment.dto.request.PaymentWebhookDTO;
import com.eatsfine.eatsfine.domain.payment.dto.request.PaymentConfirmDTO;
import com.eatsfine.eatsfine.domain.payment.dto.request.PaymentRequestDTO;
import com.eatsfine.eatsfine.domain.payment.dto.response.PaymentResponseDTO;
import com.eatsfine.eatsfine.domain.payment.dto.response.TossPaymentResponse;
import com.eatsfine.eatsfine.domain.payment.entity.Payment;
import com.eatsfine.eatsfine.domain.payment.enums.PaymentMethod;
import com.eatsfine.eatsfine.domain.payment.enums.PaymentProvider;
import com.eatsfine.eatsfine.domain.payment.enums.PaymentStatus;
import com.eatsfine.eatsfine.domain.payment.enums.PaymentType;
import com.eatsfine.eatsfine.domain.user.entity.User;
import com.eatsfine.eatsfine.domain.user.enums.Role;
import com.eatsfine.eatsfine.domain.payment.repository.PaymentRepository;
import com.eatsfine.eatsfine.domain.payment.exception.PaymentException;
import com.eatsfine.eatsfine.domain.payment.status.PaymentErrorStatus;
import com.eatsfine.eatsfine.global.apiPayload.code.status.ErrorStatus;
import com.eatsfine.eatsfine.global.apiPayload.exception.GeneralException;
import com.eatsfine.eatsfine.domain.user.repository.UserRepository;
import com.eatsfine.eatsfine.domain.user.exception.UserException;
import com.eatsfine.eatsfine.domain.user.status.UserErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
        private final UserRepository userRepository;
        private final TossPaymentService tossPaymentService;

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
        public PaymentResponseDTO.PaymentSuccessResultDTO confirmPayment(PaymentConfirmDTO dto) {
                Payment payment = paymentRepository.findByOrderId(dto.orderId())
                                .orElseThrow(() -> new PaymentException(PaymentErrorStatus._PAYMENT_NOT_FOUND));

                if (payment.getAmount().compareTo(dto.amount()) != 0) {
                        payment.failPayment();
                        throw new PaymentException(PaymentErrorStatus._PAYMENT_INVALID_AMOUNT);
                }
                // 토스 API 호출
                TossPaymentResponse response;
                try {
                        response = tossPaymentService.confirm(dto);
                } catch (Exception e) {
                        payment.failPayment();
                        throw e;
                }

                if (response == null || !"DONE".equals(response.status())) {
                        log.error("Toss Payment Confirmation Failed: Status is not DONE");
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

                Booking booking = payment.getBooking(); // 결제 엔티티에 매핑된 예약 객체 가져오기
                if (booking != null) {
                        // 예약 상태를 CONFIRMED로 변경
                        booking.confirm();
                        log.info("Booking confirmed for OrderID: {}", dto.orderId());
                }

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

        @Transactional(noRollbackFor = GeneralException.class)
        public PaymentResponseDTO.CancelPaymentResultDTO cancelPayment(String paymentKey,
                        PaymentRequestDTO.CancelPaymentDTO dto) {
                Payment payment = paymentRepository.findByPaymentKey(paymentKey)
                                .orElseThrow(() -> new PaymentException(PaymentErrorStatus._PAYMENT_NOT_FOUND));

                // 토스 결제 취소 API 호출
                TossPaymentResponse response = tossPaymentService.cancel(paymentKey, dto);

                if (response == null || !"CANCELED".equals(response.status())) {
                        log.error("Toss Payment Cancel Failed: {}", response);
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
        public PaymentResponseDTO.PaymentListResponseDTO getPaymentList(String email, Integer page, Integer limit,
                        String status) {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new UserException(UserErrorStatus.MEMBER_NOT_FOUND));
                // limit 기본값 처리 (null이거나 0 이하이면 10)
                int size = (limit != null && limit > 0) ? limit : 10;
                // page 기본값 처리 (만약 null이면 1, 0보다 작으면 1로 보정). Spring Data는 0-based index이므로 -1
                int pageNumber = (page != null && page > 0) ? page - 1 : 0;

                Pageable pageable = PageRequest.of(pageNumber, size);

                PaymentStatus paymentStatus = parsePaymentStatus(status);

                Page<Payment> paymentPage;
                if (user.getRole() == Role.ROLE_OWNER) {
                        paymentPage = (paymentStatus != null)
                                        ? paymentRepository.findAllByOwnerIdAndStatusWithDetails(user.getId(),
                                                        paymentStatus, pageable)
                                        : paymentRepository.findAllByOwnerIdWithDetails(user.getId(), pageable);
                } else {
                        paymentPage = (paymentStatus != null)
                                        ? paymentRepository.findAllByUserIdAndStatusWithDetails(user.getId(),
                                                        paymentStatus, pageable)
                                        : paymentRepository.findAllByUserIdWithDetails(user.getId(), pageable);
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
        public PaymentResponseDTO.PaymentDetailResultDTO getPaymentDetail(Long paymentId, String email) {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new UserException(UserErrorStatus.MEMBER_NOT_FOUND));
                Payment payment = paymentRepository.findByIdWithDetails(paymentId)
                                .orElseThrow(() -> new PaymentException(PaymentErrorStatus._PAYMENT_NOT_FOUND));

                boolean isBooker = payment.getBooking().getUser().getId().equals(user.getId());
                boolean isStoreOwner = payment.getBooking().getStore().getOwner().getId().equals(user.getId());

                if (!isBooker && !isStoreOwner) {
                        throw new PaymentException(PaymentErrorStatus._PAYMENT_ACCESS_DENIED);
                }

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

        @Transactional
        public void processWebhook(PaymentWebhookDTO dto) {
                // 이벤트 타입 검증
                if (!"PAYMENT_STATUS_CHANGED".equals(dto.eventType())) {
                        log.info("Webhook skipped: Unhandled event type {}", dto.eventType());
                        return;
                }

                PaymentWebhookDTO.PaymentData data = dto.data();

                Payment payment = paymentRepository.findByOrderId(data.orderId())
                                .orElseThrow(() -> new PaymentException(PaymentErrorStatus._PAYMENT_NOT_FOUND));

                PaymentStatus targetStatus = null;
                if ("DONE".equals(data.status())) {
                        targetStatus = PaymentStatus.COMPLETED;
                } else if ("CANCELED".equals(data.status())) {
                        targetStatus = PaymentStatus.REFUNDED;
                }

                if (targetStatus == null) {
                        log.info("Webhook skipped: Unknown or unhandled status {}", data.status());
                        return;
                }

                if (payment.getPaymentStatus() == targetStatus) {
                        log.info("Webhook skipped: Payment {} already in status {}", data.orderId(), targetStatus);
                        return;
                }

                // 상태 전환 유효성 검사
                // COMPLETED 완료 처리는 오직 PENDING 상태에서만 가능
                if (targetStatus == PaymentStatus.COMPLETED && payment.getPaymentStatus() != PaymentStatus.PENDING) {
                        log.warn("Webhook skipped: Invalid state transition from {} to {} for OrderID {}",
                                        payment.getPaymentStatus(), targetStatus, data.orderId());
                        return;
                }
                if (targetStatus == PaymentStatus.REFUNDED && payment.getPaymentStatus() != PaymentStatus.COMPLETED) {
                        log.warn("Webhook skipped: Invalid state transition from {} to {} for OrderID {}",
                                        payment.getPaymentStatus(), targetStatus, data.orderId());
                        return;
                }

                if (targetStatus == PaymentStatus.COMPLETED) {
                        // 금액 검증
                        if (data.totalAmount() == null || payment.getAmount().compareTo(data.totalAmount()) != 0) {
                                log.error("Webhook amount verification failed for OrderID: {}. Expected: {}, Received: {}",
                                                data.orderId(), payment.getAmount(), data.totalAmount());
                                payment.failPayment();
                                return;
                        }

                        // Provider 파싱
                        PaymentProvider provider = null;
                        if (data.easyPay() != null) {
                                String providerCode = data.easyPay().provider();
                                if ("토스페이".equals(providerCode)) {
                                        provider = PaymentProvider.TOSS;
                                } else if ("카카오페이".equals(providerCode)) {
                                        provider = PaymentProvider.KAKAOPAY;
                                }
                        }

                        payment.completePayment(
                                        LocalDateTime.now(),
                                        PaymentMethod.SIMPLE_PAYMENT,
                                        data.paymentKey(),
                                        provider,
                                        null);
                        log.info("Webhook processed: Payment {} status updated to COMPLETED", data.orderId());
                } else if (targetStatus == PaymentStatus.REFUNDED) {
                        payment.cancelPayment();
                        log.info("Webhook processed: Payment {} status updated to REFUNDED", data.orderId());
                }
        }

        private PaymentStatus parsePaymentStatus(String status) {
                if (status == null || status.isEmpty()) {
                        return null;
                }
                try {
                        return PaymentStatus.valueOf(status.toUpperCase());
                } catch (IllegalArgumentException e) {
                        throw new GeneralException(ErrorStatus._BAD_REQUEST);
                }
        }
}
