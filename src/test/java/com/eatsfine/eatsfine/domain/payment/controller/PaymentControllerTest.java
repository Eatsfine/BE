package com.eatsfine.eatsfine.domain.payment.controller;

import com.eatsfine.eatsfine.domain.payment.dto.request.PaymentConfirmDTO;
import com.eatsfine.eatsfine.domain.payment.dto.request.PaymentRequestDTO;
import com.eatsfine.eatsfine.domain.payment.dto.response.PaymentResponseDTO;
import com.eatsfine.eatsfine.domain.payment.exception.PaymentException;
import com.eatsfine.eatsfine.domain.payment.service.PaymentService;
import com.eatsfine.eatsfine.domain.payment.status.PaymentErrorStatus;
import com.eatsfine.eatsfine.domain.user.exception.UserException;
import com.eatsfine.eatsfine.domain.user.status.UserErrorStatus;
import com.eatsfine.eatsfine.global.auth.CustomAccessDeniedHandler;
import com.eatsfine.eatsfine.global.auth.CustomAuthenticationEntryPoint;
import com.eatsfine.eatsfine.global.config.jwt.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
@WithMockUser
class PaymentControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private PaymentService paymentService;

        @MockitoBean
        private JwtAuthenticationFilter jwtAuthenticationFilter;

        @MockitoBean
        private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

        @MockitoBean
        private CustomAccessDeniedHandler customAccessDeniedHandler;

        @Autowired
        private ObjectMapper objectMapper;

        @BeforeEach
        void setUp() throws ServletException, IOException {
                doAnswer(invocation -> {
                        HttpServletRequest request = invocation.getArgument(0);
                        HttpServletResponse response = invocation.getArgument(1);
                        FilterChain chain = invocation.getArgument(2);
                        chain.doFilter(request, response);
                        return null;
                }).when(jwtAuthenticationFilter).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class),
                                any(FilterChain.class));
        }

        @Test
        @DisplayName("결제 요청 성공")
        void requestPayment_success() throws Exception {
                // given
                PaymentRequestDTO.RequestPaymentDTO request = new PaymentRequestDTO.RequestPaymentDTO(1L);
                PaymentResponseDTO.PaymentRequestResultDTO response = new PaymentResponseDTO.PaymentRequestResultDTO(
                                1L, 1L, "order-id-123", BigDecimal.valueOf(10000), LocalDateTime.now());

                given(paymentService.requestPayment(any(PaymentRequestDTO.RequestPaymentDTO.class)))
                                .willReturn(response);

                // when & then
                mockMvc.perform(post("/api/v1/payments/request")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.isSuccess").value(true))
                                .andExpect(jsonPath("$.result.paymentId").value(1L))
                                .andExpect(jsonPath("$.result.orderId").value("order-id-123"));
        }

        @Test
        @DisplayName("결제 승인 성공")
        void confirmPayment_success() throws Exception {
                // given
                PaymentConfirmDTO request = PaymentConfirmDTO.builder()
                                .paymentKey("payment-key-123")
                                .orderId("order-id-123")
                                .amount(BigDecimal.valueOf(10000))
                                .build();

                PaymentResponseDTO.PaymentSuccessResultDTO response = new PaymentResponseDTO.PaymentSuccessResultDTO(
                                1L, "COMPLETED", LocalDateTime.now(), "order-id-123", BigDecimal.valueOf(10000),
                                "CARD", "TOSS", "http://receipt.url");

                given(paymentService.confirmPayment(any(PaymentConfirmDTO.class))).willReturn(response);

                // when & then
                mockMvc.perform(post("/api/v1/payments/confirm")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.isSuccess").value(true))
                                .andExpect(jsonPath("$.result.status").value("COMPLETED"));
        }

        @Test
        @DisplayName("결제 취소 성공")
        void cancelPayment_success() throws Exception {
                // given
                String paymentKey = "payment-key-123";
                PaymentRequestDTO.CancelPaymentDTO request = new PaymentRequestDTO.CancelPaymentDTO("단순 변심");
                PaymentResponseDTO.CancelPaymentResultDTO response = new PaymentResponseDTO.CancelPaymentResultDTO(
                                1L, "order-id-123", paymentKey, "CANCELED", LocalDateTime.now());

                given(paymentService.cancelPayment(eq(paymentKey), any(PaymentRequestDTO.CancelPaymentDTO.class)))
                                .willReturn(response);

                // when & then
                mockMvc.perform(post("/api/v1/payments/{paymentKey}/cancel", paymentKey)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.isSuccess").value(true))
                                .andExpect(jsonPath("$.result.status").value("CANCELED"));
        }

        @Test
        @DisplayName("결제 내역 조회 성공")
        void getPaymentList_success() throws Exception {
                // given
                PaymentResponseDTO.PaginationDTO pagination = new PaymentResponseDTO.PaginationDTO(1, 1, 1L);
                PaymentResponseDTO.PaymentHistoryResultDTO history = new PaymentResponseDTO.PaymentHistoryResultDTO(
                                1L, 1L, "Store Name", BigDecimal.valueOf(10000), "DEPOSIT", "CARD", "TOSS", "COMPLETED",
                                LocalDateTime.now());
                PaymentResponseDTO.PaymentListResponseDTO response = new PaymentResponseDTO.PaymentListResponseDTO(
                                Collections.singletonList(history), pagination);

                given(paymentService.getPaymentList(eq("user"), any(Integer.class), any(Integer.class), any()))
                                .willReturn(response);

                // when & then
                mockMvc.perform(get("/api/v1/payments")
                                .param("page", "1")
                                .param("limit", "10")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.isSuccess").value(true))
                                .andExpect(jsonPath("$.result.payments[0].storeName").value("Store Name"));
        }

        @Test
        @DisplayName("결제 상세 조회 성공")
        void getPaymentDetail_success() throws Exception {
                // given
                Long paymentId = 1L;
                PaymentResponseDTO.PaymentDetailResultDTO response = new PaymentResponseDTO.PaymentDetailResultDTO(
                                paymentId, 1L, "Store Name", "CARD", "TOSS", BigDecimal.valueOf(10000), "DEPOSIT",
                                "COMPLETED", LocalDateTime.now(), LocalDateTime.now(), "http://receipt.url", null);

                given(paymentService.getPaymentDetail(eq(paymentId), eq("user"))).willReturn(response);

                // when & then
                mockMvc.perform(get("/api/v1/payments/{paymentId}", paymentId)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.isSuccess").value(true))
                                .andExpect(jsonPath("$.result.paymentId").value(paymentId));
        }

        // ===== 예외 케이스 테스트 =====

        @Test
        @DisplayName("결제 요청 실패 - bookingId가 null이면 400 Bad Request")
        void requestPayment_fail_validationError() throws Exception {
                // given - bookingId가 null인 잘못된 요청
                String invalidRequest = "{\"bookingId\": null}";

                // when & then
                mockMvc.perform(post("/api/v1/payments/request")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidRequest))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.isSuccess").value(false));
        }

        @Test
        @DisplayName("결제 승인 실패 - 필수 파라미터 누락 시 400 Bad Request")
        void confirmPayment_fail_validationError() throws Exception {
                // given - paymentKey, orderId, amount 모두 null
                String invalidRequest = "{}";

                // when & then
                mockMvc.perform(post("/api/v1/payments/confirm")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidRequest))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.isSuccess").value(false));
        }

        @Test
        @DisplayName("결제 요청 실패 - 서비스에서 PaymentException 발생 시 에러 응답")
        void requestPayment_fail_serviceException() throws Exception {
                // given
                PaymentRequestDTO.RequestPaymentDTO request = new PaymentRequestDTO.RequestPaymentDTO(999L);

                given(paymentService.requestPayment(any(PaymentRequestDTO.RequestPaymentDTO.class)))
                                .willThrow(new PaymentException(PaymentErrorStatus._BOOKING_NOT_FOUND));

                // when & then
                mockMvc.perform(post("/api/v1/payments/request")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.isSuccess").value(false))
                                .andExpect(jsonPath("$.code").value(PaymentErrorStatus._BOOKING_NOT_FOUND.getCode()));
        }

        @Test
        @DisplayName("결제 내역 조회 실패 - 서비스에서 UserException 발생 시 에러 응답")
        void getPaymentList_fail_serviceException() throws Exception {
                // given
                given(paymentService.getPaymentList(eq("user"), any(Integer.class), any(Integer.class), any()))
                                .willThrow(new UserException(UserErrorStatus.MEMBER_NOT_FOUND));

                // when & then
                mockMvc.perform(get("/api/v1/payments")
                                .param("page", "1")
                                .param("limit", "10")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.isSuccess").value(false));
        }
}
