package com.example.demo.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class Message {
    private Long id;
    private Long memberId;            // member_id: 누구의 대화방
    private Boolean fromAdmin;        // from_admin: 도서관이 말했나 (true/false)
    private String content;           // 내용
    private LocalDateTime createdAt;  // 시간

    private String nickname;          // 테이블엔 없음. 관리자 대화 목록에서 회원 이름 보여줄 때 (JOIN
}
