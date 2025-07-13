package com.assignment.login.auth.oauth2.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class OAuth2FailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {

        String message = "소셜 로그인에 실패했습니다.";
        if (exception instanceof OAuth2AuthenticationException auth2Ex) {
            OAuth2Error error = auth2Ex.getError();
            if (error != null && error.getDescription() != null) {
                message = error.getDescription();
            }
        }

        response.sendRedirect("/member/loginPage?error=" + URLEncoder.encode(message, StandardCharsets.UTF_8));
    }

}
