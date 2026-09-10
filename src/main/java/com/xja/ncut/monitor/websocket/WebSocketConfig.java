package com.xja.ncut.monitor.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final AlarmTypeWebSocketHandler alarmTypeWebSocketHandler;

    public WebSocketConfig(AlarmTypeWebSocketHandler alarmTypeWebSocketHandler) {
        this.alarmTypeWebSocketHandler = alarmTypeWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(alarmTypeWebSocketHandler, "/ws/alarm-types");
    }
}
