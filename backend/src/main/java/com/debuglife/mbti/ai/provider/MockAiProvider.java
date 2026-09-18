package com.debuglife.mbti.ai.provider;

import com.debuglife.mbti.ai.config.AiProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * 无外部密钥时的演示 Provider。它保证 AI 用户链路能够真实运行，
 * 但不会伪装成真实大模型响应。
 */
@Component
public class MockAiProvider implements AiProvider {

    private final AiProperties properties;

    public MockAiProvider(AiProperties properties) {
        this.properties = properties;
    }

    @Override
    public String provider() { return "mock"; }

    @Override
    public String model() { return "mock-mbti-coach"; }

    @Override
    public String complete(List<AiMessage> messages) {
        return buildAnswer(lastUserMessage(messages));
    }

    @Override
    public void stream(List<AiMessage> messages, Consumer<String> onDelta) {
        String answer = complete(messages);
        int chunkSize = 8;
        for (int i = 0; i < answer.length(); i += chunkSize) {
            if (properties.getMockDelayMillis() > 0) {
                try {
                    Thread.sleep(properties.getMockDelayMillis());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            onDelta.accept(answer.substring(i, Math.min(answer.length(), i + chunkSize)));
        }
    }

    private String lastUserMessage(List<AiMessage> messages) {
        for (int i = messages.size() - 1; i >= 0; i--) {
            AiMessage message = messages.get(i);
            if ("user".equalsIgnoreCase(message.role())) return message.content();
        }
        return "";
    }

    private String buildAnswer(String prompt) {
        String normalized = prompt == null ? "" : prompt.toLowerCase(Locale.ROOT);
        String type = extractType(prompt);
        if (normalized.contains("团队") || normalized.contains("组织") || normalized.contains("协作")) {
            String template = """
                    当前为本地演示模式，以下是一份结构化的团队分析示例：

                    ## 团队整体倾向
                    团队更偏向%TYPE%相关的工作方式。成员在任务推进、信息处理和沟通节奏上可能呈现不同偏好，整体适合通过清晰目标与明确角色来协作。

                    ## 潜在沟通风险
                    1. 偏好计划的人可能认为临时调整缺少边界，偏好灵活的人则可能觉得流程过重。
                    2. 偏重事实的人容易忽略情绪信号，偏重感受的人可能觉得反馈过于直接。
                    3. 远程或异步协作时，如果没有固定同步机制，信息容易在角色之间断裂。

                    ## 协作建议
                    1. 会议同时保留“先对齐事实”和“再表达感受”两个环节，避免单一视角。
                    2. 明确每项任务的负责人、交付物和截止时间，同时保留一个可调整窗口。
                    3. 建立复盘机制，把冲突转化为流程改进，而不是归因于个人性格。

                    说明：当前使用 mock Provider，配置 AI_API_KEY 并切换 AI_PROVIDER=deepseek 后会生成真实模型报告。
                    """;
            return template.replace("%TYPE%", type == null ? "多类型" : type);
        }
        String typeContext = type == null
                ? "你可以先在系统中完成一次 MBTI 测评，这样我能结合类型给出更具体的分析。"
                : "结合你最近的 " + type + " 倾向，";
        String template = """
                %CONTEXT%建议从三个层面行动：

                1. **发挥优势**：把最自然的思考与决策方式用在核心任务上，并主动向团队说明你的工作偏好。
                2. **补足盲区**：遇到与你偏好不同的意见时，先复述对方的事实与顾虑，再表达自己的判断。
                3. **落到行动**：选择一个未来两周可验证的小目标，例如优化简历项目描述、准备一次技术分享，或建立每周复盘习惯。

                你当前使用的是本地 mock Provider。把 AI_PROVIDER 设为 deepseek 并配置 AI_API_KEY 后，就可以得到基于真实模型的个性化回答。
                """;
        return template.replace("%CONTEXT%", typeContext);
    }

    private String extractType(String prompt) {
        if (prompt == null) return null;
        String upper = prompt.toUpperCase(Locale.ROOT);
        String[] types = {"INTJ", "INTP", "ENTJ", "ENTP", "INFJ", "INFP", "ENFJ", "ENFP",
                "ISTJ", "ISFJ", "ESTJ", "ESFJ", "ISTP", "ISFP", "ESTP", "ESFP"};
        for (String type : types) {
            if (upper.contains(type)) return type;
        }
        return null;
    }
}