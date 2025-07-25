package com.assignment.login.auth.util;

import com.assignment.login.auth.domain.RefreshToken;
import com.assignment.login.auth.dto.RefreshTokenPayload;
import com.assignment.login.auth.service.RefreshTokenService;
import io.jsonwebtoken.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.security.*;
import java.security.spec.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

@Slf4j
@Component
public class JwtTokenUtil {

    private final RefreshTokenService refreshTokenService;
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private final long EXPIRATION_TIME = 1000 * 60 * 15; // 15분
    private final long REFRESH_EXPIRATION_TIME = 1000L * 60 * 60 * 24 * 2; // 기본 리프레시 2일
    private final long REMEMBER_EXPIRATION_TIME = 1000L * 60 * 60 * 24 * 14; // 자동 로그인 14일

    public JwtTokenUtil(RefreshTokenService refreshTokenService) {
        this.refreshTokenService = refreshTokenService;
    }

    @PostConstruct
    public void loadKeys() {
        try {
            // classpath 리소스에서 키 파일 읽기
            InputStream privateKeyStream = getClass().getClassLoader().getResourceAsStream("keys/private_key_pkcs8.pem");
            InputStream publicKeyStream = getClass().getClassLoader().getResourceAsStream("keys/public_key.pem");

            if (privateKeyStream == null || publicKeyStream == null) {
                throw new RuntimeException("키 파일을 찾을 수 없습니다.");
            }

            // Base64 decoding이 아닌 PEM 처리 (헤더 제거 → Base64 디코딩)
            String privatePem = new String(privateKeyStream.readAllBytes())
                    .replaceAll("-----BEGIN PRIVATE KEY-----", "")
                    .replaceAll("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", ""); // 줄바꿈, 공백 제거

            String publicPem = new String(publicKeyStream.readAllBytes())
                    .replaceAll("-----BEGIN PUBLIC KEY-----", "")
                    .replaceAll("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] privateKeyBytes = java.util.Base64.getDecoder().decode(privatePem);
            byte[] publicKeyBytes = java.util.Base64.getDecoder().decode(publicPem);

            PKCS8EncodedKeySpec privateSpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(publicKeyBytes);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            privateKey = keyFactory.generatePrivate(privateSpec);
            publicKey = keyFactory.generatePublic(publicSpec);

        } catch (Exception e) {
            throw new RuntimeException("키 로딩 실패", e);
        }
    }

    // Access Token
    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }
    //RefreshToken
    public String generateRefreshToken(String email, boolean isAutoLogin) {
        long expiration = isAutoLogin ? REMEMBER_EXPIRATION_TIME : REFRESH_EXPIRATION_TIME;

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }
    //오버로딩처리
    public String generateRefreshToken(String email) {
        return generateRefreshToken(email, false); // 기본은 자동 로그인 false
    }

    public String getEmailFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    public LocalDateTime getRefreshTokenExpiryDate() {
        return LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusDays(2);
    }

    public Optional<RefreshTokenPayload> validateAndGetRefreshToken(String token) {
        try {
            Optional<RefreshToken> saved = refreshTokenService.findByToken(token);
            if (saved.isPresent() && saved.get().getExpiredAt().isAfter(LocalDateTime.now(ZoneId.of("Asia/Seoul")))) {
                RefreshToken rt = saved.get();
                String email = getEmailFromToken(token); // 여기!
                return Optional.of(new RefreshTokenPayload(rt.getUserId(), email, rt.isAutoLogin()));
            }
        } catch (Exception e) {
            log.warn("RefreshToken 검증 중 예외 발생: {}", e.getMessage());
        }
        return Optional.empty();
    }

    public ResponseCookie createAccessTokenCookie(String token) {
        return ResponseCookie.from("accessToken", token)
                .httpOnly(true)
                .secure(true) // 운영 환경에서 true
                .path("/")
                .maxAge(15 * 60)
                .sameSite("Lax")
                .build();
    }

    public ResponseCookie createRefreshTokenCookie(String token) {
        return ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(14 * 24 * 60 * 60) // 14일
                .sameSite("Lax")
                .build();
    }

}
