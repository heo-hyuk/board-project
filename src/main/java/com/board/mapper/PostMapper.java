package com.board.mapper;

import com.board.dto.PostDto;
import com.board.dto.PostSearchDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

// MyBatis - 동적 쿼리 + 게시글 CRUD + DDL + 데이터사전 조회
@Mapper
public interface PostMapper {

    // ===== 게시글 검색 (동적 쿼리 + 페이지네이션) =====
    List<PostDto> searchPosts(PostSearchDto searchDto);
    int countPosts(PostSearchDto searchDto);

    // ===== 게시글 기본 CRUD (MyBatis) =====
    // 게시글 등록 (useGeneratedKeys로 생성된 ID 반환)
    void insertPost(Map<String, Object> params);

    // 게시글 단건 조회 (JOIN - users 테이블 포함)
    PostDto selectPostById(@Param("id") Long id);

    // 게시글 수정
    void updatePost(Map<String, Object> params);

    // 게시글 태그 관계 삭제 (post_tags 먼저 삭제)
    void deletePostTags(@Param("postId") Long postId);

    // 게시글 삭제
    void deletePost(@Param("id") Long id);

    // 조회수 증가
    void increaseViewCount(@Param("id") Long id);

    // ===== DDL 명령문 (테이블 관리) =====
    // 게시글 통계 테이블 생성 (CREATE)
    void createPostStatsTable();

    // posts 테이블에 thumbnail_url 컬럼 추가 (ALTER)
    void alterPostsAddThumbnail();

    // 게시글 통계 테이블 삭제 (DROP)
    void dropPostStatsTable();

    // ===== 인덱스 생성 DDL =====
    void createIndexPostsCategory();
    void createIndexPostsCreatedAt();

    // ===== 뷰 생성 DDL =====
    void createViewPostSummary();

    // ===== 데이터사전 조회 (INFORMATION_SCHEMA) =====
    // 테이블 목록 조회
    List<Map<String, Object>> selectTableList(@Param("schema") String schema);

    // 테이블 컬럼 구조 + 제약조건 조회
    List<Map<String, Object>> selectTableColumns(@Param("schema") String schema,
                                                  @Param("tableName") String tableName);

    // 인덱스 목록 조회
    List<Map<String, Object>> selectIndexList(@Param("schema") String schema);

    // 뷰 목록 조회
    List<Map<String, Object>> selectViewList(@Param("schema") String schema);

    // 현재 접속 중인 스키마명 조회
    String selectCurrentSchema();

    // 외래키(FK) 제약조건 목록 조회
    List<Map<String, Object>> selectForeignKeyList(@Param("schema") String schema);
}
