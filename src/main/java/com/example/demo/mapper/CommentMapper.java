package com.example.demo.mapper;

import com.example.demo.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {

    List<Comment> findComments(Long postId);   // 이 글의 댓글 목록

    void insertComment(Comment comment);       // 댓글 저장

    Comment findCommentById(Long id);          // 댓글 하나

    void updateComment(Comment comment);       // 댓글 수정

    void deleteComment(Long id);               // 댓글 삭제

    // 답글 저장
    void insertReply(Comment comment);

    // 이 댓글에 달린 답글들 삭제
    void deleteReplies(Long id);
}
