package com.debuglife.mbti.ai.provider;

import com.debuglife.mbti.ai.config.AiProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

/**
 * DeepSeek 适配器。DeepSeek 使用 OpenAI 兼容协议，
 * 仅覆盖 provider 标识并给出默认 base URL。
 */
@Component
public class DeepSeekAiProvider extends OpenAiCompatibleAiProvider {

    public DeepSeekAiProvider(AiProperties properties, ObjectMapper objectMapper) {
        super(properties, objectMapper);
    }

    @Override
    public String provider() { return "deepseek"; }
}