package com.example.demo.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostImage {
    private Long id;          // 이미지 번호 (체크박스로 지울 때 사용)
    private Long postId;      // post_id (어느 글의 이미지인지)
    private String filePath;  // file_path (화면에 보여줄 경로)
}
