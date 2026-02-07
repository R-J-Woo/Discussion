# 테스트 가이드

## 개요
이 프로젝트는 Repository, Service, Controller 계층별로 분리된 테스트 코드를 제공합니다.

## 테스트 구조

### 1. Repository 계층 테스트 (`@DataJpaTest`)
- **위치**: `src/test/java/com/discussion/ryu/repository/`
- **특징**:
  - H2 인메모리 데이터베이스 사용
  - JPA 관련 설정만 로드하여 빠른 테스트 실행
  - 실제 데이터베이스 작업 검증

#### 예시 파일:
- `UserRepositoryTest.java`
- `DiscussionPostRepositoryTest.java`

### 2. Service 계층 테스트 (`@ExtendWith(MockitoExtension.class)`)
- **위치**: `src/test/java/com/discussion/ryu/service/`
- **특징**:
  - Mockito를 사용한 단위 테스트
  - 의존성을 Mock으로 대체하여 격리된 테스트
  - 비즈니스 로직 검증에 집중

#### 예시 파일:
- `UserServiceTest.java`

### 3. Controller 계층 테스트 (`@WebMvcTest`)
- **위치**: `src/test/java/com/discussion/ryu/controller/`
- **특징**:
  - MockMvc를 사용한 HTTP 요청/응답 테스트
  - Spring Security 설정 포함
  - Service 계층은 Mock으로 대체

#### 예시 파일:
- `UserControllerTest.java`

### 4. 통합 테스트 (`@SpringBootTest`)
- **위치**: `src/test/java/com/discussion/ryu/integration/`
- **특징**:
  - 전체 애플리케이션 컨텍스트 로드
  - 실제 환경과 유사한 통합 시나리오 테스트
  - 여러 계층을 함께 테스트

#### 예시 파일:
- `UserIntegrationTest.java`

## 테스트 실행 방법

### 전체 테스트 실행
```bash
./gradlew test
```

### 특정 테스트 클래스만 실행
```bash
./gradlew test --tests UserRepositoryTest
./gradlew test --tests UserServiceTest
./gradlew test --tests UserControllerTest
```

### 특정 테스트 메서드만 실행
```bash
./gradlew test --tests UserServiceTest.signup_Success
```

### IntelliJ IDEA에서 실행
1. 테스트 클래스/메서드 옆의 녹색 화살표 클릭
2. 또는 `Ctrl + Shift + F10` (Windows/Linux) / `Ctrl + Shift + R` (Mac)

## 테스트 설정

### 테스트용 application.yml
- **위치**: `src/test/resources/application.yml`
- **주요 설정**:
  - H2 인메모리 데이터베이스
  - JPA DDL auto: create-drop
  - JWT 테스트 설정

## 주요 테스트 케이스

### UserRepositoryTest
- ✅ 사용자 저장
- ✅ username으로 조회
- ✅ username 존재 여부 확인
- ✅ name 중복 확인
- ✅ provider와 providerId로 조회

### UserServiceTest
- ✅ 회원가입 성공/실패 (중복 username, 중복 name)
- ✅ 로그인 성공/실패 (존재하지 않는 사용자, 비밀번호 불일치)
- ✅ 내 정보 조회
- ✅ 내 정보 수정 성공/실패 (중복 닉네임)
- ✅ 비밀번호 변경 성공/실패 (현재 비밀번호 불일치, 새 비밀번호 불일치, 기존 비밀번호와 동일)
- ✅ 사용자 탈퇴

### UserControllerTest
- ✅ 회원가입 API 테스트 (성공/실패)
- ✅ 로그인 API 테스트 (성공/실패)
- ✅ 내 정보 조회 API 테스트
- ✅ 내 정보 수정 API 테스트
- ✅ 비밀번호 변경 API 테스트
- ✅ 사용자 탈퇴 API 테스트

### UserIntegrationTest
- ✅ 회원가입 후 로그인 통합 시나리오
- ✅ 중복 username 검증
- ✅ 잘못된 비밀번호로 로그인 시도

### DiscussionPostRepositoryTest
- ✅ 토론글 저장
- ✅ 토론글 조회
- ✅ 전체 토론글 페이징 조회
- ✅ 사용자별 토론글 조회
- ✅ 토론글 수정
- ✅ 토론글 삭제

## 테스트 커버리지 확인

### Gradle을 통한 커버리지 확인
```bash
./gradlew test jacocoTestReport
```

커버리지 리포트는 `build/reports/jacoco/test/html/index.html`에서 확인 가능합니다.

## 테스트 작성 가이드

### 1. Repository 테스트 작성 시
```java
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Repository 이름 테스트")
class SomeRepositoryTest {
    @Autowired
    private SomeRepository someRepository;
    
    @Test
    @DisplayName("테스트 시나리오 설명")
    void testMethod() {
        // given
        // when
        // then
    }
}
```

### 2. Service 테스트 작성 시
```java
@ExtendWith(MockitoExtension.class)
@DisplayName("Service 이름 테스트")
class SomeServiceTest {
    @Mock
    private SomeRepository someRepository;
    
    @InjectMocks
    private SomeService someService;
    
    @Test
    @DisplayName("테스트 시나리오 설명")
    void testMethod() {
        // given - Mock 설정
        given(someRepository.someMethod()).willReturn(expected);
        
        // when - 실제 실행
        // then - 검증
    }
}
```

### 3. Controller 테스트 작성 시
```java
@WebMvcTest(SomeController.class)
@ActiveProfiles("test")
@DisplayName("Controller 이름 테스트")
class SomeControllerTest {
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private SomeService someService;
    
    @Test
    @DisplayName("API 테스트 시나리오")
    void testApi() throws Exception {
        mockMvc.perform(post("/api/endpoint")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.field").value(expected));
    }
}
```

## 테스트 베스트 프랙티스

1. **Given-When-Then 패턴 사용**: 테스트 구조를 명확히 합니다.
2. **@DisplayName 활용**: 테스트 의도를 명확히 표현합니다.
3. **독립적인 테스트**: 각 테스트는 서로 영향을 주지 않아야 합니다.
4. **@BeforeEach 활용**: 공통 설정을 재사용합니다.
5. **AssertJ 사용**: 가독성 높은 assertion을 작성합니다.
6. **Mock 최소화**: 필요한 경우에만 Mock을 사용합니다.

## 문제 해결

### H2 데이터베이스 연결 오류
- `application.yml`에서 H2 설정 확인
- MODE=MySQL 설정 확인

### Security 관련 오류
- `@WithMockUser` 또는 `.with(user(testUser))` 사용
- CSRF 토큰: `.with(csrf())` 추가

### MockBean 주입 오류
- `@MockBean`으로 필요한 빈 모두 선언
- `@WebMvcTest`의 controllers 속성 확인

## 추가 정보

- Spring Boot Test 문서: https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing
- Mockito 문서: https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html
- AssertJ 문서: https://assertj.github.io/doc/
