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

## 6. 개선 사항 / 추가 예정

**Q. 프로젝트에서 아쉬운 점이나 개선하고 싶은 부분이 있나요?**

- **REST API + JWT 인증**: 현재는 세션 기반 인증인데, REST API로 전환하고 JWT를 적용할 계획입니다.  
- **Redis**: 세션 또는 캐싱 용도로 도입을 고려하고 있습니다.  
- **좋아요 N+1 문제**: 목록 조회 시 좋아요 수를 별도 쿼리로 가져오는 부분을 JOIN으로 최적화할 수 있습니다.  
- **이미지 업로드**: 현재는 텍스트만 작성 가능한데 S3 연동을 통한 이미지 첨부 기능을 추가할 예정입니다.
