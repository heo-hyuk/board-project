# CodeLog

개발자 커뮤니티 게시판 포트폴리오 프로젝트

<br>

## 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 3.2.5 |
| ORM | JPA (정적 CRUD) + MyBatis (동적 검색 쿼리) |
| Security | Spring Security 6 |
| View | Thymeleaf |
| DB | MariaDB |
| Build | Maven |
| Deploy | AWS Lightsail (Ubuntu) |
| 기타 | Lombok, Bootstrap 5 |

<br>

## 주요 기능

### 회원
- 회원가입 / 로그인 / 로그아웃
- 마이페이지 (닉네임 변경, 비밀번호 변경)
- 내가 작성한 게시글 목록

### 게시글
- 카테고리별 게시글 (공지 / 자유 / 질문 / 프로젝트 / 취업·커리어)
- 게시글 CRUD (작성자 본인만 수정·삭제)
- 조회수 자동 증가
- 태그 기능 (최대 5개, 태그 클릭 시 필터링)
- 좋아요 토글 (AJAX, 비로그인 시 수만 표시)

### 댓글
- 게시글별 댓글 작성 / 삭제 (작성자 본인만)

### 검색 / 페이지네이션
- 제목 / 내용 / 제목+내용 통합 검색
- 카테고리·태그 필터
- 페이지네이션 (10개씩)

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
├── mapper/          # PostMapper (MyBatis)
├── repository/      # JPA Repository
└── service/         # PostService, CommentService, UserService
```

<br>

## DB 설계

```
users
 ├── id, username, password, email, nickname
 └── created_at, updated_at

posts
 ├── id, title, content, category, view_count
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

## 배포 방법

### 최초 서버 세팅 (1회)
```bash
sudo apt update && sudo apt install -y git maven
git clone https://github.com/heo-hyuk/board-project.git
```

### 업데이트 배포
```bash
cd ~/board-project && git pull
mvn package -DskipTests
pkill -f 'java -jar'
nohup java -jar target/board-project-0.0.1-SNAPSHOT.jar > ~/board-project/app.log 2>&1 &
```

### 로그 확인
```bash
tail -f ~/board-project/app.log
```

<br>

## 화면 구성

| 페이지 | 경로 |
|--------|------|
| 게시글 목록 | `/board` |
| 게시글 상세 | `/board/{id}` |
| 게시글 작성 | `/board/write` |
| 로그인 | `/auth/login` |
| 회원가입 | `/auth/join` |
| 마이페이지 | `/user/mypage` |
