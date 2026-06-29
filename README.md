# CodeLog — 기술 블로그 플랫폼

Java/Spring 기술 스택으로 구현한 포트폴리오 프로젝트입니다.  
마크다운 기반 글쓰기, 카테고리/태그 분류, 좋아요·댓글 등 실서비스 수준의 기능을 갖춘 기술 블로그 플랫폼입니다.

🔗 **배포 주소:** http://43.201.55.31:8080

<br>

## 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 3.2.5 |
| ORM | JPA / Hibernate (CRUD) + MyBatis (동적 검색 쿼리) |
| Security | Spring Security 6 (BCrypt, 세션 인증) |
| View | Thymeleaf |
| DB | MariaDB |
| Build | Maven |
| Deploy | AWS Lightsail (Ubuntu) |
| 기타 | Lombok, Bootstrap 5, EasyMDE, marked.js, highlight.js |

<br>

## 주요 기능

### 회원
- 회원가입 / 로그인 / 로그아웃
- 마이페이지 — 자기소개(bio) · 닉네임 · 비밀번호 변경
- 내가 작성한 포스트 목록

### 포스트
- **마크다운 에디터** (EasyMDE) — 실시간 미리보기 지원
- **마크다운 렌더링** (marked.js + DOMPurify XSS 방지 + highlight.js 코드 하이라이팅)
- 기술 카테고리 분류 (Java / Spring / Web / DevOps / 알고리즘 / CS지식 / 회고)
- 포스트 CRUD — 작성자 본인만 수정·삭제
- 요약문(summary) — 카드 목록에 표시되는 한 줄 소개
- 태그 기능 (최대 5개, 클릭 시 필터링)
- 조회수 자동 증가
- 좋아요 토글 (AJAX, 비로그인 시 수만 표시)

### 댓글
- 게시글별 댓글 작성 / 삭제 (작성자 본인만)

### 검색 / 필터링
- 제목 / 내용 / 제목+내용 통합 검색 (MyBatis 동적 쿼리)
- 카테고리·태그 필터
- 페이지네이션 (10개씩)

<br>

## 기술적 특징

### JPA + MyBatis 하이브리드 설계
단순 CRUD는 JPA, 복잡한 동적 검색 쿼리는 MyBatis로 역할을 분리했습니다.  
두 기술의 장점을 상황에 맞게 활용하는 설계를 경험할 수 있습니다.

### Spring Security 6 인증/인가
BCrypt 비밀번호 암호화, 경로별 접근 권한 제어, CSRF 보호를 적용했습니다.  
`CustomUserDetailsService`로 DB 기반 인증을 구현했습니다.

### 마크다운 + XSS 방지
EasyMDE로 작성된 마크다운을 서버에 그대로 저장하고, 클라이언트에서 marked.js로 렌더링합니다.  
DOMPurify를 통해 XSS 공격을 방지합니다.

<br>

## 프로젝트 구조

```
src/main/java/com/board
├── config/          # SecurityConfig
├── controller/      # BoardController, AuthController, CommentController
│                      LikeController, UserController, HomeController
├── domain/          # Post, User, Comment, Tag, PostLike
├── dto/             # PostDto, PostSearchDto, CommentDto, UserJoinDto
├── exception/       # GlobalExceptionHandler
├── mapper/          # PostMapper.java + PostMapper.xml (MyBatis)
├── repository/      # JPA Repository 인터페이스
└── service/         # PostService, CommentService, UserService
```

<br>

## DB 설계

```
users
 ├── id, username, password, email, nickname, bio
 └── created_at, updated_at

posts
 ├── id, title, summary, content, category, view_count
 ├── user_id (FK → users)
 └── created_at, updated_at

comments
 ├── id, content
 ├── post_id (FK → posts), user_id (FK → users)
 └── created_at

tags
 └── id, name

post_tags
 ├── post_id (FK → posts)
 └── tag_id (FK → tags)

post_likes
 ├── id
 ├── post_id (FK → posts), user_id (FK → users)
 └── created_at
```

<br>

## 화면 구성

| 페이지 | 경로 | 설명 |
|--------|------|------|
| 홈 | `/` | 히어로 섹션 + 최신 포스트 6개 카드 |
| 포스트 목록 | `/board` | 카테고리 탭 + 카드 그리드 + 검색 |
| 포스트 상세 | `/board/{id}` | 마크다운 렌더링 + 댓글 |
| 글쓰기 | `/board/write` | EasyMDE 마크다운 에디터 |
| 로그인 | `/auth/login` | — |
| 회원가입 | `/auth/join` | — |
| 마이페이지 | `/user/mypage` | 개발자 프로필 + 설정 |

<br>

## 로컬 실행 방법

```bash
# 1. MariaDB 실행 (Docker)
docker-compose up -d

# 2. 애플리케이션 실행
mvn spring-boot:run
```

`application.yml`의 DB 접속 정보를 환경에 맞게 수정 후 실행하세요.

<br>

## 배포 (AWS Lightsail)

```bash
# deploy.sh 실행 (빌드 → 전송 → 재시작 자동화)
bash deploy.sh
```
