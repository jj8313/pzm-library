package com.example.demo.websocket;

import com.example.demo.entity.Member;
import com.example.demo.service.MessageService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TalkHandler extends TextWebSocketHandler {

    private final MessageService messageService;
    private final ObjectMapper objectMapper = new ObjectMapper();   // JSON ↔ 자바 변환기

    // ★ 접속 명단
    private final Map<Long, WebSocketSession> members = new ConcurrentHashMap<>();  // 회원번호 → 연결
    private final Set<WebSocketSession> admins = ConcurrentHashMap.newKeySet();      // 관리자 연결들

    public TalkHandler(MessageService messageService) {
        this.messageService = messageService;
    }

    // ① 누가 연결했을 때 → 명단에 적기
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Member loginMember = (Member) session.getAttributes().get("loginMember");
        if (loginMember == null) {                // 로그인 안 했으면 끊기
            session.close();
            return;
        }
        if (loginMember.isAdmin()) {
            admins.add(session);                   // 관리자 명단에
        } else {
            members.put(loginMember.getId(), session);   // 회원 명단에 (번호 → 연결)
        }
    }

    // ② 메시지가 왔을 때 → 저장하고 → 보내기
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Member loginMember = (Member) session.getAttributes().get("loginMember");
        Map<String, Object> data = objectMapper.readValue(message.getPayload(), Map.class);   // JSON → Map
        String content = (String) data.get("content");

        Long memberId;          // 누구 대화방?
        boolean fromAdmin;      // 누가 말했어?
        if (loginMember.isAdmin()) {
            memberId = Long.valueOf(data.get("memberId").toString());   // 관리자는 어느 방인지 같이 보냄
            fromAdmin = true;
        } else {
            memberId = loginMember.getId();                             // 회원은 자기 방 (세션에서!)
            fromAdmin = false;
        }

        // DB 저장 (1부에서 만든 것 그대로)
        messageService.send(memberId, fromAdmin, content);

        // 보낼 내용 만들기
        Map<String, Object> out = new HashMap<>();
        out.put("memberId", memberId);
        out.put("fromAdmin", fromAdmin);
        out.put("content", content);
        out.put("time", LocalDateTime.now().format(DateTimeFormatter.ofPattern("a h:mm", Locale.KOREAN)));
        TextMessage json = new TextMessage(objectMapper.writeValueAsString(out));   // Map → JSON

        // 그 회원한테 보내기 (접속해 있으면)
        WebSocketSession memberSession = members.get(memberId);
        if (memberSession != null && memberSession.isOpen()) {
            memberSession.sendMessage(json);
        }
        // 관리자들한테도 보내기
        for (WebSocketSession admin : admins) {
            if (admin.isOpen()) {
                admin.sendMessage(json);
            }
        }
    }

    // ③ 연결이 끊겼을 때 → 명단에서 지우기
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        admins.remove(session);
        members.values().remove(session);
    }
}