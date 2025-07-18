package com.assignment.login.auth.repository;

import com.assignment.login.auth.domain.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {
    List<LoginHistory> findByMemberIdOrderByLoginAtDesc(Long memberId);

    LoginHistory findTopByMemberIdOrderByLoginAtDesc(Long id);
}
