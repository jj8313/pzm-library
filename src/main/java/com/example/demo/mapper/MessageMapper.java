package com.example.demo.mapper;

import com.example.demo.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {

    // ① 메시지 저장
    void insert(Message message);

    // ② 이 회원의 대화 전체
    List<Message> findByMemberId(Long memberId);

    // ③ 관리자: 대화 목록 (회원마다 마지막 메시지)
    List<Message> findLastMessages();

    // 회원: 도서관 메시지 중 안 읽은 개수
    int countUnread(Long memberId);

    // 회원: 도서관 메시지 전부 읽음으로
    void markRead(Long memberId);
}
