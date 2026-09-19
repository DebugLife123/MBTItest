package com.debuglife.mbti.ai.provider;

import com.debuglife.mbti.ai.config.AiProperties;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Consumer;

/** Spring AI ChatClient adapter for OpenAI-compatible models. */
@Component
@ConditionalOnProperty(prefix = "app.ai", name = "provider", havingValue = "spring-ai")
public class SpringAiProvider implements AiProvider {

    private final ChatClient chatClient;
    private final AiProperties properties;

    public SpringAiProvider(ChatClient.Builder chatClientBuilder, AiProperties properties) {
        this.chatClient = chatClientBuilder.build();
        this.properties = properties;
    }

    @Override
    public String provider() {
        return "spring-ai";
    }

    @Override
    public String model() {
        return properties.getModel();
    }

    @Override
    public String complete(List<AiMessage> messages) {
        return request(messages).call().content();
    }

    @Override
    public void stream(List<AiMessage> messages, Consumer<String> onDelta) {
        request(messages).stream().content().toStream().forEach(onDelta);
    }

    private ChatClient.ChatClientRequestSpec request(List<AiMessage> messages) {
        return chatClient.prompt().messages(toSpringMessages(messages));
    }

    private List<Message> toSpringMessages(List<AiMessage> messages) {
        return messages.stream().map(message -> (Message) switch (message.role()) {
            case "system" -> new SystemMessage(message.content());
            case "assistant" -> new AssistantMessage(message.content());
            default -> new UserMessage(message.content());
        }).toList();
    }
}