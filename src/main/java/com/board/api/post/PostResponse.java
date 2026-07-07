package com.board.api.post;

import com.board.domain.Post;
import com.board.dto.PostDto;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

// 게시글 API 응답 DTO
@Getter
@Builder
public class PostResponse {

    private Long id;
    private String title;
    private String summary;
    private String content;
    private String category;
    private int viewCount;
    private int likeCount;
    private int commentCount;
    private String nickname;
    private Long userId;
    private List<String> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Post 엔티티 → PostResponse 변환 (상세 조회용)
    public static PostResponse from(Post post, int likeCount) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .summary(post.getSummary())
                .content(post.getContent())
                .category(post.getCategory())
                .viewCount(post.getViewCount())
                .likeCount(likeCount)
                .nickname(post.getUser() != null ? post.getUser().getNickname() : null)
                .userId(post.getUser() != null ? post.getUser().getId() : null)
                .tags(post.getTags().stream()
                        .map(tag -> tag.getName())
                        .collect(Collectors.toList()))
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    // PostDto → PostResponse 변환 (목록 조회용, MyBatis 결과)
    public static PostResponse from(PostDto dto) {
        return PostResponse.builder()
                .id(dto.getId())
                .title(dto.getTitle())
                .summary(dto.getSummary())
                .content(dto.getContent())
                .category(dto.getCategory())
                .viewCount(dto.getViewCount())
                .likeCount(dto.getLikeCount())
                .commentCount(dto.getCommentCount())
                .nickname(dto.getNickname())
                .userId(dto.getUserId())
                .tags(List.of())
                .createdAt(dto.getCreatedAt())
                .build();
    }
}
