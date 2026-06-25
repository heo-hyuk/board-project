package com.board.dto;

import lombok.Getter;
import lombok.Setter;

// MyBatis 동적 검색 조건 DTO
@Getter
@Setter
public class PostSearchDto {

    private String keyword;    // 검색어
    private String searchType; // title / content / all
    private String category;   // 카테고리 필터 (null이면 전체)
    private String tag;        // 태그 필터
    private int page = 1;
    private int pageSize = 10;

    // MyBatis에서 LIMIT offset, pageSize 사용
    public int getOffset() {
        return (page - 1) * pageSize;
    }
}
