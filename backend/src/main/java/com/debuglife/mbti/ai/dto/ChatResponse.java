package com.debuglife.mbti.ai.dto;

import java.time.LocalDateTime;

public record ChatResponse(
        Long sessionId,
        String title,
        String reply,
        String provider,
        String model,
        String personalityType,
        LocalDateTime createdAt
) {}