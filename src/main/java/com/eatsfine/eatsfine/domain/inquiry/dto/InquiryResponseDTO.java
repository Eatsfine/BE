package com.eatsfine.eatsfine.domain.inquiry.dto;

import com.eatsfine.eatsfine.domain.inquiry.entity.Inquiry;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class InquiryResponseDTO {
    private Long id;
    private LocalDateTime createdAt;

    public static InquiryResponseDTO from(Inquiry inquiry) {
        return InquiryResponseDTO.builder()
                .id(inquiry.getId())
                .createdAt(inquiry.getCreatedAt())
                .build();
    }
}
