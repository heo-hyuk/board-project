package com.board.api.auth;

import lombok.Builder;
import lombok.Getter;

// JWT 토큰 응답 DTO
@Getter
@Builder
public class TokenResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;    // "Bearer"
    private long expiresIn;      // Access Token 만료 시간 (초)
}
