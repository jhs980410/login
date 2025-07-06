package com.assignment.login.loginfail.repository;

import com.assignment.login.loginfail.domain.LoginFail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoginFailRepository extends JpaRepository<LoginFail, Long> {
    Optional<LoginFail> findByUserId(Long userId);
}
