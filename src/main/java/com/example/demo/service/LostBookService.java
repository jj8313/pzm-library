package com.example.demo.service;

import com.example.demo.entity.BookCopy;
import com.example.demo.entity.Loan;
import com.example.demo.entity.LostBook;
import com.example.demo.mapper.BookMapper;
import com.example.demo.mapper.LoanMapper;
import com.example.demo.mapper.LostBookMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// 관리자: 도서 분실 관리
@Service
public class LostBookService {

    private final LostBookMapper lostBookMapper;              // 분실 기록
    private final BookMapper bookMapper;                      // 실물 책
    private final LoanMapper loanMapper;                      // 대출
    private final NotificationService notificationService;    // 회원 알림

    public LostBookService(LostBookMapper lostBookMapper, BookMapper bookMapper,
                           LoanMapper loanMapper, NotificationService notificationService) {
        this.lostBookMapper = lostBookMapper;
        this.bookMapper = bookMapper;
        this.loanMapper = loanMapper;
        this.notificationService = notificationService;
    }

    // 이 책의 실물 목록
    public List<BookCopy> findCopies(Long bookId) {
        return bookMapper.findCopiesByBookId(bookId);
    }

    // 분실 도서 목록
    public List<LostBook> findAll() {
        return lostBookMapper.findAll();
    }

    // 분실 도서 몇 권
    public int countAll() {
        return lostBookMapper.countAll();
    }

    // 실물 책 분실 처리 (사유는 관리자가 직접 입력)
    // 돌려주는 값: "OK" / "NONE" / "ALREADY" / "RESERVED"
    @Transactional
    public String markLost(Long copyId, String reason) {
        BookCopy copy = bookMapper.findCopyById(copyId);

        // ① 안 되는 경우 먼저 걸러내기
        if (copy == null) return "NONE";            // 없는 책
        if (copy.isLost()) return "ALREADY";        // 이미 분실
        if (copy.isReserved()) return "RESERVED";   // 누가 신청 중

        // ② 분실 기록 준비 (사유 = 관리자가 쓴 글)
        LostBook lost = new LostBook();
        lost.setCopyId(copyId);
        lost.setReason(reason);

        // ③ 누가 빌려 간 책이면 → 대출도 '분실(5)'로 끝내고, 회원에게 알림
        if (copy.isLoaned()) {
            Loan loan = loanMapper.findLoanedByCopyId(copyId);
            String title = loanMapper.findBookTitleByLoan(loan.getId());

            loanMapper.markLost(loan.getId());
            notificationService.notifyLost(loan.getMemberId(), title);

            lost.setMemberId(loan.getMemberId());   // 누가 빌려 갔는지 기록
        }

        // ④ 실물 책을 '분실'로 + 분실 기록 저장
        bookMapper.updateCopyLost(copyId);
        lostBookMapper.insert(lost);

        return "OK";
    }
}