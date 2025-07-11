package com.assignment.login.auth.service;

import com.assignment.login.auth.dto.LoginRequest;
import com.assignment.login.auth.util.JwtTokenUtil;
import com.assignment.login.auth.domain.RefreshToken;
import com.assignment.login.auth.repository.RefreshTokenRepository;
import com.assignment.login.member.domain.Member;
import com.assignment.login.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final LoginFailService loginFailService;
    public Map<String, String> login(LoginRequest request, String userAgent, String ipAddress) {
        //  인증 시도
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), request.getPassword()
                )
        );

        //  사용자 정보 및 토큰 발급
        String email = authentication.getName();
        boolean autoLogin = request.isAutoLogin();
        String accessToken = jwtTokenUtil.generateToken(email);
        String refreshToken = jwtTokenUtil.generateRefreshToken(email, autoLogin);

        Member member = memberRepository.findByEmail(email).orElseThrow();

        //  기존 토큰 삭제 및 저장
        RefreshToken token = RefreshToken.builder()
                .userId(member.getId())
                .token(refreshToken)
                .expiredAt(LocalDateTime.now().plusDays(autoLogin ? 14 : 2))
                .autoLogin(autoLogin)
                .userAgent(userAgent)
                .ipAddress(ipAddress)
                .build();
        refreshTokenRepository.deleteByUserId(member.getId());
        refreshTokenRepository.save(token);

        //  로그인 실패 횟수 초기화
        loginFailService.resetFailCount(email);

        //  토큰 반환
        return Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken
        );
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null || !jwtTokenUtil.validateToken(refreshToken)) {
            return;
        }

        String email = jwtTokenUtil.getEmailFromToken(refreshToken);
        Long userId = memberRepository.findByEmail(email).orElseThrow().getId();

        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByToken(refreshToken);

        if (tokenOpt.isPresent()) {
            if (tokenOpt.get().getUserId().equals(userId)) {
                refreshTokenRepository.delete(tokenOpt.get());
            }
        } else {
            // ✅ 토큰이 저장되지 않은 상태에서 로그아웃 시도된 경우를 위한 보완
            refreshTokenRepository.deleteByUserId(userId);  // fallback
        }
    }


}
