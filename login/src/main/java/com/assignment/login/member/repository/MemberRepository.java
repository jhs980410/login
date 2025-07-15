package com.assignment.login.member.repository;

import com.assignment.login.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

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
    @Modifying
    @Transactional
    @Query("UPDATE Member m SET m.password = :password WHERE m.email = :email")
   void updatePassword(@Param("email") String email, @Param("password") String password);
}
