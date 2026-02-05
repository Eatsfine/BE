package com.eatsfine.eatsfine.domain.tableimage.controller;

import com.eatsfine.eatsfine.domain.tableimage.dto.TableImageResDto;
import com.eatsfine.eatsfine.domain.tableimage.service.TableImageCommandService;
import com.eatsfine.eatsfine.domain.tableimage.service.TableImageQueryService;
import com.eatsfine.eatsfine.domain.tableimage.status.TableImageSuccessStatus;
import com.eatsfine.eatsfine.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "TableImage", description = "테이블 이미지 조회 및 관리 API")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TableImageController {

    private final TableImageCommandService tableImageCommandService;
    private final TableImageQueryService tableImageQueryService;

    @Operation(
            summary = "식당 테이블 이미지 등록",
            description = "식당 테이블 이미지들을 등록합니다."
    )
    @PostMapping(
            value = "/stores/{storeId}/table-images",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasRole('OWNER')")
    ApiResponse<TableImageResDto.UploadTableImageDto> uploadTableImage(
            @RequestPart("file") List<MultipartFile> files,
            @PathVariable Long storeId
    ) {
        return ApiResponse.of(
                TableImageSuccessStatus._STORE_TABLE_IMAGE_UPLOAD_SUCCESS,
                tableImageCommandService.uploadTableImage(storeId, files)
        );
    }

    @Operation(
            summary = "식당 테이블 이미지 조회",
            description = "식당 테이블 이미지들을 조회합니다."
    )
    @GetMapping("/stores/{storeId}/table-images")
    ApiResponse<TableImageResDto.GetTableImageDto> getTableImage(
            @PathVariable Long storeId
    ) {
        return ApiResponse.of(TableImageSuccessStatus._STORE_TABLE_IMAGE_GET_SUCCESS, tableImageQueryService.getTableImage(storeId));
    }

    @Operation(
            summary = "식당 테이블 이미지 삭제",
            description = "식당 테이블 이미지를 삭제합니다."
    )
    @DeleteMapping("/stores/{storeId}/table-images")
    @PreAuthorize("hasRole('OWNER')")
    ApiResponse<TableImageResDto.DeleteTableImageDto> deleteTableImage(
            @PathVariable Long storeId,
            @RequestBody List<Long> tableImageIds
    ) {
        return ApiResponse.of(TableImageSuccessStatus._STORE_TABLE_IMAGE_DELETE_SUCCESS, tableImageCommandService.deleteTableImage(storeId, tableImageIds));
    }


}
