package com.assignment.login.loginfail.service;

import com.assignment.login.loginfail.domain.LoginFail;
import com.assignment.login.loginfail.repository.LoginFailRepository;
import com.assignment.login.member.domain.Member;
import com.assignment.login.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class LoginFailService {

    private final LoginFailRepository loginFailRepository;
    private final MemberRepository memberRepository;

    // 로그인 실패 기록 (Member 객체 기반)

    public void recordFail(Member member) {
        LoginFail fail = loginFailRepository.findByUserId(member.getId())
                .orElseGet(() -> createNewFail(member.getId()));

        int newCount = fail.getFailCount() + 1;
        fail.setFailCount(newCount);
        fail.setLastFailAt(LocalDateTime.now());

        loginFailRepository.save(fail);

        if (newCount >= 5) {
            member.setLocked(true); // 계정 잠금
            memberRepository.save(member);
        }
    }


    // 로그인 성공 시 실패 기록 초기화
    public void resetFailCount(String email) {
        Member member = memberRepository.findByEmail(email).orElse(null);
        if (member == null) {
            return;
        }

        LoginFail loginFail = loginFailRepository.findByUserId(member.getId()).orElse(null);
        if (loginFail != null) {
            loginFail.setFailCount(0);
            loginFailRepository.save(loginFail);
        }

        if (member.isLocked()) {
            member.setLocked(false); //  잠금 해제
            memberRepository.save(member);
        }
    }

    // 실패 정보 새로 생성
    private LoginFail createNewFail(Long userId) {
        LoginFail fail = new LoginFail();
        fail.setUserId(userId);
        fail.setFailCount(0);
        fail.setLastFailAt(LocalDateTime.now());
        return fail;
    }

    public void recordFailByEmail(String email) {
        memberRepository.findByEmail(email)
                .ifPresent(this::recordFail);
    }
}
