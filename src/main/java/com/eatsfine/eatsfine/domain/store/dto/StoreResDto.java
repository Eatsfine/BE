package com.eatsfine.eatsfine.domain.store.dto;

import com.eatsfine.eatsfine.domain.businesshours.dto.BusinessHoursResDto;
import com.eatsfine.eatsfine.domain.store.enums.Category;
import com.eatsfine.eatsfine.domain.store.enums.DepositRate;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
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
            double latitude,
            double longitude,
            String mainImageUrl,
            boolean isOpenNow
    ){}

    @Builder
    public record PaginationDto(
            int currentPage,
            int totalPages,
            long totalCount,
            boolean isFirst,
            boolean isLast
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
            int depositRate,
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
    // 가게 대표 이미지 조회 응답
    @Builder
    public record GetMainImageDto(
            Long storeId,
            String mainImageUrl
    ) {}

    // 내 가게 관리 리스트 응답
    @Builder
    @Schema(description = "사장님용 내 가게 관리 단건 DTO")
    public record MyStoreDto(
            @Schema(description = "가게 ID", example = "1")
            Long storeId,
            @Schema(description = "가게명", example = "더 플레이스 강남점")
            String storeName,
            @Schema(description = "가게 주소", example = "서울 강남구 테헤란로 123")
            String address,
            @Schema(description = "카테고리", example = "ITALIAN")
            Category category,
            @Schema(description = "평점", example = "4.8")
            BigDecimal rating,
            @Schema(description = "누적 총 예약 수", example = "1234")
            Long totalBookingCount, // 총 예약 수
            @Schema(description = "리뷰 개수", example = "256")
            Long reviewCount,
            @Schema(description = "대표 이미지 URL", example = "https://s3.amazonaws.com/thumb.jpg")
            String mainImageUrl,
            @Schema(description = "현재 영업 여부", example = "true")
            boolean isOpenNow
    ){}

    @Builder
    @Schema(description = "사장님용 내 가게 관리 목록 응답")
    public record MyStoreListDto(
            @Schema(description = "소유한 가게 목록")
            List<MyStoreDto> stores
    ){}

}
