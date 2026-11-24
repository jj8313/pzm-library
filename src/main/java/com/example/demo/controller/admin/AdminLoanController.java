package com.example.demo.controller.admin;

import com.example.demo.config.I18n;
import com.example.demo.entity.Member;
import com.example.demo.service.LoanService;
import com.example.demo.service.LostBookService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// 관리자: 대출 관리 (승인 + 대출 중 + 오늘 반납 + 분실 처리)
@Controller
@RequestMapping("/admin/loans")
public class AdminLoanController {

    private final LoanService loanService;
    private final LostBookService lostBookService;
    private final I18n i18n;   // 결과 메시지를 화면 언어로

    public AdminLoanController(LoanService loanService, LostBookService lostBookService, I18n i18n) {
        this.loanService = loanService;
        this.lostBookService = lostBookService;
        this.i18n = i18n;
    }

    // 대출 관리 화면:  GET /admin/loans
    @GetMapping
    public String list(HttpSession session, Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/loginpage";
        if (!loginMember.isAdmin()) return "redirect:/";

        model.addAttribute("loans", loanService.findRequested());            // 신청 목록
        model.addAttribute("loanedList", loanService.findLoaned());          // 대출 중 목록
        model.addAttribute("returnedList", loanService.findReturnedToday()); // 오늘 반납된 책
        model.addAttribute("lostCount", lostBookService.countAll());         // 분실 몇 권 (버튼용)
        return "admin/loanList";
    }

    // 승인:  POST /admin/loans/1/approve
    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id,
                          HttpSession session, RedirectAttributes ra) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/loginpage";
        if (!loginMember.isAdmin()) return "redirect:/";

        boolean ok = loanService.approve(id);

        if (ok) {
            ra.addFlashAttribute("message", i18n.get("admin.msg.approved"));
        } else {
            ra.addFlashAttribute("message", i18n.get("admin.msg.approveFail"));
        }
        return "redirect:/admin/loans";
    }

    // 분실 처리 (대출 관리 화면에서): POST /admin/loans/copies/21/lost  (사유는 입력 창에서 받음)
    @PostMapping("/copies/{copyId}/lost")
    public String markLost(@PathVariable Long copyId,
                           @RequestParam String reason,
                           HttpSession session, RedirectAttributes ra) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/loginpage";
        if (!loginMember.isAdmin()) return "redirect:/";

        String result = lostBookService.markLost(copyId, reason);

        if (result.equals("OK")) {
            ra.addFlashAttribute("message", i18n.get("admin.msg.lostDoneNotify"));
        } else {
            ra.addFlashAttribute("message", i18n.get("admin.msg.lostFail"));
        }
        return "redirect:/admin/loans";
    }
}