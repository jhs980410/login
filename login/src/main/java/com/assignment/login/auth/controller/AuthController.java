package com.assignment.login.auth.controller;

import com.assignment.login.auth.dto.LoginRequest;
import com.assignment.login.jwt.JwtTokenUtil;
import com.assignment.login.jwt.domain.RefreshToken;
import com.assignment.login.jwt.repository.RefreshTokenRepository;
import com.assignment.login.loginfail.domain.LoginFail;
import com.assignment.login.loginfail.repository.LoginFailRepository;
import com.assignment.login.loginfail.service.LoginFailService;
import com.assignment.login.member.domain.Member;
import com.assignment.login.member.repository.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final LoginFailService loginFailService;
    private final MemberRepository memberRepository;
    private final LoginFailRepository loginFailRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @PostMapping("/login")
    public ResponseEntity<?> loginlogin(@RequestBody LoginRequest request,
                                        @RequestHeader(value = "User-Agent") String userAgent,
                                        HttpServletRequest httpRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(), request.getPassword()
                    )
            );

            String email = authentication.getName();
            boolean autoLogin = request.isAutoLogin();
            String accessToken = jwtTokenUtil.generateToken(email);
            String refreshToken = jwtTokenUtil.generateRefreshToken(email,autoLogin);

            Member member = memberRepository.findByEmail(email).orElseThrow();
            RefreshToken token = RefreshToken.builder()
                    .userId(member.getId())
                    .token(refreshToken)
                    .expiredAt(LocalDateTime.now().plusDays(request.isAutoLogin() ? 14 : 2))
                    .autoLogin(request.isAutoLogin())
                    .userAgent(userAgent)
                    .ipAddress(httpRequest.getRemoteAddr())
                    .build();
            refreshTokenRepository.deleteByUserId(member.getId());
            refreshTokenRepository.save(token);

            loginFailService.resetFailCount(email);

            return ResponseEntity.ok(Map.of(
                    "accessToken", accessToken,
                    "refreshToken", refreshToken
            ));

        } catch (LockedException e) {
            Member member = memberRepository.findByEmail(request.getEmail()).orElse(null);
            if (member != null) {
                LoginFail fail = loginFailRepository.findByUserId(member.getId()).orElse(null);
                if (fail != null && fail.getLastFailAt() != null) {
                    LocalDateTime unlockTime = fail.getLastFailAt().plusMinutes(5);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH시 mm분 이후");
                    String unlockMessage = unlockTime.format(formatter);

                    return ResponseEntity.status(HttpStatus.LOCKED)
                            .body(Map.of("message", "계정이 잠겨 있습니다. "+unlockMessage + "에 다시 로그인해주세요."));
                }
            }
            return ResponseEntity.status(HttpStatus.LOCKED)
                    .body(Map.of("message", "계정이 잠겨 있습니다. 5분 후 다시 시도해 주세요."));

        } catch (BadCredentialsException e) {
            loginFailService.recordFailByEmail(request.getEmail()); // 실패 시 카운트 증가

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "이메일 또는 비밀번호가 일치하지 않습니다."));
        }
    }


}
