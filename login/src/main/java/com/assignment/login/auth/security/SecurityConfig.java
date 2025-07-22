    package com.assignment.login.auth.security;

    import com.assignment.login.auth.handler.CustomAuthenticationFailureHandler;
    import com.assignment.login.auth.handler.CustomAuthenticationSuccessHandler;
    import com.assignment.login.auth.oauth2.handler.OAuth2FailureHandler;
    import com.assignment.login.auth.oauth2.handler.OAuth2SuccessHandler;
    import com.assignment.login.auth.oauth2.service.CustomOAuth2UserService;
    import com.assignment.login.member.service.MemberService;
    import lombok.RequiredArgsConstructor;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.security.authentication.AuthenticationManager;
    import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
    import org.springframework.security.config.annotation.web.builders.HttpSecurity;
    import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
    import org.springframework.security.config.http.SessionCreationPolicy;
    import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
    import org.springframework.security.crypto.password.PasswordEncoder;
    import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
    import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
    import org.springframework.security.oauth2.core.user.OAuth2User;
    import org.springframework.security.web.SecurityFilterChain;
    import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
    @Configuration
    @EnableWebSecurity
    @RequiredArgsConstructor
    public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;
        private final OAuth2SuccessHandler oAuth2SuccessHandler;
        private final OAuth2FailureHandler oAuth2FailureHandler;
        //  필드 주입 X
        //  직접 메서드 호출해서 Bean

        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http, OAuth2UserService<OAuth2UserRequest, OAuth2User> customOAuth2UserService) throws Exception {
            http
                    .csrf(csrf -> csrf.disable())
                    .sessionManagement(session -> session
                            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    )
                    .requiresChannel(channel ->
                            channel.anyRequest().requiresSecure()
                    )
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers(
                                    "/api/auth/**",
                                    "/link/**",
                                    "/redirect.html",
                                    "/linkAccount.html",
                                    "/member/loginPage",
                                    "/member/signup",
                                    "/api/members/check-email",
                                    "/api/members/check-nickname",
                                    "/css/**", "/js/**", "/images/**",
                                    "/", "/error/**", "/oauth2/**", "/login/oauth2/**"
                            ).permitAll()
                            .requestMatchers("/home").authenticated()
                            .anyRequest().authenticated()
                    )
                    .oauth2Login(oauth2 -> oauth2
                            .loginPage("/member/loginPage")
                            .userInfoEndpoint(userInfo -> userInfo
                                    .userService(customOAuth2UserService)
                            )
                            .successHandler(oAuth2SuccessHandler)
                            .failureHandler(oAuth2FailureHandler)
                    );

            http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
            return http.build();
        }
        //  이 Bean 등록은 유지하되, 위에서 메서드 인자로 받아야 함
        @Bean
        public OAuth2UserService<OAuth2UserRequest, OAuth2User> customOAuth2UserService(MemberService memberService) {
            return new CustomOAuth2UserService(memberService);
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
            return config.getAuthenticationManager();
        }
    }


