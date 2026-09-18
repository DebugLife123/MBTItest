package com.debuglife.mbti.ai.provider;

import com.debuglife.mbti.ai.config.AiProperties;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MockAiProviderTest {

    @Test
    void completeBuildsCareerAdviceAndExtractsPersonalityType() {
        AiProperties properties = new AiProperties();
        MockAiProvider provider = new MockAiProvider(properties);

        String reply = provider.complete(List.of(
                AiMessage.system("system"),
                AiMessage.user("我是 INTJ，想做 AI 全栈开发，应该怎么准备？")));

        assertTrue(reply.contains("INTJ"));
        assertTrue(reply.contains("发挥优势"));
        assertTrue(reply.contains("mock Provider"));
    }

    @Test
    void completeBuildsTeamTemplate() {
        MockAiProvider provider = new MockAiProvider(new AiProperties());

        String reply = provider.complete(List.of(
                AiMessage.user("团队共 3 人，INTJ × 2，ENFP × 1，请做团队分析")));

        assertTrue(reply.contains("团队整体倾向"));
        assertTrue(reply.contains("协作建议"));
        assertTrue(reply.contains("INTJ"));
    }

    @Test
    void streamEmitsNonEmptyChunksThatReassembleTheAnswer() {
        MockAiProvider provider = new MockAiProvider(new AiProperties());
        List<String> chunks = new ArrayList<>();

        provider.stream(List.of(AiMessage.user("给我一份职业规划建议")), chunks::add);

        assertTrue(chunks.size() > 1);
        assertTrue(chunks.stream().noneMatch(String::isEmpty));
        assertEquals(provider.complete(List.of(AiMessage.user("给我一份职业规划建议"))),
                String.join("", chunks));
    }
}
