package com.example.demo.service;

import com.example.demo.entity.Loan;
import com.example.demo.entity.Member;
import com.example.demo.mapper.MemberMapper;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Locale;

// 대출·분실 자동 알림
// 문장은 i18n/notify.properties 에 있고, 받는 회원의 언어(members.lang)로 골라서 보냄
@Service
public class NotificationService {

    private final MessageService messageService;
    private final MemberMapper memberMapper;
    private final MessageSource messageSource;   // properties에서 문장 꺼내는 도구

    public NotificationService(MessageService messageService,
                               MemberMapper memberMapper,
                               MessageSource messageSource) {
        this.messageService = messageService;
        this.memberMapper = memberMapper;
        this.messageSource = messageSource;
    }

    // ===================== 도우미 =====================

    // 이 회원의 언어 (ja면 일본어, 그 외는 한국어)
    private Locale localeOf(Long memberId) {
        Member member = memberMapper.findById(memberId);
        if (member != null && "ja".equals(member.getLang())) {
            return Locale.JAPANESE;
        }
        return Locale.KOREAN;
    }

    // properties에서 문장 꺼내기: 키 + 언어 + 빈칸에 넣을 값들({0}, {1}...)
    private String text(String key, Locale locale, Object... values) {
        return messageSource.getMessage(key, values, locale);
    }

    // 날짜: 11월 18일 / 11月18日
    private String md(LocalDate date, Locale locale) {
        return text("notify.date", locale, date.getMonthValue(), date.getDayOfMonth());
    }

    // 도서관 이름으로 저장 (저장은 MessageService가)
    private void send(Long memberId, String content) {
        messageService.send(memberId, true, content);
    }

    // ===================== 알림 =====================

    // 📝 대출 신청 접수
    public void notifyRequested(Long memberId, String title) {
        Locale locale = localeOf(memberId);
        send(memberId, text("notify.requested", locale, title));
    }

    // 📚 대출 승인
    public void notifyApproved(Loan loan) {
        Locale locale = localeOf(loan.getMemberId());
        send(loan.getMemberId(), text("notify.approved", locale, loan.getTitle()));
    }

    // ❌ 대출 신청 취소
    public void notifyCanceled(Long memberId, String title) {
        Locale locale = localeOf(memberId);
        send(memberId, text("notify.canceled", locale, title));
    }

    // 🔁 대출 연장
    public void notifyExtended(Loan loan, LocalDate newDue) {
        Locale locale = localeOf(loan.getMemberId());
        send(loan.getMemberId(),
                text("notify.extended", locale, loan.getTitle(), md(newDue, locale)));
    }

    // ✅ 정상 반납
    public void notifyReturned(Loan loan) {
        Locale locale = localeOf(loan.getMemberId());
        send(loan.getMemberId(), text("notify.returned", locale, loan.getTitle()));
    }

    // ⚠️ 연체 반납 (정지)
    public void notifyOverdueReturned(Loan loan, long lateDays, LocalDate until) {
        Locale locale = localeOf(loan.getMemberId());
        send(loan.getMemberId(),
                text("notify.overdueReturned", locale, loan.getTitle(), lateDays, md(until, locale)));
    }

    // ⏰ 반납 기한 알림 (days: 3, 2, 1, 0, -1)
    public void notifyDue(Loan loan, String title, int days) {
        Locale locale = localeOf(loan.getMemberId());

        String message;
        if (days > 1) {
            message = text("notify.due.days", locale, title, days);
        } else if (days == 1) {
            message = text("notify.due.tomorrow", locale, title);
        } else if (days == 0) {
            message = text("notify.due.today", locale, title);
        } else {
            message = text("notify.due.over", locale, title);
        }

        // 뒤에 (반납 기한: 11월 22일) 붙이기
        String suffix = text("notify.due.suffix", locale, md(loan.getDueDate(), locale));
        send(loan.getMemberId(), message + " " + suffix);
    }

    // ❌ 분실 처리
    public void notifyLost(Long memberId, String title) {
        Locale locale = localeOf(memberId);
        send(memberId, text("notify.lost", locale, title));
    }
}