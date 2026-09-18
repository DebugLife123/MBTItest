package com.debuglife.mbti.ai.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record TeamAnalysisResponse(
        Long id,
        int totalMembers,
        Map<String, Long> typeCounts,
        String focus,
        String report,
        String provider,
        String model,
        LocalDateTime createdAt
) {}