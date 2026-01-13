package com.eatsfine.eatsfine.domain.store.controller;

import com.eatsfine.eatsfine.domain.store.dto.StoreReqDto;
import com.eatsfine.eatsfine.domain.store.dto.StoreResDto;
import com.eatsfine.eatsfine.domain.store.enums.Category;
import com.eatsfine.eatsfine.domain.store.enums.StoreSortType;
import com.eatsfine.eatsfine.domain.store.service.StoreCommandService;
import com.eatsfine.eatsfine.domain.store.service.StoreQueryService;
import com.eatsfine.eatsfine.domain.store.status.StoreSuccessStatus;
import com.eatsfine.eatsfine.global.apiPayload.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/stores")
public class StoreController {

    private final StoreCommandService storeCommandService;
    private final StoreQueryService storeQueryService;

    // 식당 등록
    @PostMapping
    public ApiResponse<StoreResDto.StoreCreateDto> createStore(
            @RequestBody StoreReqDto.StoreCreateDto dto
    ) {
        return ApiResponse.of(StoreSuccessStatus._STORE_CREATED, storeCommandService.createStore(dto));
    }

    // 식당 검색
    @GetMapping("/search")
    public ApiResponse<StoreResDto.StoreSearchResDto> searchStore(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(required = false, defaultValue = "5") Double radius,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false, defaultValue = "DISTANCE") StoreSortType sort,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
            ) {
        return ApiResponse.of(StoreSuccessStatus._STORE_SEARCH_SUCCESS,
                storeQueryService.search(lat, lng, radius, category, sort, page, limit));
    }

    // 상세조회
    @GetMapping("/{storeId}")
    public ApiResponse<StoreResDto.StoreDetailDto> getStoreDetail(@PathVariable Long storeId) {
        return ApiResponse.of(StoreSuccessStatus._STORE_DETAIL_FOUND, storeQueryService.getStoreDetail(storeId));
    }

}
