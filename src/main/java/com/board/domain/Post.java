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

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, length = 20)
    private String category = "자유"; // 공지 / 자유 / 질문 / 프로젝트 / 취업/커리어

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
    public static Post create(String title, String content, String category, User user) {
        Post post = new Post();
        post.title = title;
        post.content = content;
        post.category = (category != null && !category.isBlank()) ? category : "자유";
        post.user = user;
        return post;
    }

    // 게시글 수정
    public void update(String title, String content, String category) {
        this.title = title;
        this.content = content;
        this.category = (category != null && !category.isBlank()) ? category : "자유";
    }

    // 조회수 증가
    public void increaseViewCount() {
        this.viewCount++;
    }
}
