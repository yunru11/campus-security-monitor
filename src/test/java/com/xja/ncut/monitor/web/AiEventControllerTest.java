package com.xja.ncut.monitor.web;

import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.xja.ncut.monitor.domain.Alarm;
import com.xja.ncut.monitor.service.AiEventService;
import com.xja.ncut.monitor.web.dto.AiEventRequest;
import com.xja.ncut.monitor.web.dto.AiEventResponse;
import com.xja.ncut.monitor.websocket.AlarmTypePublisher;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AiEventControllerTest {
    @Mock
    private AiEventService aiEventService;

    @Mock
    private AlarmTypePublisher alarmTypePublisher;

    @Test
    void eventPublishesNonBlankAlarmType() {
        Alarm alarm = new Alarm();
        alarm.setType(" 人员闯入 ");
        when(aiEventService.accept(nullable(AiEventRequest.class)))
            .thenReturn(new AiEventResponse(true, "ok", 1, 1, List.of(alarm)));

        AiEventController controller = new AiEventController(aiEventService, "", alarmTypePublisher);
        controller.event(null, null);

        verify(alarmTypePublisher).publish("人员闯入");
    }

    @Test
    void eventDoesNotPublishBlankAlarmType() {
        Alarm alarm = new Alarm();
        alarm.setType("   ");
        when(aiEventService.accept(nullable(AiEventRequest.class)))
            .thenReturn(new AiEventResponse(true, "ok", 1, 1, List.of(alarm)));

        AiEventController controller = new AiEventController(aiEventService, "", alarmTypePublisher);
        controller.event(null, null);

        verifyNoInteractions(alarmTypePublisher);
    }
}
