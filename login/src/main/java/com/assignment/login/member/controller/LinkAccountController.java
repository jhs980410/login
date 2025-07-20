package com.assignment.login.member.controller;

import com.assignment.login.auth.service.AuthService;
import com.assignment.login.auth.service.LoginHistoryService;
import com.assignment.login.auth.service.TrustedDeviceService;
import com.assignment.login.member.domain.Member;
import com.assignment.login.member.domain.enums.LoginType;
import com.assignment.login.member.service.MemberService;
import com.assignment.login.member.service.SocialAccountLinkService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

@RequestMapping("/link")
@Controller
public class LinkAccountController {

    private final AuthService authService;
    private final SocialAccountLinkService socialAccountLinkService;
    private final LoginHistoryService loginHistoryService;
    private final MemberService memberService;
    private final RedisTemplate redisTemplate;
    private final TrustedDeviceService trustedDeviceService;

    public LinkAccountController(AuthService authService, SocialAccountLinkService socialAccountLinkService, LoginHistoryService loginHistoryService, MemberService memberService, RedisTemplate redisTemplate,TrustedDeviceService trustedDeviceService) {
        this.authService = authService;
        this.socialAccountLinkService = socialAccountLinkService;
        this.loginHistoryService = loginHistoryService;
        this.memberService = memberService;
        this.redisTemplate = redisTemplate;
        this.trustedDeviceService = trustedDeviceService;
    }

    // 1. 계정 연동 페이지 렌더링
    @GetMapping("/account")
    public String showLinkPage(@RequestParam String email,
                               @RequestParam String type,
                               Model model,
                               @RequestParam(defaultValue = "link") String mode,
                               HttpSession session) {
        model.addAttribute("email", email);
        model.addAttribute("mode", mode);

        session.setAttribute("mode", mode);
        session.setAttribute("loginType", type.toUpperCase());
        return "member/linkAccount";
    }

    // 2. 인증코드 전송
    @PostMapping("/sendCode")
    public String sendLinkAuthCode(@RequestParam String email,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        authService.generateAndSendCode(email);

        String mode = (String) session.getAttribute("mode");

        redirectAttributes.addFlashAttribute("message", "인증 코드가 이메일로 전송되었습니다.");
        return "redirect:/link/account?email=" + email + "&type=" + session.getAttribute("loginType") + "&mode=" + mode;

    }

    // 3. 인증코드 검증 후 동의 페이지로 이동
    @PostMapping("/verifyCode")
    @ResponseBody
    public ResponseEntity<?> verifyLinkCode(@RequestBody Map<String, String> request,
                                            HttpSession session,
                                            HttpServletRequest httpRequest) {
        String email = request.get("email");
        String authCode = request.get("authCode");
        String deviceId = request.get("deviceId");
        String mode = (String) session.getAttribute("mode");

        if (!authService.verifyCode(email, authCode)) {
            return ResponseEntity.ok(Map.of("status", "fail", "message", "인증 코드가 올바르지 않습니다."));
        }

        session.setAttribute("linkAccountEmail", email);

        if (!"login".equals(mode)) {
            // 계정 연결 모드인 경우
            return ResponseEntity.ok(Map.of("status", "success", "mode", "link"));
        }

        Optional<Member> optionalMember = memberService.findByEmail(email);
        if (optionalMember.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("status", "fail", "message", "사용자를 찾을 수 없습니다."));
        }

        Member member = optionalMember.get();

        // 1. 로그인 히스토리 저장
        loginHistoryService.saveLoginHistory(
                member,
                httpRequest.getRemoteAddr(),
                httpRequest.getHeader("User-Agent"),
                null, null,
                true, true,
                deviceId
        );

        // 2. 인증 완료된 기기 등록
        trustedDeviceService.registerTrustedDevice(email, deviceId);
        System.out.println("인증 성공 → 등록 시도: email=" + email + ", deviceId=" + deviceId);

        // 3. Redis 인증 필요 상태 제거
        redisTemplate.delete("needs_verification:" + member.getId());

        // 4. Redis recentLogin 갱신
        String combined = httpRequest.getRemoteAddr() + "|" + httpRequest.getHeader("User-Agent");
        redisTemplate.opsForValue().set("recentLogin:" + email, combined, Duration.ofDays(30));

        // 5. 응답 반환
        return ResponseEntity.ok(Map.of("status", "success", "mode", "login"));
    }


    // 4. 동의 페이지 렌더링
    @GetMapping("/confirm")
    public String showConfirmPage(HttpSession session, Model model) {
        String email = (String) session.getAttribute("linkAccountEmail");
        String loginType = (String) session.getAttribute("loginType");
        model.addAttribute("email", email);
        model.addAttribute("loginType", loginType);
        return "member/linkConfirm";
    }

    // 5. 계정 연동 완료
    @GetMapping("/complete")
    public String completeLink(@RequestParam("type") String loginTypeParam,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("linkAccountEmail");
        String providerId = (String) session.getAttribute("providerId");

        if (email != null) {
            try {
                LoginType loginType = LoginType.valueOf(loginTypeParam.toUpperCase());
                socialAccountLinkService.link(email, loginType, providerId);

                // 세션 정리
                session.removeAttribute("linkAccountEmail");
                session.removeAttribute("loginType");
                session.removeAttribute("providerId");

                redirectAttributes.addFlashAttribute("message", "계정 연동이 완료되었습니다. 다시 로그인해주세요.");
            } catch (IllegalArgumentException e) {
                redirectAttributes.addFlashAttribute("error", "유효하지 않은 로그인 타입입니다.");
            }
        }
        return "redirect:/member/loginPage";
    }
}



