package com.assignment.login.member.service;

import com.assignment.login.member.domain.Member;
import com.assignment.login.member.dto.MemberSignupRequest;
import com.assignment.login.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public void signup(MemberSignupRequest request) {
        if (existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 등록된 이메일입니다.");
        }
        if (existsByNickname(request.getNickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        System.out.println("입력 비밀번호: " + request.getPassword());
        System.out.println("인코딩 비밀번호: " + passwordEncoder.encode(request.getPassword()));
        Member member = request.toEntity(encodedPassword); //

        memberRepository.save(member); //회원가입
    }


    public boolean existsByEmail(String email) {
        return memberRepository.existsByEmail(email);
    }

    public boolean existsByNickname(String nickname) {
        return memberRepository.existsByNickname(nickname);
    }


    public Optional<Member> findByProviderId(String providerId) {
        return memberRepository.findByProviderId(providerId);
    }

    public Member save(Member member) {
        return memberRepository.save(member);
    }

    public Optional<Member> findByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    public void update(Member member) {
        memberRepository.save(member);
    }

    public void updatePassword(String email, String newPassword) {
        String encodedPassword = passwordEncoder.encode(newPassword);
        memberRepository.updatePassword(email, encodedPassword);

    }
    public String findEmailByUserId(Long userId) {
        return memberRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("해당 사용자를 찾을 수 없습니다."))
                .getEmail(); // getUsername()을 쓴다면 이메일이 username이라면 그것도 가능
    }

}
