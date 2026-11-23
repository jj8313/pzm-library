package com.example.demo.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Comment {

    // comments 테이블 컬럼
    private Long id;                  // id
    private Long postId;              // post_id    (몇 번 글의 댓글인지)
    private Long memberId;            // member_id  (누가 썼는지)
    private String content;           // content
    private LocalDateTime createdAt;  // created_at
    private Long parentId;          // ★ 추가: 어느 댓글의 답글 (원래 댓글이면 null)

    // 테이블엔 없지만 화면에 보여주려고
    private String nickname;          // 작성자 닉네임 (members와 JOIN)
}
