package com.eatsfine.eatsfine.domain.menu.controller;

import com.eatsfine.eatsfine.domain.menu.dto.MenuReqDto;
import com.eatsfine.eatsfine.domain.menu.dto.MenuResDto;
import com.eatsfine.eatsfine.domain.menu.service.MenuCommandService;
import com.eatsfine.eatsfine.domain.menu.service.MenuQueryService;
import com.eatsfine.eatsfine.domain.menu.status.MenuSuccessStatus;
import com.eatsfine.eatsfine.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Menu", description = "가게 메뉴 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class MenuController {

    private final MenuCommandService menuCommandService;
    private final MenuQueryService menuQueryService;

    @Operation(summary = "메뉴 이미지 선 업로드 API", description = "메뉴 등록 전에 이미지를 먼저 업로드하고 KEY를 반환합니다.")
    @PostMapping(value = "/stores/{storeId}/menus/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<MenuResDto.ImageUploadDto> uploadImage(
            @PathVariable Long storeId,
            @RequestPart("image") MultipartFile file
    ){
        return ApiResponse.of(MenuSuccessStatus._MENU_IMAGE_UPLOAD_SUCCESS, menuCommandService.uploadImage(storeId, file));
    }


    @Operation(summary = "메뉴 이미지 삭제 API", description = """ 
            메뉴 이미지를 삭제합니다. 이 API는 아래 두가지 시나리오 모두 처리합니다.
            1. 메뉴 등록 전: 메뉴 등록하다가 업로드만 된 이미지(고아 이미지)를 S3에서 삭제합니다. (가게 등록 전 이미지를 먼저 업로드하기 때문)
            2. 메뉴 등록 후: DB에 연결된 메뉴의 이미지를 S3에서 삭제하고, DB의 imageKey도 null로 업데이트합니다.
            
            """)
    @DeleteMapping("/stores/{storeId}/menus/images")
    public ApiResponse<MenuResDto.ImageDeleteDto> deleteImage(
            @PathVariable Long storeId,
            @RequestParam("key") String imageKey
    ) {

        return ApiResponse.of(MenuSuccessStatus._MENU_IMAGE_DELETE_SUCCESS, menuCommandService.deleteImage(storeId,imageKey));
    }

    @Operation(summary = "메뉴 등록 API", description = "가게의 메뉴들을 등록합니다.")
    @PostMapping("/stores/{storeId}/menus")
    public ApiResponse<MenuResDto.MenuCreateDto> createMenus(
            @PathVariable Long storeId,
            @RequestBody @Valid MenuReqDto.MenuCreateDto dto
    ) {
        return ApiResponse.of(MenuSuccessStatus._MENU_CREATE_SUCCESS, menuCommandService.createMenus(storeId, dto));
    }

    @Operation(summary = "메뉴 삭제 API", description = "가게의 메뉴들을 삭제합니다.")
    @DeleteMapping("/stores/{storeId}/menus")
    public ApiResponse<MenuResDto.MenuDeleteDto> deleteMenus(
            @PathVariable Long storeId,
            @RequestBody @Valid MenuReqDto.MenuDeleteDto dto
    ) {
        return ApiResponse.of(MenuSuccessStatus._MENU_DELETE_SUCCESS, menuCommandService.deleteMenus(storeId, dto));
    }

    @Operation(summary = "메뉴 수정 API", description = "가게의 메뉴를 수정합니다.")
    @PatchMapping("/stores/{storeId}/menus/{menuId}")
    public ApiResponse<MenuResDto.MenuUpdateDto> updateMenu(
            @PathVariable Long storeId,
            @PathVariable Long menuId,
            @RequestBody @Valid MenuReqDto.MenuUpdateDto dto
    ) {
        return ApiResponse.of(MenuSuccessStatus._MENU_UPDATE_SUCCESS, menuCommandService.updateMenu(storeId, menuId, dto));
    }

    @Operation(summary = "메뉴 조회 API", description = "가게의 메뉴들을 조회합니다.")
    @GetMapping("/stores/{storeId}/menus")
    public ApiResponse<MenuResDto.MenuListDto> getMenus(
            @PathVariable Long storeId
    ) {
        return ApiResponse.of(MenuSuccessStatus._MENU_LIST_SUCCESS, menuQueryService.getMenus(storeId));

    }
}