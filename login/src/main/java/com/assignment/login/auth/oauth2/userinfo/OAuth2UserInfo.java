package com.assignment.login.auth.oauth2.userinfo;

import java.util.Map;

public abstract class OAuth2UserInfo {
    protected Map<String, Object> attributes;

    public OAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public abstract String getProvider();      // "google", "kakao" 등
    public abstract String getProviderId();    // 각 플랫폼 고유 ID
    public abstract String getEmail();
    public abstract String getName();
}
