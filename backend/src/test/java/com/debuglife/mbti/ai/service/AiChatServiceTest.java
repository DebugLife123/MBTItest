package com.debuglife.mbti.ai.service;

import com.debuglife.mbti.ai.config.AiProperties;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiChatServiceTest {

    @Mock private AiProviderFactory providerFactory;
    @Mock private AiChatSessionRepository sessionRepository;
    @Mock private AiChatMessageRepository messageRepository;
    @Mock private TestResultRepository resultRepository;
    @Mock private UserRepository userRepository;
    @Mock private TransactionTemplate transactionTemplate;
    @Mock private AiProvider provider;

    private AiProperties properties;
    private AiChatService service;

    @BeforeEach
    void setUp() {
        properties = new AiProperties();
        properties.setMaxHistoryMessages(20);
        service = new AiChatService(properties, providerFactory, sessionRepository,
                messageRepository, resultRepository, userRepository, transactionTemplate);
        lenient().when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        });
    }

    @Test
    void chatCreatesSessionAndPersistsExchange() {
        stubUser();
        stubProvider();
        when(provider.complete(anyList())).thenReturn("mock-reply");
        when(resultRepository.findByUserIdOrderByCreatedAtDesc(eq(7L), any(PageRequest.class)))
                .thenReturn(org.springframework.data.domain.Page.empty());
        when(sessionRepository.save(any(AiChatSession.class))).thenAnswer(inv -> {
            AiChatSession session = inv.getArgument(0);
            if (session.getId() == null) session.setId(10L);
            return session;
        });
        when(messageRepository.findBySessionIdOrderByCreatedAtDesc(eq(10L), any(PageRequest.class)))
                .thenReturn(Collections.emptyList());

        ChatResponse response = service.chat("alice", null, "  帮我规划 AI 全栈路线  ");

        assertEquals(10L, response.sessionId());
        assertEquals("mock-reply", response.reply());
        assertEquals("帮我规划 AI 全栈路线", response.title());
        verify(messageRepository, times(2)).save(any(AiChatMessage.class));
        verify(provider).complete(anyList());
    }

    @Test
    void chatContinuesExistingSessionAndIncludesHistory() {
        stubUser();
        stubProvider();
        when(provider.complete(anyList())).thenReturn("mock-reply");
        AiChatSession session = session(12L, 7L);
        when(sessionRepository.findByIdAndUserId(12L, 7L)).thenReturn(Optional.of(session));
        AiChatMessage previous = new AiChatMessage();
        previous.setSessionId(12L);
        previous.setRole(AiChatMessage.MessageRole.USER);
        previous.setContent("上一轮问题");
        when(messageRepository.findBySessionIdOrderByCreatedAtDesc(eq(12L), any(PageRequest.class)))
                .thenReturn(List.of(previous));

        ChatResponse response = service.chat("alice", 12L, "继续帮我分析");

        assertEquals(12L, response.sessionId());
        verify(provider).complete(argThat(context -> context.stream()
                .anyMatch(message -> "上一轮问题".equals(message.content()))));
        verify(sessionRepository).save(session);
    }

    @Test
    void streamChatEmitsDeltasAndPersistsCompleteReply() {
        stubUser();
        stubProvider();
        when(resultRepository.findByUserIdOrderByCreatedAtDesc(eq(7L), any(PageRequest.class)))
                .thenReturn(org.springframework.data.domain.Page.empty());
        when(sessionRepository.save(any(AiChatSession.class))).thenAnswer(inv -> {
            AiChatSession session = inv.getArgument(0);
            session.setId(20L);
            return session;
        });
        when(messageRepository.findBySessionIdOrderByCreatedAtDesc(eq(20L), any(PageRequest.class)))
                .thenReturn(Collections.emptyList());
        doAnswer(inv -> {
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<String> consumer = inv.getArgument(1);
            consumer.accept("mock-");
            consumer.accept("reply");
            return null;
        }).when(provider).stream(anyList(), any());

        List<String> deltas = new ArrayList<>();
        ChatResponse response = service.streamChat("alice", null, "流式测试", deltas::add);

        assertEquals(List.of("mock-", "reply"), deltas);
        assertEquals("mock-reply", response.reply());
        verify(messageRepository, times(2)).save(any(AiChatMessage.class));
    }

    @Test
    void streamChatRejectsEmptyProviderResponse() {
        stubUser();
        stubProvider();
        when(resultRepository.findByUserIdOrderByCreatedAtDesc(eq(7L), any(PageRequest.class)))
                .thenReturn(org.springframework.data.domain.Page.empty());
        when(sessionRepository.save(any(AiChatSession.class))).thenAnswer(inv -> {
            AiChatSession session = inv.getArgument(0);
            session.setId(30L);
            return session;
        });
        doNothing().when(provider).stream(anyList(), any());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.streamChat("alice", null, "空响应", delta -> {}));

        assertEquals("AI_EMPTY_RESPONSE", ex.getCode());
        verify(messageRepository, never()).save(any(AiChatMessage.class));
    }

    @Test
    void sessionAccessIsScopedToCurrentUser() {
        stubUser();
        when(sessionRepository.findByIdAndUserId(99L, 7L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.getSession("alice", 99L));

        assertEquals("AI_SESSION_NOT_FOUND", ex.getCode());
    }

    @Test
    void blankMessageIsRejectedBeforeCreatingSession() {
        stubUser();

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.chat("alice", null, "   "));

        assertEquals("AI_EMPTY_MESSAGE", ex.getCode());
        verify(sessionRepository, never()).save(any(AiChatSession.class));
    }

    @Test
    void overlongMessageIsRejected() {
        stubUser();

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.chat("alice", null, "x".repeat(4001)));

        assertEquals("AI_MESSAGE_TOO_LONG", ex.getCode());
    }

    @Test
    void deleteSessionUsesOwnershipCheck() {
        stubUser();
        AiChatSession session = session(5L, 7L);
        when(sessionRepository.findByIdAndUserId(5L, 7L)).thenReturn(Optional.of(session));

        service.deleteSession("alice", 5L);

        verify(sessionRepository).delete(session);
    }

    @Test
    void listSessionsOmitsMessageBodiesForLightweightPayload() {
        stubUser();
        when(sessionRepository.findTop50ByUserIdOrderByUpdatedAtDesc(7L))
                .thenReturn(List.of(session(1L, 7L)));

        List<ChatSessionView> sessions = service.listSessions("alice");

        assertEquals(1, sessions.size());
        assertTrue(sessions.get(0).messages().isEmpty());
    }

    private void stubUser() {
        User user = new User();
        user.setId(7L);
        user.setUsername("alice");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
    }

    private void stubProvider() {
        when(providerFactory.resolve()).thenReturn(provider);
        when(provider.provider()).thenReturn("mock");
        when(provider.model()).thenReturn("mock-mbti-coach");
    }

    private AiChatSession session(Long id, Long userId) {
        AiChatSession session = new AiChatSession();
        session.setId(id);
        session.setUserId(userId);
        session.setTitle("测试会话");
        session.setProvider("mock");
        session.setModel("mock-mbti-coach");
        session.setStatus(AiChatSession.SessionStatus.ACTIVE);
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        return session;
    }
}
