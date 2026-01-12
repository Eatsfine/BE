package com.eatsfine.eatsfine.domain.store.service;

import com.eatsfine.eatsfine.domain.businesshours.converter.BusinessHoursConverter;
import com.eatsfine.eatsfine.domain.businesshours.entity.BusinessHours;
import com.eatsfine.eatsfine.domain.region.entity.Region;
import com.eatsfine.eatsfine.domain.region.repository.RegionRepository;
import com.eatsfine.eatsfine.domain.region.status.RegionErrorStatus;
import com.eatsfine.eatsfine.domain.store.converter.StoreConverter;
import com.eatsfine.eatsfine.domain.store.dto.StoreReqDto;
import com.eatsfine.eatsfine.domain.store.dto.StoreResDto;
import com.eatsfine.eatsfine.domain.store.entity.Store;
import com.eatsfine.eatsfine.domain.store.enums.StoreApprovalStatus;
import com.eatsfine.eatsfine.domain.store.exception.StoreException;
import com.eatsfine.eatsfine.domain.store.repository.StoreRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class StoreCommandServiceImpl implements StoreCommandService {

    private final StoreRepository storeRepository;
    private final RegionRepository regionRepository;

    @Override
    public StoreResDto.StoreCreateDto createStore(StoreReqDto.StoreCreateDto dto) {
        Region region = regionRepository.findById(dto.regionId())
                .orElseThrow(() -> new StoreException(RegionErrorStatus._REGION_NOT_FOUND));

        Store store = Store.builder()
                .owner(null) // User 도메인 머지 후 owner 처리 예정
                .storeName(dto.storeName())
                .businessNumber(dto.businessNumber())
                .description(dto.description())
                .address(dto.address())
                .mainImageUrl(null) // 별도 API로 구현
                .region(region)
                .phoneNumber(dto.phoneNumber())
                .category(dto.category())
                .approvalStatus(StoreApprovalStatus.PENDING)
                .bookingIntervalMinutes(dto.bookingIntervalMinutes())
                .build();

        dto.businessHours().forEach(bhDto -> {
            BusinessHours businessHours = BusinessHoursConverter.toEntity(bhDto);
            store.addBusinessHours(businessHours);
        });

        Store savedStore = storeRepository.save(store);

        return StoreConverter.toCreateDto(savedStore);
    }

}