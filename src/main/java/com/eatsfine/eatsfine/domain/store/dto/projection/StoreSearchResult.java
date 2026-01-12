package com.eatsfine.eatsfine.domain.store.dto.projection;

import com.eatsfine.eatsfine.domain.store.entity.Store;

public record StoreSearchResult(
        Store store,
        Double distance
) {}
