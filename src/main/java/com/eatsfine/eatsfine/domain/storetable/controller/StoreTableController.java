package com.eatsfine.eatsfine.domain.storetable.controller;

import com.eatsfine.eatsfine.domain.storetable.dto.req.StoreTableReqDto;
import com.eatsfine.eatsfine.domain.storetable.dto.res.StoreTableResDto;
import com.eatsfine.eatsfine.domain.storetable.exception.status.StoreTableSuccessStatus;
import com.eatsfine.eatsfine.domain.storetable.service.StoreTableCommandService;
import com.eatsfine.eatsfine.domain.storetable.service.StoreTableQueryService;
import com.eatsfine.eatsfine.domain.tableimage.status.TableImageSuccessStatus;
import com.eatsfine.eatsfine.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

import java.time.LocalDate;

@Tag(name = "StoreTable", description = "가게 테이블 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class StoreTableController implements StoreTableControllerDocs {
    private final StoreTableCommandService storeTableCommandService;
    private final StoreTableQueryService storeTableQueryService;

    @PostMapping("/stores/{storeId}/tables")
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<StoreTableResDto.TableCreateDto> createTable(
            @PathVariable Long storeId,
            @RequestBody StoreTableReqDto.TableCreateDto dto
    ) {
        return ApiResponse.of(StoreTableSuccessStatus._TABLE_CREATED, storeTableCommandService.createTable(storeId, dto));
    }

    @PostMapping(value = "/stores/{storeId}/tables/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<StoreTableResDto.ImageUploadDto> uploadTableImageTemp(
            @PathVariable Long storeId,
            @RequestPart("image") MultipartFile file
    ) {
        return ApiResponse.of(TableImageSuccessStatus._STORE_TABLE_IMAGE_UPLOAD_SUCCESS, storeTableCommandService.uploadTableImageTemp(storeId, file));
    }

    @GetMapping("/stores/{storeId}/tables/{tableId}/slots")
    public ApiResponse<StoreTableResDto.SlotListDto> getTableSlots(
            @PathVariable Long storeId,
            @PathVariable Long tableId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date
    ) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        return ApiResponse.of(StoreTableSuccessStatus._SLOT_LIST_FOUND, storeTableQueryService.getTableSlots(storeId, tableId, targetDate));
    }

    @GetMapping("/stores/{storeId}/tables/{tableId}")
    public ApiResponse<StoreTableResDto.TableDetailDto> getTableDetail(
            @PathVariable Long storeId,
            @PathVariable Long tableId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date
    ) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        return ApiResponse.of(StoreTableSuccessStatus._TABLE_DETAIL_FOUND, storeTableQueryService.getTableDetail(storeId, tableId, targetDate));
    }

    @PatchMapping("/stores/{storeId}/tables/{tableId}")
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<StoreTableResDto.TableUpdateResultDto> updateTable(
            @PathVariable Long storeId,
            @PathVariable Long tableId,
            @RequestBody @Valid StoreTableReqDto.TableUpdateDto dto
    ) {
        return ApiResponse.of(StoreTableSuccessStatus._TABLE_UPDATED, storeTableCommandService.updateTable(storeId, tableId, dto));
    }

    @DeleteMapping("/stores/{storeId}/tables/{tableId}")
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<StoreTableResDto.TableDeleteDto> deleteTable(
            @PathVariable Long storeId,
            @PathVariable Long tableId
    ) {
        return ApiResponse.of(StoreTableSuccessStatus._TABLE_DELETED, storeTableCommandService.deleteTable(storeId, tableId));
    }

    @PostMapping(
            value = "/stores/{storeId}/tables/{tableId}/table-image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<StoreTableResDto.UploadTableImageDto> uploadTableImage(
            @PathVariable Long storeId,
            @PathVariable Long tableId,
            @RequestPart("tableImage") MultipartFile tableImage
    ) {
        return ApiResponse.of(TableImageSuccessStatus._STORE_TABLE_IMAGE_UPLOAD_SUCCESS, storeTableCommandService.uploadTableImage(storeId, tableId, tableImage));
    }

    @DeleteMapping("/stores/{storeId}/tables/{tableId}/table-image")
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<StoreTableResDto.DeleteTableImageDto> deleteTableImage(
            @PathVariable Long storeId,
            @PathVariable Long tableId
    ) {
        return ApiResponse.of(TableImageSuccessStatus._STORE_TABLE_IMAGE_DELETE_SUCCESS, storeTableCommandService.deleteTableImage(storeId, tableId));
    }
}
