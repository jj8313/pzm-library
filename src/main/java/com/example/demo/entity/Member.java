package com.example.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Member {
    private Long id;
    private String email;
    private String passwordHash;
    private String nickname;
    private String role;               // MEMBER, ADMIN
    private LocalDate suspendedUntil;
    private LocalDateTime createdAt;
    private String lang;            // 화면·알림 언어: "ko" / "ja"

    // members 테이블에는 없음. 관리자 회원 목록에서 채움
    private int loanCount;      // 대출 중 (신청 포함)
    private int overdueCount;   // 연체

    // ===== 화면에서 쓰는 판단 메서드 =====

    // 관리자인가 → 화면: ${loginMember.isAdmin()}
    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    // 탈퇴했나: 이메일이 deleted_ 로 시작 → 화면: ${m.isWithdrawn()}
    public boolean isWithdrawn() {
        return email != null && email.startsWith("deleted_");
    }

    // 대출 정지 중인가: 탈퇴 아니고, 정지 날짜가 오늘 이후 → 화면: ${m.isSuspended()}
    public boolean isSuspended() {
        return !isWithdrawn()
                && suspendedUntil != null
                && !suspendedUntil.isBefore(LocalDate.now());
    }
}
