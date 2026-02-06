package com.eatsfine.eatsfine.domain.inquiry.service;

import com.eatsfine.eatsfine.domain.inquiry.dto.InquiryRequestDTO;
import com.eatsfine.eatsfine.domain.inquiry.dto.InquiryResponseDTO;

public interface InquiryService {
    InquiryResponseDTO registerInquiry(InquiryRequestDTO request);
}
