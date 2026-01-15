package com.eatsfine.eatsfine.domain.businesshours.service;

import com.eatsfine.eatsfine.domain.businesshours.converter.BusinessHoursConverter;
import com.eatsfine.eatsfine.domain.businesshours.dto.BusinessHoursReqDto;
import com.eatsfine.eatsfine.domain.businesshours.dto.BusinessHoursResDto;
import com.eatsfine.eatsfine.domain.businesshours.entity.BusinessHours;
import com.eatsfine.eatsfine.domain.businesshours.validator.BusinessHoursValidator;
import com.eatsfine.eatsfine.domain.store.entity.Store;
import com.eatsfine.eatsfine.domain.store.exception.StoreException;
import com.eatsfine.eatsfine.domain.store.repository.StoreRepository;
import com.eatsfine.eatsfine.domain.store.status.StoreErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class BusinessHoursCommandServiceImpl implements BusinessHoursCommandService {

    StoreRepository storeRepository;

    @Override
    public BusinessHoursResDto.UpdateBusinessHoursDto updateBusinessHours(
            Long storeId,
            BusinessHoursReqDto.UpdateBusinessHoursDto dto
    ) {
        // 영업시간 검증
        BusinessHoursValidator.validateForUpdate(dto.businessHours());

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(StoreErrorStatus._STORE_NOT_FOUND));

        List<String> updatedDays = new ArrayList<>();

        dto.businessHours().forEach(s -> {
            store.updateBusinessHours(
                    s.dayOfWeek(),
                    s.openTime(),
                    s.closeTime(),
                    s.isClosed()
            );
            updatedDays.add(s.dayOfWeek().toString());
        });

        return BusinessHoursConverter.toUpdateBusinessHoursDto(storeId, updatedDays);
    }
}
