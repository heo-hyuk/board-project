package com.board.service;

import com.board.domain.RefreshToken;
import com.board.repository.RefreshTokenRepository;
import com.board.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Refresh Token CRUD 서비스
// AuthApiController의 self-call 트랜잭션 문제 방지를 위해 별도 분리
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    // 토큰 저장 또는 갱신 (Rotation) — 하나의 트랜잭션으로 처리
    @Transactional
    public void saveOrUpdate(String username, String token) {
        RefreshToken rt = refreshTokenRepository.findByUsername(username).orElse(null);
        if (rt == null) {
            // 신규 저장
            refreshTokenRepository.save(
                    RefreshToken.create(username, token, jwtTokenProvider.getRefreshTokenExpiry())
            );
        } else {
            // 기존 토큰 갱신
            rt.rotate(token, jwtTokenProvider.getRefreshTokenExpiry());
            refreshTokenRepository.save(rt);
        }
    }

    /**
     * Refresh Token 검증 + Rotation을 하나의 트랜잭션으로 처리
     * - 토큰이 유효하면 새 토큰으로 교체 후 반환
     * - 유효하지 않으면 null 반환
     */
    @Transactional
    public String rotateIfValid(String oldToken) {
        // JWT 서명/만료 검증
        if (!jwtTokenProvider.validateToken(oldToken)) {
            return null;
        }

        String username = jwtTokenProvider.getUsername(oldToken);

        // DB 저장 토큰과 일치 여부 확인 (탈취 방지)
        RefreshToken stored = refreshTokenRepository.findByUsername(username).orElse(null);
        if (stored == null || !stored.getToken().equals(oldToken) || stored.isExpired()) {
            return null;
        }

        // 새 토큰 발급 + DB 갱신 (Rotation)
        String newToken = jwtTokenProvider.generateRefreshToken(username);
        stored.rotate(newToken, jwtTokenProvider.getRefreshTokenExpiry());
        refreshTokenRepository.save(stored);

        return newToken;
    }

    // 저장된 토큰으로 username 조회 (로그아웃 후 검증용)
    @Transactional(readOnly = true)
    public RefreshToken findByUsername(String username) {
        return refreshTokenRepository.findByUsername(username).orElse(null);
    }

    // 로그아웃 시 토큰 삭제
    @Transactional
    public void deleteByUsername(String username) {
        refreshTokenRepository.deleteByUsername(username);
    }
}
