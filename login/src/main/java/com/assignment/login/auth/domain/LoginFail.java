package com.assignment.login.auth.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "login_fail")
public class LoginFail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "fail_count")
    private int failCount;

    @Column(name = "last_fail_at")
    private LocalDateTime lastFailAt;

    public LoginFail(Long userId) {
        this.userId = userId;
        this.failCount = 0;
        this.lastFailAt = LocalDateTime.now();
    }
}

