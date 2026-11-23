package com.example.demo.controller.admin;

import com.example.demo.entity.Loan;
import com.example.demo.entity.Member;
import com.example.demo.service.LoanService;
import com.example.demo.service.LostBookService;
import com.example.demo.service.MemberService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller

public class AdminDashboardController {
    private final LoanService loanService;
    private final LostBookService lostBookService;
    private final MemberService memberService;

    public AdminDashboardController(LoanService loanService,
                                    LostBookService lostBookService,
                                    MemberService memberService) {
        this.loanService = loanService;
        this.lostBookService = lostBookService;
        this.memberService = memberService;
    }

    @GetMapping("/admin/dashboard")
    public String dashboard(HttpSession session, Model model) {
        // ① 관리자만
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null || !loginMember.isAdmin()) {
            return "redirect:/";
        }

        // ② 대출 중 목록에서 연체만 세기
        List<Loan> loanedList = loanService.findLoaned();
        int overdueCount = 0;
        for (Loan loan : loanedList) {
            if (loan.isOverdue()) {
                overdueCount++;
            }
        }

        // ③ 숫자 6개 화면에 넘기기
        model.addAttribute("requestedCount", loanService.findRequested().size());
        model.addAttribute("loanedCount", loanedList.size());
        model.addAttribute("overdueCount", overdueCount);
        model.addAttribute("returnedTodayCount", loanService.findReturnedToday().size());
        model.addAttribute("lostCount", lostBookService.countAll());
        model.addAttribute("memberCount", memberService.countAll(""));

        return "admin/dashboard";
    }
}
