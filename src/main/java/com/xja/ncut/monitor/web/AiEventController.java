package com.xja.ncut.monitor.web;

import com.xja.ncut.monitor.service.AiEventService;
import com.xja.ncut.monitor.web.dto.AiEventRequest;
import com.xja.ncut.monitor.web.dto.AiEventResponse;
import com.xja.ncut.monitor.websocket.AlarmTypePublisher;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiEventController {
    private final AiEventService aiEventService;
    private final String eventToken;
    private final AlarmTypePublisher alarmTypePublisher;

    public AiEventController(
        AiEventService aiEventService,
        @Value("${ai.event.token:}") String eventToken,
        AlarmTypePublisher alarmTypePublisher
    ) {
        this.aiEventService = aiEventService;
        this.eventToken = eventToken == null ? "" : eventToken.trim();
        this.alarmTypePublisher = alarmTypePublisher;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
            "status", "ok",
            "endpoint", "/api/ai/event",
            "tokenRequired", StringUtils.hasText(eventToken)
        );
    }

    @PostMapping("/event")
    public ResponseEntity<?> event(
        @RequestHeader(value = "X-AI-Token", required = false) String requestToken,
        @RequestBody(required = false) AiEventRequest request
    ) {
        if (!tokenMatches(requestToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("accepted", false, "message", "AI 接口令牌无效"));
        }

        AiEventResponse response = aiEventService.accept(request);
        publishAlarmTypes(response);
        return ResponseEntity.ok(response);
    }

    private void publishAlarmTypes(AiEventResponse response) {
        if (response == null || response.alarms() == null) {
            return;
        }
        response.alarms().stream()
            .filter(alarm -> alarm != null && StringUtils.hasText(alarm.getType()))
            .map(alarm -> alarm.getType().trim())
            .forEach(alarmTypePublisher::publish);
    }

    private boolean tokenMatches(String requestToken) {
        if (!StringUtils.hasText(eventToken)) {
            return true;
        }
        if (!StringUtils.hasText(requestToken)) {
            return false;
        }
        return MessageDigest.isEqual(
            eventToken.getBytes(StandardCharsets.UTF_8),
            requestToken.trim().getBytes(StandardCharsets.UTF_8)
        );
    }
}
