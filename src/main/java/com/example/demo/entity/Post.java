package com.example.demo.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Post {
    private Long id;               // id
    private Long memberId;         // member_id
    private String board;          // board (NOTICE / FREE / REQUEST)
    private String title;          // title
    private String content;        // content
    private int viewCount;         // view_count
    private LocalDateTime createdAt;  // created_at

    // 테이블엔 없지만 화면에 쓰려고 추가
    private String nickname;       // 작성자 닉네임 (members와 JOIN해서 가져옴)
}
