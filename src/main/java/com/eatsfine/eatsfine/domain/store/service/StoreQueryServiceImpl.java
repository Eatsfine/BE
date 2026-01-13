package com.eatsfine.eatsfine.domain.store.service;

import com.eatsfine.eatsfine.domain.store.converter.StoreConverter;
import com.eatsfine.eatsfine.domain.store.dto.StoreResDto;
import com.eatsfine.eatsfine.domain.store.dto.projection.StoreSearchResult;
import com.eatsfine.eatsfine.domain.store.entity.Store;
import com.eatsfine.eatsfine.domain.store.enums.Category;
import com.eatsfine.eatsfine.domain.store.enums.StoreSortType;
import com.eatsfine.eatsfine.domain.store.exception.StoreException;
import com.eatsfine.eatsfine.domain.store.repository.StoreRepository;
import com.eatsfine.eatsfine.domain.store.status.StoreErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreQueryServiceImpl implements StoreQueryService {

    private final StoreRepository storeRepository;

    @Override
    public StoreResDto.StoreSearchResDto search(
            double lat,
            double lng,
            Double radius,
            Category category,
            StoreSortType sort,
            int page,
            int limit
    ) {
        Pageable pageable = PageRequest.of(page - 1, limit);

        Page<StoreSearchResult> resultPage = storeRepository.searchStores(
                lat, lng, radius, category, sort, pageable
        );

        List<StoreResDto.StoreSearchDto> stores =
                resultPage.getContent().stream()
                        .map(row -> StoreConverter.toSearchDto(
                                row.store(),
                                row.distance()
                        ))
                        .toList();

        return StoreResDto.StoreSearchResDto.builder()
                .stores(stores)
                .pagination(
                        StoreResDto.PaginationDto.builder()
                                .currentPage(page)
                                .totalPages(resultPage.getTotalPages())
                                .totalCount(resultPage.getTotalElements())
                                .build()
                )
                .build();
    }

    @Override
    public StoreResDto.StoreDetailDto getStoreDetail(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(StoreErrorStatus._STORE_NOT_FOUND));

        return StoreConverter.toDetailDto(store);
    }
}
