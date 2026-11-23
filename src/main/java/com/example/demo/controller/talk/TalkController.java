package com.example.demo.controller.talk;

import com.example.demo.entity.Member;
import com.example.demo.service.MessageService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

// 회원용 톡톡 (관리자 톡톡은 AdminTalkController)
@Controller
public class TalkController {

    private final MessageService messageService;

    public TalkController(MessageService messageService) {
        this.messageService = messageService;
    }

    // 톡톡 화면: GET /talk
    @GetMapping("/talk")
    public String talk(HttpSession session, Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/loginpage";

        messageService.markRead(loginMember.getId());   // 열었으니까 전부 읽음으로

        // 내 대화방 메시지 전부 (번호는 세션에서!)
        model.addAttribute("messages", messageService.findByMemberId(loginMember.getId()));
        return "talk/talk";
    }

    // 보내기: POST /talk
    @PostMapping("/talk")
    public String send(@RequestParam String content, HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/loginpage";

        // 회원이 보냄 → fromAdmin = false
        messageService.send(loginMember.getId(), false, content);
        return "redirect:/talk";   // PRG: 새로고침해도 두 번 안 보내짐
    }

    // 안 읽은 메시지 개수: GET /talk/unread-count
    // 헤더의 🔴 배지가 10초마다 부름 (화면 대신 숫자만 돌려줌)
    @GetMapping("/talk/unread-count")
    @ResponseBody
    public int unreadCount(HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return 0;
        return messageService.countUnread(loginMember.getId());
    }
}