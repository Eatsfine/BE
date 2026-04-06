package com.eatsfine.domain.store.dto.projection;

import com.eatsfine.domain.store.entity.Store;

public record StoreSearchResult(
        Store store,
        Double distance
) {}
