package com.eatsfine.eatsfine.domain.inquiry.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum InquiryStatus {
    WAITING("답변 대기"),
    PROCESSED("답변 완료");

    private final String description;
}
