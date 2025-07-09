package com.assignment.login.auth.security;

import com.assignment.login.auth.oauth2.userinfo.OAuth2UserInfo;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {

    private final OAuth2UserInfo userInfo;
    private final Map<String, Object> attributes;

    public CustomOAuth2User(OAuth2UserInfo userInfo, Map<String, Object> attributes) {
        this.userInfo = userInfo;
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER")); // or ROLE_SOCIAL_USER 등
    }

    @Override
    public String getName() {
        return userInfo.getProviderId(); // 유저 식별자 (ex. sub, id, sns 고유키)
    }
}

