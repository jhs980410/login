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

        if (member != null) {
            loginFailService.recordFail(member);

            // 실패 횟수 확인
            loginFailRepository.findByUserId(member.getId()).ifPresent(fail ->
                    System.out.println("실패횟수: " + fail.getFailCount())
            );
        }

        System.out.println("로그인 실패한 사용자 이메일: " + email);
        response.sendRedirect("/member/loginPage?error");
    }
}

