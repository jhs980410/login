package com.assignment.login.auth.oauth2.userinfo;

import java.util.Map;

public class AppleOAuth2UserInfo extends OAuth2UserInfo {

    public AppleOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getProvider() {
        return "apple";
    }

    @Override
    public String getProviderId() {
        return attributes.get("sub").toString();  // JWT의 subject (고유 ID)
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email"); // Apple은 이메일을 처음 로그인할 때만 제공
    }

    @Override
    public String getName() {
        return "Apple User";  // Apple은 이름 제공 안 함. "임의 이름" 설정
    }

    @Override
    public String getProfileImage() {
        return "";
    }
}
