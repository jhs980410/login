package com.assignment.login.member.controller;

import com.assignment.login.member.dto.MemberSignupRequest;
import com.assignment.login.member.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class MemberController {

    private final MemberService memberService;

    @Autowired
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/member/loginPage")
    public String showLoginPage(@RequestParam(required = false) String error,
                                @RequestParam(required = false) String logout,
                                Model model) {
        // 로그 출력으로 확인
        System.out.println("에러 상태: " + error);
        System.out.println("로그아웃 상태: " + logout);

        // 모델 속성 설정
        model.addAttribute("title", "로그인 및 회원가입 페이지");
        model.addAttribute("errorMessage", error); // 에러 메시지 전달
        model.addAttribute("logoutMessage", logout != null ? "로그아웃되었습니다" : null); // 로그아웃 여부

        return "member/loginView"; // 'loginView.html' 템플릿 로드
    }

    // 회원가입 요청 처리
    @PostMapping("/member/signup")
    public String signup(@ModelAttribute MemberSignupRequest memberSignupRequest) {
        memberService.signup(memberSignupRequest);
        // 🔧 절대 경로로 리디렉션
        return "redirect:/member/loginPage?signupSuccess=true";

    }

    // TODO: 로그인 요청은 Spring Security에서 기본 지원 (별도 처리 필요 없음)
}