# 💬 Discussion — 토론 플랫폼 백엔드

> 사용자들이 주제에 대해 찬반 의견을 나누고, 투표하고, 리액션을 남길 수 있는 토론 커뮤니티 서비스의 백엔드 API 서버입니다.

---

## 📌 프로젝트 소개

Discussion은 모바일/웹 환경에서 자유롭게 토론 주제를 게시하고, 찬성/반대 의견을 작성하며, FCM 기반 실시간 알림을 받을 수 있는 플랫폼입니다.

- 토론 게시글에 **찬성 / 반대 투표** 가능
- 의견(Opinion)에 **좋아요 / 싫어요 리액션** 가능
- 새로운 의견 작성 시 **FCM 푸시 알림** 발송
- **JWT 기반 인증**
- **Prometheus + Actuator** 기반 모니터링 지원

---

## 🛠️ 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 3.5.9 |
| Build | Gradle |
| Database | MySQL 8 |
| ORM | Spring Data JPA + Hibernate |
| Query | QueryDSL 5.0 |
| Auth | Spring Security + JWT (jjwt 0.11.5) |
| Push | Firebase Admin SDK (FCM) |
| Docs | SpringDoc OpenAPI (Swagger UI) |
| Monitoring | Spring Actuator + Micrometer (Prometheus) |
| Test | JUnit 5 + H2 (in-memory) |
| CI/CD | GitHub Actions → EC2 배포 |

---

## 📁 프로젝트 구조

```
src/main/java/com/discussion/ryu
├── config
│   ├── AsyncConfig.java
│   ├── FirebaseConfig.java
│   ├── JwtAuthenticationFilter.java
│   ├── JwtTokenProvider.java
│   ├── QueryDslConfig.java
│   ├── SecurityConfig.java
│   ├── SwaggerConfig.java
│   └── WebConfig.java
├── controller
│   ├── DiscussionPostController.java
│   ├── FcmController.java
│   ├── NotificationController.java
│   ├── OpinionController.java
│   └── UserController.java
├── service
│   ├── DiscussionPostService.java
│   ├── DiscussionVoteService.java
│   ├── FcmService.java
│   ├── FcmTokenService.java
│   ├── NotificationManagementService.java
│   ├── NotificationSendService.java
│   ├── OpinionReactionService.java
│   ├── OpinionService.java
│   └── UserService.java
├── repository
│   ├── DiscussionPostRepository.java
│   ├── DiscussionPostRepositoryCustom.java
│   ├── DiscussionPostSearchRepositoryImpl.java
│   └── ...
├── entity
│   ├── DiscussionPost.java
│   ├── DiscussionVote.java
│   ├── FcmToken.java
│   ├── Notification.java
│   ├── Opinion.java
│   ├── OpinionReaction.java
│   └── User.java
├── dto
│   ├── discussion/
│   ├── opinion/
│   ├── user/
│   └── fcm/
└── exception
    ├── GlobalExceptionHandler.java
    ├── discussion/
    ├── opinion/
    └── user/
```


---

## 📊 ERD

```
users
├── user_id (PK)
├── username (unique)
├── password
├── name (unique)
├── email (unique)
├── provider
├── provider_id
├── created_at
├── updated_at
└── deleted_at

discussion_posts
├── id (PK)
├── title
├── content
├── user_id (FK → users)
├── agree_count
├── disagree_count
├── created_at
├── updated_at
└── deleted_at

opinions
├── id (PK)
├── user_id (FK → users)
├── post_id (FK → discussion_posts)
├── content
├── stance (AGREE | DISAGREE)
├── like_count
├── dislike_count
├── created_at
├── updated_at
└── deleted_at

discussion_votes
├── id (PK)
├── user_id (FK → users)
├── post_id (FK → discussion_posts)
├── vote_type (AGREE | DISAGREE)
├── created_at
└── updated_at
* UNIQUE (user_id, post_id)

opinion_reactions
├── id (PK)
├── user_id (FK → users)
├── opinion_id (FK → opinions)
├── reaction_type (LIKE | DISLIKE)
└── created_at
* UNIQUE (user_id, opinion_id)

fcm_tokens
├── id (PK)
├── user_id (FK → users)
├── token (unique)
├── device_type (ANDROID | IOS | WEB)
├── created_at
└── updated_at

notifications
├── id (PK)
├── user_id (FK → users)
├── opinion_id (FK → opinions)
├── title
├── body
├── is_sent
└── created_at
```

**관계 요약**
- `users` 1:N `discussion_posts` — 한 유저가 여러 토론 게시글 작성
- `users` 1:N `opinions` — 한 유저가 여러 의견 작성
- `discussion_posts` 1:N `opinions` — 하나의 게시글에 여러 의견
- `users` 1:N `discussion_votes` — 게시글당 1표 제한 (유니크 제약)
- `users` 1:N `opinion_reactions` — 의견당 1리액션 제한 (유니크 제약)
- `users` 1:N `fcm_tokens` — 기기별 FCM 토큰 관리
- `users` 1:N `notifications` — 유저에게 발송된 알림 이력

---

## 🔔 알림 플로우

```
새 Opinion 작성
    │
    ▼
NotificationSendService   ← 알림 생성 및 FCM 발송 (비동기)
    │
    ├── FcmTokenService   ← 유저의 FCM 토큰 조회
    ├── FcmService        ← Firebase Admin SDK로 메시지 전송
    └── NotificationManagementService  ← Notification 엔티티 저장/관리
```

---

## 📡 API 엔드포인트

| 도메인 | Controller | 설명 |
|--------|-----------|------|
| 유저 | `UserController` | 회원가입, 로그인, 정보 수정 |
| 토론 | `DiscussionPostController` | 게시글 CRUD, 투표, 검색 (QueryDSL) |
| 의견 | `OpinionController` | 의견 CRUD, 리액션 |
| 알림 | `NotificationController` | 알림 목록 조회, 읽음 처리 |
| FCM | `FcmController` | FCM 토큰 등록/삭제 |



---

## ⚙️ 주요 설계 특징

- **소프트 딜리트**: 모든 핵심 엔티티(`users`, `discussion_posts`, `opinions`)에 `deleted_at` 컬럼 적용, `@SQLDelete` + `@SQLRestriction` 사용
- **비동기 알림**: `@Async` 기반 AsyncConfig으로 FCM 발송 비동기 처리
- **QueryDSL 검색**: `DiscussionPostSearchRepositoryImpl`로 정렬/필터 조건 동적 쿼리 처리
- **N+1 방지**: 연관 엔티티 `FetchType.LAZY` 적용 + 필요 시 fetch join
- **성능 모니터링**: `QueryCountingInterceptor`로 쿼리 수 추적, Prometheus 메트릭 노출

---

## 🚀 배포 구조 (CI/CD)

```
master 브랜치 push
    │
    ▼
GitHub Actions
    ├── JDK 21 설치
    ├── application-local.yml 주입 (GitHub Secrets)
    ├── ./gradlew clean build
    ├── SCP → EC2 /home/ubuntu/Discussion/tobe/project.jar 전송
    └── SSH → EC2
            ├── 기존 프로세스 종료 (fuser -k 8080)
            └── nohup java -jar project.jar &
```

---

## 🧪 테스트

```bash
./gradlew test
```

- H2 인메모리 DB로 독립적인 테스트 환경 구성
- `src/test/resources/application.yml` 별도 설정

---

## 🔧 로컬 실행 방법

1. `src/main/resources/application-local.yml` 작성
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/{db_name}
    username: {username}
    password: {password}

jwt:
  secret: {jwt_secret}

# Firebase Admin SDK 키 파일 경로
# src/main/resources/firebase/fcm-key.json
```

2. 빌드 및 실행
```bash
./gradlew clean build
java -jar build/libs/*.jar
```

---