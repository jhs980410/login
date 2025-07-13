package com.assignment.login;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@EntityScan(basePackages = {
        "com.assignment.login.member.domain",
        "com.assignment.login.loginfail.domain",
        "com.assignment.login.auth.domain",
        "com.assignment.login.jwt.domain"
})
public class LoginApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoginApplication.class, args);
        System.out.println("Hello World");
    }

    @PostConstruct
    public void checkEnv() {
        System.out.println("GOOGLE_CLIENT_ID = " + System.getenv("GOOGLE_CLIENT_ID"));
    }


    @PostConstruct
    public void checkEnv2() {
        System.out.println("KAKAO_CLIENT_ID = " + System.getenv("KAKAO_CLIENT_ID"));
    }

    @PostConstruct
    public void checkEnv3() {
        System.out.println("NAVER_CLIENT_ID = " + System.getenv("NAVER_CLIENT_ID"));
    }
}
