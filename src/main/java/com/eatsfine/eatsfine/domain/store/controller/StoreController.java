package com.eatsfine.eatsfine.domain.store.controller;

import com.eatsfine.eatsfine.domain.store.dto.StoreReqDto;
import com.eatsfine.eatsfine.domain.store.dto.StoreResDto;
import com.eatsfine.eatsfine.domain.store.service.StoreCommandService;
import com.eatsfine.eatsfine.domain.store.service.StoreDetailQueryService;
import com.eatsfine.eatsfine.domain.store.status.StoreSuccessStatus;
import com.eatsfine.eatsfine.global.apiPayload.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/stores")
public class StoreController {

    private final StoreCommandService storeCommandService;
    private final StoreDetailQueryService storeDetailQueryService;

    // 식당 등록
    @PostMapping
    public ApiResponse<StoreResDto.StoreCreateDto> createStore(
            @RequestBody StoreReqDto.StoreCreateDto dto
    ) {
        return ApiResponse.of(StoreSuccessStatus._STORE_CREATED, storeCommandService.createStore(dto));
    }

    // 상세조회
    @GetMapping("/{storeId}")
    public ApiResponse<StoreResDto.StoreDetailDto> getStoreDetail(@PathVariable Long storeId) {
        return ApiResponse.of(StoreSuccessStatus._STORE_DETAIL_FOUND, storeDetailQueryService.getStoreDetail(storeId));
    }

}
