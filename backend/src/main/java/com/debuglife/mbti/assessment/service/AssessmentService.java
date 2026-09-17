package com.debuglife.mbti.assessment.service;

import com.debuglife.mbti.assessment.dto.SubmitAnswersRequest;
import com.debuglife.mbti.assessment.entity.*;
import com.debuglife.mbti.assessment.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AssessmentService {
    
    private final MbtiQuestionRepository questionRepository;
    private final TestAttemptRepository attemptRepository;
    private final TestAnswerRepository answerRepository;
    private final TestResultRepository resultRepository;
    private final MbtiPersonalityRepository personalityRepository;
    
    public AssessmentService(MbtiQuestionRepository questionRepository,
                           TestAttemptRepository attemptRepository,
                           TestAnswerRepository answerRepository,
                           TestResultRepository resultRepository,
                           MbtiPersonalityRepository personalityRepository) {
        this.questionRepository = questionRepository;
        this.attemptRepository = attemptRepository;
        this.answerRepository = answerRepository;
        this.resultRepository = resultRepository;
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
    
    public List<MbtiQuestion> getQuestions(Long attemptId) {
        TestAttempt attempt = attemptRepository.findById(attemptId)
            .orElseThrow(() -> new RuntimeException("测评会话不存在"));
        
        if (attempt.getStatus() != TestAttempt.AttemptStatus.IN_PROGRESS) {
            throw new RuntimeException("测评会话已结束");
        }
        
        return questionRepository.findAllByOrderBySortOrderAsc();
    }
    
    @Transactional
    public void submitAnswers(Long attemptId, SubmitAnswersRequest request) {
        TestAttempt attempt = attemptRepository.findById(attemptId)
            .orElseThrow(() -> new RuntimeException("测评会话不存在"));
        
        if (attempt.getStatus() != TestAttempt.AttemptStatus.IN_PROGRESS) {
            throw new RuntimeException("测评会话已结束");
        }
        
        answerRepository.deleteByAttemptId(attemptId);
        
        for (SubmitAnswersRequest.AnswerItem item : request.getAnswers()) {
            TestAnswer answer = new TestAnswer();
            answer.setAttemptId(attemptId);
            answer.setQuestionId(item.getQuestionId());
            answer.setAnswer(item.getAnswer());
            answerRepository.save(answer);
        }
    }
    
    @Transactional
    public TestResult completeAttempt(Long attemptId) {
        TestAttempt attempt = attemptRepository.findById(attemptId)
            .orElseThrow(() -> new RuntimeException("测评会话不存在"));
        
        if (attempt.getStatus() != TestAttempt.AttemptStatus.IN_PROGRESS) {
            throw new RuntimeException("测评会话已结束");
        }
        
        List<TestAnswer> answers = answerRepository.findByAttemptId(attemptId);
        if (answers.isEmpty()) {
            throw new RuntimeException("未找到答案记录");
        }
        
        Map<Long, MbtiQuestion> questionMap = questionRepository.findAllById(
            answers.stream().map(TestAnswer::getQuestionId).collect(Collectors.toList())
        ).stream().collect(Collectors.toMap(MbtiQuestion::getId, q -> q));
        
        int eScore = 0, iScore = 0, sScore = 0, nScore = 0;
        int tScore = 0, fScore = 0, jScore = 0, pScore = 0;
        
        for (TestAnswer answer : answers) {
            MbtiQuestion question = questionMap.get(answer.getQuestionId());
            if (question == null) continue;
            
            boolean isCorrect = answer.getAnswer().equals(question.getAnswerType());
            Long dimId = question.getDimensionId();
            
            if (dimId == 1) { if (isCorrect) eScore++; else iScore++; }
            else if (dimId == 2) { if (isCorrect) iScore++; else eScore++; }
            else if (dimId == 3) { if (isCorrect) sScore++; else nScore++; }
            else if (dimId == 4) { if (isCorrect) nScore++; else sScore++; }
            else if (dimId == 5) { if (isCorrect) tScore++; else fScore++; }
            else if (dimId == 6) { if (isCorrect) fScore++; else tScore++; }
            else if (dimId == 7) { if (isCorrect) jScore++; else pScore++; }
            else if (dimId == 8) { if (isCorrect) pScore++; else jScore++; }
        }
        
        String typeCode = 
            (eScore >= iScore ? "E" : "I") +
            (sScore >= nScore ? "S" : "N") +
            (tScore >= fScore ? "T" : "F") +
            (jScore >= pScore ? "J" : "P");
        
        MbtiPersonality personality = personalityRepository.findByTypeCode(typeCode)
            .orElseThrow(() -> new RuntimeException("性格类型不存在: " + typeCode));
        
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
        
        return resultRepository.save(result);
    }
    
    public TestResult getResult(Long resultId) {
        return resultRepository.findById(resultId)
            .orElseThrow(() -> new RuntimeException("测评结果不存在"));
    }
    
    public Page<TestResult> getMyResults(Long userId, int page, int size) {
        return resultRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size));
    }
}
