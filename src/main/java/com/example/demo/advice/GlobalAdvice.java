package com.example.demo.advice;

import com.example.demo.entity.Member;
import com.example.demo.service.LoanService;
import com.example.demo.service.MessageService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.time.LocalDate;

// 모든 Controller에 공통으로 적용
@ControllerAdvice
public class GlobalAdvice {
    private final LoanService loanService;
    private final MessageService messageService;                 // ★ 2. 필드

    public GlobalAdvice(LoanService loanService, MessageService messageService) {   // ★ 3. 생성자
        this.loanService = loanService;
        this.messageService = messageService;
    }

    // 어떤 화면을 열든 model에 "suspendedUntil"을 자동으로 넣어 줌
    @ModelAttribute("suspendedUntil")
    public LocalDate suspendedUntil(HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return null;            // 로그인 안 했으면 없음

        return loanService.findSuspendedUntil(loginMember.getId());   // 정지 중이면 날짜, 아니면 null
    }
    // ★ 추가: 어떤 화면을 열든 model에 "loginMember"를 자동으로 넣어 줌 (헤더용)
    @ModelAttribute("loginMember")
    public Member loginMember(HttpSession session) {
        return (Member) session.getAttribute("loginMember");   // 로그인 안 했으면 null
    }
    // ★ 추가: 안 읽은 톡톡 개수 (헤더 🔴)
    @ModelAttribute("unreadCount")
    public int unreadCount(HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return 0;                       // 로그인 안 했으면 0
        return messageService.countUnread(loginMember.getId());
    }
}
