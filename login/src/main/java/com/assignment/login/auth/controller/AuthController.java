package com.assignment.login.auth.controller;

import com.assignment.login.auth.dto.LoginRequest;
import com.assignment.login.auth.service.AuthService;
import com.assignment.login.auth.domain.LoginFail;
import com.assignment.login.auth.repository.LoginFailRepository;
import com.assignment.login.auth.service.LoginFailService;
import com.assignment.login.member.domain.Member;
import com.assignment.login.member.domain.enums.LoginType;
import com.assignment.login.member.repository.MemberRepository;
import com.assignment.login.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final MemberRepository memberRepository;
    private final LoginFailRepository loginFailRepository;

    private final AuthService authService;
    private final LoginFailService loginFailService;
    private final MemberService memberService;


    @Transactional
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request,
                                   @RequestHeader("User-Agent") String userAgent,
                                   HttpServletRequest httpRequest) {
        try {
            Map<String, String> tokens = authService.login(
                    request, userAgent, httpRequest.getRemoteAddr()
            );
            return ResponseEntity.ok(tokens);

        } catch (LockedException e) {
            Member member = memberRepository.findByEmail(request.getEmail()).orElse(null);
            if (member != null) {
                LoginFail fail = loginFailRepository.findByUserId(member.getId()).orElse(null);
                if (fail != null && fail.getLastFailAt() != null) {
                    LocalDateTime unlockTime = fail.getLastFailAt().plusMinutes(5);
                    String formatted = unlockTime.format(DateTimeFormatter.ofPattern("HH시 mm분 이후"));
                    return ResponseEntity.status(HttpStatus.LOCKED)
                            .body(Map.of("message", "계정이 잠겨 있습니다. " + formatted + "에 다시 로그인해주세요."));
                }
            }
            return ResponseEntity.status(HttpStatus.LOCKED)
                    .body(Map.of("message", "계정이 잠겨 있습니다. 5분 후 다시 시도해 주세요."));

        } catch (BadCredentialsException e) {
            Member member = memberRepository.findByEmail(request.getEmail()).orElse(null);

            // 존재하는 계정일 때만 분기
            if (member != null) {
                if (member.getLoginType() != LoginType.LOCAL) {
                    // 소셜 로그인 계정인데 비밀번호 입력 시도 → 안내 메시지
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Map.of("message", "소셜 로그인 전용 계정입니다. 카카오/구글 로그인을 이용해 주세요."));
                }
                // 로컬 계정인 경우 실패 카운트 증가
                loginFailService.recordFail(member);
            }
            // 기본 응답 (비밀번호 틀림 또는 존재하지 않는 이메일)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "이메일 또는 비밀번호가 일치하지 않습니다."));
        }
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String, String> payload) {
        String refreshToken = payload.get("refreshToken");
        System.out.println("logout: " + refreshToken);
        authService.logout(refreshToken);
        return ResponseEntity.ok(Map.of("message", "로그아웃 되었습니다."));
    }


    @GetMapping("/redirect")
    public ResponseEntity<?> oauth2RedirectPage() {
        // 토큰은 SuccessHandler에서 localStorage에 이미 저장된 상태
        // 이 페이지는 JS로 /home 이동을 수행

        return ResponseEntity.ok().build(); // 또는 뷰 리턴
    }
    //비밀번호찾기//
    @PostMapping("/sendCode")
    @ResponseBody
    public ResponseEntity<?> sendPasswordResetCode(@RequestBody Map<String, String> request, HttpSession session) {
        String email = request.get("email");

        // 1. 이메일로 사용자 조회
        Optional<Member> member = memberRepository.findByEmail(email);
        if (member.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("등록되지 않은 이메일입니다.");
        }

        // 2. 소셜 로그인 계정은 비밀번호 재설정 불가
        if (member.get().getLoginType() != LoginType.LOCAL) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("소셜 로그인 계정은 비밀번호를 재설정할 수 없습니다.");
        }

        // 3. 이메일 인증코드 발송
        System.out.println("[DEBUG] /api/auth/sendCode 호출됨: email = " + email);
        session.setAttribute("resetEmail", email);
        authService.generateAndSendCode(email);

        return ResponseEntity.ok("인증 메일이 전송되었습니다.");
    }

    //인증번호확인//
    @PostMapping("/verifyCode")
    @ResponseBody
    public ResponseEntity<?> verifyCode(@RequestBody Map<String, String> request, HttpSession session) {
        String code = request.get("code");
        String email = (String) session.getAttribute("resetEmail");

        if (email == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("세션 만료 또는 유효하지 않은 요청입니다.");
        }

        boolean isValid = authService.verifyCode(email, code);
        if (!isValid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증번호가 올바르지 않습니다.");
        }

        return ResponseEntity.ok("인증 성공");
    }
    //비밀번호변경//
    @PostMapping("/resetPassword")
    @ResponseBody
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request, HttpSession session) {
        String email = (String) session.getAttribute("resetEmail");
        String newPassword = request.get("password");

        if (email == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("세션 만료 또는 유효하지 않은 요청입니다.");
        }

        memberService.updatePassword(email, newPassword);

        // ✅ 사용 완료 후 세션에서 email 제거 (보안)
        session.removeAttribute("resetEmail");

        return ResponseEntity.ok("비밀번호 변경 완료");
    }
}
