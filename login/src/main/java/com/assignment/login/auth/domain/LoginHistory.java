package com.assignment.login.auth.domain;

import com.assignment.login.member.domain.Member;
import com.assignment.login.member.domain.enums.LoginType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Member member;

    private String ipAddress;

    private String userAgent;

    private String location; // 예: "Seoul, KR"

    private String deviceType; // 예: "Mobile", "PC"
    private String deviceId; // 기기 고유 ID (UUID, localStorage 기반)
    private Boolean success;
    private Boolean suspicious;
    @Enumerated(EnumType.STRING)
    private LoginType loginType; // LOCAL, KAKAO

    private LocalDateTime loginAt;
}

