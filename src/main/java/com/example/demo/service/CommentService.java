package com.example.demo.service;

import com.example.demo.entity.Comment;
import com.example.demo.entity.Member;
import com.example.demo.mapper.CommentMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentService {

    private final CommentMapper commentMapper;

    public CommentService(CommentMapper commentMapper) {
        this.commentMapper = commentMapper;
    }

    // 이 글의 댓글 목록 (원래 댓글 + 답글 전부)
    public List<Comment> findComments(Long postId) {
        return commentMapper.findComments(postId);
    }

    // 댓글 저장
    public void writeComment(Comment comment) {
        if (comment.getContent() == null || comment.getContent().isBlank()) {
            throw new IllegalArgumentException("댓글 내용을 입력해 주세요.");
        }
        commentMapper.insertComment(comment);
    }

    // ★ 답글 저장
    public void writeReply(Comment reply) {
        if (reply.getContent() == null || reply.getContent().isBlank()) {
            throw new IllegalArgumentException("답글 내용을 입력해 주세요.");
        }

        // 부모 댓글 확인
        Comment parent = commentMapper.findCommentById(reply.getParentId());
        if (parent == null) {
            throw new IllegalArgumentException("원래 댓글이 없습니다.");
        }
        // 한 단계만: 답글에는 답글 못 달게
        if (parent.getParentId() != null) {
            throw new IllegalArgumentException("답글에는 답글을 달 수 없습니다.");
        }

        reply.setPostId(parent.getPostId());     // 글 번호는 부모 댓글 것으로 (주소 조작 방지)
        commentMapper.insertReply(reply);
    }

    // 댓글 수정 (내 댓글일 때만) → 글 번호 돌려줌  — 그대로 (답글도 이걸로 수정)
    public Long updateComment(Long id, String content, Long loginMemberId) {
        Comment comment = commentMapper.findCommentById(id);

        if (comment == null) {
            throw new IllegalStateException("댓글이 없습니다.");
        }
        if (!comment.getMemberId().equals(loginMemberId)) {
            throw new IllegalStateException("본인 댓글만 수정할 수 있습니다.");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalStateException("댓글 내용을 입력해 주세요.");
        }

        comment.setContent(content);
        commentMapper.updateComment(comment);
        return comment.getPostId();
    }

    // 댓글 삭제 (본인 또는 관리자) → 달린 답글도 같이 삭제
    @Transactional
    public Long deleteComment(Long id, Member loginMember) {
        Comment comment = commentMapper.findCommentById(id);
        if (comment == null) {
            throw new IllegalStateException("없는 댓글입니다.");
        }

        boolean isMine = comment.getMemberId().equals(loginMember.getId());   // 내 댓글인가
        boolean isAdmin = loginMember.isAdmin();              // 관리자인가

        if (!isMine && !isAdmin) {
            throw new IllegalStateException("본인 댓글만 삭제할 수 있습니다.");
        }

        Long postId = comment.getPostId();     // 지우기 전에 글 번호를 먼저 꺼내둠
        commentMapper.deleteReplies(id);       // ① 답글 먼저
        commentMapper.deleteComment(id);       // ② 댓글
        return postId;
    }
}