package com.assignment.login.auth.repository;

import com.assignment.login.auth.domain.LoginFail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoginFailRepository extends JpaRepository<LoginFail, Long> {
    Optional<LoginFail> findByUserId(Long userId);
}
