package com.board.repository;

import com.board.domain.Post;
import com.board.domain.User;
import com.board.mapper.PostMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

// H2 인메모리 DB로 JPA 레이어만 테스트
@DataJpaTest
class PostRepositoryTest {

    @Autowired private PostRepository postRepository;
    @Autowired private UserRepository userRepository;

    // @MapperScan으로 등록된 PostMapper가 SqlSessionFactory 없이 실패하는 것을 방지
    @MockBean private PostMapper postMapper;

    private User savedUser;

    @BeforeEach
    void setUp() {
        // 각 테스트마다 작성자 유저 생성
        savedUser = userRepository.save(
            User.create("testuser", "encoded_pw", "test@test.com", "테스터")
        );
    }

    @Test
    @DisplayName("게시글 저장 및 조회 - 제목, 카테고리, 요약 확인")
    void save_andFindById() {
        // given
        Post post = Post.create("테스트 포스트", "요약문", "본문 내용", "Spring", savedUser);

        // when
        Post saved = postRepository.save(post);
        Optional<Post> found = postRepository.findById(saved.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("테스트 포스트");
        assertThat(found.get().getCategory()).isEqualTo("Spring");
        assertThat(found.get().getSummary()).isEqualTo("요약문");
        assertThat(found.get().getViewCount()).isEqualTo(0);
        assertThat(found.get().getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("최신 포스트 6개 조회 - 8개 중 6개만 반환")
    void findTop6ByOrderByCreatedAtDesc() {
        // given: 게시글 8개 저장
        for (int i = 1; i <= 8; i++) {
            postRepository.save(
                Post.create("포스트" + i, null, "내용" + i, "Java", savedUser)
            );
        }
        postRepository.flush();

        // when
        List<Post> posts = postRepository.findTop6ByOrderByCreatedAtDesc();

        // then: 최대 6개만 반환
        assertThat(posts).hasSize(6);
    }

    @Test
    @DisplayName("최신 포스트 6개 조회 - 3개밖에 없으면 3개 반환")
    void findTop6_lessThan6Posts() {
        // given
        for (int i = 1; i <= 3; i++) {
            postRepository.save(Post.create("포스트" + i, null, "내용", "Java", savedUser));
        }

        // when
        List<Post> posts = postRepository.findTop6ByOrderByCreatedAtDesc();

        // then
        assertThat(posts).hasSize(3);
    }

    @Test
    @DisplayName("사용자별 게시글 목록 조회")
    void findByUserOrderByCreatedAtDesc() {
        // given
        postRepository.save(Post.create("첫번째 포스트", null, "내용", "Java", savedUser));
        postRepository.save(Post.create("두번째 포스트", null, "내용", "Spring", savedUser));
        postRepository.flush();

        // when
        List<Post> posts = postRepository.findByUserOrderByCreatedAtDesc(savedUser);

        // then
        assertThat(posts).hasSize(2);
        assertThat(posts).allMatch(p -> p.getUser().getUsername().equals("testuser"));
    }

    @Test
    @DisplayName("다른 사용자의 게시글은 조회 안 됨")
    void findByUser_onlyOwnerPosts() {
        // given
        User anotherUser = userRepository.save(
            User.create("another", "pw", "another@test.com", "다른사람")
        );
        postRepository.save(Post.create("내 포스트", null, "내용", "Java", savedUser));
        postRepository.save(Post.create("타인 포스트", null, "내용", "Java", anotherUser));

        // when
        List<Post> myPosts = postRepository.findByUserOrderByCreatedAtDesc(savedUser);
        List<Post> otherPosts = postRepository.findByUserOrderByCreatedAtDesc(anotherUser);

        // then
        assertThat(myPosts).hasSize(1);
        assertThat(otherPosts).hasSize(1);
        assertThat(myPosts.get(0).getTitle()).isEqualTo("내 포스트");
    }

    @Test
    @DisplayName("조회수 증가 후 DB 반영 확인")
    void increaseViewCount_persistedToDb() {
        // given
        Post post = postRepository.save(
            Post.create("조회수 테스트", null, "내용", "Java", savedUser)
        );
        assertThat(post.getViewCount()).isEqualTo(0);

        // when: 조회수 2번 증가
        post.increaseViewCount();
        post.increaseViewCount();
        postRepository.save(post);
        postRepository.flush();

        // then
        Post found = postRepository.findById(post.getId()).orElseThrow();
        assertThat(found.getViewCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("summary가 null이어도 저장 가능")
    void save_withNullSummary() {
        // given
        Post post = Post.create("요약 없는 포스트", null, "내용", "Java", savedUser);

        // when
        Post saved = postRepository.save(post);

        // then
        assertThat(saved.getSummary()).isNull();
    }
}
