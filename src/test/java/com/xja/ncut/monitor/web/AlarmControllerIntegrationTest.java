package com.xja.ncut.monitor.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.xja.ncut.monitor.domain.Alarm;
import com.xja.ncut.monitor.service.AlarmService;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.test.context.support.WithMockUser;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithMockUser(username = "admin", roles = "ADMIN")
class AlarmControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AlarmService alarmService;

    private Long alarmId;

    @BeforeEach
    void setUp() {
        Alarm alarm = new Alarm();
        alarm.setType("人员闯入");
        alarm.setArea("A区西门");
        alarm.setLevel("高");
        alarm.setStatus("待处置");
        alarm.setSource("AI视觉分析");
        alarm.setDetail("双模型识别：person(88.7%)；图片：test.png");
        alarm.setEventTime(new Date());
        alarmService.save(alarm);
        alarmId = alarm.getId();
    }

    @Test
    void pendingAlarmCanBeProcessedThenClosed() throws Exception {
        mockMvc.perform(patch("/alarm/{id}/status", alarmId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"处置中\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("处置中"));

        mockMvc.perform(patch("/alarm/{id}/status", alarmId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"已关闭\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("已关闭"));
    }

    @Test
    void closedAlarmCannotReturnToProcessing() throws Exception {
        alarmService.changeStatus(alarmId, "已关闭");

        mockMvc.perform(patch("/alarm/{id}/status", alarmId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"处置中\"}"))
            .andExpect(status().isConflict());
    }
}
