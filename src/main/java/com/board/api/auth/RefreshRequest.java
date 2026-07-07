package com.board.api.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

// Refresh Token 재발급 요청 DTO
@Getter
@NoArgsConstructor
public class RefreshRequest {

    @NotBlank(message = "Refresh Token을 입력해주세요.")
    private String refreshToken;
}
