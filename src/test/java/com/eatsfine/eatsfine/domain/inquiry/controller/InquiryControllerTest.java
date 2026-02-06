package com.eatsfine.eatsfine.domain.inquiry.controller;

import com.eatsfine.eatsfine.domain.inquiry.dto.InquiryRequestDTO;
import com.eatsfine.eatsfine.domain.inquiry.dto.InquiryResponseDTO;
import com.eatsfine.eatsfine.domain.inquiry.enums.InquiryType;
import com.eatsfine.eatsfine.domain.inquiry.service.InquiryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import com.eatsfine.eatsfine.global.auth.CustomAccessDeniedHandler;
import com.eatsfine.eatsfine.global.auth.CustomAuthenticationEntryPoint;
import com.eatsfine.eatsfine.global.config.jwt.JwtAuthenticationFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InquiryController.class)
@WithMockUser
class InquiryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InquiryService inquiryService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @MockBean
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
    @DisplayName("문의 등록 성공")
    void registerInquiry_success() throws Exception {
        // given
        InquiryRequestDTO request = new InquiryRequestDTO();
        ReflectionTestUtils.setField(request, "name", "홍길동");
        ReflectionTestUtils.setField(request, "email", "test@example.com");
        ReflectionTestUtils.setField(request, "type", InquiryType.ETC);
        ReflectionTestUtils.setField(request, "title", "문의 제목");
        ReflectionTestUtils.setField(request, "content", "문의 내용입니다.");

        InquiryResponseDTO response = InquiryResponseDTO.builder()
                .id(1L)
                .createdAt(LocalDateTime.now())
                .build();

        given(inquiryService.registerInquiry(any(InquiryRequestDTO.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/inquiries")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.message").value("성공입니다."))
                .andExpect(jsonPath("$.result.id").value(1L));
    }

    @Test
    @DisplayName("문의 등록 실패 - 유효성 검증 실패 (이름 누락)")
    void registerInquiry_fail_emptyName() throws Exception {
        // given
        InquiryRequestDTO request = new InquiryRequestDTO();
        ReflectionTestUtils.setField(request, "name", ""); // empty
        ReflectionTestUtils.setField(request, "email", "test@example.com");
        ReflectionTestUtils.setField(request, "type", InquiryType.ETC);
        ReflectionTestUtils.setField(request, "title", "문의 제목");
        ReflectionTestUtils.setField(request, "content", "문의 내용입니다.");

        // when & then
        mockMvc.perform(post("/api/v1/inquiries")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
