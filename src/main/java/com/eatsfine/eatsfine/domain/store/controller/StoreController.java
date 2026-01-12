package com.eatsfine.eatsfine.domain.store.controller;

import com.eatsfine.eatsfine.domain.store.dto.StoreResDto;
import com.eatsfine.eatsfine.domain.store.service.StoreDetailQueryService;
import com.eatsfine.eatsfine.domain.store.status.StoreSuccessStatus;
import com.eatsfine.eatsfine.global.apiPayload.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/stores")
public class StoreController {

    private final StoreDetailQueryService storeDetailQueryService;

    @GetMapping("/{storeId}")
    public ApiResponse<StoreResDto.StoreDetailDto> getStoreDetail(@PathVariable Long storeId) {
        return ApiResponse.of(StoreSuccessStatus._STORE_DETAIL_FOUND, storeDetailQueryService.getStoreDetail(storeId));
    }

}
