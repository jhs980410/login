package com.assignment.login.auth.service;

import com.assignment.login.auth.dto.LoginRequest;
import com.assignment.login.auth.exception.SuspiciousLoginException;
import com.assignment.login.auth.util.JwtTokenUtil;
import com.assignment.login.auth.domain.RefreshToken;
import com.assignment.login.auth.repository.RefreshTokenRepository;
import com.assignment.login.common.service.CommonService;
import com.assignment.login.common.service.EmailService;
import com.assignment.login.member.domain.Member;
import com.assignment.login.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final LoginFailService loginFailService;
    private final CommonService commonService;
    private final EmailService emailService;
    private final RedisTemplate redisTemplate;
    private final LoginHistoryService loginHistoryService;

    public Map<String, String> login(LoginRequest request, String userAgent, String ipAddress) throws SuspiciousLoginException {
        // 인증 시도
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), request.getPassword()
                )
        );

        String email = authentication.getName();
        boolean autoLogin = request.isAutoLogin();
        Member member = memberRepository.findByEmail(email).orElseThrow();

        // Redis에서 최근 로그인 정보 조회
        String redisKey = "recentLogin:" + member.getEmail();
        String recentLogin = (String) redisTemplate.opsForValue().get(redisKey);

        boolean isSuspicious = false;
        if (recentLogin != null) {
            String[] parts = recentLogin.split("\\|");
            if (parts.length >= 2) {
                String recentIp = parts[0];
                String recentUserAgent = parts[1];
                if (!recentIp.equals(ipAddress) || !recentUserAgent.equals(userAgent)) {
                    isSuspicious = true;  // 다른 IP 또는 브라우저
                }
            } else {
                isSuspicious = true; // 포맷 이상 → 의심
            }
        } else {
            // 👉 최초 로그인으로 판단
            String combined = ipAddress + "|" + userAgent;
            redisTemplate.opsForValue().set(redisKey, combined, Duration.ofDays(30));

            //  로그인 히스토리 저장
            loginHistoryService.saveLoginHistory(
                    member,
                    ipAddress,
                    userAgent,
                    null,              // location → 나중에 GeoIP 등으로 넣을 수 있음
                    null,              // deviceType → 나중에 user-agent 파싱으로 가능
                    true,              // success
                    false              // suspicious
            );

        }
        if (isSuspicious) {
            redisTemplate.opsForValue().set("needs_verification:" + member.getId(), "true", Duration.ofMinutes(10));
            loginHistoryService.saveLoginHistory(
                    member, ipAddress, userAgent, null, null, true, true
            );
            throw new SuspiciousLoginException("비정상 로그인 감지됨. 인증이 필요합니다.");
        }
        // 정상 로그인 → 최근 로그인 정보 갱신
        String combined = ipAddress + "|" + userAgent;
        redisTemplate.opsForValue().set(redisKey, combined, Duration.ofDays(30));

        // 토큰 발급
        String accessToken = jwtTokenUtil.generateToken(email);
        String refreshToken = jwtTokenUtil.generateRefreshToken(email, autoLogin);

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

        // 실패 횟수 초기화
        loginFailService.resetFailCount(email);

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
            // 토큰이 저장되지 않은 상태에서 로그아웃 시도된 경우를 위한 보완
            refreshTokenRepository.deleteByUserId(userId);  // fallback
        }
    }


    public String generateAndSendCode(String email) {
        String code = String.valueOf(new Random().nextInt(899999) + 100000); // 6자리
        commonService.put(email, code);
        // 이메일 전송 로직 (메일 API 연동 필요)
        emailService.sendAuthCode(email, code); // 이메일 전송
        System.out.println("[" + email + "] 인증코드: " + code);
        return code;
    }

    public boolean verifyCode(String email, String inputCode) {
        String stored = commonService.get(email);
        return stored != null && stored.equals(inputCode);
    }


}
