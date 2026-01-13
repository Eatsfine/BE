package com.eatsfine.eatsfine.domain.store.service;

import com.eatsfine.eatsfine.domain.businesshours.entity.BusinessHours;
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

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreQueryServiceImpl implements StoreQueryService {

    private final StoreRepository storeRepository;

    // 식당 검색
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

        LocalDateTime now = LocalDateTime.now();

        List<StoreResDto.StoreSearchDto> stores =
                resultPage.getContent().stream()
                        .map(row -> StoreConverter.toSearchDto(
                                row.store(),
                                row.distance(),
                                isOpenNow(row.store(), now)
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

    // 식당 상세 조회
    @Override
    public StoreResDto.StoreDetailDto getStoreDetail(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(StoreErrorStatus._STORE_NOT_FOUND));

        return StoreConverter.toDetailDto(store, isOpenNow(store, LocalDateTime.now()));
    }

    // 현재 영업 여부 계산 (실시간 계산)
    @Override
    public boolean isOpenNow(Store store, LocalDateTime now) {
        DayOfWeek dayOfWeek = now.getDayOfWeek();
        LocalTime time = now.toLocalTime();

        BusinessHours bh = store.getBusinessHoursByDay(dayOfWeek);

        if(bh.isHoliday()) {
            return false;
        }

        if((bh.getBreakStartTime() != null && bh.getBreakEndTime() != null)) {
            if(!time.isBefore(bh.getBreakStartTime()) && time.isBefore(bh.getBreakEndTime())) { // start <= time < end 에 쉼
                return false;
            }
        }

        if(time.isBefore(bh.getOpenTime()) || !time.isBefore(bh.getCloseTime())) { // open <= time < end 일때만 true
            return false;
        }

        return true;
    }
}
