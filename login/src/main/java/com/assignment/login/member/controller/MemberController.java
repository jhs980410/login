package com.assignment.login.member.controller;

import com.assignment.login.member.dto.MemberSignupRequest;
import com.assignment.login.member.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;

    @Autowired
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    // 로그인 및 회원가입 페이지 렌더링
    @GetMapping("/loginPage")
    public String showLoginPage(@RequestParam(required = false) String error, // 로그인 실패시 처리
                                @RequestParam(required = false) String logout, // 로그아웃 성공 알림
                                Model model) {
        System.out.println("[DEBUG] /member/loginPage 요청 처리");
        System.out.println("에러: " + error);
        System.out.println("로그아웃: " + logout);

        model.addAttribute("title", "로그인");
        model.addAttribute("error", error != null);
        model.addAttribute("logout", logout != null);


        model.addAttribute("title", "로그인 및 회원가입"); // 공통 UI 제목
        model.addAttribute("error", error != null); // 로그인 에러 여부
        model.addAttribute("logout", logout != null); // 로그아웃 성공 여부
        return "loginView"; // loginView.html 렌더링
    }

    // 회원가입 요청 처리
    @PostMapping("/signup")
    public String signup(@ModelAttribute MemberSignupRequest memberSignupRequest) {
        memberService.signup(memberSignupRequest);
        // 회원가입 완료 후 로그인 페이지로 리다이렉트
        return "redirect:/member/loginPage?signupSuccess=true";
    }

    // TODO: 로그인 요청은 Spring Security에서 기본 지원 (별도 처리 필요 없음)
}