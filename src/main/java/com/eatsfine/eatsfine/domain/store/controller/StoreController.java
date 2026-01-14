package com.eatsfine.eatsfine.domain.store.controller;

import com.eatsfine.eatsfine.domain.store.condition.StoreSearchCondition;
import com.eatsfine.eatsfine.domain.store.dto.StoreReqDto;
import com.eatsfine.eatsfine.domain.store.dto.StoreResDto;
import com.eatsfine.eatsfine.domain.store.service.StoreCommandService;
import com.eatsfine.eatsfine.domain.store.service.StoreQueryService;
import com.eatsfine.eatsfine.domain.store.status.StoreSuccessStatus;
import com.eatsfine.eatsfine.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Store", description = "식당 조회 및 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class StoreController {

    private final StoreCommandService storeCommandService;
    private final StoreQueryService storeQueryService;

    @Operation(
            summary = "식당 등록",
            description = "사장 회원이 새로운 식당을 등록합니다. 등록 후 승인 상태는 PENDING입니다."
    )
    @PostMapping("/stores")
    public ApiResponse<StoreResDto.StoreCreateDto> createStore(
            @Valid @RequestBody StoreReqDto.StoreCreateDto dto
    ) {
        return ApiResponse.of(StoreSuccessStatus._STORE_CREATED, storeCommandService.createStore(dto));
    }

    @Operation(
            summary = "식당 검색",
            description = "위치 기반으로 반경 내 식당을 검색합니다."
    )
    @GetMapping("/stores/search")
    public ApiResponse<StoreResDto.StoreSearchResDto> searchStore(
            @Valid @ParameterObject @ModelAttribute StoreSearchCondition cond,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
            ) {
        return ApiResponse.of(StoreSuccessStatus._STORE_SEARCH_SUCCESS,
                storeQueryService.search(cond, page, limit));
    }

    @Operation(
            summary = "식당 상세 조회",
            description = "식당 ID로 상세 정보를 조회합니다."
    )
    @GetMapping("/stores/{storeId}")
    public ApiResponse<StoreResDto.StoreDetailDto> getStoreDetail(@PathVariable Long storeId) {
        return ApiResponse.of(StoreSuccessStatus._STORE_DETAIL_FOUND, storeQueryService.getStoreDetail(storeId));
    }

}
