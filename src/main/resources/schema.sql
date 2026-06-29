-- ==========================================================
-- 데이터베이스 초기화 스크립트 (board_db)
-- 기술: MariaDB / 사용: Spring Boot 3.2.5 + JPA + MyBatis
-- ==========================================================

-- 데이터베이스 생성
CREATE DATABASE IF NOT EXISTS board_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE board_db;

-- ==========================================================
-- [CREATE] 회원 테이블
-- ==========================================================
CREATE TABLE IF NOT EXISTS users (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY          COMMENT '회원 PK',
    username   VARCHAR(50)  NOT NULL UNIQUE               COMMENT '로그인 아이디 (UNIQUE)',
    password   VARCHAR(255) NOT NULL                      COMMENT '암호화된 비밀번호',
    email      VARCHAR(100) NOT NULL UNIQUE               COMMENT '이메일 (UNIQUE)',
    nickname   VARCHAR(50)  NOT NULL                      COMMENT '닉네임',
    created_at DATETIME     DEFAULT CURRENT_TIMESTAMP     COMMENT '가입일시',
    updated_at DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시'
) COMMENT '회원 테이블';

-- ==========================================================
-- [CREATE] 게시글 테이블
-- ==========================================================
CREATE TABLE IF NOT EXISTS posts (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY       COMMENT '게시글 PK',
    title         VARCHAR(200) NOT NULL                   COMMENT '제목 (NOT NULL)',
    summary       VARCHAR(300) NULL                       COMMENT '요약문 (선택)',
    content       TEXT         NOT NULL                   COMMENT '본문 (NOT NULL)',
    category      VARCHAR(20)  NOT NULL DEFAULT 'Java'    COMMENT '카테고리',
    user_id       BIGINT       NOT NULL                   COMMENT '작성자 FK → users.id',
    view_count    INT          DEFAULT 0                  COMMENT '조회수',
    thumbnail_url VARCHAR(500) NULL                       COMMENT '썸네일 이미지 URL (ALTER로 추가)',
    created_at    DATETIME     DEFAULT CURRENT_TIMESTAMP  COMMENT '작성일시',
    updated_at    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT fk_posts_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) COMMENT '게시글 테이블';

-- ==========================================================
-- [CREATE] 댓글 테이블
-- ==========================================================
CREATE TABLE IF NOT EXISTS comments (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY          COMMENT '댓글 PK',
    content    VARCHAR(500) NOT NULL                      COMMENT '댓글 내용 (NOT NULL)',
    post_id    BIGINT       NOT NULL                      COMMENT '게시글 FK → posts.id',
    user_id    BIGINT       NOT NULL                      COMMENT '작성자 FK → users.id',
    created_at DATETIME     DEFAULT CURRENT_TIMESTAMP     COMMENT '작성일시',
    CONSTRAINT fk_comments_post FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) COMMENT '댓글 테이블';

-- ==========================================================
-- [CREATE] 태그 테이블
-- ==========================================================
CREATE TABLE IF NOT EXISTS tags (
    id   BIGINT AUTO_INCREMENT PRIMARY KEY               COMMENT '태그 PK',
    name VARCHAR(50) NOT NULL UNIQUE                     COMMENT '태그명 (UNIQUE)'
) COMMENT '태그 마스터 테이블';

-- ==========================================================
-- [CREATE] 게시글-태그 연결 테이블 (N:M)
-- ==========================================================
CREATE TABLE IF NOT EXISTS post_tags (
    post_id BIGINT NOT NULL                              COMMENT '게시글 FK → posts.id',
    tag_id  BIGINT NOT NULL                              COMMENT '태그 FK → tags.id',
    PRIMARY KEY (post_id, tag_id),
    CONSTRAINT fk_post_tags_post FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE,
    CONSTRAINT fk_post_tags_tag  FOREIGN KEY (tag_id)  REFERENCES tags (id)  ON DELETE CASCADE
) COMMENT '게시글-태그 N:M 연결 테이블';

-- ==========================================================
-- [CREATE] 게시글 좋아요 테이블
-- ==========================================================
CREATE TABLE IF NOT EXISTS post_likes (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY          COMMENT '좋아요 PK',
    post_id    BIGINT NOT NULL                            COMMENT '게시글 FK → posts.id',
    user_id    BIGINT NOT NULL                            COMMENT '회원 FK → users.id',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP         COMMENT '좋아요 일시',
    CONSTRAINT uq_post_like UNIQUE (post_id, user_id),
    CONSTRAINT fk_post_likes_post FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE,
    CONSTRAINT fk_post_likes_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) COMMENT '게시글 좋아요 테이블';

-- ==========================================================
-- [CREATE] 게시글 통계 테이블 (DatabaseInitializer에서도 실행)
-- ==========================================================
CREATE TABLE IF NOT EXISTS post_stats (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY          COMMENT '통계 ID',
    post_id    BIGINT       NOT NULL                      COMMENT '게시글 ID (FK)',
    view_date  DATE         NOT NULL                      COMMENT '조회 날짜',
    view_count INT          DEFAULT 0                     COMMENT '일별 조회수',
    created_at DATETIME     DEFAULT CURRENT_TIMESTAMP     COMMENT '생성일시',
    CONSTRAINT fk_post_stats_post FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE,
    CONSTRAINT uq_post_date UNIQUE (post_id, view_date)
) COMMENT '게시글 일별 조회 통계';

-- ==========================================================
-- [ALTER] posts 테이블 수정 이력 (실제 적용 예시)
-- - thumbnail_url 컬럼 추가 (DatabaseInitializer에서 실행)
-- ==========================================================
-- ALTER TABLE posts ADD COLUMN IF NOT EXISTS thumbnail_url VARCHAR(500) NULL COMMENT '썸네일 이미지 URL';

-- ==========================================================
-- [DROP] 불필요 테이블 삭제 (예시 - DatabaseInitializer 참조)
-- ==========================================================
-- DROP TABLE IF EXISTS post_stats;

-- ==========================================================
-- [CREATE INDEX] 조회 성능 향상 인덱스 (DatabaseInitializer에서 실행)
-- ==========================================================
-- CREATE INDEX IF NOT EXISTS idx_posts_category   ON posts (category);
-- CREATE INDEX IF NOT EXISTS idx_posts_created_at ON posts (created_at DESC);

-- ==========================================================
-- [CREATE VIEW] 게시글 요약 뷰 (DatabaseInitializer에서 실행)
-- ==========================================================
-- CREATE OR REPLACE VIEW v_post_summary AS
-- SELECT p.id, p.title, p.summary, p.category, p.view_count, p.created_at,
--        u.id AS user_id, u.nickname AS author_nickname, u.username AS author_username,
--        (SELECT COUNT(*) FROM comments c WHERE c.post_id = p.id) AS comment_count,
--        (SELECT COUNT(*) FROM post_likes pl WHERE pl.post_id = p.id) AS like_count
-- FROM posts p JOIN users u ON p.user_id = u.id;
