package com.xja.ncut.monitor.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.xja.ncut.monitor.domain.DeviceMedia;
import com.xja.ncut.monitor.service.DeviceMediaService;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "admin", roles = "ADMIN")
class DeviceMediaControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DeviceMediaService deviceMediaService;

    @BeforeEach
    void clearMedia() {
        deviceMediaService.getBaseMapper().delete(null);
    }

    @Test
    void returnsEveryDeviceWithItsLatestMedia() throws Exception {
        saveMedia("CAM-001", "media/older.png", new Date(1_000));
        saveMedia("CAM-001", "media/latest.png", new Date(2_000));

        mockMvc.perform(get("/device/media/latest"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].deviceCode").value("CAM-001"))
            .andExpect(jsonPath("$[0].mediaPath").value("media/latest.png"))
            .andExpect(jsonPath("$[1].deviceCode").value("CAM-013"))
            .andExpect(jsonPath("$[1].mediaPath").doesNotExist());
    }

    private void saveMedia(String deviceCode, String path, Date uploadedTime) {
        DeviceMedia media = new DeviceMedia();
        media.setDeviceCode(deviceCode);
        media.setArea("A区西门");
        media.setOriginalName("测试图片.png");
        media.setMediaType("image");
        media.setMediaPath(path);
        media.setPreviewPath("results/test.jpg");
        media.setUploadedTime(uploadedTime);
        deviceMediaService.save(media);
    }
}
