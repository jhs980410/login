package com.assignment.login.auth.security;


import com.assignment.login.auth.dto.RefreshTokenPayload;
import com.assignment.login.auth.util.JwtTokenUtil;
import com.assignment.login.member.service.MemberService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 1. Authorization 헤더 또는 accessToken 쿠키에서 토큰 추출
            String accessToken = null;
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                accessToken = authHeader.substring(7);
            } else {
                accessToken = getCookieValue(request, "accessToken");
            }

            if (accessToken != null) {
                try {
                    // 2. accessToken 유효성 확인
                    String email = jwtTokenUtil.getEmailFromToken(accessToken);
                    if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        authenticateUser(email, request);
                    }
                } catch (io.jsonwebtoken.ExpiredJwtException ex) {
                    // 3. accessToken 만료 → refreshToken 검사
                    String refreshToken = getCookieValue(request, "refreshToken");
                    if (refreshToken != null) {
                        Optional<RefreshTokenPayload> refreshOpt = jwtTokenUtil.validateAndGetRefreshToken(refreshToken);

                        if (refreshOpt.isPresent()) {
                            RefreshTokenPayload token = refreshOpt.get();

                            if (!token.isAutoLogin()) {
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                return;
                            }

                            String email = token.getEmail();
                            String newAccessToken = jwtTokenUtil.generateToken(email);

                            authenticateUser(email, request);

                            ResponseCookie newCookie = ResponseCookie.from("accessToken", newAccessToken)
                                    .httpOnly(true)
                                    .secure(false)
                                    .path("/")
                                    .maxAge(15 * 60)
                                    .sameSite("Lax")
                                    .build();

                            response.setHeader("Set-Cookie", newCookie.toString());
                        }else {
                            // refreshToken도 만료 → 401
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            return;
                        }
                    } else {
                        // refreshToken 없음 → 401
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        return;
                    }
                }
            }

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\": \"유효하지 않은 토큰입니다.\"}");
            response.getWriter().flush();
            e.printStackTrace();
        }
    }

    //쿠키에서 jwt 추출하는함수
    private String getCookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals(name)) return cookie.getValue();
        }
        return null;
    }

    private void authenticateUser(String email, HttpServletRequest request) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }
}
