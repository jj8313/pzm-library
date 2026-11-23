package com.example.demo.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class Loan {
    // loans 테이블 칸
    private Long id;
    private Long memberId;
    private Long copyId;
    private Integer status;               // 1 신청 / 2 대출 중 / 4 반납 완료
    private LocalDateTime requestedAt;    // 신청 일시
    private LocalDateTime loanedAt;       // 대출 승인 일시
    private LocalDate dueDate;            // 반납 기한
    private Integer extendCount;          // 연장 횟수
    private LocalDateTime returnedAt;     // 반납 일시  ← 꼭 있어야 함

    // JOIN이나 계산으로 채우는 칸
    private String nickname;
    private String barcode;
    private String title;
    private Integer dday;
    private Long bookId;        // 책 번호 (책 상세 링크용)

    // ===== 화면에서 쓰는 판단 메서드 (조건을 이름으로) =====

    // 신청 중인가 → 화면: ${loan.requested}
    public boolean isRequested() {
        return status == LoanStatus.REQUESTED;
    }

    // 대출 중인가 (연체 포함) → 화면: ${loan.loaned}
    public boolean isLoaned() {
        return status == LoanStatus.LOANED;
    }

    // 연체 중인가
    public boolean isOverdue() {
        if (!isLoaned()) return false;           // ① 대출 중이 아니면 → 연체 아님
        if (dueDate == null) return false;       // ② 반납 기한이 없으면 → 연체 아님
        return dueDate.isBefore(LocalDate.now()); // ③ 반납 기한이 오늘보다 앞이면 → 연체
    }

    // 늦게 반납했나
    public boolean isLateReturned() {
        if (status != LoanStatus.RETURNED) return false;   // ① 반납 완료가 아니면 → 아님
        if (returnedAt == null || dueDate == null) return false;   // ② 날짜가 없으면 → 아님

        LocalDate returnedDay = returnedAt.toLocalDate();  // 반납한 날 (시간 빼고 날짜만)
        return returnedDay.isAfter(dueDate);               // ③ 반납한 날이 기한보다 뒤면 → 늦게 반납
    }

    // 분실됐나 → 화면: ${loan.isLost()}
    public boolean isLost() {
        return status == LoanStatus.LOST;
    }
}