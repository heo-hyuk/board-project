package com.board.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// MyBatis 조회 결과 매핑 + 브랜치 트리 조립용 DTO
@Getter
@Setter
public class KnowledgePostDto {

    private Long id;
    private String title;
    private String content;
    private String branchName;  // 브랜치 관점 설명
    private String nickname;    // 작성자 닉네임 (JOIN)
    private String username;    // 작성자 아이디 (삭제 권한 확인용)
    private Long userId;
    private Long parentId;      // 부모 글 ID (null=원본)
    private Long rootId;        // 원본 글 ID
    private int depth;          // 0=원본, 1=1차...
    private int viewCount;
    private int branchCount;    // 직접 달린 브랜치 수 (목록용)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 서비스에서 트리 조립 시 사용 (DB 조회 결과와 무관)
    private List<KnowledgePostDto> children = new ArrayList<>();
}
