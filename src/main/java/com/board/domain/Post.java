package com.board.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 300)
    private String summary; // 요약문 (카드 목록에 표시, 선택사항)

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, length = 20)
    private String category = "Java"; // Java / Spring / Web / DevOps / 알고리즘 / CS지식 / 회고

    // 작성자 (N:1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private int viewCount = 0;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 댓글 목록 (1:N)
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    // 태그 목록 (N:M) - open-in-view 활성화 상태이므로 LAZY로 변경 (N+1 방지)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "post_tags",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private List<Tag> tags = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 생성 메서드
    public static Post create(String title, String summary, String content, String category, User user) {
        Post post = new Post();
        post.title = title;
        post.summary = (summary != null && !summary.isBlank()) ? summary.trim() : null;
        post.content = content;
        post.category = (category != null && !category.isBlank()) ? category : "Java";
        post.user = user;
        return post;
    }

    // 게시글 수정
    public void update(String title, String summary, String content, String category) {
        this.title = title;
        this.summary = (summary != null && !summary.isBlank()) ? summary.trim() : null;
        this.content = content;
        this.category = (category != null && !category.isBlank()) ? category : "Java";
    }

    // 태그 업데이트
    public void updateTags(List<Tag> newTags) {
        this.tags.clear();
        this.tags.addAll(newTags);
    }

    // 조회수 증가
    public void increaseViewCount() {
        this.viewCount++;
    }
}
