package com.example.demo.scheduler;

import com.example.demo.entity.Loan;
import com.example.demo.mapper.LoanMapper;
import com.example.demo.service.NotificationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// 반납 기한 알림 (매일 자동 실행)
@Component
public class LoanReminder {

    private final LoanMapper loanMapper;
    private final NotificationService notificationService;

    public LoanReminder(LoanMapper loanMapper, NotificationService notificationService) {
        this.loanMapper = loanMapper;
        this.notificationService = notificationService;
    }

    // 매일 아침 9시에 자동 실행
    @Scheduled(cron = "0 0 9 * * *")
    public void dailyReminder() {
        remind(3);     // ⏰ 3일 남음
        remind(2);     // ⏰ 2일 남음
        remind(1);     // ⏰ 내일
        remind(0);     // 📅 오늘
        remind(-1);    // ⚠️ 연체 첫날
    }

    // days일 남은 대출을 찾아서 알림 보내기 (문장은 NotificationService.notifyDue)
    private void remind(int days) {
        for (Loan loan : loanMapper.findDueInDays(days)) {
            String title = loanMapper.findBookTitleByLoan(loan.getId());
            notificationService.notifyDue(loan, title, days);
        }
    }
}