package com.xja.ncut.monitor.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.xja.ncut.monitor.domain.Device;
import com.xja.ncut.monitor.domain.DeviceMedia;
import com.xja.ncut.monitor.service.DeviceMediaService;
import com.xja.ncut.monitor.service.DeviceService;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithMockUser(username = "admin", roles = "ADMIN")
class DeviceControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private DeviceMediaService deviceMediaService;

    @Test
    void createsUpdatesAndDeletesDevice() throws Exception {
        mockMvc.perform(post("/device")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"code":"test-901","name":"测试摄像头","type":"摄像头","area":"测试区域","status":"在线"}
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.code").value("TEST-901"))
            .andExpect(jsonPath("$.name").value("测试摄像头"));

        Device created = deviceService.lambdaQuery().eq(Device::getCode, "TEST-901").one();
        assertThat(created).isNotNull();
        saveMedia("TEST-901");

        mockMvc.perform(put("/device/{id}", created.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"code":"test-902","name":"已修改摄像头","type":"摄像头","area":"新测试区域","status":"离线"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("TEST-902"))
            .andExpect(jsonPath("$.name").value("已修改摄像头"))
            .andExpect(jsonPath("$.status").value("离线"));

        assertThat(deviceMediaService.lambdaQuery().eq(DeviceMedia::getDeviceCode, "TEST-902").count())
            .isEqualTo(1);

        mockMvc.perform(delete("/device/{id}", created.getId()))
            .andExpect(status().isNoContent());

        assertThat(deviceService.getById(created.getId())).isNull();
        assertThat(deviceMediaService.lambdaQuery().eq(DeviceMedia::getDeviceCode, "TEST-902").count())
            .isZero();
    }

    @Test
    void rejectsDuplicateCode() throws Exception {
        mockMvc.perform(post("/device")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"code":"CAM-001","name":"重复设备","type":"摄像头","area":"A区西门","status":"在线"}
                    """))
            .andExpect(status().isConflict());
    }

    @Test
    void rejectsInvalidStatus() throws Exception {
        mockMvc.perform(post("/device")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"code":"TEST-903","name":"测试设备","type":"摄像头","area":"测试区域","status":"未知"}
                    """))
            .andExpect(status().isBadRequest());
    }

    private void saveMedia(String deviceCode) {
        DeviceMedia media = new DeviceMedia();
        media.setDeviceCode(deviceCode);
        media.setArea("测试区域");
        media.setOriginalName("test.png");
        media.setMediaType("image");
        media.setMediaPath("media/test.png");
        media.setPreviewPath("results/test.jpg");
        media.setUploadedTime(new Date());
        deviceMediaService.save(media);
    }
}
