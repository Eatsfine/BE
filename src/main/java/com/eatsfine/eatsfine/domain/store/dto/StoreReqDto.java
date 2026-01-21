package com.eatsfine.eatsfine.domain.store.dto;

import com.eatsfine.eatsfine.domain.businesshours.dto.BusinessHoursReqDto;
import com.eatsfine.eatsfine.domain.store.enums.Category;
import com.eatsfine.eatsfine.domain.store.enums.DepositRate;
import jakarta.persistence.Column;
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
            @Pattern(regexp = "^[0-9]{10}$", message = "사업자번호는 숫자 10자리여야 합니다.")
            String businessNumber,

            @NotBlank(message = "가게 설명은 필수입니다.")
            String description,

            @NotBlank(message = "시/도는 필수입니다.")
            String sido, // ex 경기도, 세종특별자치시

            @NotNull(message = "시/군/구는 필수입니다. (해당 사항 없을 경우 \"\"를 입력해주세요.)")
            String sigungu, // ex 성남시 분당구, ""

            @NotBlank(message = "법정동은 필수입니다.")
            String bname, // ex 서현동, 어진동

            @NotBlank(message = "전체 주소는 필수입니다.")
            String address,

            @NotNull(message = "위도는 필수입니다.")
            double latitude,

            @NotNull(message = "경도는 필수입니다,.")
            double longitude,

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
