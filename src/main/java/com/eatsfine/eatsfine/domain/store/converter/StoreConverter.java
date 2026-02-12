package com.eatsfine.eatsfine.domain.store.converter;

import com.eatsfine.eatsfine.domain.businesshours.converter.BusinessHoursConverter;
import com.eatsfine.eatsfine.domain.businesshours.entity.BusinessHours;
import com.eatsfine.eatsfine.domain.store.dto.StoreResDto;
import com.eatsfine.eatsfine.domain.store.entity.Store;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

public class StoreConverter {

    public static StoreResDto.StoreCreateDto toCreateDto(Store store) {
        return StoreResDto.StoreCreateDto.builder()
                .storeId(store.getId())
                .build();
    }

    public static StoreResDto.StoreSearchDto toSearchDto(Store store, Double distance, boolean isOpenNow) {
        return StoreResDto.StoreSearchDto.builder()
                .storeId(store.getId())
                .name(store.getStoreName())
                .address(store.getAddress())
                .category(store.getCategory())
                .rating(store.getRating())
                .reviewCount(null) // 리뷰 도메인 구현 이후 추가 예정
                .distance(distance)
                .mainImageUrl(store.getMainImageKey())
                .isOpenNow(isOpenNow)
                .build();
    }

    public static StoreResDto.StoreDetailDto toDetailDto(Store store, String mainImageUrl, boolean isOpenNow) {
        BusinessHours anyOpenDay = store.getBusinessHours().stream()
                .filter(bh -> !bh.isClosed())
                .findFirst()
                .orElse(null);

        return StoreResDto.StoreDetailDto.builder()
                .storeId(store.getId())
                .storeName(store.getStoreName())
                .description(store.getDescription())
                .address(store.getAddress())
                .phone(store.getPhoneNumber())
                .category(store.getCategory())
                .rating(store.getRating())
                .reviewCount(null) // reviewCount는 추후 리뷰 로직 구현 시 추가 예정
                .depositRate(store.getDepositRate().getPercent())
                .mainImageUrl(mainImageUrl)
                .tableImageUrls(Collections.emptyList()) // tableImages는 추후 사진 등록 API 구현 시 추가 예정
                .businessHours(
                        store.getBusinessHours().stream()
                                .map(BusinessHoursConverter::toSummary)
                                .toList())
                .breakStartTime(anyOpenDay != null ? anyOpenDay.getBreakStartTime() : null)
                .breakEndTime(anyOpenDay != null ? anyOpenDay.getBreakEndTime() : null)
                .isOpenNow(isOpenNow) // 추후 영업 여부 판단 로직 구현 예정
                .build();
    }

    public static StoreResDto.StoreUpdateDto toUpdateDto(Long storeId, List<String> updatedFields) {
        return StoreResDto.StoreUpdateDto.builder()
                .storeId(storeId)
                .updatedFields(updatedFields)
                .build();
    }

    public static StoreResDto.UploadMainImageDto toUploadMainImageDto(Long storeId, String mainImageUrl) {
        return StoreResDto.UploadMainImageDto.builder()
                .storeId(storeId)
                .mainImageUrl(mainImageUrl)
                .build();
    }

    public static StoreResDto.GetMainImageDto toGetMainImageDto(Long storeId, String key) {
        return StoreResDto.GetMainImageDto.builder()
                .storeId(storeId)
                .mainImageUrl(key)
                .build();
    }

    public static StoreResDto.MyStoreDto toMyStoreDto(Store store, boolean isOpenNow, String mainImageUrl, Long totalBookingCount) {
        return StoreResDto.MyStoreDto.builder()
                .storeId(store.getId())
                .storeName(store.getStoreName())
                .address(store.getAddress())
                .category(store.getCategory())
                .rating(store.getRating())
                .totalBookingCount(totalBookingCount)
                .reviewCount(null) // 리뷰 도메인 구현 이후 추가 예정
                .mainImageUrl(mainImageUrl)
                .isOpenNow(isOpenNow)
                .build();
    }

    public static StoreResDto.MyStoreListDto toMyStoreListDto(List<StoreResDto.MyStoreDto> stores) {
        return StoreResDto.MyStoreListDto.builder()
                .stores(stores)
                .build();
    }
}
