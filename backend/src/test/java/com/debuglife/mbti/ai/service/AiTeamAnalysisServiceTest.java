package com.debuglife.mbti.ai.service;

import com.debuglife.mbti.ai.dto.TeamAnalysisRequest;
import com.debuglife.mbti.ai.dto.TeamAnalysisResponse;
import com.debuglife.mbti.ai.entity.AiTeamAnalysis;
import com.debuglife.mbti.ai.provider.AiProvider;
import com.debuglife.mbti.ai.provider.AiProviderFactory;
import com.debuglife.mbti.ai.repository.AiTeamAnalysisRepository;
import com.debuglife.mbti.assessment.repository.TestResultRepository;
import com.debuglife.mbti.auth.entity.User;
import com.debuglife.mbti.auth.repository.UserRepository;
import com.debuglife.mbti.common.exception.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiTeamAnalysisServiceTest {

    @Mock private AiProviderFactory providerFactory;
    @Mock private AiTeamAnalysisRepository analysisRepository;
    @Mock private TestResultRepository resultRepository;
    @Mock private UserRepository userRepository;
    @Mock private TransactionTemplate transactionTemplate;
    @Mock private AiProvider provider;

    private AiTeamAnalysisService service;

    @BeforeEach
    void setUp() {
        service = new AiTeamAnalysisService(providerFactory, analysisRepository,
                resultRepository, userRepository, new ObjectMapper(), transactionTemplate);
        lenient().when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        });
    }

    @Test
    void analyzeNormalizesTypesDeduplicatesCountsAndPersistsReport() {
        User admin = new User();
        admin.setId(1L);
        admin.setUsername("admin");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(providerFactory.resolve()).thenReturn(provider);
        when(provider.provider()).thenReturn("mock");
        when(provider.model()).thenReturn("mock-mbti-coach");
        when(provider.complete(anyList())).thenReturn("团队分析报告");
        when(analysisRepository.save(any(AiTeamAnalysis.class))).thenAnswer(inv -> {
            AiTeamAnalysis analysis = inv.getArgument(0);
            analysis.setId(100L);
            analysis.setCreatedAt(LocalDateTime.now());
            return analysis;
        });
        TeamAnalysisRequest request = new TeamAnalysisRequest();
        request.setTypeCodes(List.of(" intj ", "INTJ", "enfp", "INVALID", ""));
        request.setFocus("  提高协作效率  ");

        TeamAnalysisResponse response = service.analyze("admin", request);

        assertEquals(100L, response.id());
        assertEquals(3, response.totalMembers());
        assertEquals(2L, response.typeCounts().get("INTJ"));
        assertEquals(1L, response.typeCounts().get("ENFP"));
        assertEquals("提高协作效率", response.focus());
        assertEquals("团队分析报告", response.report());
        verify(provider).complete(argThat(messages -> messages.stream()
                .anyMatch(message -> message.content().contains("INTJ × 2"))));
    }

    @Test
    void analyzeRejectsEmptyOrInvalidTypeList() {
        User admin = new User();
        admin.setId(1L);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        TeamAnalysisRequest request = new TeamAnalysisRequest();
        request.setTypeCodes(List.of("", "not-a-type"));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.analyze("admin", request));

        assertEquals("AI_TEAM_EMPTY", ex.getCode());
        verify(providerFactory, never()).resolve();
    }

    @Test
    void analyzeWrapsProviderFailureAsBusinessError() {
        User admin = new User();
        admin.setId(1L);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(providerFactory.resolve()).thenReturn(provider);
        when(provider.complete(anyList())).thenThrow(new IllegalStateException("upstream down"));
        TeamAnalysisRequest request = new TeamAnalysisRequest();
        request.setTypeCodes(List.of("INTJ"));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.analyze("admin", request));

        assertEquals("AI_PROVIDER_ERROR", ex.getCode());
        verify(analysisRepository, never()).save(any());
    }

    @Test
    void latestReturnsMostRecentRecord() {
        AiTeamAnalysis analysis = new AiTeamAnalysis();
        analysis.setId(9L);
        analysis.setTotalMembers(2);
        analysis.setTypeCounts("{\"INTJ\":2}");
        analysis.setResult("报告");
        analysis.setProvider("mock");
        analysis.setModel("mock-mbti-coach");
        analysis.setCreatedAt(LocalDateTime.now());
        when(analysisRepository.findTop20ByOrderByCreatedAtDesc()).thenReturn(List.of(analysis));

        TeamAnalysisResponse response = service.latest();

        assertEquals(9L, response.id());
        assertEquals(2L, response.typeCounts().get("INTJ"));
    }

    @Test
    void latestRejectsWhenNoHistoryExists() {
        when(analysisRepository.findTop20ByOrderByCreatedAtDesc()).thenReturn(List.of());

        BusinessException ex = assertThrows(BusinessException.class, service::latest);

        assertEquals("AI_TEAM_ANALYSIS_NOT_FOUND", ex.getCode());
    }
}
