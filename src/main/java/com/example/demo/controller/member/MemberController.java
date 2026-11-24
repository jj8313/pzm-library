package com.example.demo.controller.member;

import com.example.demo.config.I18n;
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
    private final I18n i18n;   // 에러 키 → 화면 언어 문장

    // 비밀번호 규칙: 영문 + 숫자 + 특수문자, 8~16자
    private static final String PASSWORD_RULE =
            "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*])[A-Za-z\\d!@#$%^&*]{8,16}$";

    public MemberController(MemberService memberService, MailService mailService, I18n i18n) {
        this.memberService = memberService;
        this.mailService = mailService;
        this.i18n = i18n;
    }

    // 회원가입 화면
    @GetMapping("/join")
    public String joinForm() {
        return "members/join";
    }

    // 인증코드 메일 보내기
    // JS: fetch("/members/email/send") → "OK" 또는 에러 문구(화면 언어)를 돌려줌
    @PostMapping("/email/send")
    @ResponseBody
    public String sendCode(@RequestParam String email, HttpSession session) {
        if (memberService.isEmailExists(email)) {
            return i18n.get("member.error.emailTaken");
        }
        try {
            String code = mailService.sendCode(email);
            session.setAttribute("emailCode", code);
            session.setAttribute("emailTarget", email);
            return "OK";
        } catch (Exception e) {
            e.printStackTrace();
            return i18n.get("member.error.mailFail");
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
        return i18n.get("member.error.codeMismatch");
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

        // ① 이메일 인증 했는지
        if (verifiedEmail == null || !verifiedEmail.equals(member.getEmail())) {
            model.addAttribute("error", i18n.get("member.error.needVerify"));
            return "members/join";
        }

        // ② 비밀번호 규칙
        if (!member.getPasswordHash().matches(PASSWORD_RULE)) {
            model.addAttribute("error", i18n.get("member.error.passwordRule"));
            return "members/join";
        }

        // ③ 가입 (이메일·닉네임 중복이면 예외의 키가 날아옴)
        try {
            memberService.join(member);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", i18n.get(e.getMessage()));
            return "members/join";
        }

        session.removeAttribute("verifiedEmail");
        return "redirect:/loginpage";
    }
}