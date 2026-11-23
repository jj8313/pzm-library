package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoBook {
    private String title;           // 제목
    private List<String> authors;   // 저자들 (배열이라 List)
    private String publisher;       // 출판사
    private String isbn;            // "10자리 13자리"
    private String thumbnail;       // 표지 이미지 주소
    private String contents;        // 소개
    private String url;
    // ISBN 13자리만 꺼내기
    // "8937460440 9788937460449" → "9788937460449"
    public String getIsbn13() {
        if (isbn == null || isbn.isBlank()) return "";   // 없으면 빈 글자
        String[] parts = isbn.trim().split(" ");          // 공백으로 자르기 → ["8937460440", "9788937460449"]
        return parts[parts.length - 1];                   // 마지막 것 = 13자리
    }

    // 저자 목록을 글자 하나로 합치기
    // ["헤르만 헤세", "전영애"] → "헤르만 헤세, 전영애"
    public String getAuthorsText() {
        if (authors == null) return "";                   // 없으면 빈 글자
        return String.join(", ", authors);                // 사이사이에 ", " 넣어서 합치기
    }
}
