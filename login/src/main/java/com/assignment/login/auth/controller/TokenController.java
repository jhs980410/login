package com.assignment.login.auth.controller;

import com.assignment.login.auth.util.JwtTokenUtil;
import com.assignment.login.auth.domain.RefreshToken;
import com.assignment.login.auth.service.RefreshTokenService;
import com.assignment.login.member.domain.Member;
import com.assignment.login.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class TokenController {

    private final RefreshTokenService refreshTokenService;
    private final JwtTokenUtil jwtTokenUtil;
    private final MemberRepository memberRepository;

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(@RequestBody Map<String, String> body) {
        String refreshTokenStr = body.get("refreshToken");

        RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenStr)
                .filter(token -> token.getExpiredAt().isAfter(java.time.LocalDateTime.now(ZoneId.of("Asia/Seoul"))))
                .orElse(null);

        if (refreshToken == null) {
            return ResponseEntity.status(401).body(Map.of("message", "RefreshToken이 유효하지 않거나 만료되었습니다."));
        }

        Member member = memberRepository.findById(refreshToken.getUserId()).orElse(null);
        if (member == null) {
            return ResponseEntity.status(401).body(Map.of("message", "사용자를 찾을 수 없습니다."));
        }

        String newAccessToken = jwtTokenUtil.generateToken(member.getEmail());

        return ResponseEntity.ok(Map.of(
                "accessToken", newAccessToken
        ));
    }
}
