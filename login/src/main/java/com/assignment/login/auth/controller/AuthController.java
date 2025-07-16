package com.assignment.login.auth.controller;

import com.assignment.login.auth.dto.LoginRequest;
import com.assignment.login.auth.service.AuthService;
import com.assignment.login.auth.service.LoginFailService;
import com.assignment.login.member.domain.Member;
import com.assignment.login.member.domain.enums.LoginType;
import com.assignment.login.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
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

    private final AuthService authService;
    private final LoginFailService loginFailService;
    private final MemberService memberService;


    @Transactional
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request,
                                   @RequestHeader("User-Agent") String userAgent,
                                   HttpServletRequest httpRequest,
                                   HttpServletResponse response) {
        try {
            Map<String, String> tokens = authService.login(
                    request, userAgent, httpRequest.getRemoteAddr()
            );

            //  accessToken 쿠키 설정
            ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", tokens.get("accessToken"))
                    .httpOnly(true)
                    .secure(false) // 배포 시 true
                    .path("/")
                    .maxAge(15 * 60) // 15분
                    .sameSite("Lax")
                    .build();

            //  refreshToken 쿠키 설정
            ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", tokens.get("refreshToken"))
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(14 * 24 * 60 * 60) // 14일
                    .sameSite("Lax")
                    .build();

            //  쿠키 응답에 설정
            response.setHeader("Set-Cookie", accessTokenCookie.toString());
            response.addHeader("Set-Cookie", refreshTokenCookie.toString());

            // 로그인 성공 시 실패 기록 초기화
            loginFailService.resetFailCount(request.getEmail());

            return ResponseEntity.ok(Map.of("message", "로그인 성공"));

        } catch (LockedException e) {
            Member member = memberService.findByEmail(request.getEmail()).orElse(null);
            if (member != null) {
                LocalDateTime unlockTime = loginFailService.getUnlockTime(member);
                if (unlockTime != null) {
                    String formatted = unlockTime.format(DateTimeFormatter.ofPattern("HH시 mm분 이후"));
                    return ResponseEntity.status(HttpStatus.LOCKED)
                            .body(Map.of("message", "계정이 잠겨 있습니다. " + formatted + "에 다시 로그인해주세요."));
                }
            }
            return ResponseEntity.status(HttpStatus.LOCKED)
                    .body(Map.of("message", "계정이 잠겨 있습니다. 5분 후 다시 시도해 주세요."));

        } catch (BadCredentialsException e) {
            Member member = memberService.findByEmail(request.getEmail()).orElse(null);

            if (member != null) {
                if (member.getLoginType() != LoginType.LOCAL) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Map.of("message", "소셜 로그인 전용 계정입니다. 카카오/구글 로그인을 이용해 주세요."));
                }

                // Redis에 실패 기록
                loginFailService.recordFail(member);
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "이메일 또는 비밀번호가 일치하지 않습니다."));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@CookieValue(value = "refreshToken", required = false) String refreshToken,
                                    HttpServletResponse response) {
        if (refreshToken != null) {
            authService.logout(refreshToken); // DB 삭제
        }

        ResponseCookie accessTokenClear = ResponseCookie.from("accessToken", "")
                .httpOnly(true).secure(false).path("/").maxAge(0).sameSite("Lax").build();

        ResponseCookie refreshTokenClear = ResponseCookie.from("refreshToken", "")
                .httpOnly(true).secure(false).path("/").maxAge(0).sameSite("Lax").build();

        response.setHeader("Set-Cookie", accessTokenClear.toString());
        response.addHeader("Set-Cookie", refreshTokenClear.toString());

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
        Optional<Member> member = memberService.findByEmail(email);
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
