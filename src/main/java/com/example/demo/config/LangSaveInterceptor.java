package com.example.demo.config;

import com.example.demo.entity.Member;
import com.example.demo.mapper.MemberMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

// 로그인한 회원이 언어를 바꾸면(?lang=ja) → members.lang에도 저장
// (알림을 그 회원 언어로 보내려고)
@Component
public class LangSaveInterceptor implements HandlerInterceptor {

    private final MemberMapper memberMapper;

    public LangSaveInterceptor(MemberMapper memberMapper) {
        this.memberMapper = memberMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        // ① 주소에 ?lang= 이 없으면 → 할 일 없음
        String lang = request.getParameter("lang");
        if (lang == null) return true;

        // ② ko / ja 말고 이상한 값이면 → 저장 안 함
        if (!lang.equals("ko") && !lang.equals("ja")) return true;

        // ③ 로그인 안 했으면 → 저장할 회원이 없음
        HttpSession session = request.getSession(false);
        if (session == null) return true;
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return true;

        // ④ 이미 같은 언어면 → DB 다시 안 씀
        if (lang.equals(loginMember.getLang())) return true;

        // ⑤ DB에 저장 + 세션의 회원 정보도 맞춰 두기
        memberMapper.updateLang(loginMember.getId(), lang);
        loginMember.setLang(lang);

        return true;   // true = 원래 가려던 Controller로 계속 진행
    }
}