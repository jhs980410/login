package com.assignment.login.auth.service;

import com.assignment.login.member.domain.Member;
import com.assignment.login.member.domain.enums.LoginType;
import com.assignment.login.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional
public class LoginFailService {

    private final StringRedisTemplate redisTemplate;
    private final MemberRepository memberRepository;

    private static final String PREFIX = "login:fail:";
    private static final int LOCK_THRESHOLD = 5;  //5회 지정
    private static final int LOCK_DURATION = 5; // 5분간 정지

    // 로그인 실패 기록
    public void recordFail(Member member) {
        if (member.getLoginType() != LoginType.LOCAL) return;

        String key = PREFIX + member.getId();

        Long failCount = redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, Duration.ofMinutes(LOCK_DURATION));

        if (failCount != null && failCount >= LOCK_THRESHOLD) {
            member.setLocked(true); //멤버
            memberRepository.save(member);
        }
    }

    // 이메일 기반 실패 기록
    public void recordFailByEmail(String email) {
        memberRepository.findByEmail(email)
                .ifPresent(this::recordFail);
    }

    // 로그인 성공 시 실패 기록 초기화
    public void resetFailCount(String email) {
        Member member = memberRepository.findByEmail(email).orElse(null);
        if (member == null) return;

        redisTemplate.delete(PREFIX + member.getId());

        if (member.isLocked()) {
            member.setLocked(false);
            memberRepository.save(member);
        }
    }

    // TTL 기반 잠금 해제 예상 시간 반환
    public LocalDateTime getUnlockTime(Member member) {
        String key = PREFIX + member.getId();
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);

        if (ttl != null && ttl > 0) {
            return LocalDateTime.now(ZoneId.of("Asia/Seoul")).plusSeconds(ttl);
        }

        return null;
    }
}
