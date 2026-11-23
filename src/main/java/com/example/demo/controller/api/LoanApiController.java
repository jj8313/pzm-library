package com.example.demo.controller.api;

import com.example.demo.entity.Loan;
import com.example.demo.entity.Member;
import com.example.demo.service.LoanService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// 대출 데이터 API (로그인 필요)
@RestController
@RequestMapping("/api/loans")
public class LoanApiController {

    private final LoanService loanService;

    public LoanApiController(LoanService loanService) {
        this.loanService = loanService;
    }

    // 내 대출 목록: GET /api/loans/me
    @GetMapping("/me")
    public ResponseEntity<List<Loan>> myLoans(HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();   // 로그인 안 함 → 401
        }
        return ResponseEntity.ok(loanService.findMyLoans(loginMember.getId()));   // 200 + 내 대출
    }
}