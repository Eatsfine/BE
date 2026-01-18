package com.eatsfine.eatsfine.domain.store.service;

import com.eatsfine.eatsfine.domain.store.condition.StoreSearchCondition;
import com.eatsfine.eatsfine.domain.store.converter.StoreConverter;
import com.eatsfine.eatsfine.domain.store.dto.StoreResDto;
import com.eatsfine.eatsfine.domain.store.dto.projection.StoreSearchResult;
import com.eatsfine.eatsfine.domain.store.entity.Store;
import com.eatsfine.eatsfine.domain.store.enums.Category;
import com.eatsfine.eatsfine.domain.store.enums.StoreSortType;
import com.eatsfine.eatsfine.domain.store.exception.StoreException;
import com.eatsfine.eatsfine.domain.store.repository.StoreRepository;
import com.eatsfine.eatsfine.domain.store.status.StoreErrorStatus;
import com.eatsfine.eatsfine.global.s3.S3Service;
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
    private final S3Service s3Service;

    // 식당 검색
    @Override
    public StoreResDto.StoreSearchResDto search(
            StoreSearchCondition cond,
            int page,
            int limit
    ) {
        Pageable pageable = PageRequest.of(page - 1, limit);

        Page<StoreSearchResult> resultPage = storeRepository.searchStores(
                cond.getLat(), cond.getLng(), cond.getKeyword(), cond.getCategory(), cond.getSort(),
                cond.getProvince(), cond.getCity(), cond.getDistrict(), pageable
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

    // 식당 대표 이미지 조회
    @Override
    public StoreResDto.GetMainImageDto getMainImage(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(StoreErrorStatus._STORE_NOT_FOUND));

        return StoreConverter.toGetMainImageDto(storeId, s3Service.toUrl(store.getMainImageKey()));
    }

    // 현재 영업 여부 계산 (실시간 계산)
    @Override
    public boolean isOpenNow(Store store, LocalDateTime now) {
        DayOfWeek dayOfWeek = now.getDayOfWeek();
        LocalTime time = now.toLocalTime();

        return store.findBusinessHoursByDay(dayOfWeek)
                .map(bh -> {
                    if (bh.isClosed()) return false;

                    if ((bh.getBreakStartTime() != null && bh.getBreakEndTime() != null)) {
                        if (!time.isBefore(bh.getBreakStartTime()) && (time.isBefore(bh.getBreakEndTime()))) {
                            return false; // start <= time < end 에 쉼
                        }
                    }
                    return (!time.isBefore(bh.getOpenTime()) && time.isBefore(bh.getCloseTime()));

                }).orElse(false); // 현재 요일에 해당하는 영업시간 없으면 닫힘처리
    }
}
