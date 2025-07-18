package com.assignment.login.auth.service;

import com.assignment.login.auth.domain.LoginHistory;
import com.assignment.login.auth.repository.LoginHistoryRepository;
import com.assignment.login.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LoginHistoryService {


    private final LoginHistoryRepository loginHistoryRepository;
    public void saveLoginHistory(Member member, String ip, String userAgent, String location, String deviceType, boolean success, boolean suspicious,String deviceId) {
        // suspicious이면 무조건 저장
        if (!suspicious) {
            LoginHistory lastLogin = loginHistoryRepository.findTopByMemberIdOrderByLoginAtDesc(member.getId());

            if (lastLogin != null) {
                boolean sameDevice = ip.equals(lastLogin.getIpAddress()) && userAgent.equals(lastLogin.getUserAgent());
                boolean recentEnough = Duration.between(lastLogin.getLoginAt(), LocalDateTime.now()).toMinutes() < 60;

                if (sameDevice && recentEnough) {
                    return; // 같은 기기, 최근 접속이면 기록 생략
                }
            }
        }

        // 정상 또는 의심 로그인 저장
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
                        .deviceId(deviceId)
                        .build()
        );
    }

//    public void saveLoginHistory(Member member, String ip, String userAgent, boolean success, boolean suspicious) {
//        saveLoginHistory(member, ip, userAgent, null, null, success, suspicious);
//    }



}
