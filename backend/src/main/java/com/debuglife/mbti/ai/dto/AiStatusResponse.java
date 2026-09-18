package com.debuglife.mbti.ai.dto;

public record AiStatusResponse(
        boolean enabled,
        String configuredProvider,
        String activeProvider,
        String model,
        boolean mockMode,
        String disclaimer
) {}