package com.assignment.login.member.dto;

import com.assignment.login.member.domain.Member;
import com.assignment.login.member.domain.enums.LoginType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberSignupRequest {
    private String email;
    private String password;
    private String confirmPassword;
    private String nickname;

    public Member toEntity(String encodedPassword) {
        Member member = new Member();
        member.setEmail(email);
        member.setNickname(nickname);
        member.setPassword(encodedPassword); // 암호화된 비밀번호 전달
        member.setLoginType(LoginType.LOCAL);
        return member;
    }

}
