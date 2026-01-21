package com.eatsfine.eatsfine.domain.store.service;

import com.eatsfine.eatsfine.domain.businesshours.converter.BusinessHoursConverter;
import com.eatsfine.eatsfine.domain.businesshours.entity.BusinessHours;
import com.eatsfine.eatsfine.domain.businesshours.validator.BusinessHoursValidator;
import com.eatsfine.eatsfine.domain.image.exception.ImageException;
import com.eatsfine.eatsfine.domain.image.status.ImageErrorStatus;
import com.eatsfine.eatsfine.domain.region.entity.Region;
import com.eatsfine.eatsfine.domain.region.repository.RegionRepository;
import com.eatsfine.eatsfine.domain.region.status.RegionErrorStatus;
import com.eatsfine.eatsfine.domain.store.converter.StoreConverter;
import com.eatsfine.eatsfine.domain.store.dto.StoreReqDto;
import com.eatsfine.eatsfine.domain.store.dto.StoreResDto;
import com.eatsfine.eatsfine.domain.store.entity.Store;
import com.eatsfine.eatsfine.domain.store.exception.StoreException;
import com.eatsfine.eatsfine.domain.store.repository.StoreRepository;
import com.eatsfine.eatsfine.domain.store.status.StoreErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import com.eatsfine.eatsfine.global.s3.S3Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
public class StoreCommandServiceImpl implements StoreCommandService {

    private final StoreRepository storeRepository;
    private final RegionRepository regionRepository;
    private final S3Service s3Service;

    // 가게 등록
    @Override
    public StoreResDto.StoreCreateDto createStore(StoreReqDto.StoreCreateDto dto) {
        Region region = regionRepository.findBySidoAndSigunguAndBname(
                dto.sido(), dto.sigungu(), dto.bname()
                )
                .orElseThrow(() -> new StoreException(RegionErrorStatus._REGION_NOT_FOUND));

        // 영업시간 정상 여부 검증
        BusinessHoursValidator.validateForCreate(dto.businessHours());

        Store store = Store.builder()
                .owner(null) // User 도메인 머지 후 owner 처리 예정
                .storeName(dto.storeName())
                .businessNumber(dto.businessNumber())
                .description(dto.description())
                .address(dto.address())
                .mainImageKey(null) // 별도 API로 구현
                .region(region)
                .phoneNumber(dto.phoneNumber())
                .category(dto.category())
                .bookingIntervalMinutes(dto.bookingIntervalMinutes())
                .minPrice(dto.minPrice())
                .depositRate(dto.depositRate())
                .build();

        dto.businessHours().forEach(bhDto -> {
            BusinessHours businessHours = BusinessHoursConverter.toEntity(bhDto);
            store.addBusinessHours(businessHours);
        });

        Store savedStore = storeRepository.save(store);

        return StoreConverter.toCreateDto(savedStore);
    }

    // 가게 기본 정보 수정 (필드)
    @Override
    public StoreResDto.StoreUpdateDto updateBasicInfo(Long storeId, StoreReqDto.StoreUpdateDto dto) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(StoreErrorStatus._STORE_NOT_FOUND));

        store.updateBasicInfo(dto);
        List<String> updatedFields = extractUpdatedFields(dto);

        return StoreConverter.toUpdateDto(storeId, updatedFields);
    }

    // 수정된 필드 목록
    public List<String> extractUpdatedFields(StoreReqDto.StoreUpdateDto dto) {
        List<String> updated = new ArrayList<>();

        if (dto.storeName() != null) updated.add("storeName");
        if (dto.description() != null) updated.add("description");
        if (dto.phoneNumber() != null) updated.add("phoneNumber");
        if (dto.category() != null) updated.add("category");
        if (dto.minPrice() != null) updated.add("minPrice");
        if (dto.depositRate() != null) updated.add("depositRate");
        if (dto.bookingIntervalMinutes() != null) updated.add("bookingIntervalMinutes");

        return updated;
    }
    // 가게 메인 이미지 등록
    @Override
    public StoreResDto.UploadMainImageDto uploadMainImage(Long storeId, MultipartFile file) {
        Store store = storeRepository.findById(storeId).orElseThrow(
                () -> new StoreException(StoreErrorStatus._STORE_NOT_FOUND)
        );

        if(file.isEmpty()) {
            throw new ImageException(ImageErrorStatus.EMPTY_FILE);
        }

        if(store.getMainImageKey() != null) {
            s3Service.deleteByKey(store.getMainImageKey());
        }

        String key = s3Service.upload(file, "stores/" + storeId + "/main");
        store.updateMainImageKey(key);

        String mainImageUrl = s3Service.toUrl(store.getMainImageKey());

        return StoreConverter.toUploadMainImageDto(store.getId(), mainImageUrl);
    }

}