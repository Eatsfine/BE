package com.eatsfine.eatsfine.domain.store.service;

import com.eatsfine.eatsfine.domain.businesshours.converter.BusinessHoursConverter;
import com.eatsfine.eatsfine.domain.store.dto.StoreResDto;
import com.eatsfine.eatsfine.domain.store.entity.Store;
import com.eatsfine.eatsfine.domain.store.exception.StoreException;
import com.eatsfine.eatsfine.domain.store.repository.StoreRepository;
import com.eatsfine.eatsfine.domain.store.status.StoreErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class StoreDetailQueryServiceImpl implements StoreDetailQueryService {

    private final StoreRepository storeRepository;

    @Override
    public StoreResDto.StoreDetailDto getStoreDetail(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(StoreErrorStatus._STORE_NOT_FOUND));

        return StoreResDto.StoreDetailDto.builder()
                .storeId(store.getId())
                .storeName(store.getStoreName())
                .description(store.getDescription())
                .address(store.getAddress())
                .phone(store.getPhoneNumber())
                .category(store.getCategory())
                .rating(store.getRating())
                .reviewCount(null) // reviewCount는 추후 리뷰 로직 구현 시 추가 예정
                .mainImage(store.getMainImageUrl())
                .tableImages(Collections.emptyList()) // tableImages는 추후 사진 등록 API 구현 시 추가 예정
                .businessHours(
                        store.getBusinessHours().stream()
                        .map(BusinessHoursConverter::toSummary)
                        .toList())
                .isOpenNow(false) // 추후 영업 여부 판단 로직 구현 예정
                .priceRange(null) // 추후
                .build();
    }
}
