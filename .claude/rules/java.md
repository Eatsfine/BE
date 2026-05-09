# Java 코딩 규칙

## 네이밍 규칙

| 대상 | 규칙 | 예시 |
|------|------|------|
| 클래스 | PascalCase | `UserService`, `StoreController`, `BookingService` |
| 메서드 / 변수 | camelCase | `findUserById`, `userId`, `createStore` |
| 상수 | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT`, `DEFAULT_BOOKING_INTERVAL` |
| 패키지 | 소문자 | `com.eatsfine.domain.user`, `com.eatsfine.global.config` |
| DTO (요청) | `~Request` | `CreateStoreRequest`, `UpdateBookingRequest` |
| DTO (응답) | `~Response` | `UserResponse`, `StoreDetailResponse` |
| Converter | `~Converter` | `StoreConverter`, `BookingConverter` |
| 예외 클래스 | 커스텀 예외 | `GeneralException` (프로젝트 공통 예외) |
| 상태 코드 | `~Status` | `SuccessStatus`, `ErrorStatus`, `StoreErrorStatus` |

## 레이어 역할 분리

```
Controller  → 요청/응답 처리, 유효성 검증만 담당
Service     → 비즈니스 로직 담당
Repository  → DB 접근만 담당 (비즈니스 로직 금지)
```

```java
// ✅ Controller는 Service에 위임만 (ApiResponse 사용)
@PostMapping("/stores")
public ApiResponse<StoreResponse> createStore(
        @CurrentUser User owner,
        @RequestBody @Valid CreateStoreRequest request) {
    StoreResponse response = storeCommandService.createStore(owner, request);
    return ApiResponse.onSuccess(SuccessStatus._CREATED, response);
}

// ❌ Controller에서 비즈니스 로직 처리 금지
@PostMapping("/stores")
public ApiResponse<StoreResponse> createStore(...) {
    Store store = storeRepository.findByName(request.getName()); // 금지
    if (store != null) {
        throw new GeneralException(ErrorStatus.STORE_ALREADY_EXISTS); // 금지
    }
    ...
}
```

### Command/Query 서비스 분리 (CQRS 패턴)
복잡한 도메인의 경우 Command(CUD)와 Query(R) 서비스를 분리합니다.

```java
// ✅ Command 서비스 (생성, 수정, 삭제)
@Service
@RequiredArgsConstructor
public class StoreCommandServiceImpl implements StoreCommandService {
    private final StoreRepository storeRepository;

    @Transactional
    public StoreResponse createStore(User owner, CreateStoreRequest request) {
        // 비즈니스 로직
    }
}

// ✅ Query 서비스 (조회)
@Service
@RequiredArgsConstructor
public class StoreQueryServiceImpl implements StoreQueryService {
    private final StoreRepository storeRepository;

    @Transactional(readOnly = true)
    public Page<StoreResponse> searchStores(StoreSearchCondition condition) {
        // QueryDSL 조회 로직
    }
}
```

## DTO 규칙

- Entity를 Controller에서 직접 반환 **금지** — 반드시 DTO로 변환
- 요청 DTO에는 `@Valid` 검증 어노테이션 필수
- DTO ↔ Entity 변환은 **Converter 클래스** 사용 (각 도메인의 `converter/` 패키지)

```java
// ✅ DTO 예시 (요청)
public record CreateStoreRequest(
    @NotBlank(message = "가게 이름은 필수입니다")
    String storeName,

    @NotBlank(message = "주소는 필수입니다")
    String address,

    @NotNull(message = "카테고리는 필수입니다")
    Category category,

    @Min(value = 0, message = "예약 간격은 0분 이상이어야 합니다")
    Integer bookingIntervalMinutes
) {}

// ✅ DTO 예시 (응답)
public record StoreResponse(
    Long id,
    String storeName,
    String address,
    Category category,
    BigDecimal rating
) {}

// ✅ Converter 패턴 (변환 로직 분리)
@Component
public class StoreConverter {
    public Store toEntity(CreateStoreRequest request, User owner, Region region) {
        return Store.builder()
            .owner(owner)
            .region(region)
            .storeName(request.storeName())
            .address(request.address())
            .category(request.category())
            .bookingIntervalMinutes(request.bookingIntervalMinutes())
            .build();
    }

    public StoreResponse toResponse(Store store) {
        return new StoreResponse(
            store.getId(),
            store.getStoreName(),
            store.getAddress(),
            store.getCategory(),
            store.getRating()
        );
    }
}
```

## 예외 처리

- 프로젝트 공통 예외: `GeneralException` (global/apiPayload/exception/)
- 상태 코드: `ErrorStatus`, 도메인별 ErrorStatus (예: `StoreErrorStatus`, `BookingErrorStatus`)
- `try-catch`로 예외를 삼키지 않는다 (로깅 후 반드시 re-throw 또는 변환)
- 전역 예외 처리: `GeneralExceptionAdvice` (global/apiPayload/handler/)

```java
// ✅ GeneralException + ErrorStatus 사용
@Transactional(readOnly = true)
public Store findById(Long storeId) {
    return storeRepository.findById(storeId)
        .orElseThrow(() -> new GeneralException(ErrorStatus.STORE_NOT_FOUND));
}

// ✅ 도메인별 ErrorStatus 사용
public void validateBooking(Booking booking) {
    if (booking.getStatus() == BookingStatus.CANCELED) {
        throw new GeneralException(BookingErrorStatus.ALREADY_CANCELED);
    }
}

// ❌ RuntimeException 직접 throw 금지
throw new RuntimeException("Store not found"); // 금지

// ❌ 예외를 삼키는 빈 catch 블록 금지
try {
    // ...
} catch (Exception e) {
    // 아무것도 하지 않음 - 금지
}
```

### ErrorStatus 정의 예시
```java
// global/apiPayload/code/status/ErrorStatus.java
public enum ErrorStatus implements BaseErrorCode {
    // 공통 에러
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON400", "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON401", "인증이 필요합니다."),
    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "STORE404", "존재하지 않는 식당입니다."),

    // ...
}

// domain/booking/status/BookingErrorStatus.java
public enum BookingErrorStatus implements BaseErrorCode {
    ALREADY_CANCELED(HttpStatus.BAD_REQUEST, "BOOKING400_1", "이미 취소된 예약입니다."),
    INVALID_BOOKING_TIME(HttpStatus.BAD_REQUEST, "BOOKING400_2", "유효하지 않은 예약 시간입니다."),

    // ...
}
```

## 기타

- `@Autowired` 필드 주입 금지 → 생성자 주입 사용 (Lombok `@RequiredArgsConstructor`)
- `System.out.println` 금지 → `log.info()` / `log.error()` 사용
- 매직 넘버는 상수로 추출
