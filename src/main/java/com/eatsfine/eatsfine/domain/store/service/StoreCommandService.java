package com.eatsfine.eatsfine.domain.store.service;

import com.eatsfine.eatsfine.domain.store.dto.StoreReqDto;
import com.eatsfine.eatsfine.domain.store.dto.StoreResDto;

public interface StoreCommandService {
    StoreResDto.StoreCreateDto createStore(StoreReqDto.StoreCreateDto storeCreateDto);
    StoreResDto.StoreUpdateDto updateBasicInfo(Long storeId, StoreReqDto.StoreUpdateDto storeUpdateDto);
}
