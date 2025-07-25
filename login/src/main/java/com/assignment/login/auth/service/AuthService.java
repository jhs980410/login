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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
        // 1. 인증 시도
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), request.getPassword()
                )
        );

        String email = authentication.getName();
        boolean autoLogin = request.isAutoLogin();
        Member member = memberRepository.findByEmail(email).orElseThrow();
        String deviceId = request.getDeviceId();

        // 2. Redis에서 최근 로그인 정보 조회
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
        }

        // 3. 비정상 로그인 감지 → Redis 플래그 설정 후 인증 절차 요구
        if (isSuspicious) {
            redisTemplate.opsForValue().set("needs_verification:" + member.getId(), "true", Duration.ofMinutes(10));
            throw new SuspiciousLoginException("비정상 로그인 감지됨. 인증이 필요합니다.");
        }

        // 4. Redis 정보 갱신 (정상 로그인일 때만)
        String combined = ipAddress + "|" + userAgent;
        redisTemplate.opsForValue().set(redisKey, combined, Duration.ofDays(30));

        // 5. 로그인 히스토리 저장 (성공 && 정상)
        loginHistoryService.saveLoginHistory(
                member,
                ipAddress,
                userAgent,
                null,
                null,
                true,   // success
                false,  // suspicious
                deviceId
        );

        // 6. 토큰 발급
        String accessToken = jwtTokenUtil.generateToken(email);
        String refreshToken = jwtTokenUtil.generateRefreshToken(email, autoLogin);

        RefreshToken token = RefreshToken.builder()
                .userId(member.getId())
                .token(refreshToken)
                .expiredAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusDays(autoLogin ? 14 : 2))
                .autoLogin(autoLogin)
                .userAgent(userAgent)
                .ipAddress(ipAddress)
                .build();

        refreshTokenRepository.deleteByUserId(member.getId());
        refreshTokenRepository.save(token);

        // 7. 실패 횟수 초기화
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
    public Map<String, String> forceLogin(String email, String deviceId) {
        // 1. 사용자 정보 조회 (없으면 예외 발생)
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("회원이 존재하지 않습니다."));

        // 2. JWT 액세스/리프레시 토큰 생성
        String accessToken = jwtTokenUtil.generateToken(email);
        String refreshToken = jwtTokenUtil.generateRefreshToken(email, false);

        // 3. 기존 리프레시 토큰 제거 (하나만 유지하기 위해)
        refreshTokenRepository.deleteByUserId(member.getId());

        // 4. 새로운 리프레시 토큰 저장
        RefreshToken token = RefreshToken.builder()
                .userId(member.getId())
                .token(refreshToken)
                .expiredAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusDays(2))
                .autoLogin(false)                         // 강제 로그인이므로 autoLogin false로 고정
                .userAgent("trusted")                    // trusted device에서 로그인했음을 기록
                .ipAddress("trusted")                   // 동일하게 IP도 trusted로 기록 (정상 판단 근거)
                .build();
        refreshTokenRepository.save(token);

        //  5. 최근 로그인 정보 Redis에 저장 (기기 중복 판단 위해)
        redisTemplate.opsForValue().set(
                "recentLogin:" + member.getEmail(),
                "trusted_ip|trusted_ua",                 // 단순 예시지만 실제 사용자 IP/UA를 받아도 됨
                Duration.ofDays(30)                      // 최근 로그인 TTL 설정 (30일 보관)
        );

        //  6. 로그인 히스토리 기록 (신뢰된 기기에서 발생했음을 명시)
        loginHistoryService.saveLoginHistory(
                member,
                "trusted_ip",                            // 또는 request.getRemoteAddr()
                "trusted_ua",                            // 또는 request.getHeader("User-Agent")
                null, null,
                true,                                    // 정상 로그인 여부
                false,                                   // 수상 로그인 아님
                deviceId                                 // 기기 ID 명확하게 저장
        );

        // 7. access / refresh 토큰 반환
        return Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken
        );
    }


}
