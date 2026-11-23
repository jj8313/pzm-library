package com.example.demo.config;

import com.example.demo.websocket.TalkHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final TalkHandler talkHandler;

    public WebSocketConfig(TalkHandler talkHandler) {
        this.talkHandler = talkHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(talkHandler, "/ws/talk")
                .addInterceptors(new HttpSessionHandshakeInterceptor());
    }
}