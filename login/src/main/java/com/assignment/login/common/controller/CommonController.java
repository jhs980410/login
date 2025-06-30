package com.assignment.login.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CommonController {

    @GetMapping("/")
    public String index() {
        return "index";
    }
    @GetMapping("/home")
    public String home() {
        return "home";  // /templates/home.html 을 보여줌
    }
}
