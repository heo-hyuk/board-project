package com.board.repository;

import com.board.domain.Post;
import com.board.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

// JPA - 정적 쿼리 (CRUD)
public interface PostRepository extends JpaRepository<Post, Long> {

    // 내가 작성한 게시글 목록 (마이페이지용)
    List<Post> findByUserOrderByCreatedAtDesc(User user);
}
