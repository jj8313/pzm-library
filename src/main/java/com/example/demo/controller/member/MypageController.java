package com.example.demo.controller.member;

import com.example.demo.config.I18n;
import com.example.demo.entity.Member;
import com.example.demo.service.MemberService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class MypageController {

    private final MemberService memberService;
    private final I18n i18n;   // 에러 키 → 화면 언어 문장

    public MypageController(MemberService memberService, I18n i18n) {
        this.memberService = memberService;
        this.i18n = i18n;
    }

    // 마이페이지 화면
    @GetMapping("/mypage")
    public String mypage(HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/loginpage";
        }
        return "mypage/mypage";
    }

    // 정보 수정 화면
    @GetMapping("/mypage/edit")
    public String editForm(HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/loginpage";
        }
        return "mypage/mypageedit";
    }

    // 정보 수정 저장
    @PostMapping("/mypage/edit")
    public String edit(@RequestParam String nickname,
                       @RequestParam String newPassword,
                       @RequestParam String newPasswordConfirm,
                       HttpSession session, Model model) {

        // ① 로그인 확인
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/loginpage";
        }

        // ② 비밀번호 두 칸이 다르면 막기
        if (!newPassword.equals(newPasswordConfirm)) {
            model.addAttribute("error", i18n.get("member.error.passwordMismatch"));
            return "mypage/mypageedit";
        }

        // ③ 서비스에 수정 맡기기 (닉네임 중복, 비밀번호 규칙 위반이면 예외의 키가 날아옴)
        try {
            memberService.updateInfo(loginMember.getId(), nickname, newPassword);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", i18n.get(e.getMessage()));
            return "mypage/mypageedit";
        }

        // ④ DB에서 바뀐 내 정보 다시 가져와서 세션에 덮어쓰기 (지우면 안 됨!)
        Member updated = memberService.findById(loginMember.getId());
        session.setAttribute("loginMember", updated);

        // ⑤ 마이페이지로
        return "redirect:/mypage";
    }

    // 회원탈퇴: POST /mypage/withdraw
    @PostMapping("/mypage/withdraw")
    public String withdraw(@RequestParam String password,
                           HttpSession session,
                           RedirectAttributes ra) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/loginpage";
        }

        try {
            memberService.withdraw(loginMember.getId(), password);
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("withdrawError", i18n.get(e.getMessage()));   // 비밀번호 틀림, 대출 중
            return "redirect:/mypage";
        }

        session.invalidate();   // 로그아웃
        return "redirect:/";
    }
}