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
import com.eatsfine.eatsfine.domain.payment.exception.PaymentException;
import com.eatsfine.eatsfine.domain.payment.repository.PaymentRepository;
import com.eatsfine.eatsfine.domain.store.entity.Store;
import com.eatsfine.eatsfine.domain.payment.status.PaymentErrorStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

        @InjectMocks
        private PaymentService paymentService;

        @Mock
        private PaymentRepository paymentRepository;

        @Mock
        private BookingRepository bookingRepository;

        @Mock
        private TossPaymentService tossPaymentService;

        @Test
        @DisplayName("결제 요청 성공")
        void requestPayment_success() {
                // given
                Long bookingId = 1L;
                PaymentRequestDTO.RequestPaymentDTO request = new PaymentRequestDTO.RequestPaymentDTO(bookingId);

                Booking booking = Booking.builder()
                                .id(bookingId)
                                .build();
                ReflectionTestUtils.setField(booking, "depositAmount", BigDecimal.valueOf(10000));

                Payment payment = Payment.builder()
                                .id(1L)
                                .booking(booking)
                                .amount(BigDecimal.valueOf(10000))
                                .orderId("generated-order-id")
                                .paymentStatus(PaymentStatus.PENDING)
                                .requestedAt(LocalDateTime.now())
                                .build();

                given(bookingRepository.findById(bookingId)).willReturn(Optional.of(booking));
                given(paymentRepository.save(any(Payment.class))).willReturn(payment);

                // when
                PaymentResponseDTO.PaymentRequestResultDTO response = paymentService.requestPayment(request);

                // then
                assertThat(response.amount()).isEqualTo(BigDecimal.valueOf(10000));
                assertThat(response.orderId()).isEqualTo("generated-order-id");
                verify(paymentRepository, times(1)).save(any(Payment.class));
        }

        @Test
        @DisplayName("결제 요청 실패 - 예약 없음")
        void requestPayment_fail_bookingNotFound() {
                // given
                PaymentRequestDTO.RequestPaymentDTO request = new PaymentRequestDTO.RequestPaymentDTO(999L);
                given(bookingRepository.findById(999L)).willReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> paymentService.requestPayment(request))
                                .isInstanceOf(PaymentException.class)
                                .extracting("code")
                                .isEqualTo(PaymentErrorStatus._BOOKING_NOT_FOUND);
        }

        @Test
        @DisplayName("결제 승인 성공")
        void confirmPayment_success() {
                // given
                String orderId = "order-id-123";
                String paymentKey = "payment-key-123";
                BigDecimal amount = BigDecimal.valueOf(10000);
                PaymentConfirmDTO request = PaymentConfirmDTO.builder()
                                .orderId(orderId)
                                .amount(amount)
                                .paymentKey(paymentKey)
                                .build();

                Booking booking = Booking.builder().id(1L).build();
                Payment payment = Payment.builder()
                                .id(1L)
                                .booking(booking)
                                .orderId(orderId)
                                .amount(amount)
                                .paymentStatus(PaymentStatus.PENDING)
                                .build();

                TossPaymentResponse.EasyPay easyPay = new TossPaymentResponse.EasyPay("토스페이", 10000, 0);
                TossPaymentResponse tossResponse = new TossPaymentResponse(
                                paymentKey, "NORMAL", orderId, "orderName", "mId", "KRW", "CARD",
                                10000, 10000, "DONE",
                                java.time.OffsetDateTime.now(), java.time.OffsetDateTime.now(),
                                false, null, 10000, 0,
                                easyPay, new TossPaymentResponse.Receipt("http://receipt.url"));// TossPaymentResponse
                                                                                                // record 생성자가 많아서
                                                                                                // 필드에 맞게 넣어줌 (가정)
                                                                                                // 실제 record 구조에 따라 맞춰야
                                                                                                // 함. 위 내용은 예시.
                                                                                                // TossPaymentResponse가
                                                                                                // record이므로 생성자
                                                                                                // 파라미터 순서 중요.
                                                                                                // 여기서는 Mocking을 하거나,
                                                                                                // 필드가 많으면 빌더나 생성자를
                                                                                                // 확인해야 함.
                                                                                                // TossPaymentService가
                                                                                                // Mock이므로 response
                                                                                                // 리턴값만 잘 맞춰주면 됨.

                given(paymentRepository.findByOrderId(orderId)).willReturn(Optional.of(payment));
                given(tossPaymentService.confirm(any(PaymentConfirmDTO.class))).willReturn(tossResponse);

                // when
                PaymentResponseDTO.PaymentSuccessResultDTO response = paymentService.confirmPayment(request);

                // then
                assertThat(response.status()).isEqualTo(PaymentStatus.COMPLETED.name());
                assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
        }

        @Test
        @DisplayName("결제 승인 실패 - 금액 불일치")
        void confirmPayment_fail_invalidAmount() {
                // given
                String orderId = "order-id-123";
                BigDecimal originalAmount = BigDecimal.valueOf(10000);
                BigDecimal requestAmount = BigDecimal.valueOf(5000); // Mismatch

                PaymentConfirmDTO request = PaymentConfirmDTO.builder()
                                .orderId(orderId)
                                .amount(requestAmount)
                                .paymentKey("key")
                                .build();

                Payment payment = Payment.builder()
                                .id(1L)
                                .orderId(orderId)
                                .amount(originalAmount)
                                .paymentStatus(PaymentStatus.PENDING)
                                .build();

                given(paymentRepository.findByOrderId(orderId)).willReturn(Optional.of(payment));

                // when & then
                assertThatThrownBy(() -> paymentService.confirmPayment(request))
                                .isInstanceOf(PaymentException.class)
                                .extracting("code")
                                .isEqualTo(PaymentErrorStatus._PAYMENT_INVALID_AMOUNT);

                assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);
        }

        @Test
        @DisplayName("결제 취소 성공")
        void cancelPayment_success() {
                // given
                String paymentKey = "payment-key-123";
                PaymentRequestDTO.CancelPaymentDTO request = new PaymentRequestDTO.CancelPaymentDTO("단순 변심");

                Payment payment = Payment.builder()
                                .id(1L)
                                .paymentKey(paymentKey)
                                .orderId("order-id-123")
                                .paymentStatus(PaymentStatus.COMPLETED)
                                .build();

                TossPaymentResponse tossResponse = new TossPaymentResponse(
                                paymentKey, "NORMAL", "order-id-123", "orderName", "mId", "KRW", "CARD",
                                10000, 0, "CANCELED",
                                java.time.OffsetDateTime.now(), java.time.OffsetDateTime.now(),
                                false, null, 10000, 0,
                                null, null);
                given(paymentRepository.findByPaymentKey(paymentKey)).willReturn(Optional.of(payment));
                given(tossPaymentService.cancel(eq(paymentKey), any(PaymentRequestDTO.CancelPaymentDTO.class)))
                                .willReturn(tossResponse);

                // when
                PaymentResponseDTO.CancelPaymentResultDTO response = paymentService.cancelPayment(paymentKey, request);

                // then
                assertThat(response.status()).isEqualTo("REFUNDED");
                assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.REFUNDED);
        }
}
