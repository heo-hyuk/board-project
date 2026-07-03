package com.board.service;

import com.board.domain.Comment;
import com.board.domain.Post;
import com.board.domain.User;
import com.board.repository.CommentRepository;
import com.board.repository.PostRepository;
import com.board.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock private CommentRepository commentRepository;
    @Mock private PostRepository postRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private CommentService commentService;

    @Test
    @DisplayName("댓글 작성 성공")
    void write_success() {
        // given
        User user = User.create("testuser", "pw", "test@test.com", "닉네임");
        Post post = Post.create("제목", null, "내용", "Java", user);
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(userRepository.findByUsername("testuser")).willReturn(Optional.of(user));

        // when & then
        assertThatNoException().isThrownBy(() ->
            commentService.write(1L, "좋은 글이네요!", "testuser")
        );
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    @DisplayName("댓글 작성 실패 - 내용이 공백만 있는 경우")
    void write_blankContent() {
        // when & then
        assertThatThrownBy(() -> commentService.write(1L, "   ", "testuser"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("댓글 내용을 입력해주세요.");
        // 유효성 실패 시 저장 호출 안 됨
        verify(commentRepository, never()).save(any());
    }

    @Test
    @DisplayName("댓글 작성 실패 - null 내용")
    void write_nullContent() {
        // when & then
        assertThatThrownBy(() -> commentService.write(1L, null, "testuser"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("댓글 내용을 입력해주세요.");
    }

    @Test
    @DisplayName("댓글 작성 실패 - 존재하지 않는 게시글")
    void write_postNotFound() {
        // given
        given(postRepository.findById(99L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.write(99L, "댓글 내용", "testuser"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("존재하지 않는 게시글입니다.");
    }

    @Test
    @DisplayName("댓글 삭제 성공")
    void delete_success() {
        // given
        User user = User.create("testuser", "pw", "test@test.com", "닉네임");
        Post post = Post.create("제목", null, "내용", "Java", user);
        Comment comment = Comment.create("댓글 내용", post, user);
        given(commentRepository.findById(1L)).willReturn(Optional.of(comment));

        // when & then
        assertThatNoException().isThrownBy(() -> commentService.delete(1L, "testuser"));
        verify(commentRepository).delete(comment);
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 작성자가 아닌 사용자")
    void delete_noPermission() {
        // given
        User author = User.create("author", "pw", "author@test.com", "작성자");
        Post post = Post.create("제목", null, "내용", "Java", author);
        Comment comment = Comment.create("댓글 내용", post, author);
        given(commentRepository.findById(1L)).willReturn(Optional.of(comment));

        // when & then
        assertThatThrownBy(() -> commentService.delete(1L, "otheruser"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("삭제 권한이 없습니다.");
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 존재하지 않는 댓글")
    void delete_commentNotFound() {
        // given
        given(commentRepository.findById(99L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.delete(99L, "testuser"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("존재하지 않는 댓글입니다.");
    }
}
