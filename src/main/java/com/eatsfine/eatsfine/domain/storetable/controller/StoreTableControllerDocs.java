package com.eatsfine.eatsfine.domain.storetable.controller;

import com.eatsfine.eatsfine.domain.storetable.dto.req.StoreTableReqDto;
import com.eatsfine.eatsfine.domain.storetable.dto.res.StoreTableResDto;
import com.eatsfine.eatsfine.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

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
}
