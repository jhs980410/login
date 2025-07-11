package com.assignment.login.auth.security;

import com.assignment.login.auth.handler.CustomAuthenticationFailureHandler;
import com.assignment.login.auth.handler.CustomAuthenticationSuccessHandler;
import com.assignment.login.auth.oauth2.handler.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/logout",
                                "/member/loginPage",
                                "/member/signup",
                                "/api/members/check-email",
                                "/api/members/check-nickname",
                                "/css/**", "/js/**", "/images/**",
                                "/", "/home", "/error/**","/oauth2/**", "/login/oauth2/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                ) .oauth2Login(oauth2 -> oauth2
                        .defaultSuccessUrl("/home", true)  // ✅ 로그인 성공 시 무조건 /home으로 이동
                        .successHandler(oAuth2SuccessHandler)
                        .failureUrl("/member/loginPage?error") // 실패 시 이동할 URL

                );


        //  JWT 인증 필터 등록
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    //  AuthenticationManager 등록 (로그인에 필요)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
