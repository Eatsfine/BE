package com.eatsfine.eatsfine.domain.store.service;

import com.eatsfine.eatsfine.domain.businesshours.converter.BusinessHoursConverter;
import com.eatsfine.eatsfine.domain.businesshours.entity.BusinessHours;
import com.eatsfine.eatsfine.domain.businesshours.validator.BusinessHoursValidator;
import com.eatsfine.eatsfine.domain.businessnumber.validator.BusinessNumberValidator;
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
import com.eatsfine.eatsfine.domain.store.validator.StoreValidator;
import com.eatsfine.eatsfine.domain.user.entity.User;
import com.eatsfine.eatsfine.domain.user.exception.UserException;
import com.eatsfine.eatsfine.domain.user.repository.UserRepository;
import com.eatsfine.eatsfine.domain.user.status.UserErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import com.eatsfine.eatsfine.global.s3.S3Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class StoreCommandServiceImpl implements StoreCommandService {

    private final StoreRepository storeRepository;
    private final RegionRepository regionRepository;
    private final S3Service s3Service;
    private final BusinessNumberValidator businessNumberValidator;
    private final StoreValidator storeValidator;
    private final UserRepository userRepository;

    // 가게 등록
    @Override
    public StoreResDto.StoreCreateDto createStore(StoreReqDto.StoreCreateDto dto, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(UserErrorStatus.MEMBER_NOT_FOUND));

        businessNumberValidator.validate(
                dto.businessNumberDto().businessNumber(),
                dto.businessNumberDto().startDate(),
                dto.businessNumberDto().name());

        log.info("사업자 번호 검증 성공: {}", dto.businessNumberDto().businessNumber());


        Region region = regionRepository.findBySidoAndSigunguAndBname(
                dto.sido(), dto.sigungu(), dto.bname()
                )
                .orElseThrow(() -> new StoreException(RegionErrorStatus._REGION_NOT_FOUND));

        // 영업시간 정상 여부 검증
        BusinessHoursValidator.validateForCreate(dto.businessHours());

        Store store = Store.builder()
                .owner(user)
                .storeName(dto.storeName())
                .businessNumber(dto.businessNumberDto().businessNumber())
                .description(dto.description())
                .address(dto.address())
                .mainImageKey(null) // 별도 API로 구현
                .region(region)
                .latitude(dto.latitude())
                .longitude(dto.longitude())
                .phoneNumber(dto.phoneNumber())
                .category(dto.category())
                .bookingIntervalMinutes(dto.bookingIntervalMinutes())
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
    public StoreResDto.StoreUpdateDto updateBasicInfo(Long storeId, StoreReqDto.StoreUpdateDto dto, String email) {
        Store store = storeValidator.validateStoreOwner(storeId, email);

        store.updateBasicInfo(dto);
        List<String> updatedFields = extractUpdatedFields(dto);

        return StoreConverter.toUpdateDto(storeId, updatedFields);
    }

    // 수정된 필드 목록
    public List<String> extractUpdatedFields(StoreReqDto.StoreUpdateDto dto) {
        List<String> updated = new ArrayList<>();

        // 검사할 필드 이름들을 리스트로 관리
        List<String> fieldsToTrack = List.of(
                "storeName", "description", "phoneNumber",
                "category", "depositRate", "bookingIntervalMinutes"
        );

        // 각 필드가 null이 아닌지 체크 (패턴 중복 제거)
        // DTO가 Record라면 accessor 메서드를 찾아서 체크.
        fieldsToTrack.forEach(fieldName -> {
            try {
                // Record의 필드 이름과 동일한 이름의 메서드를 호출하여 null 체크
                Object value = dto.getClass().getMethod(fieldName).invoke(dto);
                if (value != null) {
                    updated.add(fieldName);
                }
            } catch (Exception e) {
                log.error("필드 추출 중 에러 발생: {}", fieldName);
            }
        });

        return updated;
    }
    // 가게 메인 이미지 등록
    @Override
    public StoreResDto.UploadMainImageDto uploadMainImage(Long storeId, MultipartFile file, String email) {
        Store store = storeValidator.validateStoreOwner(storeId, email);

        if(file.isEmpty()) {
            throw new ImageException(ImageErrorStatus.EMPTY_FILE);
        }

        if(store.getMainImageKey() != null) {
            String oldKey = store.getMainImageKey();
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    s3Service.deleteByKey(oldKey);
                }
            });
        }

        String key = s3Service.upload(file, "stores/" + storeId + "/main");
        store.updateMainImageKey(key);

        String mainImageUrl = s3Service.toUrl(store.getMainImageKey());

        return StoreConverter.toUploadMainImageDto(store.getId(), mainImageUrl);
    }

}