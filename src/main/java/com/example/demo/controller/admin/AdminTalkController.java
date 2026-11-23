package com.example.demo.controller.admin;

import com.example.demo.entity.Member;
import com.example.demo.service.MessageService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/talk")
public class AdminTalkController {

    private final MessageService messageService;

    public AdminTalkController(MessageService messageService) {
        this.messageService = messageService;
    }

    // 관리자인지 확인
    private boolean isAdmin(HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        return loginMember != null && loginMember.isAdmin();
    }

    // ① 대화 목록: GET /admin/talk
    @GetMapping
    public String list(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/";

        model.addAttribute("talks", messageService.findLastMessages());
        return "admin/talkList";
    }

    // ② 한 회원과의 대화: GET /admin/talk/5
    @GetMapping("/{memberId}")
    public String chat(@PathVariable Long memberId, HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/";

        model.addAttribute("messages", messageService.findByMemberId(memberId));
        model.addAttribute("memberId", memberId);
        return "admin/talkChat";
    }

    // ③ 답장: POST /admin/talk/5
    @PostMapping("/{memberId}")
    public String reply(@PathVariable Long memberId,
                        @RequestParam String content,
                        HttpSession session) {
        if (!isAdmin(session)) return "redirect:/";

        // 도서관이 보냄 → fromAdmin = true
        messageService.send(memberId, true, content);
        return "redirect:/admin/talk/" + memberId;
    }
}