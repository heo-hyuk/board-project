package com.board.repository;

import com.board.domain.Post;
import com.board.domain.PostLike;
import com.board.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    // 특정 게시글에 특정 유저가 좋아요 눌렀는지 확인
    Optional<PostLike> findByPostAndUser(Post post, User user);

    // 게시글 좋아요 수
    int countByPost(Post post);

    // 유저가 눌렀는지 여부
    boolean existsByPostAndUser(Post post, User user);
}
