package com.board.service;

import com.board.domain.Post;
import com.board.domain.PostLike;
import com.board.domain.User;
import com.board.dto.PostSearchDto;
import com.board.exception.NotFoundException;
import com.board.mapper.PostMapper;
import com.board.repository.PostLikeRepository;
import com.board.repository.PostRepository;
import com.board.repository.TagRepository;
import com.board.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock private PostRepository postRepository;
    @Mock private UserRepository userRepository;
    @Mock private PostLikeRepository postLikeRepository;
    @Mock private TagRepository tagRepository;
    @Mock private PostMapper postMapper;
    @InjectMocks private PostService postService;

    private User author;
    private Post post;

    @BeforeEach
    void setUp() {
        author = User.create("author", "pw", "author@test.com", "작성자");
        post = Post.create("제목", "요약", "내용", "Java", author);
        ReflectionTestUtils.setField(post, "id", 1L);
    }

    @Test
    @DisplayName("게시글 상세 조회 - 조회수 1 증가")
    void findById_increasesViewCount() {
        // given
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        int beforeCount = post.getViewCount();

        // when
        Post found = postService.findById(1L);

        // then
        assertThat(found.getViewCount()).isEqualTo(beforeCount + 1);
    }

    @Test
    @DisplayName("게시글 상세 조회 실패 - 존재하지 않는 ID")
    void findById_notFound() {
        // given
        given(postRepository.findById(99L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.findById(99L))
            .isInstanceOf(NotFoundException.class)
            .hasMessage("존재하지 않는 게시글입니다.");
    }

    @Test
    @DisplayName("게시글 조회 (ReadOnly) - 조회수 변동 없음")
    void findByIdReadOnly_noViewCountIncrease() {
        // given
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        int beforeCount = post.getViewCount();

        // when
        Post found = postService.findByIdReadOnly(1L);

        // then
        assertThat(found.getViewCount()).isEqualTo(beforeCount); // 조회수 그대로
    }

    @Test
    @DisplayName("게시글 작성 성공 - MyBatis insert 후 postId 반환")
    void write_success() {
        // given
        User user = User.create("testuser", "pw", "test@test.com", "닉네임");
        given(userRepository.findByUsername("testuser")).willReturn(Optional.of(user));
        // MyBatis insertPost 호출 시 useGeneratedKeys 효과를 doAnswer로 시뮬레이션
        doAnswer(invocation -> {
            Map<String, Object> params = invocation.getArgument(0);
            params.put("id", 1L);
            return null;
        }).when(postMapper).insertPost(any());
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        // when
        Long postId = postService.write("제목", "요약", "내용", "Java", null, "testuser");

        // then
        assertThat(postId).isEqualTo(1L);
        verify(postMapper).insertPost(any());
    }

    @Test
    @DisplayName("게시글 삭제 성공 - post_tags → posts 순서로 삭제")
    void delete_success() {
        // given
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        // when
        postService.delete(1L, "author");

        // then: FK 제약으로 post_tags 먼저, posts 나중에 삭제해야 함
        verify(postMapper).deletePostTags(1L);
        verify(postMapper).deletePost(1L);
    }

    @Test
    @DisplayName("게시글 삭제 실패 - 작성자가 아닌 사용자")
    void delete_noPermission() {
        // given
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        // when & then
        assertThatThrownBy(() -> postService.delete(1L, "otheruser"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("삭제 권한이 없습니다.");
    }

    @Test
    @DisplayName("게시글 수정 실패 - 작성자가 아닌 사용자")
    void update_noPermission() {
        // given
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        // when & then
        assertThatThrownBy(() ->
            postService.update(1L, "새제목", null, "새내용", "Spring", null, "otheruser")
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessage("수정 권한이 없습니다.");
    }

    @Test
    @DisplayName("좋아요 추가 - 처음 누를 때 true 반환")
    void toggleLike_add() {
        // given
        User user = User.create("testuser", "pw", "test@test.com", "닉네임");
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(userRepository.findByUsername("testuser")).willReturn(Optional.of(user));
        given(postLikeRepository.findByPostAndUser(post, user)).willReturn(Optional.empty());

        // when
        boolean result = postService.toggleLike(1L, "testuser");

        // then
        assertThat(result).isTrue();
        verify(postLikeRepository).save(any(PostLike.class));
    }

    @Test
    @DisplayName("좋아요 취소 - 이미 눌렀을 때 false 반환")
    void toggleLike_cancel() {
        // given
        User user = User.create("testuser", "pw", "test@test.com", "닉네임");
        PostLike like = PostLike.create(post, user);
        given(postRepository.findById(1L)).willReturn(Optional.of(post));
        given(userRepository.findByUsername("testuser")).willReturn(Optional.of(user));
        given(postLikeRepository.findByPostAndUser(post, user)).willReturn(Optional.of(like));

        // when
        boolean result = postService.toggleLike(1L, "testuser");

        // then
        assertThat(result).isFalse();
        verify(postLikeRepository).delete(like);
    }

    @Test
    @DisplayName("isLikedByUser - 비로그인(null) 시 false 반환")
    void isLikedByUser_nullUsername() {
        // when
        boolean result = postService.isLikedByUser(1L, null);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("searchPosts - 25개 글, 페이지당 10개 → 3페이지")
    void searchPosts_totalPagesCalculation() {
        // given
        PostSearchDto dto = new PostSearchDto(); // page=1, pageSize=10 (기본값)
        given(postMapper.searchPosts(dto)).willReturn(List.of());
        given(postMapper.countPosts(dto)).willReturn(25);

        // when
        Map<String, Object> result = postService.searchPosts(dto);

        // then
        assertThat(result.get("totalCount")).isEqualTo(25);
        assertThat(result.get("totalPages")).isEqualTo(3); // Math.ceil(25.0 / 10) = 3
        assertThat(result.get("currentPage")).isEqualTo(1);
    }

    @Test
    @DisplayName("searchPosts - 정확히 10개 글 → 1페이지")
    void searchPosts_exactlyOnePage() {
        // given
        PostSearchDto dto = new PostSearchDto();
        given(postMapper.searchPosts(dto)).willReturn(List.of());
        given(postMapper.countPosts(dto)).willReturn(10);

        // when
        Map<String, Object> result = postService.searchPosts(dto);

        // then
        assertThat(result.get("totalPages")).isEqualTo(1); // ceil(10.0 / 10) = 1
    }
}
