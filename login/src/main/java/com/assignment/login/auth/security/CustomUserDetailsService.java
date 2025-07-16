package com.assignment.login.auth.security;

import com.assignment.login.auth.service.LoginFailService;
import com.assignment.login.member.domain.Member;
import com.assignment.login.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final LoginFailService loginFailService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        // 계정이 잠금 상태일 경우 잠금 해제 시간 확인
        if (member.isLocked()) {
            LocalDateTime unlockTime = loginFailService.getUnlockTime(member);

            if (unlockTime == null || unlockTime.isBefore(LocalDateTime.now())) {
                // 잠금 기간이 지났으면 계정 잠금 해제 및 Redis 기록 삭제
                member.setLocked(false);
                memberRepository.save(member);
                loginFailService.resetFailCount(email);
            }
        }

        return new CustomUserDetails(member);
    }
}
