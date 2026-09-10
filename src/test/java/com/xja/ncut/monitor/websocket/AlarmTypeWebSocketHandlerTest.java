package com.xja.ncut.monitor.websocket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

class AlarmTypeWebSocketHandlerTest {
    @Test
    void ignoresBlankTypeAndSendsTrimmedType() throws Exception {
        WebSocketSession session = org.mockito.Mockito.mock(WebSocketSession.class);
        when(session.isOpen()).thenReturn(true);
        AlarmTypeWebSocketHandler handler = new AlarmTypeWebSocketHandler();
        handler.afterConnectionEstablished(session);

        handler.publish("");
        handler.publish("   ");
        verify(session, never()).sendMessage(any());

        handler.publish(" 烟火异常 ");
        ArgumentCaptor<TextMessage> message = ArgumentCaptor.forClass(TextMessage.class);
        verify(session).sendMessage(message.capture());
        assertThat(message.getValue().getPayload()).isEqualTo("烟火异常");
    }
}
