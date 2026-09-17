package com.debuglife.mbti.assessment.api;

import com.debuglife.mbti.assessment.dto.SubmitAnswersRequest;
import com.debuglife.mbti.assessment.entity.*;
import com.debuglife.mbti.assessment.service.AssessmentService;
import com.debuglife.mbti.auth.entity.User;
import com.debuglife.mbti.auth.repository.UserRepository;
import com.debuglife.mbti.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class AssessmentController {
    
    private final AssessmentService assessmentService;
    private final UserRepository userRepository;
    
    public AssessmentController(AssessmentService assessmentService, UserRepository userRepository) {
        this.assessmentService = assessmentService;
        this.userRepository = userRepository;
    }
    
    @PostMapping("/attempts")
    public ApiResponse<TestAttempt> startAttempt(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        TestAttempt attempt = assessmentService.startAttempt(user.getId());
        return ApiResponse.success(attempt);
    }
    
    @GetMapping("/attempts/{id}/questions")
    public ApiResponse<List<MbtiQuestion>> getQuestions(@PathVariable Long id) {
        List<MbtiQuestion> questions = assessmentService.getQuestions(id);
        return ApiResponse.success(questions);
    }
    
    @PostMapping("/attempts/{id}/answers")
    public ApiResponse<Void> submitAnswers(@PathVariable Long id, @Valid @RequestBody SubmitAnswersRequest request) {
        assessmentService.submitAnswers(id, request);
        return ApiResponse.success(null);
    }
    
    @PostMapping("/attempts/{id}/complete")
    public ApiResponse<TestResult> completeAttempt(@PathVariable Long id) {
        TestResult result = assessmentService.completeAttempt(id);
        return ApiResponse.success(result);
    }
    
    @GetMapping("/results/{id}")
    public ApiResponse<TestResult> getResult(@PathVariable Long id) {
        TestResult result = assessmentService.getResult(id);
        return ApiResponse.success(result);
    }
    
    @GetMapping("/results/my")
    public ApiResponse<Page<TestResult>> getMyResults(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        User user = userRepository.findByUsername(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        Page<TestResult> results = assessmentService.getMyResults(user.getId(), page, size);
        return ApiResponse.success(results);
    }
}
