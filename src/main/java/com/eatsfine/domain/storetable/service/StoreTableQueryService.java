package com.eatsfine.domain.storetable.service;

import com.eatsfine.domain.storetable.dto.response.StoreTableResDto;

import java.time.LocalDate;

public interface StoreTableQueryService {
    StoreTableResDto.SlotListDto getTableSlots(Long storeId, Long tableId, LocalDate date, String email);

    StoreTableResDto.TableDetailDto getTableDetail(Long storeId, Long tableId, LocalDate targetDate, String email);
}
