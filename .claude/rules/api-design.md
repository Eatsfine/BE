# API 설계 규칙

## REST 엔드포인트 설계

- URL은 **소문자 kebab-case**, 명사 복수형 사용
- 동사 사용 금지 (행위는 HTTP 메서드로 표현)
- 프로젝트 기본 경로: `/api/v1/`

```
✅ GET    /api/v1/stores
✅ GET    /api/v1/stores/{storeId}
✅ POST   /api/v1/stores
✅ PATCH  /api/v1/stores/{storeId}
✅ DELETE /api/v1/stores/{storeId}

✅ POST   /api/v1/bookings
✅ GET    /api/v1/bookings/{bookingId}
✅ PATCH  /api/v1/bookings/{bookingId}/cancel

✅ POST   /api/v1/payments/confirm
✅ POST   /api/v1/payments/webhook

❌ GET  /api/v1/getStore
❌ POST /api/v1/deleteStore
❌ POST /api/v1/cancelBooking  (동사 사용 금지)
```

### URL 설계 팁
- 리소스 ID는 경로 변수로: `/stores/{storeId}`
- 하위 리소스: `/stores/{storeId}/menus`
- 특정 액션이 필요한 경우: `/bookings/{bookingId}/cancel`
- 검색/필터: 쿼리 파라미터 사용 `/stores?category=KOREAN&region=서울`

## HTTP 상태 코드

| 상황 | 코드 |
|------|------|
| 조회 성공 | 200 OK |
| 생성 성공 | 201 Created |
| 삭제 성공 (응답 없음) | 204 No Content |
| 잘못된 요청 (유효성 실패) | 400 Bad Request |
| 인증 실패 | 401 Unauthorized |
| 권한 없음 | 403 Forbidden |
| 리소스 없음 | 404 Not Found |
| 중복 리소스 | 409 Conflict |
| 서버 오류 | 500 Internal Server Error |

## 공통 응답 형식

프로젝트 공통 응답 래퍼: `global/apiPayload/ApiResponse.java`

모든 API는 **ApiResponse**로 래핑하여 반환합니다.

```json
// 성공 응답
{
  "isSuccess": true,
  "code": "STORE_1",
  "message": "식당 생성에 성공했습니다",
  "result": {
    "id": 1,
    "storeName": "맛있는 한식당",
    "address": "서울시 강남구",
    "category": "KOREAN",
    "rating": 4.5
  }
}

// 실패 응답
{
  "isSuccess": false,
  "code": "STORE404",
  "message": "존재하지 않는 식당입니다",
  "result": null
}
```

### ApiResponse 사용법

```java
// ✅ 성공 응답
@PostMapping
public ApiResponse<StoreResponse> createStore(@RequestBody @Valid CreateStoreRequest request) {
    StoreResponse response = storeCommandService.createStore(request);
    return ApiResponse.onSuccess(SuccessStatus._CREATED, response);
}

// ✅ 성공 응답 (커스텀 메시지)
return ApiResponse.onSuccess(StoreSuccessStatus.STORE_CREATED, response);

// ✅ 실패는 예외로 처리 (GeneralException)
throw new GeneralException(ErrorStatus.STORE_NOT_FOUND);
// GeneralExceptionAdvice에서 자동으로 ApiResponse로 변환
```

### 상태 코드 정의

- **SuccessStatus**: 공통 성공 상태 (`_OK`, `_CREATED`)
- **도메인별 SuccessStatus**: `StoreSuccessStatus`, `BookingSuccessStatus`
- **ErrorStatus**: 공통 에러 상태 (`BAD_REQUEST`, `UNAUTHORIZED`, `NOT_FOUND`)
- **도메인별 ErrorStatus**: `StoreErrorStatus`, `BookingErrorStatus`

## Swagger 문서화

**Springdoc OpenAPI 2.8.1** 사용

모든 API에 아래 어노테이션 필수:

```java
@Operation(
    summary = "식당 단건 조회",
    description = "식당 ID로 상세 정보를 조회합니다."
)
@ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "조회 성공"
    ),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404",
        description = "식당을 찾을 수 없습니다",
        content = @Content(schema = @Schema(implementation = ApiResponse.class))
    )
})
@GetMapping("/{storeId}")
public ApiResponse<StoreDetailResponse> getStore(@PathVariable Long storeId) {
    return ApiResponse.onSuccess(storeQueryService.getStoreDetail(storeId));
}
```

### Swagger UI 접근
- **로컬**: http://localhost:8080/swagger-ui/index.html
- **프로덕션**: https://eatsfine.co.kr/swagger-ui/index.html

### 인증 필요 API
JWT 토큰이 필요한 API는 Swagger UI에서 **Authorize** 버튼으로 토큰 설정
```
Bearer {access_token}
```

## 버전 관리

- 현재 API 버전: `/api/v1/`
- 하위 호환이 깨지는 변경 시 버전 업: `/api/v2/`
- 버전 변경은 팀 사전 협의 필수
- 기존 버전은 최소 3개월 유지 (deprecation 공지 후)

## CORS 설정

CORS 설정은 `global/config/SecurityConfig.java`에서 관리합니다.

### 현재 설정
```java
// SecurityConfig.java
CorsConfiguration config = new CorsConfiguration();
config.setAllowedOrigins(Arrays.asList(
    "https://www.eatsfine.co.kr",
    "http://localhost:3000",
    "http://localhost:5173"
));
config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
config.setAllowedHeaders(Collections.singletonList("*"));
config.setAllowCredentials(true);
```

### 주의사항
- 프론트엔드 도메인 추가 시 **팀에 공유** 후 SecurityConfig 수정
- `localhost` 포트는 개발 환경에만 허용
- 프로덕션에서는 실제 도메인만 허용

## 페이지네이션

검색/목록 조회 API는 **Spring Data의 Page** 사용

```java
@GetMapping
public ApiResponse<Page<StoreResponse>> searchStores(
    @RequestParam(required = false) String category,
    @RequestParam(required = false) String region,
    @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
) {
    Page<StoreResponse> stores = storeQueryService.searchStores(category, region, pageable);
    return ApiResponse.onSuccess(stores);
}
```

**응답 형식**:
```json
{
  "isSuccess": true,
  "code": "COMMON200",
  "message": "성공",
  "result": {
    "content": [ /* 데이터 배열 */ ],
    "pageable": { "pageNumber": 0, "pageSize": 20 },
    "totalElements": 150,
    "totalPages": 8,
    "last": false,
    "first": true
  }
}
```
