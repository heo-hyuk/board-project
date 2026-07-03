package com.board.service;

import com.board.domain.User;
import com.board.dto.UserJoinDto;
import com.board.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private UserService userService;

    // 테스트용 DTO 생성 헬퍼
    private UserJoinDto createDto(String username, String email, String nickname) {
        UserJoinDto dto = new UserJoinDto();
        dto.setUsername(username);
        dto.setPassword("password123");
        dto.setEmail(email);
        dto.setNickname(nickname);
        return dto;
    }

    @Test
    @DisplayName("회원가입 성공 - 저장 메서드 호출 확인")
    void join_success() {
        // given
        UserJoinDto dto = createDto("testuser", "test@test.com", "테스터");
        given(userRepository.existsByUsername("testuser")).willReturn(false);
        given(userRepository.existsByEmail("test@test.com")).willReturn(false);
        given(passwordEncoder.encode(any())).willReturn("encoded_pw");
        given(userRepository.save(any())).willReturn(
            User.create("testuser", "encoded_pw", "test@test.com", "테스터")
        );

        // when & then
        assertThatNoException().isThrownBy(() -> userService.join(dto));
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 아이디 중복")
    void join_duplicateUsername() {
        // given
        UserJoinDto dto = createDto("testuser", "test@test.com", "테스터");
        given(userRepository.existsByUsername("testuser")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.join(dto))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("이미 사용 중인 아이디입니다.");
        // 중복 시 저장하지 않아야 함
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void join_duplicateEmail() {
        // given
        UserJoinDto dto = createDto("testuser", "test@test.com", "테스터");
        given(userRepository.existsByUsername("testuser")).willReturn(false);
        given(userRepository.existsByEmail("test@test.com")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.join(dto))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("이미 사용 중인 이메일입니다.");
    }

    @Test
    @DisplayName("아이디로 회원 조회 성공")
    void findByUsername_success() {
        // given
        User user = User.create("testuser", "pw", "test@test.com", "테스터");
        given(userRepository.findByUsername("testuser")).willReturn(Optional.of(user));

        // when
        User found = userService.findByUsername("testuser");

        // then
        assertThat(found.getUsername()).isEqualTo("testuser");
        assertThat(found.getNickname()).isEqualTo("테스터");
    }

    @Test
    @DisplayName("아이디로 회원 조회 실패 - 존재하지 않는 회원")
    void findByUsername_notFound() {
        // given
        given(userRepository.findByUsername("unknown")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.findByUsername("unknown"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("존재하지 않는 회원입니다.");
    }

    @Test
    @DisplayName("닉네임 변경 성공")
    void updateNickname_success() {
        // given
        User user = User.create("testuser", "pw", "test@test.com", "기존닉네임");
        given(userRepository.findByUsername("testuser")).willReturn(Optional.of(user));

        // when
        userService.updateNickname("testuser", "새닉네임");

        // then
        assertThat(user.getNickname()).isEqualTo("새닉네임");
    }

    @Test
    @DisplayName("비밀번호 변경 성공")
    void updatePassword_success() {
        // given
        User user = User.create("testuser", "encoded_old", "test@test.com", "테스터");
        given(userRepository.findByUsername("testuser")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("oldpw", "encoded_old")).willReturn(true);
        given(passwordEncoder.encode("newpw")).willReturn("encoded_new");

        // when
        userService.updatePassword("testuser", "oldpw", "newpw");

        // then
        assertThat(user.getPassword()).isEqualTo("encoded_new");
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 현재 비밀번호 불일치")
    void updatePassword_wrongCurrentPassword() {
        // given
        User user = User.create("testuser", "encoded_old", "test@test.com", "테스터");
        given(userRepository.findByUsername("testuser")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("wrongpw", "encoded_old")).willReturn(false);

        // when & then
        assertThatThrownBy(() -> userService.updatePassword("testuser", "wrongpw", "newpw"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("현재 비밀번호가 올바르지 않습니다.");
    }

    @Test
    @DisplayName("자기소개 변경 성공")
    void updateBio_success() {
        // given
        User user = User.create("testuser", "pw", "test@test.com", "테스터");
        given(userRepository.findByUsername("testuser")).willReturn(Optional.of(user));

        // when
        userService.updateBio("testuser", "안녕하세요, 개발자입니다!");

        // then
        assertThat(user.getBio()).isEqualTo("안녕하세요, 개발자입니다!");
    }

    @Test
    @DisplayName("자기소개 변경 - 빈 문자열 입력 시 null 처리")
    void updateBio_emptyBecomesNull() {
        // given
        User user = User.create("testuser", "pw", "test@test.com", "테스터");
        given(userRepository.findByUsername("testuser")).willReturn(Optional.of(user));

        // when
        userService.updateBio("testuser", "   "); // 공백만 있는 경우

        // then
        assertThat(user.getBio()).isNull(); // User.updateBio()에서 blank → null 처리
    }
}
