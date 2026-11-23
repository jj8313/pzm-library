package com.example.demo.controller.admin;

import com.example.demo.entity.Member;
import com.example.demo.service.LoanService;
import com.example.demo.service.MemberService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

// 관리자: 회원 관리
@Controller
@RequestMapping("/admin/members")
public class AdminMemberController {

    private final MemberService memberService;
    private final LoanService loanService;

    public AdminMemberController(MemberService memberService, LoanService loanService) {
        this.memberService = memberService;
        this.loanService = loanService;
    }

    // 관리자인지 확인
    private boolean isAdmin(HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        return loginMember != null && loginMember.isAdmin();
    }
    // 회원 목록: GET /admin/members?keyword=테스터&page=1
    @GetMapping
    public String list(@RequestParam(defaultValue = "") String keyword,
                       @RequestParam(defaultValue = "1") int page,
                       HttpSession session, Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/loginpage";              // 인증
        if (!loginMember.isAdmin()) return "redirect:/";  // 인가

        int total = memberService.countAll(keyword);

        model.addAttribute("keyword", keyword);                            // 검색창에 다시 보여 주기
        model.addAttribute("members", memberService.findPage(keyword, page));
        model.addAttribute("total", total);
        model.addAttribute("page", page);
        model.addAttribute("totalPages", (total + 9) / 10);
        return "admin/memberList";
    }

    // 회원 상세 (빌리고 있는 책): GET /admin/members/5
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/";

        model.addAttribute("member", memberService.findById(id));
        model.addAttribute("loans", loanService.findMyLoans(id));
        return "admin/memberDetail";
    }
}