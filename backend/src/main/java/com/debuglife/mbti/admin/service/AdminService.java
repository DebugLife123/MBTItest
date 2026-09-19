package com.debuglife.mbti.admin.service;

import com.debuglife.mbti.admin.dto.*;
import com.debuglife.mbti.assessment.entity.MbtiPersonality;
import com.debuglife.mbti.assessment.entity.MbtiQuestion;
import com.debuglife.mbti.assessment.entity.TestAttempt;
import com.debuglife.mbti.assessment.entity.TestResult;
import com.debuglife.mbti.assessment.repository.MbtiPersonalityRepository;
import com.debuglife.mbti.assessment.repository.MbtiQuestionRepository;
import com.debuglife.mbti.assessment.repository.TestAttemptRepository;
import com.debuglife.mbti.assessment.repository.TestResultRepository;
import com.debuglife.mbti.auth.entity.User;
import com.debuglife.mbti.auth.repository.UserRepository;
import com.debuglife.mbti.common.config.CacheConfig;
import com.debuglife.mbti.common.exception.BusinessException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AdminService {
    private static final int ACTIVE_WINDOW_DAYS = 30;
    private static final Map<String, Long> DIMENSION_BY_LETTER = Map.of(
            "E", 1L, "I", 2L, "S", 3L, "N", 4L,
            "T", 5L, "F", 6L, "J", 7L, "P", 8L);

    private final UserRepository userRepository;
    private final TestAttemptRepository attemptRepository;
    private final TestResultRepository resultRepository;
    private final MbtiQuestionRepository questionRepository;
    private final MbtiPersonalityRepository personalityRepository;

    public AdminService(UserRepository userRepository,
                        TestAttemptRepository attemptRepository,
                        TestResultRepository resultRepository,
                        MbtiQuestionRepository questionRepository,
                        MbtiPersonalityRepository personalityRepository) {
        this.userRepository = userRepository;
        this.attemptRepository = attemptRepository;
        this.resultRepository = resultRepository;
        this.questionRepository = questionRepository;
        this.personalityRepository = personalityRepository;
    }

    @Transactional(readOnly = true)
    public Page<UserListItem> getUserList(String keyword, Pageable pageable) {
        Page<User> userPage;
        if (keyword == null || keyword.isBlank()) {
            userPage = userRepository.findAll(pageable);
        } else {
            String trimmed = keyword.trim();
            userPage = userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                    trimmed, trimmed, pageable);
        }

        List<Long> userIds = userPage.getContent().stream().map(User::getId).toList();
        Map<Long, Long> countByUser = userIds.isEmpty()
                ? Map.of()
                : attemptRepository.countByUserIds(userIds).stream().collect(Collectors.toMap(
                        TestAttemptRepository.AttemptCountProjection::getUserId,
                        TestAttemptRepository.AttemptCountProjection::getAttemptCount));
        List<UserListItem> items = userPage.getContent().stream()
                .map(user -> toListItem(user, countByUser.getOrDefault(user.getId(), 0L)))
                .toList();
        return new PageImpl<>(items, pageable, userPage.getTotalElements());
    }

    private UserListItem toListItem(User user, long attemptCount) {
        UserListItem item = new UserListItem();
        item.setId(user.getId());
        item.setUsername(user.getUsername());
        item.setEmail(user.getEmail());
        item.setNickname(user.getNickname());
        item.setRole(user.getRole() == null ? null : user.getRole().name());
        item.setEnabled(user.getEnabled());
        item.setCreatedAt(user.getCreatedAt());
        item.setLastLoginAt(user.getLastLoginAt());
        item.setAttemptCount(Math.toIntExact(attemptCount));
        return item;
    }

    @Transactional
    public void updateUserStatus(Long userId, Boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        if (user.getRole() == User.UserRole.ADMIN) {
            throw new BusinessException("不允许修改管理员账号状态");
        }
        user.setEnabled(enabled);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        if (user.getRole() == User.UserRole.ADMIN) {
            throw new BusinessException("不允许删除管理员账号");
        }
        List<TestAttempt> attempts = attemptRepository.findByUserId(userId);
        for (TestAttempt attempt : attempts) {
            resultRepository.deleteByAttemptId(attempt.getId());
        }
        attemptRepository.deleteAll(attempts);
        userRepository.delete(user);
    }

    @Cacheable(CacheConfig.CACHE_ADMIN_STATISTICS)
    @Transactional(readOnly = true)
    public UserStatistics getStatistics() {
        long totalUsers = userRepository.count();
        long totalAttempts = attemptRepository.count();
        long completedAttempts = attemptRepository.countByStatus(TestAttempt.AttemptStatus.COMPLETED);
        long activeUsers = userRepository.countByLastLoginAtAfter(LocalDateTime.now().minusDays(ACTIVE_WINDOW_DAYS));
        double completionRate = totalAttempts == 0 ? 0.0
                : Math.round((double) completedAttempts / totalAttempts * 1000.0) / 10.0;
        return new UserStatistics(totalUsers, activeUsers, totalAttempts, completedAttempts, completionRate);
    }

    @Cacheable(CacheConfig.CACHE_ADMIN_DISTRIBUTION)
    @Transactional(readOnly = true)
    public List<PersonalityDistribution> getPersonalityDistribution() {
        return buildDistribution(resultRepository.findAll());
    }

    @Transactional(readOnly = true)
    public List<CompletionRatePoint> getCompletionRateTrend(int days) {
        int safeDays = Math.min(Math.max(days, 1), 90);
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(safeDays - 1L);
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        Map<LocalDate, List<TestAttempt>> byDate = attemptRepository.findByStartedAtBetween(start, end).stream()
                .collect(Collectors.groupingBy(a -> a.getStartedAt().toLocalDate()));
        List<CompletionRatePoint> points = new ArrayList<>();
        for (int i = 0; i < safeDays; i++) {
            LocalDate date = startDate.plusDays(i);
            List<TestAttempt> attempts = byDate.getOrDefault(date, List.of());
            long total = attempts.size();
            long completed = attempts.stream()
                    .filter(a -> a.getStatus() == TestAttempt.AttemptStatus.COMPLETED).count();
            double rate = total == 0 ? 0.0 : Math.round((double) completed / total * 1000.0) / 10.0;
            points.add(new CompletionRatePoint(date, total, completed, rate));
        }
        return points;
    }

    private List<PersonalityDistribution> buildDistribution(List<TestResult> results) {
        Map<String, Long> countMap = new LinkedHashMap<>();
        for (TestResult result : results) {
            countMap.merge(result.getTypeCode(), 1L, Long::sum);
        }
        long total = results.size();
        List<PersonalityDistribution> distributions = new ArrayList<>();
        countMap.forEach((type, count) -> {
            double percentage = total == 0 ? 0.0
                    : Math.round((double) count / total * 1000.0) / 10.0;
            distributions.add(new PersonalityDistribution(type, count, percentage));
        });
        distributions.sort((a, b) -> Long.compare(b.getCount(), a.getCount()));
        return distributions;
    }

    @Transactional(readOnly = true)
    public List<QuestionView> getQuestions() {
        return questionRepository.findAllByOrderBySortOrderAsc().stream()
                .map(this::toQuestionView)
                .collect(Collectors.toList());
    }

    private QuestionView toQuestionView(MbtiQuestion question) {
        QuestionView view = new QuestionView();
        view.setId(question.getId());
        view.setDimensionId(question.getDimensionId());
        view.setContent(question.getContent());
        view.setOptionA(question.getOptionA());
        view.setOptionB(question.getOptionB());
        view.setAnswerType(question.getAnswerType());
        view.setSortOrder(question.getSortOrder());
        return view;
    }

    @Transactional
    public QuestionView createQuestion(QuestionRequest request) {
        MbtiQuestion question = new MbtiQuestion();
        applyQuestionRequest(question, request);
        question.setCreatedAt(LocalDateTime.now());
        question.setUpdatedAt(LocalDateTime.now());
        return toQuestionView(questionRepository.save(question));
    }

    @Transactional
    public QuestionView updateQuestion(Long id, QuestionRequest request) {
        MbtiQuestion question = questionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("题目不存在"));
        applyQuestionRequest(question, request);
        question.setUpdatedAt(LocalDateTime.now());
        return toQuestionView(questionRepository.save(question));
    }

    private void applyQuestionRequest(MbtiQuestion question, QuestionRequest request) {
        String answerType = request.getAnswerType() == null
                ? null : request.getAnswerType().trim().toUpperCase();
        if (answerType == null || !DIMENSION_BY_LETTER.containsKey(answerType)) {
            throw new BusinessException("题目选项A维度必须是 E/I/S/N/T/F/J/P 之一");
        }
        Long dimensionId = request.getDimensionId() != null
                ? request.getDimensionId() : DIMENSION_BY_LETTER.get(answerType);
        if (!DIMENSION_BY_LETTER.containsValue(dimensionId)) {
            throw new BusinessException("所属维度取值必须在 1-8 之间");
        }
        question.setDimensionId(dimensionId);
        question.setContent(request.getContent());
        question.setOptionA(request.getOptionA());
        question.setOptionB(request.getOptionB());
        question.setAnswerType(answerType);
        question.setSortOrder(request.getSortOrder() == null
                ? (int) (questionRepository.count() + 1) : request.getSortOrder());
    }

    @Transactional
    public void deleteQuestion(Long id) {
        if (!questionRepository.existsById(id)) {
            throw new BusinessException("题目不存在");
        }
        questionRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<MbtiPersonality> getPersonalities() {
        return personalityRepository.findAll();
    }

    @Transactional
    public MbtiPersonality updatePersonality(String typeCode, PersonalityRequest request) {
        MbtiPersonality personality = personalityRepository
                .findByTypeCode(typeCode.toUpperCase())
                .orElseThrow(() -> new BusinessException("性格类型不存在: " + typeCode));
        if (request.getTypeName() != null) personality.setTypeName(request.getTypeName());
        if (request.getDescription() != null) personality.setDescription(request.getDescription());
        if (request.getStrengths() != null) personality.setStrengths(request.getStrengths());
        if (request.getWeaknesses() != null) personality.setWeaknesses(request.getWeaknesses());
        if (request.getCareerSuggestions() != null) personality.setCareerSuggestions(request.getCareerSuggestions());
        return personalityRepository.save(personality);
    }
}
