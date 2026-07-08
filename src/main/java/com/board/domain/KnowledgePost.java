package com.board.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "knowledge_posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KnowledgePost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content; // 마크다운

    @Column(name = "branch_name", length = 150)
    private String branchName; // 브랜치 관점 설명 (원본은 null)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "parent_id")
    private Long parentId; // 부모 글 ID (null = 원본)

    @Column(name = "root_id")
    private Long rootId; // 원본 글 ID

    private int depth = 0; // 0=원본, 1=1차 브랜치, 2=2차 브랜치...

    @Column(name = "view_count")
    private int viewCount = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 원본 글 생성
    public static KnowledgePost createRoot(String title, String content, User user) {
        KnowledgePost p = new KnowledgePost();
        p.title = title;
        p.content = content;
        p.user = user;
        p.depth = 0;
        return p;
    }

    // 원본 글 저장 후 rootId = 자기 자신 ID로 초기화
    public void initRootId() {
        this.rootId = this.id;
    }

    // 브랜치 생성
    public static KnowledgePost createBranch(String title, String content, String branchName,
                                              User user, Long parentId, Long rootId, int parentDepth) {
        KnowledgePost p = new KnowledgePost();
        p.title = title;
        p.content = content;
        p.branchName = branchName;
        p.user = user;
        p.parentId = parentId;
        p.rootId = rootId;
        p.depth = parentDepth + 1;
        return p;
    }

    public void increaseViewCount() {
        this.viewCount++;
    }
}
