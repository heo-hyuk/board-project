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

    // 토큰 저장 또는 갱신 (Rotation)
    @Transactional
    public void saveOrUpdate(String username, String token) {
        RefreshToken rt = refreshTokenRepository.findByUsername(username).orElse(null);
        if (rt == null) {
            // 신규 저장
            refreshTokenRepository.save(
                    RefreshToken.create(username, token, jwtTokenProvider.getRefreshTokenExpiry())
            );
        } else {
            // 기존 토큰 갱신 — rotate 후 명시적 save로 확실히 반영
            rt.rotate(token, jwtTokenProvider.getRefreshTokenExpiry());
            refreshTokenRepository.save(rt);
        }
    }

    // 저장된 토큰 조회
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
