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
| ORM | JPA / Hibernate + MyBatis (하이브리드) |
| Security | Spring Security 6 (BCrypt, 세션 인증) |
| View | Thymeleaf + Bootstrap 5 |
| DB | MariaDB |
| Build | Maven |
| Deploy | AWS Lightsail (Ubuntu) |
| 기타 | Lombok, EasyMDE, marked.js, highlight.js, DOMPurify |

<br>

## 주요 기능

### 회원
- 회원가입 / 로그인 / 로그아웃
- 마이페이지 — 자기소개(bio) · 닉네임 · 비밀번호 변경
- 내가 작성한 포스트 목록

### 포스트
- **마크다운 에디터** (EasyMDE) — 실시간 미리보기
- **마크다운 렌더링** (marked.js + DOMPurify XSS 방지 + highlight.js 코드 하이라이팅)
- 기술 카테고리 (Java / Spring / Web / DevOps / 알고리즘 / CS지식 / 회고)
- 포스트 CRUD — 작성자 본인만 수정·삭제
- 요약문(summary) — 카드 목록 한 줄 소개
- 태그 (최대 5개, 클릭 시 필터링)
- 조회수 자동 증가 / 좋아요 토글

### 검색 / 필터링
- 제목 / 내용 / 통합 검색 (MyBatis 동적 쿼리)
- 카테고리·태그 필터 + 페이지네이션

<br>

## 기술적 특징

### JPA + MyBatis 하이브리드 설계

| 역할 | 기술 |
|------|------|
| 게시글 등록 / 수정 / 삭제 | **MyBatis** (insertPost, updatePost, deletePost) |
| 게시글 목록 검색 + 페이지네이션 | **MyBatis** (동적 WHERE절, JOIN, 서브쿼리) |
| 댓글 / 좋아요 / 태그 CRUD | **JPA** (Spring Data Repository) |
| DDL 관리 (CREATE / ALTER / DROP) | **MyBatis** (PostMapper.xml + DatabaseInitializer) |
| 데이터사전 조회 | **MyBatis** (INFORMATION_SCHEMA) |

### DB 스키마 자동 관리 (DatabaseInitializer)
앱 시작 시 `DatabaseInitializer`가 MyBatis Mapper를 통해 DDL을 실행합니다.
- `CREATE TABLE post_stats` — 일별 조회 통계 테이블
- `ALTER TABLE posts ADD COLUMN thumbnail_url` — 컬럼 추가
- `CREATE INDEX idx_posts_category` — 카테고리 조회 성능 향상
- `CREATE INDEX idx_posts_created_at` — 최신순 정렬 성능 향상
- `CREATE OR REPLACE VIEW v_post_summary` — posts + users JOIN 뷰

### Spring Security 6
BCrypt 비밀번호 암호화, 경로별 접근 권한 제어, CSRF 보호, `CustomUserDetailsService` DB 기반 인증

### 반응형 UI
Bootstrap 5 기반, 모바일 햄버거 메뉴 드롭다운 (헤더 아래 표시, 좌측 정렬)

<br>

## 프로젝트 구조

```
src/main/java/com/board
├── config/          # SecurityConfig, DatabaseInitializer (DDL 자동 실행)
├── controller/      # BoardController, AuthController, CommentController
│                      LikeController, UserController, HomeController
├── domain/          # Post, User, Comment, Tag, PostLike
├── dto/             # PostDto, PostSearchDto, CommentDto, UserJoinDto
├── exception/       # GlobalExceptionHandler
├── mapper/          # PostMapper.java + PostMapper.xml (MyBatis CRUD + DDL + 데이터사전)
├── repository/      # JPA Repository 인터페이스 5개
└── service/         # PostService, CommentService, UserService

src/main/resources
├── mapper/          # PostMapper.xml
├── templates/       # Thymeleaf 템플릿
├── static/css/      # style.css
├── schema.sql       # 전체 스키마 문서
└── application.yml
```

<br>

## DB 설계

```
users
 ├── id, username(UNIQUE), password, email(UNIQUE), nickname, bio
 └── created_at, updated_at

posts
 ├── id, title, summary, content, category, view_count, thumbnail_url
 ├── user_id (FK → users, CASCADE)
 └── created_at, updated_at

comments
 ├── id, content
 ├── post_id (FK → posts, CASCADE), user_id (FK → users, CASCADE)
 └── created_at

tags        — id, name(UNIQUE)
post_tags   — post_id(FK), tag_id(FK)   [N:M]
post_likes  — id, post_id(FK), user_id(FK), UNIQUE(post_id,user_id)
post_stats  — id, post_id(FK), view_date, view_count  [일별 통계]

[인덱스]
idx_posts_category   ON posts(category)
idx_posts_created_at ON posts(created_at DESC)

[뷰]
v_post_summary — posts + users JOIN (category, view_count, comment_count, like_count 포함)
```

<br>

## 화면 구성

| 페이지 | 경로 | 설명 |
|--------|------|------|
| 홈 | `/` | 히어로 섹션 + 최신 포스트 6개 카드 |
| 포스트 목록 | `/board` | 카테고리 탭 + 카드 그리드 + 검색 |
| 포스트 상세 | `/board/{id}` | 마크다운 렌더링 + 댓글 + 좋아요 |
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
# 코드 push 후 서버에서 pull + 빌드 + 재시작
ssh -i "C:\aws\LightsailDefaultKey-ap-northeast-2.pem" ubuntu@43.201.55.31 \
  "cd ~/board-project && git pull origin main && mvn clean package -DskipTests -q \
   && pkill -f 'java -jar'; sleep 2 \
   && nohup java -jar target/board-project-0.0.1-SNAPSHOT.jar > ~/app/app.log 2>&1 &"
```

> 자세한 작업 이력 및 다음 작업 목록은 [NOTES.md](./NOTES.md) 참고
