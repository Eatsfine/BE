package com.eatsfine.eatsfine.domain.store.dto;

import com.eatsfine.eatsfine.domain.businesshours.dto.BusinessHoursReqDto;
import com.eatsfine.eatsfine.domain.store.enums.Category;
import lombok.Builder;

import java.util.List;

public class StoreReqDto {

    @Builder
    public record StoreCreateDto(
            String storeName,
            String businessNumber,
            String description,
            Long regionId,
            String address,
            String phoneNumber,
            Category category,
            int bookingIntervalMinutes,
            List<BusinessHoursReqDto.Summary> businessHours,
            int minPrice,
            int maxPrice
    ){}
}
