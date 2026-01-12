package com.eatsfine.eatsfine.domain.store.dto;

import com.eatsfine.eatsfine.domain.businesshours.dto.BusinessHoursResDto;
import com.eatsfine.eatsfine.domain.businesshours.entity.BusinessHours;
import com.eatsfine.eatsfine.domain.businesshours.enums.DayOfWeek;
import com.eatsfine.eatsfine.domain.store.enums.Category;
import com.eatsfine.eatsfine.domain.store.enums.StoreApprovalStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

public class StoreResDto {

    @Builder
    public record StoreCreateDto(
            Long storeId,
            StoreApprovalStatus status
    ){}

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
            String mainImage,
            List<String> tableImages,
            List<BusinessHoursResDto.Summary> businessHours,
            boolean isOpenNow
    ){}

}
