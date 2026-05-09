# 개인 설정 파일 (CLAUDE.local.md)

**이 파일을 복사하여 `CLAUDE.local.md`로 이름을 변경하고 사용하세요.**

```bash
cp CLAUDE.local.example.md CLAUDE.local.md
```

**참고**: `CLAUDE.local.md`는 `.gitignore`에 포함되어 있어 Git에 커밋되지 않습니다.

---

## 👤 담당자 정보

- **이름**: [이름을 입력하세요]
- **담당 도메인**: [담당 도메인을 나열하세요]

---

## 📂 담당 도메인 및 수정 가능 범위

### ✅ 내가 담당하는 도메인 (수정 가능)

```
src/main/java/com/eatsfine/domain/
├── booking/          # 예약 관리 (담당)
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   ├── dto/
│   ├── converter/
│   └── ...
│
└── payment/          # 결제 관리 (담당)
    ├── controller/
    ├── service/
    ├── repository/
    ├── entity/
    ├── dto/
    └── ...
```

**설명**:
- 위에 나열된 도메인만 수정할 수 있습니다
- 각 도메인의 모든 하위 패키지 (controller, service, repository, dto 등) 수정 가능
- 테스트 파일도 포함됩니다

---

## 🚫 절대 수정 금지 도메인

**다른 팀원이 담당하는 도메인입니다. 절대 수정하지 마세요!**

```
src/main/java/com/eatsfine/domain/
├── store/            # 식당 관리 (팀원 A 담당)
├── user/             # 회원 관리 (팀원 B 담당)
├── menu/             # 메뉴 관리 (팀원 C 담당)
├── businesshours/    # 영업시간 (팀원 D 담당)
├── storetable/       # 테이블 관리 (팀원 E 담당)
├── tablelayout/      # 테이블 배치도 (팀원 F 담당)
├── tableimage/       # 식당 이미지 (팀원 G 담당)
├── tableblock/       # 테이블 블록 (팀원 H 담당)
├── inquiry/          # 1:1 문의 (팀원 I 담당)
├── region/           # 지역 정보 (공통)
├── term/             # 약관 (공통)
├── businessnumber/   # 사업자번호 검증 (공통)
└── image/            # 이미지 공통 (공통)
```

**만약 다른 도메인을 수정해야 하는 경우**:
1. 담당 팀원에게 Slack으로 먼저 요청
2. 담당 팀원의 승인을 받은 후 수정
3. PR에 해당 내용 명시

---

## ⚠️ 공통 모듈 수정 시 주의사항

### 수정 가능 (단, 신중히)
```
src/main/java/com/eatsfine/domain/[내 담당 도메인]/
├── exception/        # 도메인별 예외
├── status/           # 도메인별 상태 코드
└── validator/        # 도메인별 검증 로직
```

### 수정 금지 (팀 전체 논의 필요)
```
src/main/java/com/eatsfine/global/
├── apiPayload/       # 공통 응답 & 예외 처리
├── auth/             # 보안 & 인증
├── config/           # 전역 설정
│   ├── SecurityConfig.java
│   ├── SwaggerConfig.java
│   ├── JpaAuditConfig.java
│   └── jwt/
├── common/           # BaseEntity 등
└── s3/               # AWS S3 서비스
```

**global 패키지 수정이 필요한 경우**:
1. Slack에 사전 공유 필수
2. 팀 회의에서 논의
3. PR에 최소 2명 이상 승인 필요

---

## 📝 Entity 수정 시 주의사항

### 내 담당 도메인 Entity 수정
```java
// 담당 도메인의 Entity는 수정 가능
Booking.java          # 예약 (담당)
Payment.java          # 결제 (담당)
```

**단, 다음 경우에는 Slack 사전 공유 필수**:
- 테이블 구조 변경 (컬럼 추가/삭제/타입 변경)
- 연관관계 변경 (ManyToOne, OneToMany 등)
- 제약조건 변경 (unique, nullable 등)

### 다른 도메인 Entity 수정 절대 금지
```java
// 절대 수정 금지!
Store.java            # 식당 (팀원 A 담당)
User.java             # 회원 (팀원 B 담당)
Menu.java             # 메뉴 (팀원 C 담당)
```

---

## 🔗 도메인 간 연관관계 처리

### 다른 도메인 Entity를 사용해야 하는 경우

**✅ 가능 (읽기 전용)**:
```java
// Service에서 다른 도메인의 Repository 조회
@Service
@RequiredArgsConstructor
public class BookingCommandServiceImpl {
    private final BookingRepository bookingRepository;
    private final StoreRepository storeRepository;  // 다른 도메인 (읽기만)
    private final UserRepository userRepository;    // 다른 도메인 (읽기만)

    public BookingResponse createBooking(...) {
        Store store = storeRepository.findById(storeId)  // ✅ 조회만
            .orElseThrow(...);
        // store 정보 사용 (읽기만)
    }
}
```

**❌ 불가능 (수정)**:
```java
// 다른 도메인 Entity를 직접 수정하지 마세요!
public BookingResponse createBooking(...) {
    Store store = storeRepository.findById(storeId)
        .orElseThrow(...);

    store.setStoreName("새 이름");  // ❌ 절대 금지!
    storeRepository.save(store);    // ❌ 절대 금지!
}
```

**올바른 방법**:
- 다른 도메인의 데이터를 수정해야 한다면 담당 팀원에게 요청
- 또는 담당 팀원이 제공하는 Service 메서드 호출

---

## 🧪 테스트 파일 수정 범위

### ✅ 수정 가능
```
src/test/java/com/eatsfine/domain/
├── booking/          # 내 담당 도메인 테스트
│   ├── service/
│   ├── controller/
│   └── repository/
│
└── payment/          # 내 담당 도메인 테스트
    └── ...
```

### 🚫 수정 금지
```
src/test/java/com/eatsfine/domain/
├── store/            # 다른 팀원 담당
├── user/             # 다른 팀원 담당
└── ...
```

---

## 🎯 내 작업 우선순위

1. **High**: 내 담당 도메인의 핵심 기능 구현
2. **Medium**: 내 담당 도메인의 테스트 작성
3. **Low**: 코드 리팩토링 및 최적화

---

## 📌 추가 개인 규칙

여기에 개인적으로 지키고 싶은 규칙을 추가하세요.

예시:
- 매일 오전 9시에 develop 브랜치 최신화
- 커밋 전에 항상 `./gradlew test` 실행
- 하루에 최소 1개 이상의 이슈 완료
- 주석은 한글로 작성

---

## 🚨 Claude Code에게 전달할 지시사항

**Claude Code는 이 파일을 읽고 다음 규칙을 반드시 준수해야 합니다**:

### 1. 도메인 수정 제한
- **절대 수정 금지**: 위에 나열된 "절대 수정 금지 도메인"의 모든 파일
- **수정 가능**: "내가 담당하는 도메인"의 파일만 수정

### 2. 파일 수정 전 확인
모든 파일 수정 전에 다음을 확인:
```
이 파일이 내 담당 도메인에 속하는가?
├─ Yes → 수정 가능
└─ No → 절대 수정 금지, 사용자에게 알림
```

### 3. 다른 도메인 수정 요청 시 대응
사용자가 다른 팀원의 도메인을 수정하려고 할 때:
```
⚠️ 경고: [도메인명]은(는) 담당 도메인이 아닙니다.
이 도메인은 [담당자명] 팀원이 담당하고 있습니다.

수정이 필요한 경우:
1. Slack에서 담당 팀원에게 요청
2. 담당 팀원의 승인을 받은 후 진행
3. PR에 해당 내용 명시

계속 진행하시겠습니까?
```

### 4. global 패키지 수정 시도 시 대응
```
⚠️ 경고: global 패키지는 팀 전체가 공유하는 공통 모듈입니다.

수정 전 필수 단계:
1. Slack에 사전 공유
2. 팀 회의에서 논의
3. PR에 최소 2명 이상 승인 필요

정말로 수정하시겠습니까?
```

---

## 📖 사용 예시

### 시나리오 1: 담당 도메인 작업
```
사용자: "Booking 도메인에 취소 기능을 추가해줘"
Claude: ✅ Booking은 담당 도메인입니다. 취소 기능을 구현하겠습니다.
```

### 시나리오 2: 다른 팀원 도메인
```
사용자: "Store 도메인에 새로운 필드를 추가해줘"
Claude: ⚠️ Store 도메인은 팀원 A가 담당하고 있습니다.
        Slack에서 팀원 A에게 먼저 요청해주세요.
```

### 시나리오 3: 읽기 전용 사용
```
사용자: "Booking 생성 시 Store 정보를 조회해서 사용해줘"
Claude: ✅ Store 정보를 조회(읽기)만 하는 것은 가능합니다.
        StoreRepository.findById()를 사용하겠습니다.
```

---

## 💡 팁

1. **담당 도메인을 명확히 작성**하세요
   - 예: `booking`, `payment`, `user` 등

2. **자주 변경되는 도메인 목록**을 업데이트하세요

3. **팀원과 Slack**으로 소통하세요

4. **충돌 시 develop 브랜치**를 자주 pull 하세요

---

이 파일을 수정하여 사용하세요!
