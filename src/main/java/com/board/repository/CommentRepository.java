package com.board.repository;

import com.board.domain.Comment;
import com.board.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

// JPA - 정적 쿼리 (CRUD)
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 게시글에 달린 댓글 목록
    List<Comment> findByPostOrderByCreatedAtAsc(Post post);
}
