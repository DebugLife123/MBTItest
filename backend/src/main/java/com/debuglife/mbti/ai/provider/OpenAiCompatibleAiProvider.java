package com.debuglife.mbti.ai.provider;

import com.debuglife.mbti.ai.config.AiProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;

/**
 * 兼容 OpenAI Chat Completions 协议的 Provider。
 * DeepSeek、OpenAI 以及大多数国产兼容网关均可复用此实现。
 */
@Component
public class OpenAiCompatibleAiProvider implements AiProvider {

    private static final Logger log = LoggerFactory.getLogger(OpenAiCompatibleAiProvider.class);

    private final AiProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public OpenAiCompatibleAiProvider(AiProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(Math.max(properties.getTimeoutSeconds(), 5)))
                .build();
    }

    @Override
    public String provider() { return "openai-compatible"; }

    @Override
    public String model() { return properties.getModel(); }

    @Override
    public String complete(List<AiMessage> messages) {
        ObjectNode body = requestBody(messages);
        body.put("stream", false);
        HttpResponse<String> response = send(body, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            log.warn("AI complete request failed: status={}, body={}", response.statusCode(), abbreviate(response.body()));
            throw new IllegalStateException("AI 服务返回异常状态: " + response.statusCode());
        }
        try {
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode content = root.path("choices").path(0).path("message").path("content");
            if (content.isMissingNode() || content.isNull() || content.asText().isBlank()) {
                throw new IllegalStateException("AI 响应缺少 choices[0].message.content");
            }
            return content.asText();
        } catch (Exception ex) {
            throw new IllegalStateException("无法解析 AI 响应", ex);
        }
    }

    @Override
    public void stream(List<AiMessage> messages, Consumer<String> onDelta) {
        ObjectNode body = requestBody(messages);
        body.put("stream", true);
        HttpResponse<InputStream> response = send(body, HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            String errorBody;
            try (InputStream input = response.body()) {
                errorBody = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            } catch (Exception ignored) {
                errorBody = "";
            }
            log.warn("AI stream request failed: status={}, body={}", response.statusCode(), abbreviate(errorBody));
            throw new IllegalStateException("AI 服务返回异常状态: " + response.statusCode());
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank() || !line.startsWith("data:")) continue;
                String payload = line.substring(5).trim();
                if ("[DONE]".equals(payload)) break;
                JsonNode root = objectMapper.readTree(payload);
                JsonNode delta = root.path("choices").path(0).path("delta").path("content");
                if (!delta.isMissingNode() && !delta.isNull() && !delta.asText().isEmpty()) {
                    onDelta.accept(delta.asText());
                }
            }
        } catch (Exception ex) {
            throw new IllegalStateException("AI 流式响应中断", ex);
        }
    }

    private ObjectNode requestBody(List<AiMessage> messages) {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", properties.getModel());
        ArrayNode array = body.putArray("messages");
        for (AiMessage message : messages) {
            ObjectNode item = array.addObject();
            item.put("role", message.role());
            item.put("content", message.content());
        }
        body.put("temperature", 0.7);
        return body;
    }

    private <T> HttpResponse<T> send(ObjectNode body, HttpResponse.BodyHandler<T> handler) {
        try {
            String json = objectMapper.writeValueAsString(body);
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint()))
                    .timeout(Duration.ofSeconds(Math.max(properties.getTimeoutSeconds(), 5)))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8));
            if (properties.hasApiKey()) {
                builder.header("Authorization", "Bearer " + properties.getApiKey());
            }
            return httpClient.send(builder.build(), handler);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("AI 请求被中断", ex);
        } catch (Exception ex) {
            throw new IllegalStateException("无法连接 AI 服务", ex);
        }
    }

    private String endpoint() {
        String base = properties.getBaseUrl() == null || properties.getBaseUrl().isBlank()
                ? "https://api.deepseek.com" : properties.getBaseUrl().trim();
        while (base.endsWith("/")) base = base.substring(0, base.length() - 1);
        return base.endsWith("/chat/completions") ? base : base + "/chat/completions";
    }

    private String abbreviate(String value) {
        if (value == null) return "";
        return value.length() > 500 ? value.substring(0, 500) + "..." : value;
    }
}