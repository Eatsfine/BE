package com.eatsfine.eatsfine.domain.storetable.service;

import com.eatsfine.eatsfine.domain.storetable.dto.res.StoreTableResDto;

import java.time.LocalDate;

public interface StoreTableQueryService {
    StoreTableResDto.SlotListDto getTableSlots(Long storeId, Long tableId, LocalDate date);
}
