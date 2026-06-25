package com.board.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

// MyBatis 검색 결과 매핑용 DTO
@Getter
@Setter
public class PostDto {

    private Long id;
    private String title;
    private String content;
    private int viewCount;
    private LocalDateTime createdAt;
    private String nickname; // 작성자 닉네임 (JOIN 결과)
    private Long userId;
    private int commentCount; // 댓글 수
    private String category;  // 카테고리
}
