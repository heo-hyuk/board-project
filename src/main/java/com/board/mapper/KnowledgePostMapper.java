package com.board.mapper;

import com.board.dto.KnowledgePostDto;
import com.board.dto.KnowledgeSearchDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

// 지식나눔 게시글 MyBatis 매퍼 (목록/상세/브랜치 조회 + DDL)
@Mapper
public interface KnowledgePostMapper {

    // 원본 글 목록 (parent_id IS NULL, 페이지네이션 + 키워드 검색)
    List<KnowledgePostDto> selectRootPosts(KnowledgeSearchDto searchDto);

    // 원본 글 수 (페이지 계산용)
    int countRootPosts(KnowledgeSearchDto searchDto);

    // 단건 조회 (원본 or 브랜치)
    KnowledgePostDto selectById(@Param("id") Long id);

    // 특정 원본의 모든 브랜치 조회 (root_id 기준, depth 오름차순 + created_at 오름차순)
    List<KnowledgePostDto> selectBranchesByRootId(@Param("rootId") Long rootId);

    // 조회수 증가
    void increaseViewCount(@Param("id") Long id);

    // [DDL] 지식나눔 테이블 생성
    void createKnowledgePostsTable();
}
