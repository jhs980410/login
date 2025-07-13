package com.assignment.login.member.controller;

import com.assignment.login.auth.service.AuthService;
import com.assignment.login.member.domain.enums.LoginType;
import com.assignment.login.member.service.SocialAccountLinkService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/link")
public class LinkAccountController {

    private final AuthService authService;
    private final SocialAccountLinkService socialAccountLinkService;

    // 1. 계정 연동 페이지 렌더링
    @GetMapping("/account")
    public String showLinkPage(@RequestParam String email, Model model) {
        model.addAttribute("email", email);
        return "member/linkAccount";
    }

    // 2. 인증코드 전송
    @PostMapping("/sendCode")
    public String sendLinkAuthCode(@RequestParam String email, RedirectAttributes redirectAttributes) {
        authService.generateAndSendCode(email);
        redirectAttributes.addFlashAttribute("message", "인증 코드가 이메일로 전송되었습니다.");
        return "redirect:/link/account?email=" + email;
    }

    // 3. 인증코드 검증
    @PostMapping("/verifyCode")
    public String verifyLinkCode(@RequestParam String email,
                                 @RequestParam String authCode,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        if (authService.verifyCode(email, authCode)) {
            session.setAttribute("linkAccountEmail", email);
            return "redirect:/link/complete";
        } else {
            redirectAttributes.addFlashAttribute("error", "인증 코드가 올바르지 않습니다.");
            return "redirect:/link/account?email=" + email;
        }
    }

    // 4. 계정 연동 완료
    @GetMapping("/complete")
    public String completeLink(@RequestParam("type") String loginTypeParam,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("linkAccountEmail");
        if (email != null) {
            try {
                //  문자열로 전달된 "kakao", "google" 등을 enum으로 변환
                LoginType loginType = LoginType.valueOf(loginTypeParam.toUpperCase());

                socialAccountLinkService.link(email, loginType);
                session.removeAttribute("linkAccountEmail");
                redirectAttributes.addFlashAttribute("message", "계정 연동이 완료되었습니다. 다시 로그인해주세요.");
            } catch (IllegalArgumentException e) {
                redirectAttributes.addFlashAttribute("error", "유효하지 않은 로그인 타입입니다.");
            }
        }
        return "redirect:/member/loginPage";
    }
}
