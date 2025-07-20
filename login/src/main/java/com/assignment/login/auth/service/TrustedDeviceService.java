package com.assignment.login.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrustedDeviceService {

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 신뢰된 기기로 등록
     */
    public void registerTrustedDevice(String email, String deviceId) {
        if (email != null && deviceId != null) {
            redisTemplate.opsForSet().add("trusted_devices:" + email, deviceId);
        }
    }

    /**
     * 이미 등록된 신뢰 기기인지 확인
     */
    public boolean isTrustedDevice(String email, String deviceId) {
        if (email == null || deviceId == null) return false;
        return Boolean.TRUE.equals(
                redisTemplate.opsForSet().isMember("trusted_devices:" + email, deviceId)
        );
    }
}
