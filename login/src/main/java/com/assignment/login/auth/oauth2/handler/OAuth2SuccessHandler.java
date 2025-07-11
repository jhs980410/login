package com.assignment.login.auth.oauth2.handler;

import com.assignment.login.auth.domain.RefreshToken;
import com.assignment.login.auth.repository.RefreshTokenRepository;
import com.assignment.login.auth.security.CustomOAuth2User;
import com.assignment.login.auth.util.JwtTokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Map;
@Transactional
@Slf4j
@RequiredArgsConstructor
@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final JwtTokenUtil jwtTokenUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ObjectMapper objectMapper;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        Long userId = oAuth2User.getMember().getId();
        String email = oAuth2User.getMember().getEmail();

        // 1. 토큰 발급
        String accessToken = jwtTokenUtil.generateToken(email);
        String refreshToken = jwtTokenUtil.generateRefreshToken(email); // 예: 유효기간 14일

        // 2. RefreshToken DB 저장 (userId 기준 중복 제거 후 저장)
        refreshTokenRepository.deleteByUserId(userId); // 기존 토큰 삭제 (선택)
        refreshTokenRepository.save(
                RefreshToken.builder()
                        .userId(userId)
                        .token(refreshToken)
                        .expiredAt(jwtTokenUtil.getRefreshTokenExpiryDate())
                        .autoLogin(false) // 명시
                        .userAgent(request.getHeader("User-Agent")) // 브라우저 정보
                        .ipAddress(request.getRemoteAddr()) // IP 주소
                        .build()
        );
        log.info("🔐 RefreshToken 저장 완료 - userId: {}, refreshToken: {}", userId, refreshToken);
        // 3. 응답 반환
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(
                Map.of(
                        "status", "success",
                        "accessToken", accessToken,
                        "refreshToken", refreshToken
                )
        ));

    }
}
