package com.eatsfine.eatsfine.domain.store.dto;

import com.eatsfine.eatsfine.domain.businesshours.dto.BusinessHoursResDto;
import com.eatsfine.eatsfine.domain.store.enums.Category;
import com.eatsfine.eatsfine.domain.store.enums.StoreApprovalStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

public class StoreResDto {

    // 가게 등록 응답
    @Builder
    public record StoreCreateDto(
            Long storeId,
            StoreApprovalStatus status
    ){}

    // 가게 상세 조회 응답
    @Builder
    public record StoreDetailDto(
            Long storeId,
            String storeName,
            String description,
            String address,
            String phone,
            Category category,
            BigDecimal rating,
            Long reviewCount,
            String mainImageUrl,
            List<String> tableImageUrls,
            List<BusinessHoursResDto.Summary> businessHours,
            boolean isOpenNow
    ){}

    // 가게 대표 이미지 등록 응답
    @Builder
    public record uploadMainImageResDto(
            String mainImageUrl
    ) {}

}
