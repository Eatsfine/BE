package com.eatsfine.eatsfine.domain.businesshours.service;

import com.eatsfine.eatsfine.domain.booking.repository.BookingRepository;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BusinessHoursCommandServiceImpl implements BusinessHoursCommandService {

    private final StoreRepository storeRepository;
    private final StoreValidator storeValidator;
    private final BookingRepository bookingRepository;

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

        // 브레이크 타임 해제 요청인 경우 (두 시간 모두 null)
        if (dto.breakStartTime() == null && dto.breakEndTime() == null) {
            for(BusinessHours bh : store.getBusinessHours()) {
                bh.updateBreakTime(null, null, LocalDate.now());
            }
            return BusinessHoursConverter.toUpdateBreakTimeDto(storeId, dto, null);

        }

        LocalTime adjustedBreakStart = dto.breakStartTime().minusMinutes(store.getBookingIntervalMinutes());

        // 1. 예약 충돌 확인
        Optional<LocalDate> lastConflictDate = bookingRepository.findLastConflictingDate(
                storeId, dto.breakStartTime(), dto.breakEndTime(), adjustedBreakStart
        );
        LocalDate effectiveDate;

        effectiveDate = lastConflictDate.map(
                localDate -> localDate.plusDays(1)) // 예약이 있으면 그 다음날 부터
                .orElseGet(LocalDate::now); // 예약 없으면 오늘부터


        store.getBusinessHours().forEach(s -> {
            if(!s.isClosed()) {
                s.updateBreakTime(dto.breakStartTime(), dto.breakEndTime(), effectiveDate);
            }
        });

        return BusinessHoursConverter.toUpdateBreakTimeDto(storeId, dto, effectiveDate);
    }
}
