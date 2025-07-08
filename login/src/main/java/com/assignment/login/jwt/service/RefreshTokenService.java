package com.assignment.login.jwt.service;

import com.assignment.login.jwt.domain.RefreshToken;
import com.assignment.login.jwt.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    public void save(Long userId, String token, String userAgent, String ipAddress) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(userId);
        refreshToken.setToken(token);
        refreshToken.setUserAgent(userAgent);
        refreshToken.setIpAddress(ipAddress);
        refreshToken.setExpiredAt(LocalDateTime.now().plusDays(7)); // 7일 유효

        refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public void deleteByUserId(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
}
