package com.assignment.login.auth.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
    private String deviceId; //기기식별

    boolean autoLogin;
}