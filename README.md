# BE
# 🍽️ Eatsfine BE

**Eatsfine 백엔드 레포지토리입니다.**

🖇️**Service URL**
https://www.eatsfine.co.kr

📚**API Documentation**
https://eatsfine.co.kr/swagger-ui/index.html

- Language: **Java 21**
- Framework: **Spring Boot 3.4.1**
- Database: **MySQL 8.0**, **Redis**
- ORM: **Spring Data JPA**, **QueryDSL 5.1.0**
- Security: **Spring Security**, **OAuth2 Client**, **JWT**
- Cloud & Infra: **AWS S3**, **Docker**, **GitHub Actions**
- Build Tool: **Gradle**

## 🔥 Git Commit Convention (커밋 규칙)

효율적인 협업을 위해 다음과 같은 커밋 메세지 규칙을 사용합니다.

**type은 대문자로 통일합니다.**

| 커밋 타입     | 설명                           |
| ------------- | ------------------------------ |
| 🎉 `FEAT`     | 새로운 기능 추가               |
| 🐛 `FIX`      | 버그/오류 수정                 |
| 🛠 `CHORE`    | 코드/내부 파일/설정 수정       |
| 📝 `DOCS`     | 문서 수정 (README 등)          |
| 🔄 `REFACTOR` | 코드 리팩토링 (기능 변경 없음) |
| 🧪 `TEST`     | 테스트 코드 추가/수정          |
| 🎨 `STYLE`    | 스타일 변경(포맷, 세미콜론 등) |

💻 **예시**

```bash
git commit -m "[FEAT]: 예약 생성 API 구현"
git commit -m "[FIX]: OAuth2 로그인 리다이렉트 오류 수정"
git commit -m "[CHORE]: SecurityConfig CORS 설정 변경"
```

## 📁 폴더 구조

<details>
  <summary>폴더 구조 펼치기/접기</summary>

```plaintext
src/main/java/com/eatsfine/eatsfine/
  ├── domain/             # 도메인별 비즈니스 로직
  │   ├── booking/        # 예약 관리
  │   ├── businesshours/  # 영업시간 관리
  │   ├── businessnumber/ # 사업자번호 검증
  │   ├── image/          # 이미지 처리
  │   ├── inquiry/        # 문의 관리
  │   ├── menu/           # 메뉴 관리
  │   ├── payment/        # 결제 시스템
  │   ├── region/         # 지역 관리
  │   ├── store/          # 식당 정보 관리
  │   ├── storetable/     # 식당 테이블 관리
  │   ├── table_layout/   # 테이블 배치도
  │   ├── tableblock/     # 테이블 블록 관리
  │   ├── tableimage/     # 테이블 이미지
  │   ├── term/           # 약관 관리
  │   └── user/           # 사용자(회원) 관리
  │
  └── global/             # 전역 설정 및 공통 모듈
      ├── annotation/     # 커스텀 어노테이션
      ├── apiPayload/     # 공통 응답/예외 처리 (ApiResponse)
      ├── auth/           # 보안/인증 로직 (CustomHandler 등)
      ├── common/         # 공통 유틸리티
      ├── config/         # 설정 파일 (Security, Swagger, QueryDSL 등)
      ├── controller/     # 공통 컨트롤러 (HealthCheck)
      ├── resolver/       # Argument Resolver
      ├── s3/             # AWS S3 연동
      └── validator/      # 커스텀 검증기
```
</details>

 
## 🌿 Branch

- main : 배포/최종 안정 브랜치 **(직접 push 금지)**
- develop: 개발 통합 브랜치 (기본 작업 브랜치)
- 작업 브랜치 네이밍:
  - `feat/booking-api`
  - `fix/oauth-login`
  - `chore/swagger-config`
  - `refactor/payment-service`

## 🎯 작업 루틴

기본 브랜치는 develop

작업은 항상 `develop`에서 브랜치를 따서 진행하고, PR은 develop으로 올립니다.

### 1. 작업 시작 전 (최신화)

```bash
git checkout develop
git pull --rebase origin develop
```

### 2. 작업 브랜치 생성

```bash
git checkout -b feat/featureName
```

### 3. 작업 후 커밋 & 푸시

```bash
git add .     # 필요하면 git add file명 으로 특정 파일만 추가해도 됨
git commit -m "feat: 자세한 내용 적기"
git push -u origin feat/featureName
```

### 4. PR 생성

- feat/<featureName> → develop 로 PR 생성
- PR 본문에 Closes #이슈번호 작성해서 merge 시 이슈가 자동으로 닫히도록 설정

```md
Closes #이슈번호
```

### 5. 리뷰 & 머지

- 최소 2명 승인 후 merge
- main은 배포/최종용 브랜치이기에 **직접 push 금지**

## 🔒 보안

- `application.yml` 및 민감정보는 절대 커밋 금지
- 공유가 필요한 환경변수는 `application-local.yml` 등을 통해 관리하거나 노션/슬랙을 통해 공유합니다.

## 👥 팀 규칙

- **작업 시작전 develop 최신화: git pull --rebase origin develop**
- PR은 가능한 작게 쪼개서 올리기
- PR에 작업 요약 + 테스트 결과 포함하기
- 충돌 발생 시 브랜치에서 먼저 해결 후 PR 업데이트

## 🛠️ 팀원 정보 ##

| 이름        | 주요 담당 업무                                 |
| --------- | ---------------------------------------- |
| 민토리 / 성민주 | 회원/인증, JWT 등 공통 보안 설정, 공통 응답 + 예외 처리 |
| 앤디 / 박영찬  |                                 |
| 영도 / 이도영  |                                |
| 준 / 손준규   |                               |
| 누리 / 정준영  |                                   |


## 💡 시작 방법

### 1. Clone & Install

```bash
git clone https://github.com/Eatsfine/BE.git
cd eatsfine-be
./gradlew clean build
```

### 2. Environment Values

DB 접속 정보 및 외부 API 키 등은 환경변수 또는 로컬 설정 파일로 관리합니다.
`src/main/resources/application-local.yml`을 생성하여 필요한 설정을 추가하세요.

<details>
  <summary>폴더 구조 펼치기/접기</summary>
  
```yaml
server:
  port: 8080
  profile: local

spring:
  config:
    activate:
      on-profile: local
  datasource:
    url: jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&serverTimezone=Asia/Seoul
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver
  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT}
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope:
              - email
              - profile
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
            authorization-grant-type: authorization_code
          kakao:
            client-id: ${KAKAO_CLIENT_ID}
            client-secret: ${KAKAO_CLIENT_SECRET}
            scope:
              - profile_nickname
              - profile_image
              - account_email
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
            authorization-grant-type: authorization_code
            client-name: Kakao
            provider: kakao
        provider:
          google:
            authorization-uri: https://accounts.google.com/o/oauth2/v2/auth
            token-uri: https://oauth2.googleapis.com/token
            user-info-uri: https://www.googleapis.com/oauth2/v3/userinfo
          kakao:
            authorization-uri: https://kauth.kakao.com/oauth/authorize
            token-uri: https://kauth.kakao.com/oauth/token
            user-info-uri: https://kapi.kakao.com/v2/user/me
            user-name-attribute: id

payment:
  toss:
    widget-secret-key: ${TOSS_WIDGET_SECRET_KEY}

cloud:
  aws:
    region: ${AWS_REGION}
    s3:
      bucket: ${AWS_S3_BUCKET}
      base-url: ${AWS_S3_BASE_URL}

jwt:
  secret: ${SECRET_KEY}
```
</details>

### 3. Run

```bash
./gradlew bootRun
```

### 4. API Docs (Swagger)

서버 실행 후 아래 주소로 접속하여 API 명세를 확인할 수 있습니다.
- Local: http://localhost:8080/swagger-ui/index.html
