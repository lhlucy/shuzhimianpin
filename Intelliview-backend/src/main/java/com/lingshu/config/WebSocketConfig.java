package com.lingshu.config;

import com.lingshu.websocket.AIInterviewRealtimeAsrWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final AIInterviewRealtimeAsrWebSocketHandler realtimeAsrWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(realtimeAsrWebSocketHandler, "/ws/ai/asr/realtime")
                .setAllowedOriginPatterns("*");
    }
}
