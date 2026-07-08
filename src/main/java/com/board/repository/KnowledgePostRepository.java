package com.board.repository;

import com.board.domain.KnowledgePost;
import org.springframework.data.jpa.repository.JpaRepository;

// 지식나눔 게시글 JPA 레포지토리 (저장/삭제에만 사용, 조회는 MyBatis)
public interface KnowledgePostRepository extends JpaRepository<KnowledgePost, Long> {
}
