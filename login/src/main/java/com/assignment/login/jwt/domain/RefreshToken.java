package com.assignment.login.jwt.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "remember_token")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 2048)
    private String token;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;
    @Column(nullable = false)
    private boolean autoLogin; // true면 자동 로그인용, false면 refresh token 용
    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

}