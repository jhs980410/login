package com.assignment.login.auth.config;

import com.assignment.login.auth.handler.CustomAuthenticationFailureHandler;
import com.assignment.login.auth.handler.CustomAuthenticationSuccessHandler;
import com.assignment.login.auth.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
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
                //  CSRF는 JWT 기반 API이므로 비활성화 (람다 방식)
                .csrf(csrf -> csrf.disable())

                //  세션을 사용하지 않음 (STATELESS)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                //  권한/인증 없이 접근 가능한 경로
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/login",              // 로그인 처리
                                "/member/signup",               // 회원가입
                                "/api/members/check-email",     // 중복 검사
                                "/api/members/check-nickname",
                                "/css/**", "/js/**", "/images/**",
                                "/", "/home", "/error/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )

                //  formLogin 대신 직접 login API 처리
                .formLogin(form -> form
                        .loginProcessingUrl("/api/auth/login")
                        .successHandler(customAuthenticationSuccessHandler)
                        .failureHandler(customAuthenticationFailureHandler)
                        .permitAll()
                )

                //  로그아웃도 필요시 커스터마이징
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessUrl("/member/loginPage?logout=true")
                        .permitAll()
                );

        return http.build();
    }

    //  AuthenticationManager 등록 (로그인에 필요)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
