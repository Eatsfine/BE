# 코드 리뷰 (백엔드)

변경사항을 백엔드 팀 규칙 기준으로 리뷰합니다.

## 리뷰 절차

### 1. 변경사항 확인

```bash
git diff origin/develop...HEAD
```

### 2. 레이어별 체크리스트

#### 🔴 머지 블로킹 (반드시 수정)

**공통**
- [ ] `System.out.println` 사용
- [ ] 민감 정보(비밀번호, API Key) 하드코딩
- [ ] `application-local.yml` / `.env` 파일 커밋
- [ ] 테스트 실패

**Controller**
- [ ] 비즈니스 로직이 Controller에 직접 작성됨 (Service로 위임 필수)
- [ ] Entity를 직접 반환 (반드시 DTO로 변환)
- [ ] 요청 DTO에 `@Valid` 없음
- [ ] ApiResponse 사용 안 함 (통일된 응답 형식)
- [ ] Swagger 문서화 없음 (`@Operation`, `@ApiResponses`)

**Service**
- [ ] Repository를 2개 이상 호출하는 로직에 `@Transactional` 없음
- [ ] 조회 전용 메서드에 `@Transactional(readOnly = true)` 없음
- [ ] 예외를 삼키는 빈 catch 블록
- [ ] `RuntimeException` 직접 throw (GeneralException + ErrorStatus 사용 필수)

**Repository**
- [ ] Service / 비즈니스 로직이 Repository에 작성됨 (Repository는 DB 접근만)

**Entity**
- [ ] Entity 변경 시 팀 사전 공유 안 함 (DB 스키마 영향)

#### 🟡 권장 수정 (리뷰어 판단)

- [ ] 새 API에 Swagger `@Operation` 없음
- [ ] HTTP 상태 코드가 규칙과 다름 (예: 생성인데 SuccessStatus._OK 반환)
- [ ] SuccessStatus 대신 도메인별 SuccessStatus 사용 권장 (예: StoreSuccessStatus)
- [ ] ErrorStatus 대신 도메인별 ErrorStatus 사용 권장 (예: BookingErrorStatus)
- [ ] 필드 주입(`@Autowired`) 사용 → 생성자 주입(`@RequiredArgsConstructor`) 권장
- [ ] Converter 사용 안 함 (DTO ↔ Entity 변환 로직이 Service에 있음)
- [ ] 복잡한 Service에 Command/Query 분리 안 함
- [ ] QueryDSL 사용 가능한데 일반 JPA 쿼리 사용
- [ ] 테스트 given/when/then 구분 없음
- [ ] 단위 테스트에서 `@SpringBootTest` 사용 (너무 무거움)

#### 🔵 Nit (사소한 의견, 머지 블로킹 아님)

- 네이밍 개선 제안
- 더 간결한 코드 작성 방법

### 3. 리뷰 코멘트 작성 방식

```
nit: 변수명을 좀 더 직관적으로 지으면 어떨까요? ex) result → savedUser
[질문] 여기서 @Transactional(readOnly = true) 를 안 쓴 이유가 있나요?
[제안] 이 로직은 UserValidator 로 분리하면 테스트하기 더 편할 것 같아요.
```

- 한국어로, 팀원에게 말하듯 부드럽게
- 문제점만 지적하지 말고 개선 방향도 함께 제시

### 4. 리뷰 요약

```
✅ 전반적으로 깔끔합니다.
🔴 블로킹: N건
🟡 권장 수정: N건
🔵 Nit: N건
```
