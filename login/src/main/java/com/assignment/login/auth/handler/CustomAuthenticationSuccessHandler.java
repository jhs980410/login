package com.assignment.login.auth.handler;

import com.assignment.login.auth.util.JwtTokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenUtil jwtTokenUtil;
    //폼로그인용 토큰발급처
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        String email = authentication.getName(); // UserDetailsService에서 반환한 식별자
        String token = jwtTokenUtil.generateToken(email); //토큰 발급

        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"status\":\"success\", \"token\":\"" + token + "\"}");
    }
}
