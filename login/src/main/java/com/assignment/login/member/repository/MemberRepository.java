package com.assignment.login.member.repository;

import com.assignment.login.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long>{
    //중복 닉네임
    boolean existsByNickname(String nickname);
    //중복 이메일
    boolean existsByEmail(String email);
    //이메일 찾기
    Optional<Member> findByEmail(String email);

    Optional<Member> findByPassword(String password);

    Optional<Member> findByProviderId(String providerId);
}
