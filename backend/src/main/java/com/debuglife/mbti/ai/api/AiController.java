package com.debuglife.mbti.ai.api;

import com.debuglife.mbti.ai.config.AiProperties;
import com.debuglife.mbti.ai.dto.AiStatusResponse;
import com.debuglife.mbti.ai.dto.ChatRequest;
import com.debuglife.mbti.ai.dto.ChatResponse;
import com.debuglife.mbti.ai.dto.ChatSessionView;
import com.debuglife.mbti.ai.provider.AiProviderFactory;
import com.debuglife.mbti.ai.service.AiChatService;
import com.debuglife.mbti.common.api.ApiResponse;
import com.debuglife.mbti.common.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.core.task.TaskExecutor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/ai")
@Tag(name = "AI 咨询服务", description = "基于 MBTI 测评结果的个性化职业与成长咨询")
public class AiController {

    private static final long SSE_TIMEOUT_MILLIS = 300_000L;

    private final AiChatService chatService;
    private final AiProviderFactory providerFactory;
    private final AiProperties properties;
    private final TaskExecutor applicationTaskExecutor;

    public AiController(AiChatService chatService,
                        AiProviderFactory providerFactory,
                        AiProperties properties,
                        TaskExecutor applicationTaskExecutor) {
        this.chatService = chatService;
        this.providerFactory = providerFactory;
        this.properties = properties;
        this.applicationTaskExecutor = applicationTaskExecutor;
    }

    @GetMapping("/status")
    @Operation(summary = "获取 AI 能力状态", description = "返回当前是否启用、实际 Provider 与模型；未配置密钥时明确标识 mock 模式")
    public ApiResponse<AiStatusResponse> status() {
        if (!properties.isEnabled()) {
            return ApiResponse.success(new AiStatusResponse(false, providerFactory.configuredProvider(),
                    "disabled", properties.getModel(), false, "AI 能力已关闭"));
        }
        var provider = providerFactory.resolve();
        boolean mock = "mock".equals(provider.provider());
        return ApiResponse.success(new AiStatusResponse(true, providerFactory.configuredProvider(),
                provider.provider(), provider.model(), mock,
                mock ? "当前为本地演示模式，配置 AI_API_KEY 后可切换真实模型"
                        : "AI 回答仅作为倾向性参考，不构成医学诊断或职业承诺"));
    }

    @PostMapping("/chat")
    @Operation(summary = "发送 AI 咨询消息")
    public ApiResponse<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        return ApiResponse.success(chatService.chat(currentUsername(), request.getSessionId(), request.getMessage()));
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "流式发送 AI 咨询消息", description = "使用 SSE 推送 delta、done、error 事件")
    public SseEmitter stream(@Valid @RequestBody ChatRequest request) {
        String username = currentUsername();
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MILLIS);
        applicationTaskExecutor.execute(() -> {
            try {
                ChatResponse response = chatService.streamChat(username, request.getSessionId(), request.getMessage(),
                        delta -> send(emitter, "delta", delta));
                if (send(emitter, "done", response)) {
                    emitter.complete();
                }
            } catch (BusinessException ex) {
                if (send(emitter, "error", new ErrorPayload(ex.getCode(), ex.getMessage()))) {
                    emitter.complete();
                }
            } catch (Exception ex) {
                if (send(emitter, "error",
                        new ErrorPayload("AI_INTERNAL_ERROR", "AI 服务暂时不可用，请稍后重试"))) {
                    emitter.complete();
                }
            }
        });
        return emitter;
    }

    @GetMapping("/sessions")
    @Operation(summary = "获取当前用户 AI 会话列表")
    public ApiResponse<List<ChatSessionView>> sessions() {
        return ApiResponse.success(chatService.listSessions(currentUsername()));
    }

    @GetMapping("/sessions/{id}")
    @Operation(summary = "获取 AI 会话详情")
    public ApiResponse<ChatSessionView> session(@PathVariable Long id) {
        return ApiResponse.success(chatService.getSession(currentUsername(), id));
    }

    @DeleteMapping("/sessions/{id}")
    @Operation(summary = "删除 AI 会话")
    public ApiResponse<Void> deleteSession(@PathVariable Long id) {
        chatService.deleteSession(currentUsername(), id);
        return ApiResponse.success(null);
    }

    private boolean send(SseEmitter emitter, String event, Object data) {
        try {
            emitter.send(SseEmitter.event().name(event).data(data, MediaType.APPLICATION_JSON));
            return true;
        } catch (IOException ex) {
            // 客户端已断开：终止 SSE，避免继续重复推送。业务回复仍由服务层决定是否落库。
            emitter.completeWithError(ex);
            return false;
        }
    }

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            throw new BusinessException("UNAUTHORIZED", "请先登录");
        }
        return authentication.getName();
    }

    public record ErrorPayload(String code, String message) {}
}
