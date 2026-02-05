package com.eatsfine.eatsfine.domain.store.controller;

import com.eatsfine.eatsfine.domain.store.condition.StoreSearchCondition;
import com.eatsfine.eatsfine.domain.store.dto.StoreReqDto;
import com.eatsfine.eatsfine.domain.store.dto.StoreResDto;
import com.eatsfine.eatsfine.domain.store.service.StoreCommandService;
import com.eatsfine.eatsfine.domain.store.service.StoreQueryService;
import com.eatsfine.eatsfine.domain.store.status.StoreSuccessStatus;
import com.eatsfine.eatsfine.global.annotation.CurrentUser;
import com.eatsfine.eatsfine.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Store", description = "식당 조회 및 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class StoreController {

    private final StoreCommandService storeCommandService;
    private final StoreQueryService storeQueryService;

    @Operation(
            summary = "식당 등록",
            description = "사장 회원이 새로운 식당을 등록합니다"
    )
    @PostMapping("/stores")
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<StoreResDto.StoreCreateDto> createStore(
            @Valid @RequestBody StoreReqDto.StoreCreateDto dto,
            @CurrentUser User user
    ) {
        return ApiResponse.of(StoreSuccessStatus._STORE_CREATED, storeCommandService.createStore(dto, user.getUsername()));
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

    @Operation(
            summary = "가게 기본 정보 수정",
            description = "가게 기본 정보(영업시간, 브레이크타임 제외)를 수정합니다. " +
                    "영업시간, 브레이크타임, 이미지는 별도 엔티티/컬렉션이므로 개별 API로 분리"
    )
    @PatchMapping("/stores/{storeId}")
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<StoreResDto.StoreUpdateDto> updateStoreBasicInfo(
            @PathVariable Long storeId,
            @Valid @RequestBody StoreReqDto.StoreUpdateDto dto,
            @CurrentUser User user
            ) {
        return ApiResponse.of(StoreSuccessStatus._STORE_UPDATE_SUCCESS, storeCommandService.updateBasicInfo(storeId, dto, user.getUsername()));
    }


    @Operation(
        summary = "식당 대표 이미지 등록",
            description = "식당의 대표 이미지를 등록합니다."
    )
    @PostMapping(
            value = "/stores/{storeId}/main-image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<StoreResDto.UploadMainImageDto> uploadMainImage(
            @RequestPart("mainImage")MultipartFile mainImage,
            @PathVariable Long storeId,
            @CurrentUser User user
            ){
        return ApiResponse.of(StoreSuccessStatus._STORE_MAIN_IMAGE_UPLOAD_SUCCESS, storeCommandService.uploadMainImage(storeId, mainImage, user.getUsername()));
    }

    @Operation(
            summary = "식당 대표 이미지 조회",
            description = "식당의 대표 이미지를 조회합니다."
    )
    @GetMapping("/stores/{storeId}/main-image")
    public ApiResponse<StoreResDto.GetMainImageDto> getMainImage(
            @PathVariable Long storeId
    ) {
        return ApiResponse.of(StoreSuccessStatus._STORE_MAIN_IMAGE_GET_SUCCESS, storeQueryService.getMainImage(storeId));
    }

}
