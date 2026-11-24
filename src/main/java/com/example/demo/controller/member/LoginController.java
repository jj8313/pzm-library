package com.example.demo.controller.member;

import com.example.demo.config.I18n;
import com.example.demo.entity.Member;
import com.example.demo.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;

@Controller
@RequiredArgsConstructor
public class LoginController {

    private final MemberService memberService;
    private final LocaleResolver localeResolver;   // 화면 언어 바꾸기 (WebConfig에서 만든 것)
    private final I18n i18n;                       // 에러 키 → 화면 언어 문장

    // 로그인 화면
    @GetMapping("/loginpage")
    public String loginpage() {
        return "members/loginpage";
    }

    // 로그인 처리 → 관리자는 대시보드, 회원은 메인(/)으로
    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpSession session, Model model,
                        HttpServletRequest request, HttpServletResponse response) {
        // ① 로그인 확인 (틀리면 에러 메시지와 함께 로그인 화면으로)
        Member member;
        try {
            member = memberService.login(email, password);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", i18n.get(e.getMessage()));   // 키 → 번역
            return "members/loginpage";
        }

        // ② 세션에 넣기 (지우면 안 됨!)
        session.setAttribute("loginMember", member);

        // ③ 이 회원이 저장해 둔 언어로 화면 맞추기 (ja면 일본어, 그 외는 한국어)
        if ("ja".equals(member.getLang())) {
            localeResolver.setLocale(request, response, Locale.JAPANESE);
        } else {
            localeResolver.setLocale(request, response, Locale.KOREAN);
        }

        // ④ 관리자면 대시보드로
        if (member.isAdmin()) {
            return "redirect:/admin/dashboard";
        }

        // ⑤ 일반 회원은 메인으로
        return "redirect:/";
    }

    // 로그아웃 → 메인(/)으로
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}