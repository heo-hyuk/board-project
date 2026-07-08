package com.board.config;

import com.board.mapper.KnowledgePostMapper;
import com.board.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 애플리케이션 시작 시 DDL 명령문 실행
 * - 인덱스 생성 (조회 성능 향상)
 * - 뷰 생성 (posts + users JOIN 결과를 뷰로 정의)
 * - 추가 컬럼 ALTER (thumbnail_url)
 * - 통계 테이블 생성 (CREATE)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseInitializer implements ApplicationRunner {

    private final PostMapper postMapper;
    private final KnowledgePostMapper knowledgePostMapper;

    @Override
    public void run(ApplicationArguments args) {

        // [CREATE] 지식나눔 게시글 테이블 생성
        try {
            knowledgePostMapper.createKnowledgePostsTable();
            log.info("[DDL] knowledge_posts 테이블 생성 완료 (CREATE TABLE IF NOT EXISTS)");
        } catch (Exception e) {
            log.warn("[DDL] knowledge_posts 테이블 생성 스킵: {}", e.getMessage());
        }

        // [CREATE] 게시글 통계 테이블 생성
        try {
            postMapper.createPostStatsTable();
            log.info("[DDL] post_stats 테이블 생성 완료 (CREATE TABLE)");
        } catch (Exception e) {
            log.warn("[DDL] post_stats 테이블 생성 스킵: {}", e.getMessage());
        }

        // [ALTER] posts 테이블에 thumbnail_url 컬럼 추가
        try {
            postMapper.alterPostsAddThumbnail();
            log.info("[DDL] posts.thumbnail_url 컬럼 추가 완료 (ALTER TABLE)");
        } catch (Exception e) {
            log.warn("[DDL] thumbnail_url 컬럼 추가 스킵: {}", e.getMessage());
        }

        // [CREATE INDEX] 카테고리 조회 인덱스
        try {
            postMapper.createIndexPostsCategory();
            log.info("[DDL] idx_posts_category 인덱스 생성 완료");
        } catch (Exception e) {
            log.warn("[DDL] idx_posts_category 인덱스 생성 스킵: {}", e.getMessage());
        }

        // [CREATE INDEX] 최신순 정렬 인덱스
        try {
            postMapper.createIndexPostsCreatedAt();
            log.info("[DDL] idx_posts_created_at 인덱스 생성 완료");
        } catch (Exception e) {
            log.warn("[DDL] idx_posts_created_at 인덱스 생성 스킵: {}", e.getMessage());
        }

        // [CREATE VIEW] 게시글 요약 뷰 생성
        try {
            postMapper.createViewPostSummary();
            log.info("[DDL] v_post_summary 뷰 생성 완료 (CREATE OR REPLACE VIEW)");
        } catch (Exception e) {
            log.warn("[DDL] v_post_summary 뷰 생성 스킵: {}", e.getMessage());
        }
    }
}
