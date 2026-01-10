package com.eatsfine.eatsfine.domain.store.service;

import com.eatsfine.eatsfine.domain.store.dto.StoreResDto;

public interface StoreDetailQueryService {
    public StoreResDto.StoreDetailDto getStoreDetail(Long storeId);

}
