package com.eatsfine.eatsfine.domain.store.repository;

import com.eatsfine.eatsfine.domain.store.dto.projection.StoreSearchResult;
import com.eatsfine.eatsfine.domain.store.enums.Category;
import com.eatsfine.eatsfine.domain.store.enums.StoreSortType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StoreRepositoryCustom {
    Page<StoreSearchResult> searchStores(
            Double lat,
            Double lng,
            String keyword,
            Category category,
            StoreSortType sort,
            String sido,
            String sigungu,
            String bname,
            Pageable pageable
    );
}
