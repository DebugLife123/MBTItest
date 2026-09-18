package com.debuglife.mbti.ai.provider;

import com.debuglife.mbti.ai.config.AiProperties;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AiProviderFactoryTest {

    @Test
    void fallsBackToMockWhenApiKeyIsMissing() {
        AiProperties properties = new AiProperties();
        properties.setProvider("deepseek");
        properties.setApiKey("");
        MockAiProvider mock = new MockAiProvider(properties);

        AiProviderFactory factory = new AiProviderFactory(properties, List.of(
                mock,
                new DeepSeekAiProvider(properties, new com.fasterxml.jackson.databind.ObjectMapper())));

        assertEquals("mock", factory.resolve().provider());
        assertEquals("deepseek", factory.configuredProvider());
        assertTrue(factory.isUsingMock());
    }

    @Test
    void usesConfiguredProviderWhenApiKeyExists() {
        AiProperties properties = new AiProperties();
        properties.setProvider("deepseek");
        properties.setApiKey("test-key");
        MockAiProvider mock = new MockAiProvider(properties);

        AiProviderFactory factory = new AiProviderFactory(properties, List.of(
                mock,
                new DeepSeekAiProvider(properties, new com.fasterxml.jackson.databind.ObjectMapper())));

        assertEquals("deepseek", factory.resolve().provider());
        assertFalse(factory.isUsingMock());
    }

    @Test
    void unknownProviderFallsBackToMock() {
        AiProperties properties = new AiProperties();
        properties.setProvider("unknown-provider");
        MockAiProvider mock = new MockAiProvider(properties);

        AiProviderFactory factory = new AiProviderFactory(properties, List.of(mock));

        assertEquals("mock", factory.resolve().provider());
    }

    @Test
    void disabledProviderCannotBeResolved() {
        AiProperties properties = new AiProperties();
        properties.setEnabled(false);
        MockAiProvider mock = new MockAiProvider(properties);

        AiProviderFactory factory = new AiProviderFactory(properties, List.of(mock));

        assertThrows(IllegalStateException.class, factory::resolve);
        assertTrue(factory.isUsingMock());
    }
}
