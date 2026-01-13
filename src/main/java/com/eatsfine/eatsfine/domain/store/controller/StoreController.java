package com.eatsfine.eatsfine.domain.store.controller;

import com.eatsfine.eatsfine.domain.store.dto.StoreReqDto;
import com.eatsfine.eatsfine.domain.store.dto.StoreResDto;
import com.eatsfine.eatsfine.domain.store.enums.Category;
import com.eatsfine.eatsfine.domain.store.enums.StoreSortType;
import com.eatsfine.eatsfine.domain.store.service.StoreCommandService;
import com.eatsfine.eatsfine.domain.store.service.StoreQueryService;
import com.eatsfine.eatsfine.domain.store.status.StoreSuccessStatus;
import com.eatsfine.eatsfine.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Store", description = "식당 조회 및 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/stores")
public class StoreController {

    private final StoreCommandService storeCommandService;
    private final StoreQueryService storeQueryService;

    @Operation(
            summary = "식당 등록",
            description = "사장 회원이 새로운 식당을 등록합니다. 등록 후 승인 상태는 PENDING입니다."
    )
    @PostMapping
    public ApiResponse<StoreResDto.StoreCreateDto> createStore(
            @Valid @RequestBody StoreReqDto.StoreCreateDto dto
    ) {
        return ApiResponse.of(StoreSuccessStatus._STORE_CREATED, storeCommandService.createStore(dto));
    }

    @Operation(
            summary = "식당 검색",
            description = "위치 기반으로 반경 내 식당을 검색합니다."
    )
    @GetMapping("/search")
    public ApiResponse<StoreResDto.StoreSearchResDto> searchStore(
            @Parameter(description = "위도", example = "37.5665")
            @RequestParam double lat,

            @Parameter(description = "경도", example = "127.9740")
            @RequestParam double lng,

            @Parameter(description = "검색 반경 (km)", example = "1.0")
            @RequestParam(required = false, defaultValue = "5") Double radius,

            @Parameter(description = "카테고리")
            @RequestParam(required = false) Category category,

            @Parameter(description = "정렬 기준", example = "DISTANCE")
            @RequestParam(required = false, defaultValue = "DISTANCE") StoreSortType sort,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
            ) {
        return ApiResponse.of(StoreSuccessStatus._STORE_SEARCH_SUCCESS,
                storeQueryService.search(lat, lng, radius, category, sort, page, limit));
    }

    @Operation(
            summary = "식당 상세 조회",
            description = "식당 ID로 상세 정보를 조회합니다."
    )
    @GetMapping("/{storeId}")
    public ApiResponse<StoreResDto.StoreDetailDto> getStoreDetail(@PathVariable Long storeId) {
        return ApiResponse.of(StoreSuccessStatus._STORE_DETAIL_FOUND, storeQueryService.getStoreDetail(storeId));
    }

}
