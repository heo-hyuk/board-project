package com.board.dto;

import lombok.Getter;
import lombok.Setter;

// 지식나눔 게시글 목록 검색 + 페이지네이션 파라미터
@Getter
@Setter
public class KnowledgeSearchDto {

    private String keyword;          // 검색어 (제목+내용)
    private int page = 1;            // 현재 페이지 (기본 1)
    private int pageSize = 10;       // 페이지당 게시글 수

    // MyBatis LIMIT offset, pageSize 용
    public int getOffset() {
        return (page - 1) * pageSize;
    }
}
