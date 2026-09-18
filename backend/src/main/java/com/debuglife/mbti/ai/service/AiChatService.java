package com.debuglife.mbti.ai.service;

import com.debuglife.mbti.ai.config.AiProperties;
import com.debuglife.mbti.ai.dto.ChatMessageView;
import com.debuglife.mbti.ai.dto.ChatResponse;
import com.debuglife.mbti.ai.dto.ChatSessionView;
import com.debuglife.mbti.ai.entity.AiChatMessage;
import com.debuglife.mbti.ai.entity.AiChatSession;
import com.debuglife.mbti.ai.provider.AiMessage;
import com.debuglife.mbti.ai.provider.AiProvider;
import com.debuglife.mbti.ai.provider.AiProviderFactory;
import com.debuglife.mbti.ai.repository.AiChatMessageRepository;
import com.debuglife.mbti.ai.repository.AiChatSessionRepository;
import com.debuglife.mbti.assessment.entity.TestResult;
import com.debuglife.mbti.assessment.repository.TestResultRepository;
import com.debuglife.mbti.auth.entity.User;
import com.debuglife.mbti.auth.repository.UserRepository;
import com.debuglife.mbti.common.exception.BusinessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * AI 咨询用例服务。
 *
 * <p>流式调用的外部模型耗时不可控，因此不能在模型调用期间持有数据库事务。
 * 这里将一次问答拆成三个短事务边界：准备会话、调用 Provider、保存问答。</p>
 */
@Service
public class AiChatService {

    private final AiProperties properties;
    private final AiProviderFactory providerFactory;
    private final AiChatSessionRepository sessionRepository;
    private final AiChatMessageRepository messageRepository;
    private final TestResultRepository resultRepository;
    private final UserRepository userRepository;
    private final TransactionTemplate transactionTemplate;

    public AiChatService(AiProperties properties,
                         AiProviderFactory providerFactory,
                         AiChatSessionRepository sessionRepository,
                         AiChatMessageRepository messageRepository,
                         TestResultRepository resultRepository,
                         UserRepository userRepository,
                         TransactionTemplate transactionTemplate) {
        this.properties = properties;
        this.providerFactory = providerFactory;
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
        this.resultRepository = resultRepository;
        this.userRepository = userRepository;
        this.transactionTemplate = transactionTemplate;
    }

    public ChatResponse chat(String username, Long sessionId, String content) {
        PreparedChat prepared = transactionTemplate.execute(status -> prepare(username, sessionId, content));
        String reply = complete(prepared.provider(), prepared.context());
        return transactionTemplate.execute(status -> saveExchange(prepared.session(), prepared.content(), reply, prepared.provider()));
    }

    /**
     * 流式输出。Provider 回调期间不持有事务，完整响应生成后再落库。
     */
    public ChatResponse streamChat(String username, Long sessionId, String content, Consumer<String> onDelta) {
        PreparedChat prepared = transactionTemplate.execute(status -> prepare(username, sessionId, content));
        StringBuilder reply = new StringBuilder();
        try {
            prepared.provider().stream(prepared.context(), delta -> {
                if (delta == null || delta.isEmpty()) return;
                reply.append(delta);
                onDelta.accept(delta);
            });
        } catch (RuntimeException ex) {
            throw new BusinessException("AI_PROVIDER_ERROR", "AI 服务暂时不可用，请稍后重试");
        }
        if (reply.isEmpty()) {
            throw new BusinessException("AI_EMPTY_RESPONSE", "AI 未返回有效内容");
        }
        return transactionTemplate.execute(status ->
                saveExchange(prepared.session(), prepared.content(), reply.toString(), prepared.provider()));
    }

    @Transactional(readOnly = true)
    public List<ChatSessionView> listSessions(String username) {
        User user = requireUser(username);
        return sessionRepository.findTop50ByUserIdOrderByUpdatedAtDesc(user.getId()).stream()
                .map(session -> toSessionView(session, false))
                .toList();
    }

    @Transactional(readOnly = true)
    public ChatSessionView getSession(String username, Long sessionId) {
        User user = requireUser(username);
        AiChatSession session = requireOwnedSession(sessionId, user.getId());
        return toSessionView(session, true);
    }

    @Transactional
    public void deleteSession(String username, Long sessionId) {
        User user = requireUser(username);
        AiChatSession session = requireOwnedSession(sessionId, user.getId());
        sessionRepository.delete(session);
    }

    private PreparedChat prepare(String username, Long sessionId, String content) {
        User user = requireUser(username);
        String normalizedContent = content == null ? "" : content.trim();
        if (normalizedContent.isBlank()) {
            throw new BusinessException("AI_EMPTY_MESSAGE", "消息内容不能为空");
        }
        if (normalizedContent.length() > 4000) {
            throw new BusinessException("AI_MESSAGE_TOO_LONG", "单条消息不能超过4000字");
        }
        AiProvider provider = providerFactory.resolve();
        AiChatSession session = sessionId == null
                ? createSession(user, provider, normalizedContent)
                : requireOwnedSession(sessionId, user.getId());
        return new PreparedChat(session, normalizedContent, buildContext(session, normalizedContent), provider);
    }

    private String complete(AiProvider provider, List<AiMessage> context) {
        try {
            String reply = provider.complete(context);
            if (reply == null || reply.isBlank()) {
                throw new BusinessException("AI_EMPTY_RESPONSE", "AI 未返回有效内容");
            }
            return reply;
        } catch (BusinessException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new BusinessException("AI_PROVIDER_ERROR", "AI 服务暂时不可用，请稍后重试");
        }
    }

    private ChatResponse saveExchange(AiChatSession session, String userContent,
                                       String assistantContent, AiProvider provider) {
        AiChatMessage userMessage = new AiChatMessage();
        userMessage.setSessionId(session.getId());
        userMessage.setRole(AiChatMessage.MessageRole.USER);
        userMessage.setContent(userContent);
        messageRepository.save(userMessage);

        AiChatMessage assistantMessage = new AiChatMessage();
        assistantMessage.setSessionId(session.getId());
        assistantMessage.setRole(AiChatMessage.MessageRole.ASSISTANT);
        assistantMessage.setContent(assistantContent);
        messageRepository.save(assistantMessage);

        session.setProvider(provider.provider());
        session.setModel(provider.model());
        session.setUpdatedAt(LocalDateTime.now());
        sessionRepository.save(session);
        return new ChatResponse(session.getId(), session.getTitle(), assistantContent,
                provider.provider(), provider.model(), session.getPersonalityType(), LocalDateTime.now());
    }

    private AiChatSession createSession(User user, AiProvider provider, String firstMessage) {
        TestResult latest = latestResult(user.getId());
        AiChatSession session = new AiChatSession();
        session.setUserId(user.getId());
        session.setTitle(titleFrom(firstMessage));
        session.setPersonalityType(latest == null ? null : latest.getTypeCode());
        session.setProvider(provider.provider());
        session.setModel(provider.model());
        return sessionRepository.save(session);
    }

    private List<AiMessage> buildContext(AiChatSession session, String currentMessage) {
        List<AiMessage> context = new ArrayList<>();
        context.add(AiMessage.system(systemPrompt(session)));
        List<AiChatMessage> history = messageRepository
                .findBySessionIdOrderByCreatedAtDesc(
                        session.getId(), PageRequest.of(0, Math.max(properties.getMaxHistoryMessages(), 2)));
        Collections.reverse(history);
        for (AiChatMessage message : history) {
            if (message.getRole() == AiChatMessage.MessageRole.USER) {
                context.add(AiMessage.user(message.getContent()));
            } else if (message.getRole() == AiChatMessage.MessageRole.ASSISTANT) {
                context.add(AiMessage.assistant(message.getContent()));
            }
        }
        context.add(AiMessage.user(currentMessage));
        return context;
    }

    private String systemPrompt(AiChatSession session) {
        String type = session.getPersonalityType();
        String typeContext = type == null || type.isBlank()
                ? "用户尚未提供测评类型。请先询问其工作场景，不要擅自推断类型。"
                : "用户最近一次 MBTI 测评结果是 " + type + "。请将其作为倾向性参考，不要进行确定性人格诊断。";
        return """
                你是一位资深的 MBTI 性格分析师与职业规划师，主要服务 AI 全栈开发求职者。
                请用专业、温暖、客观、简洁的中文回答，优先给出可执行的行动建议。
                %TYPE_CONTEXT%
                回答必须说明 MBTI 是倾向性参考而非医学诊断；涉及职业决策时，提醒用户结合真实经历与市场信息。
                不要输出与用户问题无关的寒暄，不要编造用户未提供的经历。
                """.replace("%TYPE_CONTEXT%", typeContext);
    }

    private AiChatSession requireOwnedSession(Long sessionId, Long userId) {
        return sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new BusinessException("AI_SESSION_NOT_FOUND", "AI 会话不存在或无权访问"));
    }

    private User requireUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
    }

    private TestResult latestResult(Long userId) {
        return resultRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, 1))
                .stream().findFirst().orElse(null);
    }

    private String titleFrom(String message) {
        String title = message.replaceAll("\\s+", " ").trim();
        return title.length() <= 30 ? title : title.substring(0, 30) + "...";
    }

    private ChatSessionView toSessionView(AiChatSession session, boolean includeMessages) {
        List<ChatMessageView> messages = List.of();
        if (includeMessages) {
            messages = messageRepository.findBySessionIdOrderByCreatedAtAsc(session.getId()).stream()
                    .map(message -> new ChatMessageView(message.getId(), message.getRole().name(),
                            message.getContent(), message.getCreatedAt()))
                    .toList();
        }
        return new ChatSessionView(session.getId(), session.getTitle(), session.getPersonalityType(),
                session.getProvider(), session.getModel(), session.getStatus().name(),
                session.getCreatedAt(), session.getUpdatedAt(), messages);
    }

    protected record PreparedChat(AiChatSession session, String content,
                                  List<AiMessage> context, AiProvider provider) {}
}
