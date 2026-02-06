package com.eatsfine.eatsfine.domain.inquiry.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum InquiryType {
    RESERVATION("예약 문의"),
    PAYMENT_REFUND("결제/환불 문의"),
    RESTAURANT_REGISTRATION("식당 등록 문의"),
    REVIEW("리뷰 관련"),
    TECH_SUPPORT("기술 지원"),
    ETC("기타");

    private final String description;
}
