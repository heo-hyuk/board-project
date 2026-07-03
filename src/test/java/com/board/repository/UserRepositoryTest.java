package com.board.repository;

import com.board.domain.User;
import com.board.mapper.PostMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

// H2 인메모리 DB로 JPA 레이어만 테스트 (MariaDB 불필요)
@DataJpaTest
class UserRepositoryTest {

    @Autowired private UserRepository userRepository;

    // @MapperScan으로 등록된 PostMapper가 SqlSessionFactory 없이 실패하는 것을 방지
    @MockBean private PostMapper postMapper;

    // 테스트용 유저 저장 헬퍼
    private User saveUser(String username, String email) {
        return userRepository.save(
            User.create(username, "encoded_pw", email, "닉네임_" + username)
        );
    }

    @Test
    @DisplayName("아이디로 회원 조회 성공")
    void findByUsername_found() {
        // given
        saveUser("testuser", "test@test.com");

        // when
        Optional<User> result = userRepository.findByUsername("testuser");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("testuser");
        assertThat(result.get().getEmail()).isEqualTo("test@test.com");
    }

    @Test
    @DisplayName("아이디로 회원 조회 - 없는 경우 Optional.empty")
    void findByUsername_notFound() {
        // when
        Optional<User> result = userRepository.findByUsername("notexist");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("아이디 중복 확인 - 존재하는 아이디")
    void existsByUsername_true() {
        // given
        saveUser("duplicate", "dup@test.com");

        // then
        assertThat(userRepository.existsByUsername("duplicate")).isTrue();
    }

    @Test
    @DisplayName("아이디 중복 확인 - 존재하지 않는 아이디")
    void existsByUsername_false() {
        // then
        assertThat(userRepository.existsByUsername("notexist")).isFalse();
    }

    @Test
    @DisplayName("이메일 중복 확인 - 사용 중인 이메일")
    void existsByEmail_true() {
        // given
        saveUser("user1", "used@test.com");

        // then
        assertThat(userRepository.existsByEmail("used@test.com")).isTrue();
    }

    @Test
    @DisplayName("이메일 중복 확인 - 사용하지 않는 이메일")
    void existsByEmail_false() {
        // then
        assertThat(userRepository.existsByEmail("unused@test.com")).isFalse();
    }

    @Test
    @DisplayName("회원 저장 후 @PrePersist 동작 - createdAt 자동 설정")
    void save_prePersist_setsCreatedAt() {
        // when
        User saved = saveUser("userA", "a@test.com");
        Optional<User> found = userRepository.findById(saved.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getCreatedAt()).isNotNull();
        assertThat(found.get().getBio()).isNull(); // bio 기본값 없음
    }

    @Test
    @DisplayName("닉네임 변경 후 DB 반영 확인")
    void updateNickname_persistedToDb() {
        // given
        User user = saveUser("userB", "b@test.com");

        // when
        user.updateNickname("변경된닉네임");
        userRepository.save(user);
        userRepository.flush();

        // then
        Optional<User> found = userRepository.findByUsername("userB");
        assertThat(found).isPresent();
        assertThat(found.get().getNickname()).isEqualTo("변경된닉네임");
    }
}
