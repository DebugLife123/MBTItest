package com.debuglife.mbti.ai.api;

import com.debuglife.mbti.ai.config.AiProperties;
import com.debuglife.mbti.ai.dto.ChatResponse;
import com.debuglife.mbti.ai.dto.ChatSessionView;
import com.debuglife.mbti.ai.provider.AiProvider;
import com.debuglife.mbti.ai.provider.AiProviderFactory;
import com.debuglife.mbti.ai.service.AiChatService;
import org.junit.jupiter.api.BeforeEach;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AiControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private AiChatService chatService;
    @MockitoBean private AiProviderFactory providerFactory;
    @MockitoBean private AiProperties properties;
    @MockitoBean(name = "mockAiProvider") private AiProvider provider;

    private UsernamePasswordAuthenticationToken auth;

    @BeforeEach
    void setUp() {
        auth = new UsernamePasswordAuthenticationToken(
                "testuser", null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(properties.isEnabled()).thenReturn(true);
        when(properties.getModel()).thenReturn("mock-mbti-coach");
        when(providerFactory.configuredProvider()).thenReturn("mock");
        when(providerFactory.resolve()).thenReturn(provider);
        when(provider.provider()).thenReturn("mock");
        when(provider.model()).thenReturn("mock-mbti-coach");
    }

    @Test
    void statusExposesMockModeExplicitly() throws Exception {
        mockMvc.perform(get("/api/v1/ai/status").with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.mockMode").value(true))
                .andExpect(jsonPath("$.data.activeProvider").value("mock"));
    }

    @Test
    void chatReturnsUnifiedResponse() throws Exception {
        ChatResponse response = new ChatResponse(9L, "职业规划", "建议如下", "mock",
                "mock-mbti-coach", "INTJ", LocalDateTime.now());
        when(chatService.chat(eq("testuser"), eq(null), eq("帮我规划"))).thenReturn(response);

        mockMvc.perform(post("/api/v1/ai/chat")
                        .with(authentication(auth))
                        .contentType("application/json")
                        .content("{\"message\":\"帮我规划\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sessionId").value(9))
                .andExpect(jsonPath("$.data.reply").value("建议如下"));
    }

    @Test
    void chatRejectsBlankMessage() throws Exception {
        mockMvc.perform(post("/api/v1/ai/chat")
                        .with(authentication(auth))
                        .contentType("application/json")
                        .content("{\"message\":\"   \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sessionsReturnsSessionList() throws Exception {
        ChatSessionView view = new ChatSessionView(1L, "职业规划", "INTJ", "mock", "mock-mbti-coach",
                "ACTIVE", LocalDateTime.now(), LocalDateTime.now(), List.of());
        when(chatService.listSessions("testuser")).thenReturn(List.of(view));

        mockMvc.perform(get("/api/v1/ai/sessions").with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].personalityType").value("INTJ"));
    }

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mockMvc.perform(get("/api/v1/ai/status"))
                .andExpect(status().isUnauthorized());
    }
}
