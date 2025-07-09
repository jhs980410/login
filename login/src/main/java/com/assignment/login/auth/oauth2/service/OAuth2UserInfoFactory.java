package com.assignment.login.auth.oauth2.service;

import com.assignment.login.auth.oauth2.userinfo.*;

import java.util.Map;

public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        switch (registrationId.toLowerCase()) {
            case "google":
                return new GoogleOAuth2UserInfo(attributes);
            case "kakao":
                return new KakaoOAuth2UserInfo(attributes);
            case "naver":
                return new NaverOAuth2UserInfo(attributes);
            case "apple":
                return new AppleOAuth2UserInfo(attributes);
            default:
                throw new IllegalArgumentException("지원하지 않는 소셜 로그인 타입: " + registrationId);
        }
    }
}
