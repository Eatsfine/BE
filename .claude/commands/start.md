# 작업 시작

인자 형식: `/start {이슈번호} {타입} {설명}`
예시: `/start 123 feat cancel-booking`

$ARGUMENTS: $ARGUMENTS

---

## 실행 단계

### Step 0: 정보 수집

`$ARGUMENTS`를 파싱합니다:
- 첫 번째 토큰 = 이슈번호 (숫자만, `#` 없이)
- 두 번째 토큰 = 타입 (`feat` / `fix` / `refactor` / `chore` / `docs`)
- 세 번째 이후 = 설명 (영문 kebab-case)

`$ARGUMENTS`가 비어있거나 정보가 부족하면 사용자에게 아래 3가지를 물어봅니다:
1. **이슈 번호** (예: `123`)
2. **타입** (`feat` / `fix` / `refactor` / `chore` / `docs`)
3. **간단한 설명** (영문 kebab-case, 예: `cancel-booking`)

### Step 1: develop 브랜치 최신화

아래 명령어를 순서대로 실행합니다:

```bash
git checkout develop
git pull origin develop
```

### Step 2: 작업 브랜치 생성

브랜치명 규칙: `{타입}/#${이슈번호}-{설명}`

수집한 정보로 브랜치를 생성합니다:

```bash
git checkout -b {타입}/#${이슈번호}-{설명}
```

### Step 3: 완료 확인

```bash
git branch --show-current
```

현재 브랜치명을 사용자에게 알리고 아래 내용을 안내합니다:
- GitHub 이슈 `#${이슈번호}` 체크리스트를 확인하며 개발 진행
- 커밋 메시지 형식: `{타입}: {설명} #{이슈번호}` (예: `feat: 예약 취소 서비스 구현 #123`)
- 구현 완료 후 `/pr-create` 로 PR 자동 생성
