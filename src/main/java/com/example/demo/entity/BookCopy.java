package com.example.demo.entity;

import lombok.Getter;
import lombok.Setter;

// 실물 책 1권 (book_copies 테이블)
@Getter
@Setter
public class BookCopy {
    private Long id;
    private Long bookId;
    private String barcode;
    private String status;      // AVAILABLE / RESERVED / LOANED / LOST

    public boolean isAvailable() { return "AVAILABLE".equals(status); }   // 대출 가능
    public boolean isReserved()  { return "RESERVED".equals(status); }    // 신청됨
    public boolean isLoaned()    { return "LOANED".equals(status); }      // 대출 중
    public boolean isLost()      { return "LOST".equals(status); }        // 분실
}
