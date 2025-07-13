package com.assignment.login.auth.oauth2.service;

import com.assignment.login.auth.oauth2.userinfo.OAuth2UserInfo;
import com.assignment.login.auth.security.CustomOAuth2User;
import com.assignment.login.member.domain.Member;
import com.assignment.login.member.domain.enums.LoginType;
import com.assignment.login.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberService memberService;
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        // 1. 유저 정보 받아오기
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 2. registrationId (google, kakao 등)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // 3. 사용자 정보 맵
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 4. 추상화된 OAuth2UserInfo 객체 생성
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, attributes);

        String email = userInfo.getEmail();
        String providerId = userInfo.getProviderId();
        LoginType loginType = LoginType.valueOf(registrationId.toUpperCase());

        Optional<Member> optionalByEmail = memberService.findByEmail(email);

        // 기존 이메일이 존재하는 경우
        if (optionalByEmail.isPresent()) {
            Member existing = optionalByEmail.get();

            // 동일 소셜 계정으로 로그인한 경우
            if (existing.getLoginType() == loginType) {
                return new CustomOAuth2User(userInfo, attributes, existing);
            }

            // 로컬 계정으로 가입된 경우 → 연동 유도
            if (existing.getLoginType() == LoginType.LOCAL) {
                return new CustomOAuth2User(userInfo, attributes, existing); // 연동 유도
            }

            // 다른 소셜 계정으로 이미 가입된 경우
            // → 인증 유도 불가 (예: 카카오로 가입했는데 구글로 시도)
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_login", "이미 다른 소셜 계정으로 가입된 이메일입니다.", null)
            );
        }


        // 기존 이메일이 없고 providerId로도 없으면 신규 가입
        Optional<Member> optionalByProvider = memberService.findByProviderId(providerId);
        Member member;

        if (optionalByProvider.isPresent()) {
            member = optionalByProvider.get();
        } else {
            // 신규 가입 처리
            member = new Member();
            member.setEmail(email);
            member.setNickname(userInfo.getName());
            member.setLoginType(loginType);
            member.setProviderId(providerId);
            member.setProfileImage((String) attributes.get("picture"));
            memberService.save(member);
        }

        return new CustomOAuth2User(userInfo, attributes, member);
    }

    @Transactional
    public void linkSocialToLocalAccount(String email) {
        Member member = memberService.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        member.setLoginType(LoginType.KAKAO); // 나중에 loginType 동적으로 주입 가능
        member.setProviderId(currentSocialProviderId());
        memberService.save(member);
    }

    private String currentSocialProviderId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}