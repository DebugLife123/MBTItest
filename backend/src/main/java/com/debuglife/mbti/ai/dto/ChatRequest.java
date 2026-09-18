package com.debuglife.mbti.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChatRequest {

    private Long sessionId;

    @NotBlank(message = "消息内容不能为空")
    @Size(max = 4000, message = "单条消息不能超过4000字")
    private String message;

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}