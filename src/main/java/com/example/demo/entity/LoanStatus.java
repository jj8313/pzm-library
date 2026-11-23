package com.example.demo.entity;

public class LoanStatus {
    public static final int REQUESTED = 1;   // 신청
    public static final int LOANED    = 2;   // 대출 중
    public static final int RETURNED  = 4;   // 반납 완료
    public static final int LOST      = 5;   // 분실  ← 추가
}
