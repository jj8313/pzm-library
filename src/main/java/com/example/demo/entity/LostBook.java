package com.example.demo.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

// 분실 기록 1건 (lost_books 테이블)
@Getter
@Setter
public class LostBook {
    // lost_books 테이블 칸
    private Long id;
    private Long copyId;
    private Long memberId;          // 잃어버린 회원 (서가 분실이면 null)
    private String reason;          // 사유
    private LocalDateTime lostAt;   // 분실 처리 일시

    // JOIN으로 채우는 칸 (목록 화면용)
    private String barcode;         // 실물 바코드
    private Long bookId;            // 책 번호 (책 상세 링크)
    private String title;           // 책 제목
    private String nickname;        // 회원 닉네임
}
