package com.xja.ncut.monitor.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.xja.ncut.monitor.domain.Alarm;
import com.xja.ncut.monitor.domain.DeviceMedia;
import com.xja.ncut.monitor.web.dto.AiDetectionObject;
import com.xja.ncut.monitor.web.dto.AiEventRequest;
import com.xja.ncut.monitor.web.dto.AiEventResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class AiEventServiceTest {
    @Test
    @SuppressWarnings("unchecked")
    void createsBusinessAlarmsForTwoModelResults() {
        AlarmService alarmService = mock(AlarmService.class);
        DeviceMediaService deviceMediaService = mock(DeviceMediaService.class);
        when(alarmService.saveBatch(anyList())).thenReturn(true);
        when(deviceMediaService.save(org.mockito.ArgumentMatchers.any(DeviceMedia.class))).thenReturn(true);
        AiEventService service = new AiEventService(alarmService, deviceMediaService, 0.25);
        AiEventRequest request = new AiEventRequest(
            "07_停车场_多人和汽车.png",
            "E:\\model\\ai-watch\\07_停车场_多人和汽车.png",
            null,
            "CAM-010",
            "image",
            "07_停车场_多人和汽车.png",
            "media/parking.png",
            "results/parking.jpg",
            List.of(
                new AiDetectionObject("person", 0.94),
                new AiDetectionObject("PERSON", 0.88),
                new AiDetectionObject("car", 0.91),
                new AiDetectionObject("smoke", 0.84),
                new AiDetectionObject("dog", 0.72),
                new AiDetectionObject("backpack", 0.66)
            )
        );

        AiEventResponse response = service.accept(request);

        assertThat(response.accepted()).isTrue();
        assertThat(response.detectionCount()).isEqualTo(6);
        assertThat(response.alarmCount()).isEqualTo(5);
        assertThat(response.alarms()).extracting(Alarm::getType)
            .containsExactly("人员聚集", "烟火异常", "车辆异常", "动物进入", "物品异常");
        assertThat(response.alarms()).allSatisfy(alarm -> {
            assertThat(alarm.getArea()).isEqualTo("停车场");
            assertThat(alarm.getStatus()).isEqualTo("待处置");
            assertThat(alarm.getSource()).isEqualTo("AI视觉分析");
            assertThat(alarm.getOriginalPath()).isEqualTo("media/parking.png");
            assertThat(alarm.getPreviewPath()).isEqualTo("results/parking.jpg");
        });

        ArgumentCaptor<List<Alarm>> captor = ArgumentCaptor.forClass(List.class);
        verify(alarmService).saveBatch(captor.capture());
        assertThat(captor.getValue()).hasSize(5);

        ArgumentCaptor<DeviceMedia> mediaCaptor = ArgumentCaptor.forClass(DeviceMedia.class);
        verify(deviceMediaService).save(mediaCaptor.capture());
        assertThat(mediaCaptor.getValue().getDeviceCode()).isEqualTo("CAM-010");
        assertThat(mediaCaptor.getValue().getArea()).isEqualTo("停车场");
        assertThat(mediaCaptor.getValue().getMediaPath()).isEqualTo("media/parking.png");
    }

    @Test
    void ignoresUnsupportedAndLowConfidenceDetections() {
        AlarmService alarmService = mock(AlarmService.class);
        DeviceMediaService deviceMediaService = mock(DeviceMediaService.class);
        AiEventService service = new AiEventService(alarmService, deviceMediaService, 0.25);
        AiEventRequest request = new AiEventRequest(
            "10_校园广场_正常空场景.png",
            null,
            "校园广场",
            List.of(
                new AiDetectionObject("person", 0.20),
                new AiDetectionObject("chair", 0.92)
            )
        );

        AiEventResponse response = service.accept(request);

        assertThat(response.detectionCount()).isEqualTo(1);
        assertThat(response.alarmCount()).isZero();
        assertThat(response.message()).contains("未发现");
        verifyNoInteractions(alarmService);
    }
}
