package com.assignment.login.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 👇 DI 사용을 위해 passwordEncoder 주입
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails user = User.withUsername("tempuser")
                .password(passwordEncoder.encode("temp1234"))
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, UserDetailsService userDetailsService) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF 비활성화
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/member/**", "/css/**", "/js/**", "/images/**", "/", "/home", "/error/**","/api/members/check-email", "/api/members/check-nickname").permitAll()

                        .anyRequest().authenticated()
                )

                .formLogin(form -> form
                        .loginPage("/member/loginPage") // 로그인 페이지
                        .loginProcessingUrl("/member/process-login") // 로그인 처리 URL
                        .defaultSuccessUrl("/home", true) // 로그인 성공 후 이동
                        .failureUrl("/member/loginPage?error=true") // 실패 시 이동
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout") // 로그아웃 처리 URL
                        .logoutSuccessUrl("/member/loginPage?logout=true") // 로그아웃 성공 후 이동
                        .permitAll()
                )
                .rememberMe(r -> r.userDetailsService(userDetailsService)) // Remember-Me
                // 이전 요청 캐시 활성화
                .requestCache(requestCache -> requestCache.disable());

        return http.build();
    }



}
