package com.assignment.login.common.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @GetMapping("/error")
    public String handleError() {
        // 기본 오류 페이지 템플릿
        return "error/defaultError"; // templates/error/defaultError.html
    }
}