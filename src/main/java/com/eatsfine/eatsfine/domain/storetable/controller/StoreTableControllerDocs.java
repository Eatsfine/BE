package com.eatsfine.eatsfine.domain.storetable.controller;

import com.eatsfine.eatsfine.domain.storetable.dto.req.StoreTableReqDto;
import com.eatsfine.eatsfine.domain.storetable.dto.res.StoreTableResDto;
import com.eatsfine.eatsfine.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
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

    @Operation(
            summary = "테이블 상세 조회",
            description = """                                                                                          
                  특정 테이블의 상세 정보를 조회합니다.
                  - 테이블 기본 정보 (최소/최대 인원, 이미지, 평점, 리뷰 수, 테이블 유형)
                  - 예약 가능 상태 (날짜별 총 슬롯 수, 예약 가능한 슬롯 수)
                  - date 파라미터가 없으면 오늘 날짜로 조회합니다.
                  """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "테이블 상세 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "테이블이 가게에 속하지 않음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "테이블을 찾을 수 없음")
    })
    ApiResponse<StoreTableResDto.TableDetailDto> getTableDetail(
            @Parameter(description = "가게 ID", required = true, example = "1")
            Long storeId,

            @Parameter(description = "테이블 ID", required = true, example = "1")
            Long tableId,

            @Parameter(description = "조회 날짜 (yyyy-MM-dd)", example = "2026-01-23")
            LocalDate date
    );

    @Operation(
            summary = "테이블 정보 수정",
            description = """
                      특정 테이블의 정보를 수정합니다.
                      
                      **통합 API**: 테이블 번호, 좌석 수, 테이블 유형을 하나의 API에서 처리합니다.
                      
                      - **선택적 업데이트**: 모든 필드가 Optional이며, 제공된 필드만 업데이트됩니다.
                      - **최소 하나 필수**: 최소 하나 이상의 필드는 반드시 제공되어야 합니다.
                      
                      1. **테이블 번호 (tableNumber)**:
                         - 숫자 문자열로 전달 (예: "3")
                         - 자동으로 "N번 테이블" 형식으로 변환
                         - 중복 시 기존 테이블과 번호 스왑
                      
                      2. **좌석 수 (minSeatCount, maxSeatCount)**:
                         - 둘 중 하나만 제공 시, 다른 값은 기존 값 유지
                         - 최소 인원 ≤ 최대 인원 검증
                      
                      3. **테이블 유형 (seatsType)**:
                         - GENERAL, WINDOW, ROOM, BAR, OUTDOOR 중 선택
                      
                      ### 응답:
                      - updatedTables: 변경된 테이블 정보만 표시
                      - 번호 스왑 발생 시 두 테이블 모두 포함
                      - 스왑 없을 시 요청 테이블만 포함
                      
                      ### 예시:
                      ```json
                      // Request (모든 필드 수정)
                      {
                        "tableNumber": "5",
                        "minSeatCount": 2,
                        "maxSeatCount": 4,
                        "seatsType": "ROOM"
                      }
                      
                      // Request (번호만 수정)
                      {
                        "tableNumber": "3"
                      }
                      
                      // Request (좌석 수만 수정)
                      {
                        "minSeatCount": 4,
                        "maxSeatCount": 6
                      }
                      
                      // Request (좌석 유형만 수정)
                      {
                        "seatsType": "WINDOW"
                      }
                      ```
                      """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "테이블 수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 (수정 필드 없음, 좌석 범위 오류 등)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "가게 또는 테이블을 찾을 수 없음")
    })
    ApiResponse<StoreTableResDto.TableUpdateResultDto> updateTable(
            @Parameter(description = "가게 ID", required = true, example = "1")
            Long storeId,

            @Parameter(description = "테이블 ID", required = true, example = "1")
            Long tableId,

            @RequestBody @Valid StoreTableReqDto.TableUpdateDto dto
    );

    @Operation(
            summary = "테이블 삭제",
            description = """
                      특정 가게의 테이블을 삭제합니다.
  
                      **삭제 조건:**
                      - 현재 시간 이후의 예약(CONFIRMED 또는 PENDING 상태)이 존재하는 테이블은 삭제할 수 없습니다.
                      - Soft Delete 방식으로 처리되어 실제 데이터는 삭제되지 않고 is_deleted 플래그가 true로 변경됩니다.
                      - deleted_at 필드에 삭제 시간이 기록됩니다.
                      - 삭제된 테이블 위치에 새 테이블 생성 시, 겹침 검증 로직에서 삭제된 테이블은 제외됩니다.
                      """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "테이블 삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "테이블에 미래 예약이 존재함"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "테이블 또는 가게를 찾을 수 없음")
    })
    ApiResponse<StoreTableResDto.TableDeleteDto> deleteTable(
            @Parameter(description = "가게 ID", required = true, example = "1")
            Long storeId,
            @Parameter(description = "테이블 ID", required = true, example = "1")
            Long tableId
    );

    @Operation(
            summary = "테이블 이미지 등록",
            description = """
            특정 테이블의 이미지를 등록합니다.
            
            - 테이블당 1개의 이미지만 등록 가능합니다.
            - 기존 이미지가 있는 경우 자동으로 삭제되고 새 이미지로 교체됩니다.
            - S3 저장 경로: stores/{storeId}/tables/{tableId}/
            """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "테이블 이미지 등록 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 (빈 파일, 지원하지 않는 파일 형식 등)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "가게 또는 테이블을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "S3 업로드 실패")
    })
    ApiResponse<StoreTableResDto.UploadTableImageDto> uploadTableImage(
            @Parameter(description = "가게 ID", required = true, example = "1")
            Long storeId,

            @Parameter(description = "테이블 ID", required = true, example = "1")
            Long tableId,

            @Parameter(
                    description = "업로드할 테이블 이미지 파일",
                    required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
            MultipartFile tableImage
    );

    @Operation(
            summary = "테이블 이미지 삭제",
            description = """
                특정 테이블의 이미지를 삭제합니다.
                
                - 등록된 이미지가 없는 경우 404 에러가 발생합니다.
                - S3에서 이미지가 삭제되고, DB의 이미지 URL도 null로 업데이트됩니다.
                - 삭제 후 다시 이미지를 등록할 수 있습니다.
                """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "테이블 이미지 삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "테이블이 해당 가게에 속하지 않음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "가게, 테이블을 찾을 수 없거나 이미지가 등록되지 않음")
    })
    ApiResponse<StoreTableResDto.DeleteTableImageDto> deleteTableImage(
            @Parameter(description = "가게 ID", required = true, example = "1")
            Long storeId,

            @Parameter(description = "테이블 ID", required = true, example = "1")
            Long tableId
    );
}
