package com.board.service;

import com.board.domain.Comment;
import com.board.domain.Post;
import com.board.domain.User;
import com.board.dto.CommentDto;
import com.board.repository.CommentRepository;
import com.board.repository.PostRepository;
import com.board.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // 댓글 목록 조회 (JPA)
    public List<CommentDto> findByPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        return commentRepository.findByPostOrderByCreatedAtAsc(post).stream()
                .map(c -> {
                    CommentDto dto = new CommentDto();
                    dto.setId(c.getId());
                    dto.setContent(c.getContent());
                    dto.setNickname(c.getUser().getNickname());
                    dto.setUserId(c.getUser().getId());
                    dto.setUsername(c.getUser().getUsername()); // 삭제 권한 확인용
                    dto.setCreatedAt(c.getCreatedAt());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // 댓글 작성 (JPA)
    @Transactional
    public void write(Long postId, String content, String username) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("댓글 내용을 입력해주세요.");
        }
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        commentRepository.save(Comment.create(content.trim(), post, user));
    }

    // 댓글 삭제 (JPA)
    @Transactional
    public void delete(Long commentId, String username) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));
        if (!comment.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }
        commentRepository.delete(comment);
    }
}
