package com.eatsfine.eatsfine.domain.inquiry.controller;

import com.eatsfine.eatsfine.domain.inquiry.dto.InquiryRequestDTO;
import com.eatsfine.eatsfine.domain.inquiry.dto.InquiryResponseDTO;
import com.eatsfine.eatsfine.domain.inquiry.service.InquiryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/inquiries")
@Tag(name = "1:1 문의 API", description = "1:1 문의 등록 API")
public class InquiryController {

    private final InquiryService inquiryService;

    @Operation(summary = "1:1 문의 등록 API", description = "이름, 이메일, 문의 유형, 제목, 내용을 입력받아 문의를 등록합니다.")
    @PostMapping
    public ResponseEntity<InquiryResponseDTO> registerInquiry(@Valid @RequestBody InquiryRequestDTO request) {
        InquiryResponseDTO response = inquiryService.registerInquiry(request);
        return ResponseEntity.ok(response);
    }
}
