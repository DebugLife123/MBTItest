package com.debuglife.mbti.ai.dto;

import java.time.LocalDateTime;

public record ChatMessageView(
        Long id,
        String role,
        String content,
        LocalDateTime createdAt
) {}