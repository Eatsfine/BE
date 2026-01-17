package com.eatsfine.eatsfine.domain.store.dto;

import com.eatsfine.eatsfine.domain.businesshours.dto.BusinessHoursReqDto;
import com.eatsfine.eatsfine.domain.store.enums.Category;
import com.eatsfine.eatsfine.domain.store.enums.DepositRate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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

            @Pattern(
                    regexp = "^0\\d{1,2}-\\d{3,4}-\\d{4}$",
                    message = "전화번호 형식이 올바르지 않습니다."
            )
            @NotBlank(message = "전화번호는 필수입니다.")
            String phoneNumber,

            @NotNull(message = "카테고리는 필수입니다.")
            Category category,

            @NotNull(message = "최소 메뉴 가격은 필수입니다.")
            int minPrice,

            @NotNull(message = "예약금 비율은 필수입니다.")
            DepositRate depositRate,

            int bookingIntervalMinutes,

            @Valid
            List<BusinessHoursReqDto.Summary> businessHours
    ){}

    @Builder
    public record StoreUpdateDto(
            String storeName,

            String description,

            @Pattern(
                    regexp = "^0\\d{1,2}-\\d{3,4}-\\d{4}$",
                    message = "전화번호 형식이 올바르지 않습니다. (예: 02-123-4567, 010-1234-5678)"
            )
            String phoneNumber,

            Category category,

            Integer minPrice,

            DepositRate depositRate,

            Integer bookingIntervalMinutes
    ){}
}
