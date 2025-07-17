package com.assignment.login.auth.service;

import com.assignment.login.auth.domain.LoginHistory;
import com.assignment.login.auth.repository.LoginHistoryRepository;
import com.assignment.login.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LoginHistoryService {


    private final LoginHistoryRepository loginHistoryRepository;

    public void saveLoginHistory(Member member, String ip, String userAgent, String location, String deviceType, boolean success, boolean suspicious) {
        loginHistoryRepository.save(
                LoginHistory.builder()
                        .member(member)
                        .ipAddress(ip)
                        .userAgent(userAgent)
                        .location(location)
                        .deviceType(deviceType)
                        .success(success)
                        .suspicious(suspicious)
                        .loginType(member.getLoginType())
                        .loginAt(LocalDateTime.now())
                        .build()
        );
    }

    public void saveLoginHistory(Member member, String ip, String userAgent, boolean success, boolean suspicious) {
        saveLoginHistory(member, ip, userAgent, null, null, success, suspicious);
    }



}
