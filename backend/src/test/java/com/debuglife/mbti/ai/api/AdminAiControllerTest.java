package com.debuglife.mbti.ai.api;

import com.debuglife.mbti.ai.dto.TeamAnalysisResponse;
import com.debuglife.mbti.ai.service.AiTeamAnalysisService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminAiControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private AiTeamAnalysisService service;

    private UsernamePasswordAuthenticationToken auth(String role) {
        return new UsernamePasswordAuthenticationToken("admin", null,
                List.of(new SimpleGrantedAuthority(role)));
    }

    @Test
    void adminCanGenerateTeamAnalysis() throws Exception {
        TeamAnalysisResponse response = new TeamAnalysisResponse(5L, 3, Map.of("INTJ", 2L, "ENFP", 1L),
                "协作", "报告", "mock", "mock-mbti-coach", LocalDateTime.now());
        when(service.analyze(eq("admin"), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/admin/ai/team-analysis")
                        .with(authentication(auth("ROLE_ADMIN")))
                        .contentType("application/json")
                        .content("{\"typeCodes\":[\"INTJ\",\"INTJ\",\"ENFP\"],\"focus\":\"协作\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(5))
                .andExpect(jsonPath("$.data.totalMembers").value(3))
                .andExpect(jsonPath("$.data.typeCounts.INTJ").value(2));
    }

    @Test
    void nonAdminCannotGenerateTeamAnalysis() throws Exception {
        mockMvc.perform(post("/api/v1/admin/ai/team-analysis")
                        .with(authentication(auth("ROLE_USER")))
                        .contentType("application/json")
                        .content("{\"typeCodes\":[\"INTJ\"]}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void nonAdminCannotReadAnalysisHistory() throws Exception {
        mockMvc.perform(get("/api/v1/admin/ai/team-analysis/history")
                        .with(authentication(auth("ROLE_USER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void anonymousUserCannotReadAnalysisHistory() throws Exception {
        mockMvc.perform(get("/api/v1/admin/ai/team-analysis/history"))
                .andExpect(status().isUnauthorized());
    }
}
