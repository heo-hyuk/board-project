package com.board.api.user;

import com.board.domain.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// 내 정보 응답 DTO (password 필드 제외)
@Getter
@Builder
public class UserMeResponse {

    private Long id;
    private String username;
    private String email;
    private String nickname;
    private String bio;
    private LocalDateTime createdAt;

    public static UserMeResponse from(User user) {
        return UserMeResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .bio(user.getBio())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
