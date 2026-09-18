package com.debuglife.mbti.ai.service;

import com.debuglife.mbti.ai.dto.TeamAnalysisRequest;
import com.debuglife.mbti.ai.dto.TeamAnalysisResponse;
import com.debuglife.mbti.ai.entity.AiTeamAnalysis;
import com.debuglife.mbti.ai.provider.AiMessage;
import com.debuglife.mbti.ai.provider.AiProvider;
import com.debuglife.mbti.ai.provider.AiProviderFactory;
import com.debuglife.mbti.ai.repository.AiTeamAnalysisRepository;
import com.debuglife.mbti.assessment.entity.TestResult;
import com.debuglife.mbti.assessment.repository.TestResultRepository;
import com.debuglife.mbti.auth.entity.User;
import com.debuglife.mbti.auth.repository.UserRepository;
import com.debuglife.mbti.common.exception.BusinessException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AiTeamAnalysisService {

    private static final Set<String> VALID_TYPES = Set.of(
            "INTJ", "INTP", "ENTJ", "ENTP", "INFJ", "INFP", "ENFJ", "ENFP",
            "ISTJ", "ISFJ", "ESTJ", "ESFJ", "ISTP", "ISFP", "ESTP", "ESFP");

    private final AiProviderFactory providerFactory;
    private final AiTeamAnalysisRepository analysisRepository;
    private final TestResultRepository resultRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final TransactionTemplate transactionTemplate;

    public AiTeamAnalysisService(AiProviderFactory providerFactory,
                                 AiTeamAnalysisRepository analysisRepository,
                                 TestResultRepository resultRepository,
                                 UserRepository userRepository,
                                 ObjectMapper objectMapper,
                                 TransactionTemplate transactionTemplate) {
        this.providerFactory = providerFactory;
        this.analysisRepository = analysisRepository;
        this.resultRepository = resultRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        this.transactionTemplate = transactionTemplate;
    }

    /**
     * 团队分析用例。
     *
     * <p>模型调用耗时不可控，因此事务只覆盖读校验与最终落库两个短区间，
     * 避免连接被长时间占用。</p>
     */
    public TeamAnalysisResponse analyze(String username, TeamAnalysisRequest request) {
        PreparedAnalysis prepared = transactionTemplate.execute(status -> prepare(username, request));
        AiProvider provider = prepared.provider();
        String report;
        try {
            report = provider.complete(teamPrompt(prepared.counts(), prepared.total(), prepared.focus()));
        } catch (RuntimeException ex) {
            throw new BusinessException("AI_PROVIDER_ERROR", "AI 服务暂时不可用，请稍后重试");
        }
        return transactionTemplate.execute(status -> persist(prepared, report));
    }

    private PreparedAnalysis prepare(String username, TeamAnalysisRequest request) {
        User admin = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
        List<String> types = normalizeTypes(request.getTypeCodes());
        if (types.isEmpty()) {
            throw new BusinessException("AI_TEAM_EMPTY", "请至少提供一条有效的 MBTI 类型");
        }
        Map<String, Long> counts = types.stream()
                .collect(Collectors.groupingBy(type -> type, LinkedHashMap::new, Collectors.counting()));
        String focus = request.getFocus() == null ? "" : request.getFocus().trim();
        return new PreparedAnalysis(admin.getId(), counts, types.size(), focus, providerFactory.resolve());
    }

    private TeamAnalysisResponse persist(PreparedAnalysis prepared, String report) {
        AiTeamAnalysis analysis = new AiTeamAnalysis();
        analysis.setAdminUserId(prepared.adminUserId());
        analysis.setTotalMembers(prepared.total());
        analysis.setTypeCounts(toJson(prepared.counts()));
        analysis.setFocus(prepared.focus().isBlank() ? null : prepared.focus());
        analysis.setResult(report);
        analysis.setProvider(prepared.provider().provider());
        analysis.setModel(prepared.provider().model());
        return toResponse(analysisRepository.save(analysis));
    }

    @Transactional(readOnly = true)
    public List<TeamAnalysisResponse> history() {
        return analysisRepository.findTop20ByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TeamAnalysisResponse latest() {
        return history().stream().findFirst()
                .orElseThrow(() -> new BusinessException("AI_TEAM_ANALYSIS_NOT_FOUND", "暂无团队分析记录"));
    }

    @Transactional(readOnly = true)
    public List<String> availableTypes() {
        return resultRepository.findAll(PageRequest.of(0, 500)).stream()
                .map(TestResult::getTypeCode)
                .filter(this::isValidType)
                .distinct()
                .sorted()
                .toList();
    }

    private List<AiMessage> teamPrompt(Map<String, Long> counts, int total, String focus) {
        String distribution = counts.entrySet().stream()
                .map(entry -> entry.getKey() + " × " + entry.getValue())
                .collect(Collectors.joining("，"));
        String focusLine = focus.isBlank() ? "无特别关注点" : focus;
        return List.of(
                AiMessage.system("""
                        你是一位资深的组织行为学专家和 MBTI 团队分析顾问。
                        请基于给定的团队类型分布，输出专业、客观、可执行的中文团队画像。
                        要求：1.总结团队整体性格倾向和氛围；2.指出潜在沟通冲突点或短板；
                        3.给出3条切实可行的团队管理与协作建议；4.说明 MBTI 仅作为倾向性参考。
                        使用清晰的小标题，避免人格歧视和确定性诊断。
                        """),
                AiMessage.user("团队共 " + total + " 人，MBTI 类型分布：" + distribution
                        + "。特别关注点：" + focusLine + "。请出具团队画像分析报告。"));
    }

    private List<String> normalizeTypes(List<String> requested) {
        if (requested == null || requested.isEmpty()) {
            return List.of();
        }
        return requested.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(value -> value.trim().toUpperCase(Locale.ROOT))
                .filter(this::isValidType)
                .toList();
    }

    private boolean isValidType(String type) {
        return type != null && VALID_TYPES.contains(type.toUpperCase(Locale.ROOT));
    }

    private record PreparedAnalysis(Long adminUserId, Map<String, Long> counts, int total,
                                    String focus, AiProvider provider) {}

    private String toJson(Map<String, Long> counts) {
        try {
            return objectMapper.writeValueAsString(counts);
        } catch (Exception ex) {
            throw new BusinessException("AI_TEAM_SERIALIZE_ERROR", "团队类型分布序列化失败");
        }
    }

    private TeamAnalysisResponse toResponse(AiTeamAnalysis analysis) {
        Map<String, Long> counts;
        try {
            counts = objectMapper.readValue(analysis.getTypeCounts(), new TypeReference<>() {});
        } catch (Exception ex) {
            counts = Map.of();
        }
        return new TeamAnalysisResponse(analysis.getId(), analysis.getTotalMembers(), counts,
                analysis.getFocus(), analysis.getResult(), analysis.getProvider(), analysis.getModel(),
                analysis.getCreatedAt());
    }
}