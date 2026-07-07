# 포트폴리오 프로젝트 면접 대비 Q&A

> Spring Boot 기반 기술 블로그 게시판 프로젝트

---

## 1. 프로젝트 개요

**Q. 이 프로젝트는 무엇인가요?**

Spring Boot 3.2.5 기반의 기술 블로그 게시판 프로젝트입니다.  
회원가입/로그인, 게시글 CRUD, 댓글, 좋아요, 태그 기능을 구현했으며  
GitHub Actions를 통해 AWS Lightsail 서버에 자동 배포되는 구조입니다.

---

## 2. 기술 스택 관련

**Q. JPA와 MyBatis를 함께 쓴 이유가 무엇인가요?**

두 기술의 장단점을 실무처럼 경험해보기 위해 혼용했습니다.  
- **JPA**: 엔티티 중심의 단순 CRUD, 연관관계 처리(태그 M:N, 댓글 1:N)에 사용했습니다.  
- **MyBatis**: 동적 검색 쿼리(키워드+카테고리+페이지네이션)처럼 SQL을 직접 제어해야 할 때 사용했습니다.  
목록 조회처럼 복잡한 조건이 붙는 쿼리는 MyBatis XML로 관리하는 것이 가독성과 유지보수 면에서 유리하다고 판단했습니다.

---

**Q. Spring Security를 어떻게 적용했나요?**

`SecurityFilterChain`을 직접 Bean으로 등록해 URL별 접근 권한을 설정했습니다.  
- 게시글 목록/상세는 비로그인 허용  
- 게시글 작성/수정/삭제, 댓글, 좋아요, 마이페이지는 로그인 필수  
- `CustomUserDetailsService`를 구현해 DB에서 사용자 정보를 조회합니다.  
- 비밀번호는 `BCryptPasswordEncoder`로 암호화해 저장합니다.  
- 커스텀 로그인 페이지를 별도로 구성했습니다.

---

**Q. 비밀번호는 어떻게 처리했나요?**

BCrypt 해시 알고리즘을 사용합니다.  
BCrypt는 Salt를 자동으로 추가하고 반복 연산으로 레인보우 테이블 공격에 강합니다.  
회원가입 시 `passwordEncoder.encode()`로 암호화하고, 로그인 시 Spring Security가 자동으로 `matches()`로 비교합니다.

---

**Q. 트랜잭션은 어떻게 관리했나요?**

`PostService`에 클래스 레벨로 `@Transactional(readOnly = true)`를 선언해  
기본적으로 모든 조회 메서드는 읽기 전용 트랜잭션으로 동작합니다.  
쓰기 작업(작성/수정/삭제/좋아요)이 필요한 메서드에만 `@Transactional`을 추가로 선언해 오버라이드했습니다.  
readOnly 옵션은 JPA의 변경 감지(dirty checking)를 비활성화해 성능을 높여줍니다.

---

## 3. 설계 관련

**Q. 엔티티 설계는 어떻게 했나요?**

- `User` - `Post`: 1:N (한 유저가 여러 게시글 작성)  
- `Post` - `Comment`: 1:N (한 게시글에 여러 댓글, `cascade = ALL`로 게시글 삭제 시 댓글도 삭제)  
- `Post` - `Tag`: N:M (`post_tags` 중간 테이블, `@ManyToMany`)  
- `Post` - `PostLike`: 1:N (좋아요 별도 엔티티로 관리, 중복 방지)  

**생성 메서드 패턴**을 사용해 외부에서 직접 필드를 설정하지 못하도록 `@NoArgsConstructor(access = PROTECTED)`로 기본 생성자를 막고, `static create()` 메서드로만 객체를 생성합니다.

---

**Q. 좋아요 중복 방지는 어떻게 구현했나요?**

`PostLike` 엔티티를 별도로 만들어 (Post + User) 조합을 저장합니다.  
좋아요 요청 시 `findByPostAndUser()`로 이미 존재하는지 확인하고,  
- 존재하면 → 삭제 (취소)  
- 없으면 → 저장 (추가)  
이 방식으로 토글 기능을 구현했습니다.

---

**Q. 조회수는 어떻게 처리했나요?**

게시글 상세 조회 시 `@Transactional` 메서드 안에서 `post.increaseViewCount()`를 호출합니다.  
JPA 변경 감지(dirty checking)에 의해 트랜잭션 종료 시 자동으로 UPDATE 쿼리가 실행됩니다.  
단, 수정 페이지에서는 조회수가 올라가면 안 되므로 `findByIdReadOnly()`를 별도로 만들어 `@Transactional`을 붙이지 않았습니다.

---

**Q. 태그 기능은 어떻게 구현했나요?**

쉼표로 구분된 문자열을 입력받아 파싱합니다.  
- `#` 기호 자동 제거  
- 최대 5개 제한  
- 이미 존재하는 태그면 재사용, 없으면 새로 생성 (`orElseGet`)  
- Post와 Tag는 `@ManyToMany`로 연결, `post_tags` 중간 테이블 사용

---

## 4. 예외 처리

**Q. 예외 처리는 어떻게 했나요?**

`@ControllerAdvice`를 사용한 `GlobalExceptionHandler`를 구현했습니다.  
- `IllegalArgumentException`: 비즈니스 오류 (존재하지 않는 게시글, 권한 없음 등) → `error/business-error` 페이지로 이동  
- `Exception`: 서버 오류 → `error/server-error` 페이지로 이동  

서비스 레이어에서 `throw new IllegalArgumentException("메시지")`로 예외를 던지면  
핸들러가 이를 잡아 사용자 친화적인 에러 페이지를 보여줍니다.

---

## 5. CI/CD 관련

**Q. CI/CD를 어떻게 구성했나요?**

GitHub Actions를 사용했습니다.  
`main` 브랜치에 Push하면 다음 순서로 자동 실행됩니다.

1. 코드 체크아웃  
2. JDK 21 세팅  
3. Maven 빌드 + 테스트 (`mvn package`)  
4. SSH로 AWS Lightsail 서버 접속  
5. JAR 파일 전송  
6. 기존 프로세스 종료 후 새 JAR 실행  

SSH 키는 GitHub Secrets에 Base64로 인코딩해 저장하고, Actions에서 디코딩해 사용합니다.

---

**Q. 테스트는 어떻게 작성했나요?**

JUnit 5와 Spring Boot Test를 사용했습니다.  
- `@SpringBootTest`: 통합 테스트 (UserRepository, PostRepository)  
- `@WebMvcTest`: 컨트롤러 단위 테스트 (AuthController, BoardController)  
- `@ExtendWith(MockitoExtension)`: 서비스 단위 테스트 (PostService, CommentService, UserService)  
- 테스트 DB는 H2 인메모리 DB를 사용해 운영 DB와 분리했습니다.  
- Spring Security 테스트는 `@WithMockUser`로 인증된 사용자를 모킹했습니다.

---

## 6. REST API + Swagger

**Q. REST API는 왜 추가했나요?**

기존 프로젝트는 Thymeleaf 서버 사이드 렌더링(SSR) 방식으로 구현됐습니다.  
여기에 REST API 레이어를 추가한 이유는 세 가지입니다.

1. **프론트엔드 분리 대응**: React, Vue 같은 SPA 프레임워크와 연동할 수 있는 구조를 갖추기 위해
2. **모바일 앱 확장 가능성**: 앱 클라이언트는 HTML이 아닌 JSON 데이터가 필요하기 때문에
3. **기술 역량 증명**: RESTful API 설계 경험을 직접 구현해 보여주기 위해

---

**Q. REST API 구조는 어떻게 설계했나요?**

`/api/v1/` 접두사로 기존 MVC 컨트롤러와 URL 충돌 없이 공존하도록 설계했습니다.

| 메서드 | URL | 설명 | 인증 |
|--------|-----|------|------|
| GET | `/api/v1/posts` | 게시글 목록 (검색/페이지네이션) | 불필요 |
| GET | `/api/v1/posts/{id}` | 게시글 상세 | 불필요 |
| POST | `/api/v1/posts` | 게시글 작성 | 필요 |
| PUT | `/api/v1/posts/{id}` | 게시글 수정 | 필요 (작성자만) |
| DELETE | `/api/v1/posts/{id}` | 게시글 삭제 | 필요 (작성자만) |
| GET | `/api/v1/comments?postId=` | 댓글 목록 | 불필요 |
| POST | `/api/v1/comments` | 댓글 작성 | 필요 |
| DELETE | `/api/v1/comments/{id}` | 댓글 삭제 | 필요 (작성자만) |
| GET | `/api/v1/users/me` | 내 정보 조회 | 필요 |

---

**Q. 공통 응답 포맷은 어떻게 처리했나요?**

`ApiResponse<T>` 제네릭 래퍼 클래스로 모든 응답을 통일했습니다.

```json
{
  "success": true,
  "message": "ok",
  "data": { ... }
}
```

성공/실패 여부를 `success` 필드로 구분하므로 클라이언트가 HTTP 상태 코드 외에도 명확하게 결과를 판단할 수 있습니다.  
에러 응답도 동일한 포맷으로 반환합니다.

```json
{
  "success": false,
  "message": "존재하지 않는 게시글입니다.",
  "data": null
}
```

---

**Q. API 예외 처리는 기존 MVC와 어떻게 분리했나요?**

기존 `GlobalExceptionHandler`는 `@ControllerAdvice`로 View(HTML)를 반환합니다.  
REST API는 JSON 응답이 필요하므로 `ApiExceptionHandler`를 별도로 만들었습니다.

```java
@RestControllerAdvice(basePackages = "com.board.api")
public class ApiExceptionHandler { ... }
```

`basePackages`를 지정해 `com.board.api` 패키지 하위의 컨트롤러에서 발생한 예외만 처리합니다.  
Spring은 더 구체적인 핸들러를 우선 적용하므로 기존 핸들러와 충돌이 없습니다.

---

**Q. CSRF 처리는 어떻게 했나요?**

Spring Security의 CSRF 보호는 폼 기반 웹에서 필요하지만, REST API는 `Authorization` 헤더 기반 인증을 사용하므로 API 경로에서는 불필요합니다.  
JWT 도입 후 SecurityFilterChain을 두 개로 분리해 API 체인에서는 CSRF를 완전히 비활성화했습니다.

```java
// API 전용 체인 - CSRF 비활성화
http.securityMatcher("/api/**").csrf(csrf -> csrf.disable())

// MVC 전용 체인 - CSRF 그대로 유지 (Thymeleaf 폼에 _csrf 토큰 적용)
```

---

**Q. Swagger UI를 도입한 이유가 무엇인가요?**

`springdoc-openapi 2.5.0`을 사용해 자동으로 API 문서를 생성합니다.  
`/swagger-ui.html`에 접속하면 모든 엔드포인트 목록, 요청/응답 스펙을 확인하고 브라우저에서 직접 호출해볼 수 있습니다.

도입 이유는 두 가지입니다.

1. **문서 자동화**: 코드에 `@Operation(summary = "게시글 작성")` 한 줄만 추가하면 문서가 자동 생성됩니다. 코드와 문서가 항상 일치합니다.
2. **테스트 편의성**: Postman 없이 브라우저에서 바로 API를 호출해 결과를 확인할 수 있습니다.

---

**Q. 입력값 검증은 어떻게 했나요?**

요청 DTO에 `@Valid` + Bean Validation 어노테이션을 적용했습니다.

```java
@NotBlank(message = "제목을 입력해주세요.")
@Size(max = 200, message = "제목은 200자 이내로 입력해주세요.")
private String title;
```

검증 실패 시 `MethodArgumentNotValidException`이 발생하고, `ApiExceptionHandler`가 이를 잡아  
실패한 필드의 메시지를 모아 `ApiResponse.fail("제목을 입력해주세요.")` 형태로 반환합니다.

---

## 7. JWT 인증

**Q. JWT를 도입한 이유가 무엇인가요?**

기존 REST API는 세션 쿠키 기반 인증을 사용했는데, 세션 방식은 서버가 상태를 유지해야 하므로 수평 확장(Scale-out) 시 세션 공유 문제가 발생합니다.  
JWT는 토큰 자체에 인증 정보를 포함하므로 서버가 상태를 저장하지 않아도 되는 **Stateless** 방식입니다.

---

**Q. Access Token과 Refresh Token을 나눈 이유가 무엇인가요?**

- **Access Token**: 짧은 만료 시간(30분)으로 보안성 확보. 탈취되더라도 피해를 최소화할 수 있습니다.
- **Refresh Token**: 긴 만료 시간(7일)으로 사용자 편의성 확보. Access Token이 만료되면 재로그인 없이 새 토큰을 발급받습니다.

두 토큰을 분리하지 않으면 긴 만료 시간의 토큰 하나만 사용하게 되어, 탈취 시 장기간 악용될 수 있습니다.

---

**Q. JWT 인증 흐름을 설명해주세요.**

```
① POST /api/v1/auth/login {username, password}
   → AuthenticationManager로 자격증명 검증
   → Access Token(30분) + Refresh Token(7일) 반환
   → Refresh Token은 DB(refresh_tokens 테이블)에 저장

② API 호출: Authorization: Bearer <accessToken> 헤더 포함
   → JwtAuthenticationFilter가 토큰 추출 → 검증 → SecurityContext 등록

③ Access Token 만료 시: POST /api/v1/auth/refresh {refreshToken}
   → DB에 저장된 토큰과 일치 여부 확인 (탈취 방지)
   → 새 Access Token + 새 Refresh Token 발급 (Token Rotation)

④ POST /api/v1/auth/logout
   → DB에서 Refresh Token 삭제
```

---

**Q. Refresh Token을 DB에 저장한 이유가 무엇인가요?**

순수 JWT 방식은 서버에서 토큰을 취소(revoke)할 수 없습니다.  
Refresh Token을 DB에 저장하면:
1. **탈취 방지**: 클라이언트가 보낸 Refresh Token과 DB 저장값을 비교해 불일치 시 거부합니다.
2. **강제 로그아웃**: 로그아웃 시 DB에서 삭제하면 더 이상 토큰 갱신이 불가능합니다.
3. **Token Rotation**: 갱신 시마다 새 Refresh Token을 DB에 덮어쓰므로, 이전 토큰은 즉시 무효화됩니다.

---

**Q. Token Rotation이란 무엇인가요?**

Refresh Token을 사용할 때마다 새 Refresh Token으로 교체하는 방식입니다.

```
기존: Refresh Token A → (갱신) → 새 Access Token + Refresh Token A (재사용)
RTR: Refresh Token A → (갱신) → 새 Access Token + 새 Refresh Token B
```

Refresh Token A가 탈취됐더라도, 정상 사용자가 먼저 갱신하면 A는 무효화됩니다.  
이후 탈취자가 A로 갱신 시도 시 DB에 B가 저장되어 있으므로 불일치 → 거부됩니다.

---

**Q. SecurityFilterChain을 두 개 사용한 이유가 무엇인가요?**

Thymeleaf MVC(세션 기반)와 REST API(JWT 기반)를 하나의 체인으로 관리하면 설정이 복잡해지고 충돌 가능성이 생깁니다.  
`@Order`로 우선순위를 지정해 두 개로 완전히 분리했습니다.

```java
@Bean @Order(1)  // API 전용: /api/** 매칭, Stateless, CSRF 비활성화, JWT 필터 적용
public SecurityFilterChain apiFilterChain(HttpSecurity http) { ... }

@Bean @Order(2)  // MVC 전용: 나머지 경로, Stateful, CSRF 활성화, 폼 로그인
public SecurityFilterChain mvcFilterChain(HttpSecurity http) { ... }
```

기존 화면(로그인, 게시판 등)은 변경 없이 그대로 동작하고, API만 JWT 인증이 적용됩니다.

---

**Q. JwtAuthenticationFilter는 어떻게 동작하나요?**

`OncePerRequestFilter`를 상속해 모든 요청에 한 번씩 실행됩니다.

```
1. Authorization 헤더에서 "Bearer <token>" 추출
2. JwtTokenProvider.validateToken()으로 서명/만료 검증
3. 유효하면 username 추출 → UserDetails 로드 → SecurityContext에 등록
4. 토큰이 없거나 유효하지 않으면 SecurityContext를 등록하지 않음
   → SecurityFilterChain의 인가 규칙이 401/403으로 처리
```

필터 자체는 인증 실패를 직접 응답하지 않고, 뒤의 인가 필터에 위임합니다.

---

**Q. JWT 시크릿 키는 어떻게 관리했나요?**

`application.yml`에 Base64 인코딩된 256비트 이상의 키를 설정했습니다.  
운영 환경에서는 환경변수나 AWS Secrets Manager로 교체해야 합니다.  
JJWT 0.12.x의 `Keys.hmacShaKeyFor()`를 사용해 HMAC-SHA256 알고리즘으로 서명합니다.

---

## 8. JWT API 실사용 테스트 및 버그 수정

**Q. JWT API를 실제 배포 환경에서 테스트하면서 어떤 문제가 발생했나요?**

총 3가지 버그를 발견하고 수정했습니다.

---

**버그 1. `@Transactional` self-call 문제 — Refresh Token이 DB에 반영 안 됨**

**현상**: 로그인 후 발급된 Refresh Token으로 갱신 요청 시 "Refresh Token이 유효하지 않습니다." 오류 발생

**원인**:  
`AuthApiController.login()`이 같은 클래스의 `saveOrUpdateRefreshToken()` 메서드를 직접 호출했습니다.  
Spring의 `@Transactional`은 AOP 프록시 방식으로 동작하기 때문에, **같은 클래스 내의 메서드 직접 호출(self-call)은 프록시를 거치지 않아** `@Transactional`이 무시됩니다.  
결과적으로 트랜잭션이 열리지 않아 기존 토큰에 `rotate()`를 호출해도 DB에 반영되지 않았습니다.

```java
// 잘못된 구조: self-call → @Transactional 무시됨
public ApiResponse<TokenResponse> login(...) {
    ...
    saveOrUpdateRefreshToken(username, refreshToken); // this.save... 호출
}

@Transactional // 실제로는 적용 안 됨
protected void saveOrUpdateRefreshToken(...) { ... }
```

**해결**: `RefreshTokenService`를 별도 Spring Bean으로 분리.  
다른 Bean의 메서드 호출은 AOP 프록시를 거치므로 `@Transactional`이 정상 적용됩니다.

```java
// 수정: 별도 서비스 Bean 호출 → 프록시 적용 → 트랜잭션 보장
refreshTokenService.saveOrUpdate(username, refreshToken);
```

---

**버그 2. API 인증 실패 시 401 대신 302 리다이렉트**

**현상**: 토큰 없이 `/api/v1/auth/logout` 호출 시 로그인 페이지로 302 리다이렉트

**원인**:  
`apiFilterChain`에 `exceptionHandling`을 설정하지 않아 Spring Security 기본 동작(폼 로그인 페이지로 리다이렉트)이 적용됐습니다.  
REST API 클라이언트는 리다이렉트를 처리할 수 없으므로 명시적으로 401을 반환해야 합니다.

**해결**: `HttpStatusEntryPoint(UNAUTHORIZED)` 추가

```java
.exceptionHandling(ex -> ex
    .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
)
```

---

**버그 3. 같은 초(second)에 생성된 Refresh Token이 동일한 토큰**

**현상**: Refresh Token Rotation 후 이전 토큰으로 재사용이 가능함 (탈취 방지 로직이 의미 없어짐)

**원인**:  
JWT 페이로드는 `{sub, iat, exp}` 조합으로 구성됩니다.  
`iat`(발급 시각)가 초 단위이므로 같은 초 안에 토큰을 두 번 생성하면 **payload가 완전히 동일**해지고, 서명도 같아 문자열이 동일한 토큰이 만들어집니다.  
Rotation을 했어도 OLD 토큰 = NEW 토큰이 되어 탈취 방지 체크가 무력화됐습니다.

```
OLD: eyJhbGc...nj8ZrFzcMqaOI1IEYxv0
NEW: eyJhbGc...nj8ZrFzcMqaOI1IEYxv0  ← 동일!
```

**해결**: `jti`(JWT ID) claim에 UUID 추가로 항상 유니크한 토큰 보장

```java
Jwts.builder()
    .subject(username)
    .id(UUID.randomUUID().toString()) // 고유 식별자 추가
    .issuedAt(now)
    .expiration(new Date(now.getTime() + refreshTokenExpiration))
    .signWith(getSigningKey())
    .compact();
```

---

**Q. 최종 테스트 결과는 어떻게 됐나요?**

| 시나리오 | 결과 |
|----------|------|
| 로그인 → Access + Refresh Token 발급 | ✅ |
| Access Token으로 게시글 목록 API 조회 | ✅ |
| 토큰 없이 보호된 API 호출 → HTTP 401 | ✅ |
| Refresh Token Rotation (OLD ≠ NEW 확인) | ✅ |
| 기존 Refresh Token 재사용 시도 → 실패 (탈취 방지) | ✅ |
| 새 Access Token으로 로그아웃 | ✅ |
| 로그아웃 후 Refresh Token 재사용 시도 → 실패 | ✅ |

---

**Q. `@Transactional` self-call 문제를 피하는 방법은 무엇인가요?**

세 가지 방법이 있습니다.

1. **별도 서비스 Bean 분리** (이 프로젝트에서 적용): 가장 깔끔한 방법. 책임 분리도 자연스럽게 이루어집니다.
2. **자기 자신을 주입 (`@Autowired private MyService self`)**: 프록시를 통해 self-call 가능하지만 순환 의존 문제와 코드 가독성 저하
3. **`@Transactional`을 외부 호출 진입점(상위 메서드)으로 이동**: 가장 간단하지만 트랜잭션 범위가 넓어지는 단점

실무에서는 서비스 레이어 분리가 가장 바람직합니다.

---

## 9. 개선 사항 / 추가 예정

**Q. 프로젝트에서 아쉬운 점이나 개선하고 싶은 부분이 있나요?**

- **Redis**: JWT Refresh Token 저장소를 MariaDB → Redis로 전환하면 만료 처리가 자동화되고 성능이 향상됩니다.
- **좋아요 N+1 문제**: 목록 조회 시 좋아요 수를 별도 쿼리로 가져오는 부분을 JOIN으로 최적화할 수 있습니다.
- **이미지 업로드**: S3 연동을 통한 이미지 첨부 기능을 추가할 예정입니다.
- **JWT 블랙리스트**: Access Token 만료 전 강제 무효화가 필요할 경우 Redis 블랙리스트로 처리할 수 있습니다.
