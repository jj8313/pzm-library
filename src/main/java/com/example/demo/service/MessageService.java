package com.example.demo.service;

import com.example.demo.entity.Message;
import com.example.demo.mapper.MessageMapper;
import org.springframework.stereotype.Service;

import java.util.List;

// 톡톡 메시지 (저장·보기·읽음 처리)
@Service
public class MessageService {

    private final MessageMapper messageMapper;

    public MessageService(MessageMapper messageMapper) {
        this.messageMapper = messageMapper;
    }

    // 메시지 보내기 (저장)
    public void send(Long memberId, boolean fromAdmin, String content) {
        if (content == null || content.isBlank()) {
            return;
        }
        Message message = new Message();
        message.setMemberId(memberId);
        message.setFromAdmin(fromAdmin);
        message.setContent(content);
        messageMapper.insert(message);
    }

    // 이 회원의 대화 전체
    public List<Message> findByMemberId(Long memberId) {
        return messageMapper.findByMemberId(memberId);
    }

    // 관리자: 대화 목록
    public List<Message> findLastMessages() {
        return messageMapper.findLastMessages();
    }

    // 회원: 안 읽은 개수 (헤더 🔴)
    public int countUnread(Long memberId) {
        return messageMapper.countUnread(memberId);
    }

    // 회원: 톡톡 열면 전부 읽음으로
    public void markRead(Long memberId) {
        messageMapper.markRead(memberId);
    }
}