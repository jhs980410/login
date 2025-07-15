package com.assignment.login.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Getter
@AllArgsConstructor

public class RefreshTokenPayload {
    private Long userId;
    private String email;
    private boolean autoLogin;

}
