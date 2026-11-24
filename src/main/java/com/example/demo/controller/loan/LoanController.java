package com.example.demo.controller.loan;

import com.example.demo.config.I18n;
import com.example.demo.entity.Member;
import com.example.demo.service.LoanService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// 회원 대출 기능 (관리자 대출은 AdminLoanController)
@Controller
public class LoanController {

    private final LoanService loanService;
    private final I18n i18n;   // 결과 메시지를 화면 언어로

    public LoanController(LoanService loanService, I18n i18n) {
        this.loanService = loanService;
        this.i18n = i18n;
    }

    // 내 대출 목록:  GET /mypage/loans
    @GetMapping("/mypage/loans")
    public String myLoans(HttpSession session, Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/loginpage";

        model.addAttribute("loans", loanService.findMyLoans(loginMember.getId()));
        return "loan/myloans";
    }

    // 대출 이력:  GET /mypage/loans/history
    @GetMapping("/mypage/loans/history")
    public String history(HttpSession session, Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/loginpage";

        model.addAttribute("loans", loanService.findMyHistory(loginMember.getId()));
        return "loan/loanHistory";
    }

    // 반납하기:  POST /mypage/loans/1/return  (누르면 바로 반납 완료)
    @PostMapping("/mypage/loans/{id}/return")
    public String returnBook(@PathVariable Long id,
                             HttpSession session, RedirectAttributes ra) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/loginpage";

        boolean ok = loanService.memberReturn(id, loginMember.getId());

        if (ok) {
            ra.addFlashAttribute("message", i18n.get("loan.msg.returned"));
        } else {
            ra.addFlashAttribute("message", i18n.get("loan.msg.returnFail"));
        }
        return "redirect:/mypage/loans";
    }

    // 대출 연장:  POST /mypage/loans/1/extend
    @PostMapping("/mypage/loans/{id}/extend")
    public String extend(@PathVariable Long id,
                         HttpSession session, RedirectAttributes ra) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/loginpage";

        String result = loanService.extend(id, loginMember.getId());

        if (result.equals("OK")) {
            ra.addFlashAttribute("message", i18n.get("loan.msg.extended"));
        } else if (result.equals("OVERDUE")) {
            ra.addFlashAttribute("message", i18n.get("loan.msg.extendOverdue"));
        } else if (result.equals("ALREADY")) {
            ra.addFlashAttribute("message", i18n.get("loan.msg.extendAlready"));
        } else {
            ra.addFlashAttribute("message", i18n.get("loan.msg.extendFail"));
        }
        return "redirect:/mypage/loans";
    }

    // 대출 신청 취소:  POST /mypage/loans/1/cancel
    @PostMapping("/mypage/loans/{id}/cancel")
    public String cancel(@PathVariable Long id,
                         HttpSession session, RedirectAttributes ra) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/loginpage";

        boolean ok = loanService.cancelRequest(id, loginMember.getId());

        if (ok) {
            ra.addFlashAttribute("message", i18n.get("loan.msg.canceled"));
        } else {
            ra.addFlashAttribute("message", i18n.get("loan.msg.cancelFail"));
        }
        return "redirect:/mypage/loans";
    }
}