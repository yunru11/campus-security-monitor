package com.xja.ncut.monitor.web.dto;

import com.xja.ncut.monitor.domain.Alarm;
import java.util.List;

public record AiEventResponse(
    boolean accepted,
    String message,
    int detectionCount,
    int alarmCount,
    List<Alarm> alarms
) {
}
