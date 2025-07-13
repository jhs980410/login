package com.assignment.login.member.service;

import com.assignment.login.member.domain.Member;
import com.assignment.login.member.domain.enums.LoginType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SocialAccountLinkService {

    private final MemberService memberService;

    /**
     * 소셜 계정을 기존 로컬 계정에 연동하는 로직
     *
     * @param email      연동 대상 이메일 (기존 로컬 계정)
     * @param loginType  연동할 소셜 로그인 타입 (KAKAO, NAVER, GOOGLE 등)
     */
    public void link(String email, LoginType loginType) {
        memberService.findByEmail(email).ifPresent(member -> {
            // 기존 계정이 LOCAL일 경우에만 소셜 연동
            if (member.getLoginType() == LoginType.LOCAL) {
                member.setLoginType(loginType);  // 🔄 동적 타입으로 연동
                memberService.update(member);
            }
        });
    }
}
