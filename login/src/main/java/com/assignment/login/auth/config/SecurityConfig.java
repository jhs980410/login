package com.assignment.login.auth.config;

import com.assignment.login.auth.handler.CustomAuthenticationFailureHandler;
import com.assignment.login.auth.handler.CustomAuthenticationSuccessHandler;
import com.assignment.login.auth.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;
    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/member/**", "/css/**", "/js/**", "/images/**", "/", "/home", "/error/**",
                                "/api/members/check-email", "/api/members/check-nickname"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/member/loginPage")
                        .loginProcessingUrl("/member/process-login")
                        .defaultSuccessUrl("/home", true)
                        .failureHandler(customAuthenticationFailureHandler) //로그인실패시
                        .successHandler(customAuthenticationSuccessHandler) //로그인 성공
                        .usernameParameter("email")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/member/logout")
                        .logoutSuccessUrl("/member/loginPage?logout=true")
                        .permitAll()
                )
                .rememberMe(r -> r.userDetailsService(customUserDetailsService))
                .requestCache(requestCache -> requestCache.disable());

        return http.build();
    }
}
