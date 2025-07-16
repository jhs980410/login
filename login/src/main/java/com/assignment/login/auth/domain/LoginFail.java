package com.assignment.login.auth.domain;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class LoginFail implements Serializable {

    private Long userId;
    private int failCount;
    private LocalDateTime lastFailAt;

    public LoginFail() {
        // 기본 생성자 (역직렬화를 위해 필요)
    }

    public LoginFail(Long userId) {
        this.userId = userId;
        this.failCount = 0;
        this.lastFailAt = LocalDateTime.now();
    }
}
