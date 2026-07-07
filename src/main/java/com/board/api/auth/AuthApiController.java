package com.board.api.auth;

import com.board.api.common.ApiResponse;
import com.board.security.JwtTokenProvider;
import com.board.service.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "JWT 인증 API")
public class AuthApiController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    // POST /api/v1/auth/login — Access Token + Refresh Token 발급
    @PostMapping("/login")
    @Operation(summary = "로그인", description = "아이디/비밀번호로 JWT 토큰 발급")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            // Spring Security로 인증 처리
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            String username = authentication.getName();
            String accessToken = jwtTokenProvider.generateAccessToken(username);
            String refreshToken = jwtTokenProvider.generateRefreshToken(username);

            // Refresh Token DB 저장 (기존 토큰 있으면 갱신)
            refreshTokenService.saveOrUpdate(username, refreshToken);

            return ApiResponse.ok(TokenResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(1800)
                    .build());

        } catch (AuthenticationException e) {
            return ApiResponse.fail("아이디 또는 비밀번호가 올바르지 않습니다.");
        }
    }

    // POST /api/v1/auth/refresh — Access Token 재발급 (Refresh Token Rotation)
    @PostMapping("/refresh")
    @Operation(summary = "토큰 갱신", description = "Refresh Token으로 새 Access Token + Refresh Token 발급")
    public ApiResponse<TokenResponse> refresh(@RequestBody RefreshRequest request) {
        String oldRefreshToken = request.getRefreshToken();

        // 검증 + Rotation을 하나의 트랜잭션으로 처리 (null 반환 시 유효하지 않음)
        String newRefreshToken = refreshTokenService.rotateIfValid(oldRefreshToken);
        if (newRefreshToken == null) {
            return ApiResponse.fail("유효하지 않거나 만료된 Refresh Token입니다. 다시 로그인해주세요.");
        }

        String username = jwtTokenProvider.getUsername(oldRefreshToken);
        String newAccessToken = jwtTokenProvider.generateAccessToken(username);

        return ApiResponse.ok(TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(1800)
                .build());
    }

    // POST /api/v1/auth/logout — Refresh Token 삭제
    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "서버의 Refresh Token 삭제")
    public ApiResponse<Void> logout(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            refreshTokenService.deleteByUsername(userDetails.getUsername());
        }
        return ApiResponse.ok("로그아웃 되었습니다.");
    }
}
