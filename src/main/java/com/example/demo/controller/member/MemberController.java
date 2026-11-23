package com.example.demo.controller.member;

import com.example.demo.entity.Member;
import com.example.demo.service.MailService;
import com.example.demo.service.MemberService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;
    private final MailService mailService;

    public MemberController(MemberService memberService, MailService mailService) {
        this.memberService = memberService;
        this.mailService = mailService;
    }

    // 회원가입 화면
    @GetMapping("/join")
    public String joinForm() {
        return "members/join";
    }

    // 인증코드 메일 보내기
    // JS: fetch("/members/email/send")
    @PostMapping("/email/send")
    @ResponseBody
    public String sendCode(@RequestParam String email, HttpSession session) {
        if (memberService.isEmailExists(email)) {
            return "이미 가입된 이메일입니다.";
        }
        try {
            String code = mailService.sendCode(email);
            session.setAttribute("emailCode", code);
            session.setAttribute("emailTarget", email);
            return "OK";
        } catch (Exception e) {
            e.printStackTrace();
            return "메일 발송에 실패했습니다. 이메일 주소를 확인해 주세요.";
        }
    }

    // 인증코드 확인
    // JS: fetch("/members/email/verify") - 확인 버튼
    @PostMapping("/email/verify")
    @ResponseBody
    public String verifyCode(@RequestParam String email,
                             @RequestParam String code,
                             HttpSession session) {
        String savedCode = (String) session.getAttribute("emailCode");
        String savedEmail = (String) session.getAttribute("emailTarget");

        if (savedCode != null && savedCode.equals(code) && email.equals(savedEmail)) {
            session.setAttribute("verifiedEmail", email);
            session.removeAttribute("emailCode");
            return "OK";
        }
        return "인증코드가 일치하지 않습니다.";
    }

    // 닉네임 중복 확인 (fetch가 부름 → 화면 대신 true/false를 돌려줌)
    @GetMapping("/check-nickname")
    @ResponseBody
    public boolean checkNickname(@RequestParam String nickname) {
        return memberService.isNicknameExists(nickname);   // true = 이미 있음
    }

    // 회원가입 처리
    // form: action="/members/join" - 가입하기 버튼
    @PostMapping("/join")
    public String join(Member member, HttpSession session, Model model) {
        String verifiedEmail = (String) session.getAttribute("verifiedEmail");

        if (verifiedEmail == null || !verifiedEmail.equals(member.getEmail())) {
            model.addAttribute("error", "이메일 인증을 완료해 주세요.");
            return "members/join";
        }

        if (!member.getPasswordHash().matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*])[A-Za-z\\d!@#$%^&*]{8,16}$")) {
            model.addAttribute("error", "비밀번호는 영문, 숫자, 특수문자를 포함해 8~16자로 입력해 주세요.");
            return "members/join";
        }

        try {
            memberService.join(member);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "members/join";
        }

        session.removeAttribute("verifiedEmail");
        return "redirect:/loginpage";
    }
}