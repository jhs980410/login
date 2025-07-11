package com.assignment.login.auth.oauth2.handler;

import com.assignment.login.auth.domain.RefreshToken;
import com.assignment.login.auth.oauth2.service.RememberTokenService;
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
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final JwtTokenUtil jwtTokenUtil;
    private final RememberTokenService rememberTokenService;



    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        System.out.println(" SuccessHandler 호출됨");
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        Long userId = oAuth2User.getMember().getId();
        String email = oAuth2User.getMember().getEmail();

        // 토큰 발급
        String accessToken = jwtTokenUtil.generateToken(email);
        String refreshToken = jwtTokenUtil.generateRefreshToken(email);
        LocalDateTime refreshExp = jwtTokenUtil.getRefreshTokenExpiryDate();
        System.out.println("🔍 [DEBUG] OAuth2 로그인 성공 후 토큰 저장 시작");
        System.out.println("🔑 userId: " + userId);
        System.out.println("🔑 refreshToken: " + refreshToken);
        System.out.println("🔑 refreshExp: " + refreshExp);
        System.out.println("🔑 userAgent: " + request.getHeader("User-Agent"));
        System.out.println("🔑 ipAddress: " + request.getRemoteAddr());

        //  별도 트랜잭션 서비스로 토큰 저장
        rememberTokenService.saveRefreshToken(
                userId,
                refreshToken,
                refreshExp,
                request.getHeader("User-Agent"),
                request.getRemoteAddr(),
                false // 소셜 로그인은 auto_login = 0
        );

        // 응답
        String redirectUrl = String.format("/redirect.html?accessToken=%s&refreshToken=%s", accessToken, refreshToken);
        response.sendRedirect(redirectUrl);
    }
}
