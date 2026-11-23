package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {
    // yml에 적은 Gmail 정보로 스프링이 만들어 둔 "메일 보내는 기계"
    private final JavaMailSender mailSender;

    // yml의 spring.mail.username (보내는 사람 주소)
    @Value("${spring.mail.username}")
    private String from;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public String sendCode(String to) {
        // 100000 ~ 999999 사이 6자리 숫자
        String code = String.valueOf((int) (Math.random() * 900000) + 100000);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);                          // 보내는 사람
        message.setTo(to);                              // 받는 사람
        message.setSubject("[PZM 도서관] 회원가입 인증코드");   // 제목
        message.setText("인증코드: " + code + "\n\n회원가입 화면에 입력해 주세요.");  // 내용

        mailSender.send(message);   // 실제 발송
        return code;                // 컨트롤러가 세션에 저장하려고 돌려받음
    }
}
