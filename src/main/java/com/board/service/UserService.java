package com.board.service;

import com.board.domain.User;
import com.board.dto.UserJoinDto;
import com.board.exception.NotFoundException;
import com.board.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    @Transactional
    public void join(UserJoinDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        User user = User.create(dto.getUsername(), encodedPassword, dto.getEmail(), dto.getNickname());
        userRepository.save(user);
    }

    // 아이디로 회원 조회
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 회원입니다."));
    }

    // 닉네임 수정
    @Transactional
    public void updateNickname(String username, String nickname) {
        User user = findByUsername(username);
        user.updateNickname(nickname);
    }

    // 비밀번호 변경
    @Transactional
    public void updatePassword(String username, String currentPassword, String newPassword) {
        User user = findByUsername(username);
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 올바르지 않습니다.");
        }
        user.updatePassword(passwordEncoder.encode(newPassword));
    }

    // 자기소개 변경
    @Transactional
    public void updateBio(String username, String bio) {
        User user = findByUsername(username);
        user.updateBio(bio);
    }
}
