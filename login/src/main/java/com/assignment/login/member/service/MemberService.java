package com.assignment.login.member.service;

import com.assignment.login.member.domain.Member;
import com.assignment.login.member.dto.MemberSignupRequest;
import com.assignment.login.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private PasswordEncoder passwordEncoder;

    public void signup(MemberSignupRequest request) {
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 등록된 이메일입니다.");
        }
        if (memberRepository.existsByNickname(request.getNickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        Member member = new Member();
        member.setEmail(request.getEmail());
        member.setNickname(request.getNickname());
        member.setPassword(passwordEncoder.encode(request.getPassword()));
        member.setLoginType("EMAIL");
        memberRepository.save(member);
    }
}
