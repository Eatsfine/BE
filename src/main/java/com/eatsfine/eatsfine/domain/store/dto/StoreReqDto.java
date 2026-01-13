package com.eatsfine.eatsfine.domain.store.dto;

import com.eatsfine.eatsfine.domain.businesshours.dto.BusinessHoursReqDto;
import com.eatsfine.eatsfine.domain.store.enums.Category;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

public class StoreReqDto {

    @Builder
    public record StoreCreateDto(

            @NotBlank(message = "가게명은 필수입니다.")
            String storeName,

            @NotBlank(message = "사업자번호는 필수입니다.")
            String businessNumber,

            String description,

            @NotNull(message = "지역은 필수입니다.")
            Long regionId,

            @NotBlank(message = "주소는 필수입니다.")
            String address,

            @NotBlank(message = "전화번호는 필수입니다.")
            String phoneNumber,

            @NotNull(message = "카테고리는 필수입니다.")
            Category category,

            int bookingIntervalMinutes,

            @Valid
            List<BusinessHoursReqDto.Summary> businessHours
    ){}
}
