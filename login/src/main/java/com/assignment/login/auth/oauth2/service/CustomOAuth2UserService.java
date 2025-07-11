package com.assignment.login.auth.oauth2.service;

import com.assignment.login.auth.oauth2.userinfo.OAuth2UserInfo;
import com.assignment.login.auth.security.CustomOAuth2User;
import com.assignment.login.member.domain.Member;
import com.assignment.login.member.domain.enums.LoginType;
import com.assignment.login.member.repository.MemberRepository;
import com.assignment.login.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;


@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberService memberService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. 유저 정보 받아오기
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 2. registrationId (google, kakao 등)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // 3. 사용자 정보 맵
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 4. 추상화된 OAuth2UserInfo 객체 생성 (ex: GoogleOAuth2UserInfo)
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, attributes);

        // 5. 유저 DB 조회
        Optional<Member> optionalMember = memberService.findByProviderId(userInfo.getProviderId());

        Member member;
        if (optionalMember.isPresent()) {
            member = optionalMember.get();
        } else {
            // 6. 신규 회원 저장
            member = new Member();
            member.setEmail(userInfo.getEmail());
            member.setNickname(userInfo.getName());
            member.setLoginType(LoginType.valueOf(registrationId.toUpperCase()));
            member.setProviderId(userInfo.getProviderId());
            member.setProfileImage((String) attributes.get("picture")); // 선택
            memberService.save(member);
        }

        // 7. 사용자 인증 정보 객체 반환
        return new CustomOAuth2User(userInfo, attributes, member);
    }
}