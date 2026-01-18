package com.eatsfine.eatsfine.domain.store.dto;

import com.eatsfine.eatsfine.domain.businesshours.dto.BusinessHoursResDto;
import com.eatsfine.eatsfine.domain.store.enums.Category;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

public class StoreResDto {

    // 가게 등록 응답
    @Builder
    public record StoreCreateDto(
            Long storeId
    ){}

    @Builder
    public record StoreSearchDto(
            Long storeId,
            String name,
            String address,
            Category category,
            BigDecimal rating,
            Integer reviewCount, // 리뷰 도메인이 존재하지 않아 null 허용
            double distance,
            String mainImageUrl,
            boolean isOpenNow
    ){}

    @Builder
    public record PaginationDto(
            int currentPage,
            int totalPages,
            long totalCount
    ){}

    @Builder
    public record StoreSearchResDto(
            List<StoreResDto.StoreSearchDto> stores,
            PaginationDto pagination
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
            BigDecimal depositAmount,
            String mainImageUrl,
            List<String> tableImageUrls,
            List<BusinessHoursResDto.Summary> businessHours,

            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
            LocalTime breakStartTime,

            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
            LocalTime breakEndTime,

            boolean isOpenNow
    ){}

    // 가게 대표 이미지 등록 응답
    @Builder
    public record UploadMainImageDto(
            Long storeId,
            String mainImageUrl
    ) {}

    // 식당 수정 응답
    @Builder
    public record StoreUpdateDto(
            Long storeId,
            List<String> updatedFields
    ){};

}
