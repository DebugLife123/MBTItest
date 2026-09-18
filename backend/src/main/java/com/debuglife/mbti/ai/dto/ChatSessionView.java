package com.debuglife.mbti.ai.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ChatSessionView(
        Long id,
        String title,
        String personalityType,
        String provider,
        String model,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<ChatMessageView> messages
) {}