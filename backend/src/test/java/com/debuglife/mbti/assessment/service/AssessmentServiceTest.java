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
import com.debuglife.mbti.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssessmentServiceTest {

    private AssessmentService assessmentService;

    @Mock private MbtiQuestionRepository questionRepository;
    @Mock private TestAttemptRepository attemptRepository;
    @Mock private TestAnswerRepository answerRepository;
    @Mock private TestResultRepository resultRepository;
    @Mock private MbtiPersonalityRepository personalityRepository;

    @BeforeEach
    void setUp() {
        assessmentService = new AssessmentService(
                questionRepository, attemptRepository, answerRepository,
                resultRepository, personalityRepository);
    }

    @Test
    void startAttemptCreatesOwnedInProgressAttempt() {
        when(attemptRepository.save(any(TestAttempt.class))).thenAnswer(invocation -> {
            TestAttempt attempt = invocation.getArgument(0);
            attempt.setId(1L);
            return attempt;
        });

        TestAttempt result = assessmentService.startAttempt(42L);

        assertEquals(42L, result.getUserId());
        assertEquals(TestAttempt.AttemptStatus.IN_PROGRESS, result.getStatus());
        verify(attemptRepository).save(any(TestAttempt.class));
    }

    @Test
    void getQuestionsReturnsQuestionsForOwner() {
        TestAttempt attempt = inProgressAttempt(1L, 42L);
        MbtiQuestion question = new MbtiQuestion();
        question.setId(1L);
        question.setContent("Question 1");

        when(attemptRepository.findById(1L)).thenReturn(Optional.of(attempt));
        when(questionRepository.findAllByOrderBySortOrderAsc()).thenReturn(List.of(question));

        List<MbtiQuestion> results = assessmentService.getQuestions(1L, 42L);

        assertEquals(1, results.size());
        assertEquals("Question 1", results.get(0).getContent());
    }

    @Test
    void getQuestionsRejectsMissingAttempt() {
        when(attemptRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> assessmentService.getQuestions(1L, 42L));
    }

    @Test
    void getQuestionsRejectsCompletedAttempt() {
        TestAttempt attempt = inProgressAttempt(1L, 42L);
        attempt.setStatus(TestAttempt.AttemptStatus.COMPLETED);
        when(attemptRepository.findById(1L)).thenReturn(Optional.of(attempt));

        assertThrows(BusinessException.class, () -> assessmentService.getQuestions(1L, 42L));
    }

    @Test
    void getQuestionsRejectsDifferentUser() {
        when(attemptRepository.findById(1L)).thenReturn(Optional.of(inProgressAttempt(1L, 99L)));

        assertThrows(BusinessException.class, () -> assessmentService.getQuestions(1L, 42L));
    }

    @Test
    void submitAnswersPersistsAnswers() {
        TestAttempt attempt = inProgressAttempt(1L, 42L);
        MbtiQuestion question = new MbtiQuestion();
        question.setId(1L);
        SubmitAnswersRequest request = new SubmitAnswersRequest();
        request.setAnswers(List.of(new SubmitAnswersRequest.AnswerItem(1L, "a")));

        when(attemptRepository.findById(1L)).thenReturn(Optional.of(attempt));
        when(questionRepository.findAllByOrderBySortOrderAsc()).thenReturn(List.of(question));

        assessmentService.submitAnswers(1L, 42L, request);

        verify(answerRepository).deleteByAttemptId(1L);
        verify(answerRepository).save(any(TestAnswer.class));
    }

    @Test
    void submitAnswersRejectsDuplicateQuestions() {
        TestAttempt attempt = inProgressAttempt(1L, 42L);
        MbtiQuestion questionOne = new MbtiQuestion();
        questionOne.setId(1L);
        MbtiQuestion questionTwo = new MbtiQuestion();
        questionTwo.setId(2L);
        SubmitAnswersRequest request = new SubmitAnswersRequest();
        request.setAnswers(List.of(
                new SubmitAnswersRequest.AnswerItem(1L, "A"),
                new SubmitAnswersRequest.AnswerItem(1L, "B")));

        when(attemptRepository.findById(1L)).thenReturn(Optional.of(attempt));
        when(questionRepository.findAllByOrderBySortOrderAsc()).thenReturn(List.of(questionOne, questionTwo));

        assertThrows(BusinessException.class, () -> assessmentService.submitAnswers(1L, 42L, request));
    }

    @Test
    void completeAttemptCalculatesAndPersistsResult() {
        TestAttempt attempt = inProgressAttempt(1L, 42L);
        MbtiQuestion question = new MbtiQuestion();
        question.setId(1L);
        question.setDimensionId(1L);
        question.setAnswerType("E");
        TestAnswer answer = new TestAnswer();
        answer.setQuestionId(1L);
        answer.setAnswer("A");
        MbtiPersonality personality = new MbtiPersonality();
        personality.setId(1L);
        personality.setTypeCode("ESTJ");

        when(attemptRepository.findById(1L)).thenReturn(Optional.of(attempt));
        when(answerRepository.findByAttemptId(1L)).thenReturn(List.of(answer));
        when(questionRepository.findAllByOrderBySortOrderAsc()).thenReturn(List.of(question));
        when(personalityRepository.findByTypeCode("ESTJ")).thenReturn(Optional.of(personality));
        when(resultRepository.save(any(TestResult.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TestResult result = assessmentService.completeAttempt(1L, 42L);

        assertEquals("ESTJ", result.getTypeCode());
        assertEquals(1, result.getEScore());
        assertEquals(42L, result.getUserId());
        verify(attemptRepository).save(attempt);
        verify(resultRepository).save(any(TestResult.class));
    }

    @Test
    void completeAttemptReturnsExistingResult() {
        TestAttempt attempt = inProgressAttempt(1L, 42L);
        TestResult existing = new TestResult();
        existing.setId(9L);
        existing.setAttemptId(1L);
        existing.setUserId(42L);
        existing.setTypeCode("INTJ");

        when(attemptRepository.findById(1L)).thenReturn(Optional.of(attempt));
        when(resultRepository.findByAttemptId(1L)).thenReturn(Optional.of(existing));

        TestResult result = assessmentService.completeAttempt(1L, 42L);

        assertEquals(9L, result.getId());
        verify(resultRepository, times(0)).save(any(TestResult.class));
    }

    @Test
    void getResultReturnsOwnedResult() {
        TestResult result = new TestResult();
        result.setId(1L);
        result.setUserId(42L);
        result.setTypeCode("ENTJ");
        when(resultRepository.findById(1L)).thenReturn(Optional.of(result));

        assertEquals("ENTJ", assessmentService.getResult(1L, 42L).getTypeCode());
    }

    @Test
    void getResultRejectsDifferentUser() {
        TestResult result = new TestResult();
        result.setId(1L);
        result.setUserId(99L);
        when(resultRepository.findById(1L)).thenReturn(Optional.of(result));

        assertThrows(BusinessException.class, () -> assessmentService.getResult(1L, 42L));
    }

    @Test
    void getMyResultsUsesBoundedPagination() {
        TestResult result = new TestResult();
        result.setId(1L);
        Page<TestResult> page = new PageImpl<>(Collections.singletonList(result));
        when(resultRepository.findByUserIdOrderByCreatedAtDesc(42L, PageRequest.of(0, 100))).thenReturn(page);

        Page<TestResult> results = assessmentService.getMyResults(42L, -5, 1000);

        assertNotNull(results);
        assertEquals(1, results.getContent().size());
    }

    private TestAttempt inProgressAttempt(Long id, Long userId) {
        TestAttempt attempt = new TestAttempt();
        attempt.setId(id);
        attempt.setUserId(userId);
        attempt.setStatus(TestAttempt.AttemptStatus.IN_PROGRESS);
        attempt.setStartedAt(LocalDateTime.now());
        return attempt;
    }
}
