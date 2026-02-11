package com.eatsfine.eatsfine.domain.businesshours.service;

import com.eatsfine.eatsfine.domain.businesshours.converter.BusinessHoursConverter;
import com.eatsfine.eatsfine.domain.businesshours.dto.BusinessHoursReqDto;
import com.eatsfine.eatsfine.domain.businesshours.dto.BusinessHoursResDto;
import com.eatsfine.eatsfine.domain.businesshours.entity.BusinessHours;
import com.eatsfine.eatsfine.domain.businesshours.exception.BusinessHoursException;
import com.eatsfine.eatsfine.domain.businesshours.validator.BreakTimeValidator;
import com.eatsfine.eatsfine.domain.businesshours.validator.BusinessHoursValidator;
import com.eatsfine.eatsfine.domain.store.entity.Store;
import com.eatsfine.eatsfine.domain.store.exception.StoreException;
import com.eatsfine.eatsfine.domain.store.repository.StoreRepository;
import com.eatsfine.eatsfine.domain.store.status.StoreErrorStatus;
import com.eatsfine.eatsfine.domain.store.validator.StoreValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BusinessHoursCommandServiceImpl implements BusinessHoursCommandService {

    private final StoreRepository storeRepository;
    private final StoreValidator storeValidator;

    @Override
    public BusinessHoursResDto.UpdateBusinessHoursDto updateBusinessHours(
            Long storeId,
            BusinessHoursReqDto.UpdateBusinessHoursDto dto,
            String email
    ) {

        storeValidator.validateStoreOwner(storeId, email);

        // 영업시간 검증
        BusinessHoursValidator.validateForUpdate(dto.businessHours());

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(StoreErrorStatus._STORE_NOT_FOUND));

        dto.businessHours().forEach(s -> {
            store.updateBusinessHours(
                    s.day(),
                    s.openTime(),
                    s.closeTime(),
                    s.isClosed()
            );
        });

        return BusinessHoursConverter.toUpdateBusinessHoursDto(storeId, store.getBusinessHours());
    }

    @Override
    public BusinessHoursResDto.UpdateBreakTimeDto updateBreakTime(
            Long storeId,
            BusinessHoursReqDto.UpdateBreakTimeDto dto,
            String email
    ) {

        Store store = storeValidator.validateStoreOwner(storeId, email);

        for(BusinessHours bh : store.getBusinessHours()) {
            if(bh.isClosed()) continue;
            try {
                BreakTimeValidator.validateBreakTime(bh.getOpenTime(), bh.getCloseTime(), dto.breakStartTime(), dto.breakEndTime());
            } catch (BusinessHoursException e) {
                log.error("브레이크 타임 검증 실패 - 요일: {}, 영업시간: {}~{}, 브레이크: {}~{}",
                        bh.getDayOfWeek(), bh.getOpenTime(), bh.getCloseTime(),
                        dto.breakStartTime(), dto.breakEndTime());
                throw e;
            }
        }

        store.getBusinessHours().forEach(s -> {
            if(!s.isClosed()) {
                s.updateBreakTime(dto.breakStartTime(), dto.breakEndTime());
            }
        });

        return BusinessHoursConverter.toUpdateBreakTimeDto(storeId, dto);
    }
}
