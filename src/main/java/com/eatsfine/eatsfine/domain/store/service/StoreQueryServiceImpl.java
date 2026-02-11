package com.eatsfine.eatsfine.domain.store.service;

import com.eatsfine.eatsfine.domain.businesshours.entity.BusinessHours;
import com.eatsfine.eatsfine.domain.store.condition.StoreSearchCondition;
import com.eatsfine.eatsfine.domain.store.converter.StoreConverter;
import com.eatsfine.eatsfine.domain.store.dto.StoreResDto;
import com.eatsfine.eatsfine.domain.store.dto.projection.StoreSearchResult;
import com.eatsfine.eatsfine.domain.store.entity.Store;
import com.eatsfine.eatsfine.domain.store.exception.StoreException;
import com.eatsfine.eatsfine.domain.store.repository.StoreRepository;
import com.eatsfine.eatsfine.domain.store.status.StoreErrorStatus;
import com.eatsfine.eatsfine.global.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
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
                cond.getSido(), cond.getSigungu(), cond.getBname(), pageable
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
                                .isFirst(resultPage.isFirst())
                                .isLast(resultPage.isLast())
                                .build()
                )
                .build();
    }

    // 식당 상세 조회
    @Override
    public StoreResDto.StoreDetailDto getStoreDetail(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(StoreErrorStatus._STORE_NOT_FOUND));

        String mainImageUrl = s3Service.toUrl(store.getMainImageKey());
        return StoreConverter.toDetailDto(store, mainImageUrl, isOpenNow(store, LocalDateTime.now()));
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
        DayOfWeek today = now.getDayOfWeek();
        DayOfWeek yesterday = today.minus(1);

        LocalTime time = now.toLocalTime();

        // 1. 오늘 기준 영업 중인지 확인
        boolean openToday = store.findBusinessHoursByDay(today)
                .map(bh -> isEffectiveOpen(bh, time, true))
                .orElse(false);

        if (openToday) return true;

        // 2. 어제 시작된 심야 영업이 아직 종료되지 않았는지 확인
        return store.findBusinessHoursByDay(yesterday)
                .map(bh -> isEffectiveOpen(bh, time, false))
                .orElse(false);
    }

    private boolean isEffectiveOpen(BusinessHours bh, LocalTime time, boolean isToday) {
        LocalTime open = bh.getOpenTime();
        LocalTime close = bh.getCloseTime();

        if (bh.isClosed()) return false;

        // 브레이크 타임 체크 (어제 오픈한 가게의 새벽 브레이크 타임도 걸러내야 함)
        if(bh.getBreakStartTime() != null && bh.getBreakEndTime() != null) {
            if (!time.isBefore(bh.getBreakStartTime()) && time.isBefore(bh.getBreakEndTime())) {
                return false;
            }
        }

        // 24시간 영업 체크 (open == close)
        if(open.equals(close)) return true;

        // 영업 시간 체크
        if(open.isBefore(close)) {
            // 일반 영업 (예: 09:00 ~ 18:00)
            return isToday && (!time.isBefore(open) && time.isBefore(close));
        } else {
            // 심야 영업 (예: 23:00 ~ 02:00)
            if(isToday) {
                return !time.isBefore(open);
            } else {
                return time.isBefore(close);
            }
        }
    }
}
