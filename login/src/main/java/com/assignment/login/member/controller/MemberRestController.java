package com.assignment.login.member.controller;

import com.assignment.login.member.service.MemberService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/members")
public class MemberRestController {
    private final MemberService memberService;

    public MemberRestController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/check-email")
    public Map<String, Object> checkEmail(@RequestParam String email) {
        boolean exists = memberService.existsByEmail(email);
        Map<String, Object> res = new HashMap<>();
        res.put("exists", exists);
        res.put("status", exists ? "fail" : "ok");
        res.put("message", exists ? "이미 사용 중인 이메일입니다." : "사용 가능한 이메일입니다.");

        return res;
    }

    @GetMapping("/check-nickname")
    public Map<String, Object> checkNickname(@RequestParam String nickname) {
        boolean exists = memberService.existsByNickname(nickname);
        Map<String, Object> res = new HashMap<>();
        res.put("exists", exists);
        res.put("status", exists ? "fail" : "ok");
        res.put("message", exists ? "이미 사용 중인 닉네임입니다." : "사용 가능한 닉네임입니다.");
        return res;
    }
}
