package com.eatsfine.eatsfine.domain.store.service;

import com.eatsfine.eatsfine.domain.store.dto.StoreResDto;
import com.eatsfine.eatsfine.domain.store.enums.Category;
import com.eatsfine.eatsfine.domain.store.enums.StoreSortType;

public interface StoreQueryService {
    StoreResDto.StoreSearchResDto search(
            double lat,
            double lng,
            Double radius,
            Category category,
            StoreSortType sort,
            int page,
            int limit
    );

    StoreResDto.StoreDetailDto getStoreDetail(Long storeId);

}
