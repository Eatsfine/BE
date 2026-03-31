package com.eatsfine.domain.store.service;

import com.eatsfine.domain.store.condition.StoreSearchCondition;
import com.eatsfine.domain.store.dto.response.StoreResDto;
import com.eatsfine.domain.store.entity.Store;
import com.eatsfine.domain.store.enums.Category;
import com.eatsfine.domain.store.enums.StoreSortType;

import java.time.LocalDateTime;

public interface StoreQueryService {
    StoreResDto.StoreSearchResDto search(
            StoreSearchCondition cond,
            int page,
            int limit
    );

    StoreResDto.StoreDetailDto getStoreDetail(Long storeId);

    StoreResDto.GetMainImageDto getMainImage(Long storeId);

    boolean isOpenNow(Store store, LocalDateTime now);

    StoreResDto.MyStoreListDto getMyStores(String username);

}
