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
        System.out.println("✅ SuccessHandler 호출됨");
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        System.out.println("✅ 세션 ID in SuccessHandler = " + request.getSession().getId());

        if (oAuth2User.getMember().getLoginType().name().equalsIgnoreCase("LOCAL")) {
            String email = oAuth2User.getMember().getEmail();
            String loginType = oAuth2User.getUserInfo().getProvider().toLowerCase();

            // 연동 처리용 세션 저장
            request.getSession().setAttribute("providerId", oAuth2User.getUserInfo().getProviderId());
            request.getSession().setAttribute("profileImage", oAuth2User.getUserInfo().getProfileImage());
            request.getSession().setAttribute("loginType", loginType);
            request.getSession().setAttribute("linkAccountEmail", email);

            String redirectUrl = "/link/account?email=" + email + "&type=" + loginType;
            log.warn("⚠️ 연동 필요 사용자. Redirecting to {}", redirectUrl);
            response.sendRedirect(redirectUrl);
            return;
        }

        // ✅ 정상 소셜 로그인 사용자
        Long userId = oAuth2User.getMember().getId();
        String email = oAuth2User.getMember().getEmail();

        // JWT 발급
        String accessToken = jwtTokenUtil.generateToken(email);
        String refreshToken = jwtTokenUtil.generateRefreshToken(email);
        LocalDateTime refreshExp = jwtTokenUtil.getRefreshTokenExpiryDate();

        // 저장
        rememberTokenService.saveRefreshToken(
                userId,
                refreshToken,
                refreshExp,
                request.getHeader("User-Agent"),
                request.getRemoteAddr(),
                false // 소셜 로그인은 자동 로그인 X
        );

        // ✅ 쿠키 설정
        response.setHeader("Set-Cookie", jwtTokenUtil.createAccessTokenCookie(accessToken).toString());
        response.addHeader("Set-Cookie", jwtTokenUtil.createRefreshTokenCookie(refreshToken).toString());

        // ✅ redirect.html로 이동 (토큰은 쿠키로 전달됨)
        response.sendRedirect("/redirect.html");
    }
}
