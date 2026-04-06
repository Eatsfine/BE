package com.eatsfine.domain.inquiry.service;

import com.eatsfine.domain.inquiry.dto.request.InquiryRequestDTO;
import com.eatsfine.domain.inquiry.dto.response.InquiryResponseDTO;

public interface InquiryService {
    InquiryResponseDTO registerInquiry(InquiryRequestDTO request);
}
