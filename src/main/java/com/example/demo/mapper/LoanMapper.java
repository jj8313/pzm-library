package com.example.demo.mapper;

import com.example.demo.entity.Loan;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface LoanMapper {
    // ① 빌릴 수 있는 실물 1권의 id (없으면 null)
    Long findAvailableCopyId(Long bookId);

    // ② 대출 신청 기록 남기기
    void insertRequest(@Param("memberId") Long memberId, @Param("copyId") Long copyId);

    // ③ 그 실물을 '신청됨'으로
    void updateCopyReserved(Long copyId);

    // ④ 관리자: 신청 들어온 목록
    List<Loan> findRequested();

    // ⑤ 대출 1건 꺼내기
    Loan findById(Long id);

    // ⑥ 대출 승인
    void approve(Long id);

    // ⑦ 실물을 '대출 중'으로
    void updateCopyLoaned(Long copyId);

    // ⑧ 회원: 내 대출 목록
    List<Loan> findMyLoans(Long memberId);

    // ── 반납 ──
    List<Loan> findLoaned();               // ⑨ 대출 중 목록
    void returnLoan(Long id);              // ⑩ 대출 → RETURNED
    void updateCopyAvailable(Long copyId); // ⑪ 실물 → AVAILABLE

     List<Loan> findReturnedToday();    // ← 추가
    // ── 대출 규칙 ──
    int countActive(Long memberId);                                                     // ⑬
    int countSameBook(@Param("memberId") Long memberId, @Param("bookId") Long bookId);   // ⑭

    //반납기한
    void updateSuspended(@Param("memberId") Long memberId,
                         @Param("days") int days);

    // 정지 중인가 (1이면 정지 중)
    int countSuspended(Long memberId);
    // 연체 중인 책 몇 권인가
    int countOverdue(Long memberId);


    // 회원: 대출 이력 (반납 끝난 것)
    List<Loan> findMyHistory(Long memberId);

    // 정지 끝나는 날 (정지 중이 아니면 null)
    LocalDate findSuspendedUntil(Long memberId);

    // 반납 기한까지 딱 days일 남은 대출 (알림용)
    List<Loan> findDueInDays(int days);

    // 책 제목만 (신청 알림용)
    String findBookTitle(Long bookId);

    // 대출 번호로 책 제목만 (알림용)
    String findBookTitleByLoan(Long loanId);
    // 대출 연장 (+7일)
    void extend(Long id);

    // 관리자: 이 책의 대출 이력
    List<Loan> findByBookId(Long bookId);

    int deleteRequest(Long id);

    Loan findLoanedByCopyId(Long copyId);
    void markLost(Long id);
}
