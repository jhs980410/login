package com.assignment.login.auth.service;

import com.assignment.login.loginfail.domain.LoginFail;
import com.assignment.login.loginfail.repository.LoginFailRepository;
import com.assignment.login.member.domain.Member;
import com.assignment.login.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final LoginFailRepository loginFailRepository;
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        LoginFail loginFail = loginFailRepository.findByUserId(member.getId())
                .orElse(null);

        //  잠금 상태일 때만 검사
        if (member.isLocked() && loginFail != null && loginFail.getLastFailAt() != null) {
            Duration duration = Duration.between(loginFail.getLastFailAt(), LocalDateTime.now());

            if (duration.toMinutes() >= 5) {
                //  5분 지났으면 계정 잠금 해제 + 실패 횟수 초기화
                member.setLocked(false);
                loginFail.setFailCount(0);
                memberRepository.save(member);
                loginFailRepository.save(loginFail);
            }
        }

        return new CustomUserDetails(member, loginFail);
    }

}
