package com.example.demo.service;

import com.example.demo.entity.Loan;
import com.example.demo.entity.LoanStatus;
import com.example.demo.mapper.LoanMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class LoanService {

    private final LoanMapper loanMapper;
    private final NotificationService notificationService;   // 회원 알림

    public LoanService(LoanMapper loanMapper, NotificationService notificationService) {
        this.loanMapper = loanMapper;
        this.notificationService = notificationService;
    }

    // 회원 대출 신청
    // 돌려주는 값: "OK" / "SUSPENDED" / "OVERDUE" / "LIMIT" / "DUPLICATE" / "NO_BOOK"
    @Transactional
    public String requestLoan(Long memberId, Long bookId) {
        if (loanMapper.countSuspended(memberId) > 0) return "SUSPENDED";        // 정지 중
        if (loanMapper.countOverdue(memberId) > 0)   return "OVERDUE";          // 연체 중
        if (loanMapper.countActive(memberId) >= 5)   return "LIMIT";            // 5권
        if (loanMapper.countSameBook(memberId, bookId) > 0) return "DUPLICATE"; // 같은 책

        Long copyId = loanMapper.findAvailableCopyId(bookId);
        if (copyId == null) return "NO_BOOK";

        loanMapper.insertRequest(memberId, copyId);
        loanMapper.updateCopyReserved(copyId);

        notificationService.notifyRequested(memberId, loanMapper.findBookTitle(bookId));   // 📝
        return "OK";
    }

    // 관리자: 신청 들어온 목록
    public List<Loan> findRequested() {
        return loanMapper.findRequested();
    }

    // 관리자: 대출 승인
    @Transactional
    public boolean approve(Long loanId) {
        Loan loan = loanMapper.findById(loanId);
        if (loan == null || loan.getStatus() != LoanStatus.REQUESTED) return false;

        loanMapper.approve(loanId);
        loanMapper.updateCopyLoaned(loan.getCopyId());

        notificationService.notifyApproved(loan);   // 📚
        return true;
    }

    // 회원: 내 대출 목록
    public List<Loan> findMyLoans(Long memberId) {
        return loanMapper.findMyLoans(memberId);
    }

    // 관리자: 대출 중 목록
    public List<Loan> findLoaned() {
        return loanMapper.findLoaned();
    }

    // 관리자: 오늘 반납된 책
    public List<Loan> findReturnedToday() {
        return loanMapper.findReturnedToday();
    }

    // 회원: 반납 (누르면 바로 반납 완료)
    @Transactional
    public boolean memberReturn(Long loanId, Long memberId) {
        Loan loan = loanMapper.findById(loanId);

        // ① 내 대출인지
        if (loan == null || !loan.getMemberId().equals(memberId)) return false;
        // ② 대출 중인지
        if (loan.getStatus() != LoanStatus.LOANED) return false;

        // ③ 반납 완료 + 실물 책 다시 빌릴 수 있게
        loanMapper.returnLoan(loanId);
        loanMapper.updateCopyAvailable(loan.getCopyId());

        // ④ 늦었으면 정지 + ⚠️ 메시지, 아니면 ✅ 메시지
        long lateDays = ChronoUnit.DAYS.between(loan.getDueDate(), LocalDate.now());
        if (lateDays > 0) {
            loanMapper.updateSuspended(loan.getMemberId(), (int) lateDays);
            notificationService.notifyOverdueReturned(loan, lateDays, LocalDate.now().plusDays(lateDays));
        } else {
            notificationService.notifyReturned(loan);
        }
        return true;
    }

    // 회원: 대출 이력
    public List<Loan> findMyHistory(Long memberId) {
        return loanMapper.findMyHistory(memberId);
    }

    // 정지 끝나는 날
    public LocalDate findSuspendedUntil(Long memberId) {
        return loanMapper.findSuspendedUntil(memberId);
    }

    // 회원: 대출 연장 (+7일, 1번만)
    // 돌려주는 값: "OK" / "NOT_MINE" / "NOT_LOANED" / "OVERDUE" / "ALREADY"
    @Transactional
    public String extend(Long loanId, Long memberId) {
        Loan loan = loanMapper.findById(loanId);

        if (loan == null || !loan.getMemberId().equals(memberId)) return "NOT_MINE";   // ① 본인
        if (loan.getStatus() != LoanStatus.LOANED) return "NOT_LOANED";                 // ② 대출 중
        if (loan.getDueDate().isBefore(LocalDate.now())) return "OVERDUE";               // ③ 연체 전
        if (loan.getExtendCount() >= 1) return "ALREADY";                                // ④ 1번만

        loanMapper.extend(loanId);
        notificationService.notifyExtended(loan, loan.getDueDate().plusDays(7));   // 🔁
        return "OK";
    }

    // 관리자: 이 책의 대출 이력
    public List<Loan> findByBookId(Long bookId) {
        return loanMapper.findByBookId(bookId);
    }

    // 대출 신청 취소: 내 신청이고 '신청(1)' 상태일 때만
    @Transactional
    public boolean cancelRequest(Long loanId, Long memberId) {
        Loan loan = loanMapper.findById(loanId);

        if (loan == null || !loan.getMemberId().equals(memberId)) return false;   // ① 내 신청
        if (loan.getStatus() != LoanStatus.REQUESTED) return false;                // ② 신청 상태

        Long copyId = loan.getCopyId();              // 지우기 전에 실물 책 번호를 먼저 꺼내둠
        loanMapper.deleteRequest(loanId);            // ③ 신청 기록 삭제
        loanMapper.updateCopyAvailable(copyId);      // ④ 실물 책 다시 '빌릴 수 있음'으로
        notificationService.notifyCanceled(memberId, loan.getTitle());   // ❌
        return true;
    }
}