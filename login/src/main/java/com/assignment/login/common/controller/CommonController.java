package com.assignment.login.common.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CommonController {

    @GetMapping("/")
    public String rootRedirect(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            // 로그인한 사용자라면 홈으로
            return "redirect:/home";
        } else {
            // 로그인 안 한 사용자라면 로그인 페이지로
            return "redirect:/member/loginPage";
        }
    }
    @GetMapping("/home")
    public String home() {
        return "home";  // /templates/home.html 을 보여줌
    }
}
