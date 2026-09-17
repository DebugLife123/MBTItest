package com.debuglife.mbti.assessment.api;

import com.debuglife.mbti.assessment.dto.SubmitAnswersRequest;
import com.debuglife.mbti.assessment.entity.MbtiQuestion;
import com.debuglife.mbti.assessment.entity.TestAttempt;
import com.debuglife.mbti.assessment.entity.TestResult;
import com.debuglife.mbti.assessment.service.AssessmentService;
import com.debuglife.mbti.auth.entity.User;
import com.debuglife.mbti.auth.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AssessmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AssessmentService assessmentService;

    @MockitoBean
    private UserRepository userRepository;

    private UsernamePasswordAuthenticationToken auth;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(42L);
        user.setUsername("testuser");
        user.setPassword("encoded");
        user.setRole(User.UserRole.USER);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        auth = new UsernamePasswordAuthenticationToken(
                "testuser", null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void startAttemptReturnsAttempt() throws Exception {
        TestAttempt attempt = new TestAttempt();
        attempt.setId(7L);
        attempt.setUserId(42L);
        attempt.setStatus(TestAttempt.AttemptStatus.IN_PROGRESS);
        when(assessmentService.startAttempt(42L)).thenReturn(attempt);

        mockMvc.perform(post("/api/v1/attempts").with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value(7));
    }

    @Test
    void getQuestionsReturnsQuestionList() throws Exception {
        MbtiQuestion question = new MbtiQuestion();
        question.setId(1L);
        question.setContent("Question");
        question.setSortOrder(1);
        when(assessmentService.getQuestions(eq(7L), eq(42L))).thenReturn(List.of(question));

        mockMvc.perform(get("/api/v1/attempts/7/questions").with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    void submitAnswersReturnsSuccess() throws Exception {
        SubmitAnswersRequest request = new SubmitAnswersRequest();
        request.setAnswers(List.of(new SubmitAnswersRequest.AnswerItem(1L, "A")));
        doNothing().when(assessmentService).submitAnswers(eq(7L), eq(42L), any(SubmitAnswersRequest.class));

        mockMvc.perform(post("/api/v1/attempts/7/answers")
                        .with(authentication(auth))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void completeAttemptReturnsResult() throws Exception {
        TestResult result = new TestResult();
        result.setId(99L);
        result.setTypeCode("INTJ");
        when(assessmentService.completeAttempt(7L, 42L)).thenReturn(result);

        mockMvc.perform(post("/api/v1/attempts/7/complete").with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(99));
    }
}
