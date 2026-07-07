package com.board.repository;

import com.board.domain.Post;
import com.board.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

// JPA - 정적 쿼리 (CRUD)
public interface PostRepository extends JpaRepository<Post, Long> {

    // 내가 작성한 게시글 목록 (마이페이지용)
    List<Post> findByUserOrderByCreatedAtDesc(User user);

    // 홈 화면 최신 포스트 6개 - JOIN FETCH로 user 즉시 로딩 (LazyInitializationException 방지)
    @Query("SELECT p FROM Post p JOIN FETCH p.user ORDER BY p.createdAt DESC LIMIT 6")
    List<Post> findTop6ByOrderByCreatedAtDesc();
}
