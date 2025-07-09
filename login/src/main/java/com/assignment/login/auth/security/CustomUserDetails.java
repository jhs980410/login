package com.assignment.login.auth.security;

import com.assignment.login.auth.domain.LoginFail;
import com.assignment.login.member.domain.Member;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;


public class CustomUserDetails implements UserDetails {

    private final Member member;
    private final LoginFail loginFail;

    public CustomUserDetails(Member member, LoginFail loginFail) {
        this.member = member;
        this.loginFail = loginFail;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return member.getPassword();
    }

    @Override
    public String getUsername() {
        return member.getEmail(); // 로그인 식별자는 이메일
    }

    @Override public boolean isAccountNonExpired() {
        return true;
    }
    @Override
    public boolean isAccountNonLocked() {
        if (!member.isLocked()) return true; // 애초에 잠겨 있지 않으면 통과

        // 로그인 실패 기록이 없으면 잠금 해제
        if (loginFail == null || loginFail.getLastFailAt() == null) return true;

        // 5분 이상 지났다면 잠금 해제
        Duration duration = Duration.between(loginFail.getLastFailAt(), LocalDateTime.now());
        return duration.toMinutes() >= 5;
    }

    @Override public boolean isCredentialsNonExpired() {
        return true;
    }
    @Override public boolean isEnabled() {
        return true;
    }

    public String getNickname() {
        return member.getNickname();
    }

    public Long getId() {
        return member.getId();
    }
}
