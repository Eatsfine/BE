package com.eatsfine.eatsfine.domain.store.service;

import com.eatsfine.eatsfine.domain.businesshours.entity.BusinessHours;
import com.eatsfine.eatsfine.domain.booking.repository.BookingRepository;
import com.eatsfine.eatsfine.domain.store.condition.StoreSearchCondition;
import com.eatsfine.eatsfine.domain.store.converter.StoreConverter;
import com.eatsfine.eatsfine.domain.store.dto.StoreResDto;
import com.eatsfine.eatsfine.domain.store.dto.projection.StoreSearchResult;
import com.eatsfine.eatsfine.domain.store.entity.Store;
import com.eatsfine.eatsfine.domain.store.exception.StoreException;
import com.eatsfine.eatsfine.domain.store.repository.StoreRepository;
import com.eatsfine.eatsfine.domain.store.status.StoreErrorStatus;
import com.eatsfine.eatsfine.domain.user.entity.User;
import com.eatsfine.eatsfine.domain.user.exception.UserException;
import com.eatsfine.eatsfine.domain.user.repository.UserRepository;
import com.eatsfine.eatsfine.domain.user.status.UserErrorStatus;
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
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
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
        if (bh.isClosed()) return false;

        LocalTime open = bh.getOpenTime();
        LocalTime close = bh.getCloseTime();

        boolean isWithinBusinessHours;

        // 1. 영업 시간 범위 먼저 체크
        if (open.equals(close)) {
            isWithinBusinessHours = isToday;
            // 24시간 영업
        } else if (open.isBefore(close)) {
            // 일반 영업 (예: 09:00 ~ 18:00)
            isWithinBusinessHours = isToday && (!time.isBefore(open) && time.isBefore(close));
        } else {
            // 심야 영업 (예: 22:00 ~ 03:00)
            isWithinBusinessHours = isToday ? !time.isBefore(open) : time.isBefore(close);
        }

        // 2. 영업 시간일 경우에만 브레이크 타임 검사
        if (isWithinBusinessHours) {
            if (bh.getBreakStartTime() != null && bh.getBreakEndTime() != null) {
                // 브레이크 타임 안에 있으면 false (영업 아님) 반환
                if (!time.isBefore(bh.getBreakStartTime()) && time.isBefore(bh.getBreakEndTime())) {
                    return false;
                }
            }
            return true; // 영업 시간이고 브레이크 타임도 아님
        }

        return false; // 영업 시간 자체가 아님
    }

    @Override
    public StoreResDto.MyStoreListDto getMyStores(String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UserException(UserErrorStatus.MEMBER_NOT_FOUND));

        List<Store> myStores = storeRepository.findAllByOwner(user);
        LocalDateTime now = LocalDateTime.now();

        List<StoreResDto.MyStoreDto> storeDtos = myStores.stream()
                .map(store -> {
                    boolean isOpen = isOpenNow(store, now);
                    Long totalBookingCount = bookingRepository.countActiveBookings(store);
                    String mainImageUrl = s3Service.toUrl(store.getMainImageKey());
                    return StoreConverter.toMyStoreDto(store, isOpen, totalBookingCount, mainImageUrl);
                })
                .toList();

        return StoreConverter.toMyStoreListDto(storeDtos);
    }
}
