package com.assignment.login.auth.handler;

import com.assignment.login.auth.service.LoginFailService;
import com.assignment.login.member.domain.Member;
import com.assignment.login.member.repository.MemberRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final MemberRepository memberRepository;
    private final LoginFailService loginFailService;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception)
            throws IOException, ServletException {

        String email = request.getParameter("email");
        Member member = memberRepository.findByEmail(email).orElse(null);

        // 사용자 존재 시 로그인 실패 처리
        if (member != null) {
            loginFailService.recordFail(member);

            if (member.isLocked()) {
                // Redis에 저장된 TTL 기반으로 잠금 해제 시간 계산
                LocalDateTime unlockTime = loginFailService.getUnlockTime(member);
                if (unlockTime != null) {
                    String formattedTime = unlockTime.format(DateTimeFormatter.ofPattern("HH시 mm분 이후"));
                    response.sendRedirect("/member/loginPage?locked=true&wait=" + formattedTime);
                } else {
                    response.sendRedirect("/member/loginPage?locked=true");
                }
                return;
            }
        }

        // 기본 실패 처리 (비밀번호 틀림 등)
        response.sendRedirect("/member/loginPage?error");
    }
}
