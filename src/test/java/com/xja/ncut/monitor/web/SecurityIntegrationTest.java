package com.xja.ncut.monitor.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void unauthenticatedPageRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/index.html"))
            .andExpect(status().isFound())
            .andExpect(redirectedUrlPattern("**/login.html"));
    }

    @Test
    void unauthenticatedAlarmApiReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/alarm/list"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void configuredAdminCanLogIn() throws Exception {
        mockMvc.perform(formLogin().user("admin").password("Monitor@2026"))
            .andExpect(status().isFound())
            .andExpect(authenticated().withUsername("admin"));
    }
}
