package com.assignment.login.auth.handler;

import com.assignment.login.loginfail.repository.LoginFailRepository;
import com.assignment.login.loginfail.service.LoginFailService;
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
import java.time.Duration;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final MemberRepository memberRepository;
    private final LoginFailService loginFailService;
    private final LoginFailRepository loginFailRepository;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception)
            throws IOException, ServletException {

        String email = request.getParameter("email");
        Member member = memberRepository.findByEmail(email).orElse(null);

        // 사용자 존재 시 로그인 실패
        if (member != null) {
            loginFailService.recordFail(member);

            // 계정이 잠겨있는 경우
            if (member.isLocked()) {
                loginFailRepository.findByUserId(member.getId()).ifPresentOrElse(fail -> {
                    long minutesLeft = Math.max(5 - Duration.between(
                            fail.getLastFailAt(), LocalDateTime.now()).toMinutes(), 0);
                    try {
                        response.sendRedirect("/member/loginPage?locked=true&wait=" + minutesLeft);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }, () -> {
                    try {
                        response.sendRedirect("/member/loginPage?locked=true");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                return; // 리다이렉트
            }

            // 잠긴 계정이 아니라면 실패 횟수 로그 출력 (선택 사항)
            loginFailRepository.findByUserId(member.getId()).ifPresent(fail ->
                    System.out.println("실패횟수: " + fail.getFailCount()));
        }

        // 기본 로그인 실패 처리 (비밀번호 틀림 등)
        response.sendRedirect("/member/loginPage?error");
    }


}

