# GitHub 이슈 생성 가이드

**모든 개발 작업은 반드시 GitHub 이슈를 먼저 생성하고 시작합니다!**

---

## 📋 이슈 템플릿 선택

Eatsfine 프로젝트는 3가지 이슈 템플릿을 제공합니다:

### 1. Feature Request (기능 추가)
**파일**: `.github/ISSUE_TEMPLATE/feature_request.yml`

**사용 시점**:
- 새로운 기능 개발
- 기존 기능 확장

**예시**:
- 예약 취소 기능 추가
- OAuth 소셜 로그인 추가
- 식당 검색 필터 추가

---

### 2. Bug Report (버그 수정)
**파일**: `.github/ISSUE_TEMPLATE/bug_report.yml`

**사용 시점**:
- 기능 오류 수정
- 예외 처리 누락
- 데이터 정합성 문제

**예시**:
- 예약 내역 조회 시 500 에러 발생
- 결제 취소 시 환불 금액 계산 오류
- JWT 토큰 만료 시 무한 리다이렉트

---

### 3. Refactor Template (리팩토링)
**파일**: `.github/ISSUE_TEMPLATE/refactor_template.yml`

**사용 시점**:
- 코드 구조 개선
- 성능 최적화
- 아키텍처 패턴 적용

**예시**:
- StoreService CQRS 패턴 적용
- N+1 쿼리 문제 해결
- 중복 코드 제거

---

## 📝 Feature 이슈 작성 예시

### Title
```
[FEAT]: 예약 취소 및 환불 기능 구현
```

### 📄 설명
```markdown
사용자가 예약을 취소하고 환불받을 수 있는 기능을 구현합니다.

**요구사항**:
- 예약 24시간 전까지 취소 가능
- 취소 사유 필수 입력
- 결제 금액 자동 환불 (Toss Payments)
- 본인만 취소 가능 (권한 검증)
- 취소된 예약은 상태가 CANCELED로 변경
```

### ✅ 작업할 내용
```markdown
- [ ] `CancelBookingRequest` DTO 작성
  - cancelReason: String (필수)
- [ ] `BookingCommandService.cancelBooking()` 메서드 구현
  - 취소 권한 검증 (본인 확인)
  - 취소 가능 시간 검증 (24시간 전)
  - 예약 상태 CANCELED로 변경
- [ ] `TossPaymentService.cancelPayment()` 연동
  - 결제 환불 로직
  - 환불 실패 시 롤백 처리
- [ ] `BookingController.cancelBooking()` API 구현
  - PATCH `/api/v1/bookings/{bookingId}/cancel`
  - @CurrentUser 인증 필수
- [ ] 단위 테스트 작성
  - 정상 취소 케이스
  - 권한 없는 사용자 케이스
  - 취소 불가 시간 케이스
  - 결제 환불 실패 케이스
- [ ] Swagger 문서화
  - @Operation, @ApiResponses 추가
```

### 🙋🏻 참고 자료
```markdown
- Toss Payments 결제 취소 API: https://docs.tosspayments.com/reference#결제-취소
- 관련 프론트 이슈: #120
```

---

## 🐛 Bug Report 이슈 작성 예시

### Title
```
[BUG]: 예약 내역 조회 시 500 에러 발생
```

### 🐛 버그 설명
```markdown
사용자가 마이페이지에서 예약 내역을 조회할 때 500 Internal Server Error가 발생합니다.
```

### 🔄 재현 방법
```markdown
1. 로그인 후 마이페이지 접속
2. "내 예약" 메뉴 클릭
3. 500 에러 발생 (예약 목록이 표시되지 않음)
```

### ✅ 예상 동작
```markdown
사용자의 예약 내역 목록이 정상적으로 표시되어야 합니다.
```

### ❌ 실제 동작
```markdown
500 서버 에러가 발생하고 예약 내역을 불러올 수 없습니다.
```

### 📸 스크린샷 / 로그
```markdown
**에러 로그**:
```
org.hibernate.LazyInitializationException: failed to lazily initialize a collection of role: com.eatsfine.domain.booking.entity.Booking.bookingTables
```

**스택 트레이스**:
at com.eatsfine.domain.booking.service.BookingQueryServiceImpl.getBookingList(BookingQueryServiceImpl.java:45)
```
```

### 🔍 원인 분석
```markdown
`BookingQueryServiceImpl.getBookingList()` 메서드에 `@Transactional(readOnly = true)` 어노테이션이 누락되어 Lazy Loading 시 LazyInitializationException이 발생합니다.
```

### 🙋🏻 참고 자료
```markdown
- 관련 커밋: ba3ece4
- 프론트엔드 이슈: #155
```

---

## 🔧 Refactor 이슈 작성 예시

### Title
```
[REFACTOR]: StoreService CQRS 패턴 적용
```

### 🔍 리팩토링 사유
```markdown
현재 `StoreService`가 조회와 명령 로직을 모두 포함하고 있어 책임이 과중합니다. CQRS 패턴을 적용하여 조회(Query)와 명령(Command) 로직을 분리하면 다음과 같은 이점이 있습니다:

- 책임 분리로 코드 가독성 향상
- 조회 로직에 `@Transactional(readOnly = true)` 적용 가능
- 성능 최적화 가능 (조회와 명령 독립적으로)
- 테스트 작성 용이
```

### 📄 리팩토링 상세 내용
```markdown
**변경 전**:
```java
StoreService
  - createStore()
  - updateStore()
  - deleteStore()
  - findById()
  - search()
```

**변경 후**:
```java
StoreCommandService (인터페이스)
  └── StoreCommandServiceImpl
      - createStore()
      - updateStore()
      - deleteStore()

StoreQueryService (인터페이스)
  └── StoreQueryServiceImpl
      - findById()
      - search()
      - getStoreDetail()
```

**작업 내용**:
- [ ] `StoreCommandService` 인터페이스 및 구현체 생성
- [ ] `StoreQueryService` 인터페이스 및 구현체 생성
- [ ] 기존 `StoreService` 로직 이동
- [ ] Controller에서 서비스 주입 변경
- [ ] 기존 테스트 수정
- [ ] 새로운 테스트 추가
```

---

## 🚀 이슈 생성 후

### 1. 이슈 번호 확인
이슈를 생성하면 `#123`과 같은 번호를 받습니다.

### 2. 브랜치 생성
```bash
git checkout -b feat/#123-cancel-booking
```

### 3. 작업 시작
```bash
/start
```

---

## 💡 이슈 작성 팁

### 체크리스트 세분화
- 큰 작업을 작은 단위로 나누기
- 각 체크리스트는 1시간 이내에 완료 가능하도록
- 테스트와 문서화도 체크리스트에 포함

### 구체적인 설명
- "예약 기능 구현" ❌
- "사용자가 식당 테이블을 예약하고, 선입금을 결제하며, 예약 내역을 조회할 수 있는 기능 구현" ✅

### 참고 자료 첨부
- API 문서 링크
- 관련 이슈/PR 링크
- 스크린샷
- 에러 로그

---

## 📌 GitHub CLI로 이슈 생성

```bash
# 이슈 목록 보기
gh issue list

# Feature 이슈 생성
gh issue create \
  --title "[FEAT]: 예약 취소 기능 구현" \
  --body "이슈 내용" \
  --label "feature"

# Bug 이슈 생성
gh issue create \
  --title "[BUG]: 예약 내역 조회 에러" \
  --body "이슈 내용" \
  --label "bug"

# 이슈에 나 할당
gh issue create \
  --title "[FEAT]: 예약 취소 기능" \
  --assignee "@me"
```

---

## ✅ 다음 단계

이슈를 생성했다면 `/start` 커맨드로 작업을 시작하세요!