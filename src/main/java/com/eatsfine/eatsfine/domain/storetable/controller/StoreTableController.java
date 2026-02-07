package com.eatsfine.eatsfine.domain.storetable.controller;

import com.eatsfine.eatsfine.domain.booking.dto.response.BookingResponseDTO;
import com.eatsfine.eatsfine.domain.booking.service.BookingCommandService;
import com.eatsfine.eatsfine.domain.booking.service.BookingQueryService;
import com.eatsfine.eatsfine.domain.booking.status.BookingSuccessStatus;
import com.eatsfine.eatsfine.domain.storetable.dto.req.StoreTableReqDto;
import com.eatsfine.eatsfine.domain.storetable.dto.res.StoreTableResDto;
import com.eatsfine.eatsfine.domain.storetable.exception.status.StoreTableSuccessStatus;
import com.eatsfine.eatsfine.domain.storetable.service.StoreTableCommandService;
import com.eatsfine.eatsfine.domain.storetable.service.StoreTableQueryService;
import com.eatsfine.eatsfine.domain.tableimage.status.TableImageSuccessStatus;
import com.eatsfine.eatsfine.global.annotation.CurrentUser;
import com.eatsfine.eatsfine.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;


@Tag(name = "StoreTable", description = "가게 테이블 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class StoreTableController implements StoreTableControllerDocs {
    private final StoreTableCommandService storeTableCommandService;
    private final StoreTableQueryService storeTableQueryService;
    private final BookingQueryService bookingQueryService;
    private final BookingCommandService bookingCommandService;

    @PostMapping("/stores/{storeId}/tables")
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<StoreTableResDto.TableCreateDto> createTable(
            @PathVariable Long storeId,
            @RequestBody @Valid StoreTableReqDto.TableCreateDto dto,
            @CurrentUser User user
            ) {
        return ApiResponse.of(StoreTableSuccessStatus._TABLE_CREATED, storeTableCommandService.createTable(storeId, dto, user.getUsername()));
    }

    @PostMapping(value = "/stores/{storeId}/tables/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<StoreTableResDto.ImageUploadDto> uploadTableImageTemp(
            @PathVariable Long storeId,
            @RequestPart("image") MultipartFile file,
            @CurrentUser User user
    ) {
        return ApiResponse.of(TableImageSuccessStatus._STORE_TABLE_IMAGE_UPLOAD_SUCCESS, storeTableCommandService.uploadTableImageTemp(storeId, file, user.getUsername()));
    }

    @GetMapping("/stores/{storeId}/tables/{tableId}/slots")
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<StoreTableResDto.SlotListDto> getTableSlots(
            @PathVariable Long storeId,
            @PathVariable Long tableId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @CurrentUser User user
    ) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        return ApiResponse.of(StoreTableSuccessStatus._SLOT_LIST_FOUND, storeTableQueryService.getTableSlots(storeId, tableId, targetDate, user.getUsername()));
    }

    // 사장이 특정 슬롯(예약)의 상세 정보 조회
    @GetMapping("/stores/{storeId}/tables/{tableId}/slots/{bookingId}")
    public ApiResponse<BookingResponseDTO.BookingDetailDTO> getTableSlotDetail(
            @PathVariable Long storeId,
            @PathVariable Long tableId,
            @PathVariable Long bookingId) {

        return ApiResponse.of(StoreTableSuccessStatus._TABLE_BOOKING_FOUND
                ,bookingQueryService.getBookingDetail(storeId,tableId,bookingId));
    }

    // 사장이 특정 테이블 슬롯의 예약 취소
    @PatchMapping("/stores/{storeId}/tables/{tableId}/slots/{bookingId}/cancel")
    public ApiResponse<BookingResponseDTO.OwnerCancelBookingResultDTO> cancelTableSlotBooking(
            @PathVariable Long storeId,
            @PathVariable Long tableId,
            @PathVariable Long bookingId
            ) {

        return ApiResponse.of(StoreTableSuccessStatus._TABLE_CANCELLED,bookingCommandService.cancelBookingByOwner(storeId, tableId, bookingId));
    }

    @GetMapping("/stores/{storeId}/tables/{tableId}")
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<StoreTableResDto.TableDetailDto> getTableDetail(
            @PathVariable Long storeId,
            @PathVariable Long tableId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @CurrentUser User user
    ) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        return ApiResponse.of(StoreTableSuccessStatus._TABLE_DETAIL_FOUND, storeTableQueryService.getTableDetail(storeId, tableId, targetDate, user.getUsername()));
    }

    @PatchMapping("/stores/{storeId}/tables/{tableId}")
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<StoreTableResDto.TableUpdateResultDto> updateTable(
            @PathVariable Long storeId,
            @PathVariable Long tableId,
            @RequestBody @Valid StoreTableReqDto.TableUpdateDto dto,
            @CurrentUser User user
    ) {
        return ApiResponse.of(StoreTableSuccessStatus._TABLE_UPDATED, storeTableCommandService.updateTable(storeId, tableId, dto, user.getUsername()));
    }

    @DeleteMapping("/stores/{storeId}/tables/{tableId}")
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<StoreTableResDto.TableDeleteDto> deleteTable(
            @PathVariable Long storeId,
            @PathVariable Long tableId,
            @CurrentUser User user
    ) {
        return ApiResponse.of(StoreTableSuccessStatus._TABLE_DELETED, storeTableCommandService.deleteTable(storeId, tableId, user.getUsername()));
    }

    @PostMapping(
            value = "/stores/{storeId}/tables/{tableId}/table-image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<StoreTableResDto.UploadTableImageDto> uploadTableImage(
            @PathVariable Long storeId,
            @PathVariable Long tableId,
            @RequestPart("tableImage") MultipartFile tableImage,
            @CurrentUser User user
    ) {
        return ApiResponse.of(TableImageSuccessStatus._STORE_TABLE_IMAGE_UPLOAD_SUCCESS, storeTableCommandService.uploadTableImage(storeId, tableId, tableImage, user.getUsername()));
    }

    @DeleteMapping("/stores/{storeId}/tables/{tableId}/table-image")
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<StoreTableResDto.DeleteTableImageDto> deleteTableImage(
            @PathVariable Long storeId,
            @PathVariable Long tableId,
            @CurrentUser User user
    ) {
        return ApiResponse.of(TableImageSuccessStatus._STORE_TABLE_IMAGE_DELETE_SUCCESS, storeTableCommandService.deleteTableImage(storeId, tableId, user.getUsername()));
    }
}
