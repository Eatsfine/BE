package com.eatsfine.eatsfine.domain.businesshours.service;

import com.eatsfine.eatsfine.domain.businesshours.converter.BusinessHoursConverter;
import com.eatsfine.eatsfine.domain.businesshours.dto.BusinessHoursReqDto;
import com.eatsfine.eatsfine.domain.businesshours.dto.BusinessHoursResDto;
import com.eatsfine.eatsfine.domain.businesshours.entity.BusinessHours;
import com.eatsfine.eatsfine.domain.businesshours.validator.BreakTimeValidator;
import com.eatsfine.eatsfine.domain.businesshours.validator.BusinessHoursValidator;
import com.eatsfine.eatsfine.domain.store.entity.Store;
import com.eatsfine.eatsfine.domain.store.exception.StoreException;
import com.eatsfine.eatsfine.domain.store.repository.StoreRepository;
import com.eatsfine.eatsfine.domain.store.status.StoreErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class BusinessHoursCommandServiceImpl implements BusinessHoursCommandService {

    private final StoreRepository storeRepository;

    @Override
    public BusinessHoursResDto.UpdateBusinessHoursDto updateBusinessHours(
            Long storeId,
            BusinessHoursReqDto.UpdateBusinessHoursDto dto
    ) {
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
            BusinessHoursReqDto.UpdateBreakTimeDto dto
    ) {
        Store store = storeRepository.findById(storeId)
                        .orElseThrow(() -> new StoreException(StoreErrorStatus._STORE_NOT_FOUND));

        for(BusinessHours bh : store.getBusinessHours()) {
            if(bh.isClosed()) continue;
            BreakTimeValidator.validateBreakTime(bh.getOpenTime(), bh.getCloseTime(), dto.breakStartTime(), dto.breakEndTime());
        }

        store.getBusinessHours().forEach(s -> {
            if(!s.isClosed()) {
                s.updateBreakTime(dto.breakStartTime(), dto.breakEndTime());
            }
        });

        return BusinessHoursConverter.toUpdateBreakTimeDto(storeId, dto);
    }
}
