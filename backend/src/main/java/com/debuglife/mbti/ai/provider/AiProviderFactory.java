package com.debuglife.mbti.ai.provider;

import com.debuglife.mbti.ai.config.AiProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class AiProviderFactory {

    private static final Logger log = LoggerFactory.getLogger(AiProviderFactory.class);

    private final AiProperties properties;
    private final Map<String, AiProvider> providers;
    private final AiProvider mockProvider;

    public AiProviderFactory(AiProperties properties, List<AiProvider> providers) {
        this.properties = properties;
        this.providers = providers.stream()
                .collect(Collectors.toUnmodifiableMap(
                        provider -> provider.provider().toLowerCase(Locale.ROOT), Function.identity()));
        this.mockProvider = this.providers.get("mock");
    }

    public AiProvider resolve() {
        if (!properties.isEnabled()) {
            throw new IllegalStateException("AI 能力已关闭");
        }
        String configured = properties.normalizedProvider();
        if ("mock".equals(configured)) {
            return mockProvider;
        }
        AiProvider provider = providers.get(configured);
        if (provider == null) {
            log.warn("未识别的 AI Provider: {}，回退到 mock", configured);
            return mockProvider;
        }
        if (!"ollama".equals(configured) && !properties.hasApiKey()) {
            log.warn("未配置 AI_API_KEY，Provider {} 回退到 mock", configured);
            return mockProvider;
        }
        return provider;
    }

    public boolean isUsingMock() {
        try {
            return "mock".equals(resolve().provider());
        } catch (IllegalStateException ex) {
            return true;
        }
    }

    public String configuredProvider() {
        return properties.normalizedProvider();
    }
}