# 테스트 작성 규칙

## 테스트 레이어 구분

| 레이어 | 테스트 방식 | 도구 |
|--------|-------------|------|
| Service | 순수 단위 테스트 (Mock 사용) | JUnit 5 + Mockito |
| Controller | 슬라이스 테스트 | `@WebMvcTest` + MockMvc |
| Repository | 슬라이스 테스트 | `@DataJpaTest` |
| 통합 | 전체 컨텍스트 | `@SpringBootTest` (최소화) |

## 테스트 메서드 네이밍

한글 네이밍 사용 (형식: 메서드명_상황_기대결과)

```java
@Test
void findById_존재하는식당ID_식당정보반환() { ... }

@Test
void findById_존재하지않는ID_GeneralException발생() { ... }

@Test
void createStore_중복된가게이름_GeneralException발생() { ... }

@Test
void createBooking_예약가능시간_예약성공() { ... }

@Test
void cancelBooking_이미취소된예약_BookingErrorStatus예외발생() { ... }
```

## Service 단위 테스트 예시

### Query Service 테스트

```java
package com.eatsfine.domain.store.service;

@ExtendWith(MockitoExtension.class)
class StoreQueryServiceImplTest {

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private StoreConverter storeConverter;

    @InjectMocks
    private StoreQueryServiceImpl storeQueryService;

    @Test
    void findById_존재하는식당ID_식당정보반환() {
        // given
        Long storeId = 1L;
        Store store = Store.builder()
            .id(storeId)
            .storeName("맛있는 한식당")
            .address("서울시 강남구")
            .category(Category.KOREAN)
            .build();
        StoreResponse expectedResponse = new StoreResponse(
            storeId, "맛있는 한식당", "서울시 강남구", Category.KOREAN, BigDecimal.valueOf(4.5)
        );

        given(storeRepository.findById(storeId)).willReturn(Optional.of(store));
        given(storeConverter.toResponse(store)).willReturn(expectedResponse);

        // when
        StoreResponse result = storeQueryService.findById(storeId);

        // then
        assertThat(result.storeName()).isEqualTo("맛있는 한식당");
        assertThat(result.category()).isEqualTo(Category.KOREAN);
        verify(storeRepository, times(1)).findById(storeId);
    }

    @Test
    void findById_존재하지않는ID_GeneralException발생() {
        // given
        given(storeRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> storeQueryService.findById(999L))
            .isInstanceOf(GeneralException.class)
            .hasMessageContaining("STORE_NOT_FOUND");
    }
}
```

### Command Service 테스트

```java
@ExtendWith(MockitoExtension.class)
class BookingCommandServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BookingCommandServiceImpl bookingCommandService;

    @Test
    void createBooking_예약가능시간_예약성공() {
        // given
        Long userId = 1L;
        Long storeId = 2L;
        CreateBookingRequest request = new CreateBookingRequest(
            storeId,
            LocalDate.now().plusDays(1),
            LocalTime.of(18, 0),
            4,
            true
        );

        User user = User.builder().id(userId).name("홍길동").build();
        Store store = Store.builder().id(storeId).storeName("맛집").build();
        Booking booking = Booking.builder()
            .id(1L)
            .user(user)
            .store(store)
            .bookingDate(request.bookingDate())
            .bookingTime(request.bookingTime())
            .partySize(request.partySize())
            .status(BookingStatus.PENDING)
            .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(storeRepository.findById(storeId)).willReturn(Optional.of(store));
        given(bookingRepository.save(any(Booking.class))).willReturn(booking);

        // when
        BookingResponse result = bookingCommandService.createBooking(userId, request);

        // then
        assertThat(result.status()).isEqualTo(BookingStatus.PENDING);
        assertThat(result.partySize()).isEqualTo(4);
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    void cancelBooking_이미취소된예약_GeneralException발생() {
        // given
        Long bookingId = 1L;
        Booking canceledBooking = Booking.builder()
            .id(bookingId)
            .status(BookingStatus.CANCELED)
            .build();

        given(bookingRepository.findById(bookingId)).willReturn(Optional.of(canceledBooking));

        // when & then
        assertThatThrownBy(() -> bookingCommandService.cancelBooking(bookingId))
            .isInstanceOf(GeneralException.class);
    }
}
```

## Controller 슬라이스 테스트 예시

```java
package com.eatsfine.domain.store.controller;

@WebMvcTest(StoreController.class)
class StoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StoreQueryService storeQueryService;

    @MockBean
    private StoreCommandService storeCommandService;

    @Test
    void getStore_존재하는ID_조회성공() throws Exception {
        // given
        Long storeId = 1L;
        StoreDetailResponse response = new StoreDetailResponse(
            storeId,
            "맛있는 한식당",
            "서울시 강남구",
            Category.KOREAN,
            BigDecimal.valueOf(4.5),
            "02-1234-5678"
        );
        given(storeQueryService.getStoreDetail(storeId)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/stores/{storeId}", storeId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.isSuccess").value(true))
            .andExpect(jsonPath("$.result.storeName").value("맛있는 한식당"))
            .andExpect(jsonPath("$.result.category").value("KOREAN"));
    }

    @Test
    void createStore_유효한요청_생성성공() throws Exception {
        // given
        CreateStoreRequest request = new CreateStoreRequest(
            "새로운 맛집",
            "서울시 서초구",
            Category.KOREAN,
            30
        );
        StoreResponse response = new StoreResponse(1L, "새로운 맛집", "서울시 서초구", Category.KOREAN, null);

        given(storeCommandService.createStore(any(User.class), any(CreateStoreRequest.class)))
            .willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/stores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.isSuccess").value(true))
            .andExpect(jsonPath("$.result.storeName").value("새로운 맛집"));
    }

    @Test
    void createStore_필수값누락_검증실패() throws Exception {
        // given
        CreateStoreRequest invalidRequest = new CreateStoreRequest(
            "",  // 빈 문자열 (유효성 실패)
            "서울시 서초구",
            null,  // null (유효성 실패)
            30
        );

        // when & then
        mockMvc.perform(post("/api/v1/stores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }
}
```

## Repository 슬라이스 테스트 예시

QueryDSL을 사용하는 복잡한 쿼리의 경우 Repository 테스트 작성

```java
package com.eatsfine.domain.store.repository;

@DataJpaTest
@Import(QueryDslConfig.class)  // QueryDSL 설정 임포트
class StoreRepositoryTest {

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findByCategory_카테고리로조회_성공() {
        // given
        Store koreanStore = Store.builder()
            .storeName("한식당")
            .category(Category.KOREAN)
            .build();
        entityManager.persist(koreanStore);
        entityManager.flush();

        // when
        List<Store> result = storeRepository.findByCategory(Category.KOREAN);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStoreName()).isEqualTo("한식당");
    }
}
```

## 통합 테스트 예시

전체 플로우 검증이 필요한 경우만 `@SpringBootTest` 사용 (최소화)

```java
@SpringBootTest
@Transactional
class BookingIntegrationTest {

    @Autowired
    private BookingCommandService bookingCommandService;

    @Autowired
    private BookingQueryService bookingQueryService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Test
    void 예약생성부터조회까지_전체플로우_성공() {
        // given: 사용자와 식당 생성
        User user = userRepository.save(User.builder().name("홍길동").build());
        Store store = storeRepository.save(Store.builder().storeName("맛집").build());

        CreateBookingRequest request = new CreateBookingRequest(
            store.getId(),
            LocalDate.now().plusDays(1),
            LocalTime.of(18, 0),
            4,
            true
        );

        // when: 예약 생성
        BookingResponse created = bookingCommandService.createBooking(user.getId(), request);

        // then: 예약 조회 확인
        BookingResponse found = bookingQueryService.findById(created.id());
        assertThat(found.status()).isEqualTo(BookingStatus.PENDING);
    }
}
```

## 테스트 작성 기준

### 필수 테스트
- [ ] 새로운 **Service 메서드**: 단위 테스트 필수
- [ ] 새로운 **API 엔드포인트**: Controller 테스트 필수
- [ ] **복잡한 QueryDSL 쿼리**: Repository 테스트 권장
- [ ] **결제, 예약 등 중요 비즈니스 로직**: 통합 테스트 권장

### 테스트 작성 가이드
- **given / when / then** 주석으로 구분하여 가독성 확보
- `@SpringBootTest`는 꼭 필요한 경우만 (느림, 전체 컨텍스트 로드)
- Mock 객체는 `@Mock`, `@MockBean` 사용
- 테스트용 픽스처는 `src/test/java/.../fixture/` 에 별도 관리
- 테스트 환경 설정: `application-test.yml` 사용 (H2 DB)

### 테스트 커버리지 기준
- **목표: 80% 이상**
- 필수 테스트 영역:
  - 결제 로직 (`payment/` 도메인): **80% 이상 필수**
  - 예약 로직 (`booking/` 도메인): **80% 이상 필수**
  - 인증/인가 (`user/service/auth`, `global/auth`): **80% 이상 필수**
- 권장 테스트 영역:
  - 모든 Service 메서드: 단위 테스트
  - 모든 API 엔드포인트: Controller 테스트
  - 복잡한 QueryDSL 쿼리: Repository 테스트

### 테스트 실행 명령어
```bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트 클래스만 실행
./gradlew test --tests "com.eatsfine.domain.store.service.StoreQueryServiceImplTest"

# 특정 패키지만 실행
./gradlew test --tests "com.eatsfine.domain.booking.*"
```
