package com.xja.ncut.monitor.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.xja.ncut.monitor.domain.Alarm;
import com.xja.ncut.monitor.service.AlarmService;
import com.xja.ncut.monitor.service.DeviceMediaService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AiEventControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AlarmService alarmService;

    @Autowired
    private DeviceMediaService deviceMediaService;

    @BeforeEach
    void clearAlarms() {
        alarmService.getBaseMapper().delete(null);
        deviceMediaService.getBaseMapper().delete(null);
    }

    @Test
    void acceptsGuidePayloadAndPersistsAlarm() throws Exception {
        String payload = """
            {
              "image": "01_A区西门_单人闯入.png",
              "imagePath": "D:\\\\model\\\\ai-watch\\\\01_A区西门_单人闯入.png",
              "deviceCode": "CAM-001",
              "mediaType": "image",
              "mediaName": "01_A区西门_单人闯入.png",
              "mediaUrl": "media/cam001.png",
              "previewUrl": "results/cam001.jpg",
              "objects": [
                {
                  "class": "person",
                  "conf": 0.886722,
                  "model": "COCO",
                  "box": {"x1": 948.91, "y1": 495.98, "x2": 1035.05, "y2": 753.38}
                }
              ]
            }
            """;

        mockMvc.perform(post("/api/ai/event")
                .header("X-AI-Token", "test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accepted").value(true))
            .andExpect(jsonPath("$.detectionCount").value(1))
            .andExpect(jsonPath("$.alarmCount").value(1))
            .andExpect(jsonPath("$.alarms[0].type").value("人员闯入"))
            .andExpect(jsonPath("$.alarms[0].area").value("A区西门"));

        List<Alarm> alarms = alarmService.list();
        assertThat(alarms).hasSize(1);
        assertThat(alarms.get(0).getType()).isEqualTo("人员闯入");
        assertThat(alarms.get(0).getStatus()).isEqualTo("待处置");
        assertThat(alarms.get(0).getSource()).isEqualTo("AI视觉分析");
        assertThat(alarms.get(0).getOriginalPath()).isEqualTo("media/cam001.png");
        assertThat(alarms.get(0).getPreviewPath()).isEqualTo("results/cam001.jpg");
        assertThat(deviceMediaService.count()).isEqualTo(1);
        assertThat(deviceMediaService.list().get(0).getDeviceCode()).isEqualTo("CAM-001");
    }

    @Test
    void rejectsRequestWhenTokenIsMissing() throws Exception {
        mockMvc.perform(post("/api/ai/event")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"objects\":[]}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.accepted").value(false));

        assertThat(alarmService.count()).isZero();
    }
}
