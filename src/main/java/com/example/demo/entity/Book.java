package com.example.demo.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
public class Book {
    // books 테이블 컬럼
    private Long id;                  // id
    private String isbn;              // isbn (13자리)
    private String title;             // title
    private String authors;           // authors ("A, B" 글자 하나)
    private String publisher;         // publisher
    private String category;          // category (지금은 안 씀)
    private String coverUrl;          // cover_url
    private String description;       // description
    private LocalDateTime createdAt;  //
    private String url;

    private int totalCount;      // 전체 권수
    private int availableCount;  // 대출 가능 권수
}
