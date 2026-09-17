package com.debuglife.mbti.auth.api;

import com.debuglife.mbti.auth.dto.AuthResponse;
import com.debuglife.mbti.auth.dto.LoginRequest;
import com.debuglife.mbti.auth.dto.RegisterRequest;
import com.debuglife.mbti.auth.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    private AuthResponse stubAuthResponse() {
        AuthResponse.UserDTO user = new AuthResponse.UserDTO(
                1L, "testuser", "Test User", "test@example.com", null, "USER", "2026-01-01T00:00:00");
        return new AuthResponse("access-token", "refresh-token", user);
    }

    @Test
    void registerReturnsUnifiedResponse() throws Exception {
        when(authService.register(any(RegisterRequest.class))).thenReturn(stubAuthResponse());

        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setEmail("test@example.com");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.token").value("access-token"));
    }

    @Test
    void loginReturnsUnifiedResponse() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenReturn(stubAuthResponse());

        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"));
    }

    @Test
    void registerRejectsMissingUsername() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setPassword("password123");
        request.setEmail("test@example.com");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginRejectsMissingPassword() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
