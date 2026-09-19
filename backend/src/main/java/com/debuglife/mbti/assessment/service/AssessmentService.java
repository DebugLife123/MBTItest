package com.debuglife.mbti.assessment.service;

import com.debuglife.mbti.assessment.dto.SubmitAnswersRequest;
import com.debuglife.mbti.assessment.entity.MbtiPersonality;
import com.debuglife.mbti.assessment.entity.MbtiQuestion;
import com.debuglife.mbti.assessment.entity.TestAnswer;
import com.debuglife.mbti.assessment.entity.TestAttempt;
import com.debuglife.mbti.assessment.entity.TestResult;
import com.debuglife.mbti.assessment.repository.MbtiPersonalityRepository;
import com.debuglife.mbti.assessment.repository.MbtiQuestionRepository;
import com.debuglife.mbti.assessment.repository.TestAnswerRepository;
import com.debuglife.mbti.assessment.repository.TestAttemptRepository;
import com.debuglife.mbti.assessment.repository.TestResultRepository;
import com.debuglife.mbti.common.config.CacheConfig;
import com.debuglife.mbti.common.exception.BusinessException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AssessmentService {

    private final MbtiQuestionRepository questionRepository;
    private final TestAttemptRepository attemptRepository;
    private final TestAnswerRepository answerRepository;
    private final TestResultRepository resultRepository;
    private final com.debuglife.mbti.common.notification.NotificationService notificationService;
    private final com.debuglife.mbti.auth.repository.UserRepository userRepository;
    private final MbtiPersonalityRepository personalityRepository;

    public AssessmentService(MbtiQuestionRepository questionRepository,
                             TestAttemptRepository attemptRepository,
                             TestAnswerRepository answerRepository,
                             TestResultRepository resultRepository,
                             MbtiPersonalityRepository personalityRepository,
            com.debuglife.mbti.common.notification.NotificationService notificationService,
            com.debuglife.mbti.auth.repository.UserRepository userRepository) {
        this.questionRepository = questionRepository;
        this.attemptRepository = attemptRepository;
        this.answerRepository = answerRepository;
        this.resultRepository = resultRepository;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
        this.personalityRepository = personalityRepository;
    }

    @Transactional
    public TestAttempt startAttempt(Long userId) {
        TestAttempt attempt = new TestAttempt();
        attempt.setUserId(userId);
        attempt.setStatus(TestAttempt.AttemptStatus.IN_PROGRESS);
        attempt.setStartedAt(LocalDateTime.now());
        return attemptRepository.save(attempt);
    }

    @Transactional(readOnly = true)
    public List<MbtiQuestion> getQuestions(Long attemptId, Long userId) {
        requireOwnedInProgressAttempt(attemptId, userId);
        return questionRepository.findAllByOrderBySortOrderAsc();
    }

    @Transactional
    public void submitAnswers(Long attemptId, Long userId, SubmitAnswersRequest request) {
        requireOwnedInProgressAttempt(attemptId, userId);

        List<MbtiQuestion> questions = questionRepository.findAllByOrderBySortOrderAsc();
        if (questions.isEmpty()) {
            throw new BusinessException("题库为空，无法提交测评");
        }

        Map<Long, MbtiQuestion> questionMap = questions.stream()
                .collect(Collectors.toMap(MbtiQuestion::getId, q -> q));
        if (request == null || request.getAnswers() == null || request.getAnswers().isEmpty()) {
            throw new BusinessException("答案不能为空");
        }
        if (request.getAnswers().size() != questions.size()) {
            throw new BusinessException("请完成全部题目后再提交");
        }
        long distinctQuestionCount = request.getAnswers().stream()
                .map(SubmitAnswersRequest.AnswerItem::getQuestionId)
                .distinct()
                .count();
        if (distinctQuestionCount != questions.size()) {
            throw new BusinessException("答案存在重复或缺失题目");
        }

        answerRepository.deleteByAttemptId(attemptId);
        for (SubmitAnswersRequest.AnswerItem item : request.getAnswers()) {
            MbtiQuestion question = questionMap.get(item.getQuestionId());
            if (question == null) {
                throw new BusinessException("答案中存在无效题目");
            }
            String answer = item.getAnswer() == null ? null : item.getAnswer().trim().toUpperCase();
            if (!"A".equals(answer) && !"B".equals(answer)) {
                throw new BusinessException("答案只能选择 A 或 B");
            }

            TestAnswer entity = new TestAnswer();
            entity.setAttemptId(attemptId);
            entity.setQuestionId(item.getQuestionId());
            entity.setAnswer(answer);
            answerRepository.save(entity);
        }
    }

    @CacheEvict(cacheNames = {CacheConfig.CACHE_ADMIN_STATISTICS, CacheConfig.CACHE_ADMIN_DISTRIBUTION}, allEntries = true)
    @Transactional
    public TestResult completeAttempt(Long attemptId, Long userId) {
        TestAttempt attempt = requireOwnedInProgressAttempt(attemptId, userId);
        return resultRepository.findByAttemptId(attemptId).orElseGet(() -> doComplete(attempt));
    }

    private TestResult doComplete(TestAttempt attempt) {
        Long attemptId = attempt.getId();
        List<TestAnswer> answers = answerRepository.findByAttemptId(attemptId);
        if (answers.isEmpty()) {
            throw new BusinessException("未找到答案记录");
        }

        List<MbtiQuestion> allQuestions = questionRepository.findAllByOrderBySortOrderAsc();
        if (answers.size() != allQuestions.size()) {
            throw new BusinessException("请完成全部题目后再提交");
        }

        Map<Long, MbtiQuestion> questionMap = allQuestions.stream()
                .collect(Collectors.toMap(MbtiQuestion::getId, q -> q));

        int eScore = 0, iScore = 0, sScore = 0, nScore = 0;
        int tScore = 0, fScore = 0, jScore = 0, pScore = 0;

        for (TestAnswer answer : answers) {
            MbtiQuestion question = questionMap.get(answer.getQuestionId());
            if (question == null) {
                throw new BusinessException("答案中存在无效题目");
            }
            String selected = answer.getAnswer();
            if (!"A".equals(selected) && !"B".equals(selected)) {
                throw new BusinessException("答案只能选择 A 或 B");
            }

            String selectedLetter = "A".equals(selected)
                    ? question.getAnswerType()
                    : oppositeLetter(question.getAnswerType());
            switch (selectedLetter) {
                case "E" -> eScore++;
                case "I" -> iScore++;
                case "S" -> sScore++;
                case "N" -> nScore++;
                case "T" -> tScore++;
                case "F" -> fScore++;
                case "J" -> jScore++;
                case "P" -> pScore++;
                default -> throw new BusinessException("题目选项维度配置错误");
            }
        }

        String typeCode =
                (eScore >= iScore ? "E" : "I") +
                (sScore >= nScore ? "S" : "N") +
                (tScore >= fScore ? "T" : "F") +
                (jScore >= pScore ? "J" : "P");

        MbtiPersonality personality = personalityRepository.findByTypeCode(typeCode)
                .orElseThrow(() -> new BusinessException("性格类型不存在: " + typeCode));

        attempt.setStatus(TestAttempt.AttemptStatus.COMPLETED);
        attempt.setCompletedAt(LocalDateTime.now());
        attempt.setResultType(typeCode);
        attemptRepository.save(attempt);

        TestResult result = new TestResult();
        result.setAttemptId(attemptId);
        result.setUserId(attempt.getUserId());
        result.setPersonalityId(personality.getId());
        result.setTypeCode(typeCode);
        result.setEScore(eScore);
        result.setIScore(iScore);
        result.setSScore(sScore);
        result.setNScore(nScore);
        result.setTScore(tScore);
        result.setFScore(fScore);
        result.setJScore(jScore);
        result.setPScore(pScore);
        TestResult saved = resultRepository.save(result);
        // 异步发送结果通知（邮件未启用时自动降级为日志，不影响主流程）
        userRepository.findById(attempt.getUserId()).ifPresent(u ->
                notificationService.sendAssessmentCompleted(u, typeCode, personality.getTypeName()));
        return saved;
    }

    private String oppositeLetter(String letter) {
        return switch (letter) {
            case "E" -> "I";
            case "I" -> "E";
            case "S" -> "N";
            case "N" -> "S";
            case "T" -> "F";
            case "F" -> "T";
            case "J" -> "P";
            case "P" -> "J";
            default -> throw new BusinessException("题目选项维度配置错误");
        };
    }

    @Transactional(readOnly = true)
    public TestResult getResult(Long resultId, Long userId) {
        TestResult result = resultRepository.findById(resultId)
                .orElseThrow(() -> new BusinessException("测评结果不存在"));
        if (!result.getUserId().equals(userId)) {
            throw new BusinessException("无权查看该测评结果");
        }
        return result;
    }

    @Transactional(readOnly = true)
    public Page<TestResult> getMyResults(Long userId, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        return resultRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(safePage, safeSize));
    }

    private TestAttempt requireOwnedInProgressAttempt(Long attemptId, Long userId) {
        TestAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new BusinessException("测评会话不存在"));
        if (!attempt.getUserId().equals(userId)) {
            throw new BusinessException("无权访问该测评会话");
        }
        if (attempt.getStatus() != TestAttempt.AttemptStatus.IN_PROGRESS) {
            throw new BusinessException("测评会话已结束");
        }
        return attempt;
    }
}
