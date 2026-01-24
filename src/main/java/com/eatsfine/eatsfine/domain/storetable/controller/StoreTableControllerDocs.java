package com.eatsfine.eatsfine.domain.storetable.controller;

import com.eatsfine.eatsfine.domain.storetable.dto.req.StoreTableReqDto;
import com.eatsfine.eatsfine.domain.storetable.dto.res.StoreTableResDto;
import com.eatsfine.eatsfine.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

public interface StoreTableControllerDocs {

    @Operation(
            summary = "테이블 생성",
            description = """
                      배치도에 새 테이블을 추가합니다.
                      
                      - 테이블 번호는 자동으로 순차 생성됩니다. (1번 테이블, 2번 테이블, ...)
                      - 좌표와 크기는 배치도 그리드 범위 내에 있어야 합니다.
                      - 다른 테이블과 겹치지 않아야 합니다.
                      - 최소 인원은 최대 인원보다 작거나 같아야 합니다.
                      - 활성화된 배치도에만 테이블을 추가할 수 있습니다.
                      """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "테이블 생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 (좌표 범위 초과, 테이블 겹침 등)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "가게 또는 배치도를 찾을 수 없음")
    })
    ApiResponse<StoreTableResDto.TableCreateDto> createTable(
            @Parameter(description = "가게 ID", required = true, example = "1")
            Long storeId,
            @RequestBody @Valid StoreTableReqDto.TableCreateDto dto
    );

    @Operation(
            summary = "테이블 예약 시간대 조회",
            description = """                                                                                      
                        특정 테이블의 예약 가능한 시간대를 조회합니다.
                        - 동적 슬롯 생성 방식을 사용합니다.
                        - 영업시간을 기준으로 예약 간격(bookingIntervalMinutes)만큼 슬롯을 생성합니다.
                        - 각 슬롯의 상태는 다음과 같이 결정됩니다:
                          * BREAK_TIME: 브레이크타임에 해당하는 시간대
                          * BLOCKED: 사장이 차단한 시간대
                          * BOOKED: 이미 예약된 시간대
                          * AVAILABLE: 예약 가능한 시간대
                        - date 파라미터가 없으면 오늘 날짜로 조회합니다.
                        - 운영 시간 11:00~22:00, 예약 간격 30분이면 21:30이 마지막 슬롯입니다.
                        """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "슬롯 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "테이블 또는 영업시간을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "테이블이 해당 가게에 속하지 않음")
    })
    ApiResponse<StoreTableResDto.SlotListDto> getTableSlots(
            @Parameter(description = "가게 ID", required = true, example = "1")
            Long storeId,

            @Parameter(description = "테이블 ID", required = true, example = "1")
            Long tableId,

            @Parameter(description = "조회할 날짜 (yyyy-MM-dd 형식, 미입력 시 오늘 날짜)", example = "2026-01-12")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date
    );
}
