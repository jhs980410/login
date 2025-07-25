package com.assignment.login.auth.service;

import com.assignment.login.auth.domain.LoginHistory;
import com.assignment.login.auth.repository.LoginHistoryRepository;
import com.assignment.login.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class LoginHistoryService {


    private final LoginHistoryRepository loginHistoryRepository;
    public void saveLoginHistory(Member member,
                                 String ip,
                                 String userAgent,
                                 String location,
                                 String deviceType,
                                 boolean success,
                                 boolean suspicious,
                                 String deviceId) {

        // suspicious가 false일 때만 중복 기록 생략 조건 검사
        if (!suspicious) {
            LoginHistory lastLogin = loginHistoryRepository.findTopByMemberIdOrderByLoginAtDesc(member.getId());

            if (lastLogin != null) {
                boolean sameDevice =
                        ip.equals(lastLogin.getIpAddress())
                                && userAgent.equals(lastLogin.getUserAgent())
                                && ((deviceId == null && lastLogin.getDeviceId() == null)
                                || (deviceId != null && deviceId.equals(lastLogin.getDeviceId())));
                boolean recentEnough =
                        Duration.between(lastLogin.getLoginAt(), LocalDateTime.now(ZoneId.of("Asia/Seoul"))).toMinutes() < 60;

                if (sameDevice && recentEnough) {
                    return; // 같은 기기이고 최근이면 기록 생략
                }
            }
        }

        // 기록 저장
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
                        .loginAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                        .deviceId(deviceId)
                        .build()
        );
    }

    public boolean existsByMemberIdAndDeviceIdAndSuccessTrue(Long userId, String deviceId) {
        return loginHistoryRepository.existsByMemberIdAndDeviceIdAndSuccessTrue(userId, deviceId);
    }

}
