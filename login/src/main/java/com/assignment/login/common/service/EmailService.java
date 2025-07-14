package com.assignment.login.common.service;


import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class EmailService {
    private final JavaMailSender mailSender;

    public void sendAuthCode(String toEmail, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("[로그인] 이메일 인증 코드 안내");
        message.setText("요청하신 인증 코드는 다음과 같습니다:\n\n" + code + "\n\n3분 내로 입력해주세요.");

        mailSender.send(message);
    }

}
