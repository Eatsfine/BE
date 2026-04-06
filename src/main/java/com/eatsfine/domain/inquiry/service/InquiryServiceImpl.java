package com.eatsfine.domain.inquiry.service;

import com.eatsfine.domain.inquiry.dto.request.InquiryRequestDTO;
import com.eatsfine.domain.inquiry.dto.response.InquiryResponseDTO;
import com.eatsfine.domain.inquiry.entity.Inquiry;
import com.eatsfine.domain.inquiry.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryServiceImpl implements InquiryService {

    private final InquiryRepository inquiryRepository;

    @Override
    @Transactional
    public InquiryResponseDTO registerInquiry(InquiryRequestDTO request) {
        Inquiry inquiry = Inquiry.builder()
                .name(request.getName())
                .email(request.getEmail())
                .type(request.getType())
                .title(request.getTitle())
                .content(request.getContent())
                .build();

        Inquiry savedInquiry = inquiryRepository.save(inquiry);
        return InquiryResponseDTO.from(savedInquiry);
    }
}
