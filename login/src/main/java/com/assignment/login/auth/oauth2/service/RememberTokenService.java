package com.assignment.login.auth.oauth2.service;

import com.assignment.login.auth.domain.RefreshToken;
import com.assignment.login.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RememberTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public void saveRefreshToken(Long userId, String refreshToken, LocalDateTime expiredAt,
                                 String userAgent, String ipAddress, boolean autoLogin) {
        System.out.println("💾 saveRefreshToken called");
        refreshTokenRepository.deleteByUserId(userId); // 기존 토큰 삭제
        refreshTokenRepository.save(
                RefreshToken.builder()
                        .userId(userId)
                        .token(refreshToken)
                        .expiredAt(expiredAt)
                        .userAgent(userAgent)
                        .ipAddress(ipAddress)
                        .autoLogin(autoLogin)
                        .build()
        );
    }
}
