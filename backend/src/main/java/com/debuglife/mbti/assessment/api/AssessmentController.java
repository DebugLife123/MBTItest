package com.debuglife.mbti.assessment.api;

import com.debuglife.mbti.assessment.dto.SubmitAnswersRequest;
import com.debuglife.mbti.assessment.entity.MbtiQuestion;
import com.debuglife.mbti.assessment.entity.TestAttempt;
import com.debuglife.mbti.assessment.entity.TestResult;
import com.debuglife.mbti.assessment.service.AssessmentService;
import com.debuglife.mbti.auth.entity.User;
import com.debuglife.mbti.auth.repository.UserRepository;
import com.debuglife.mbti.common.api.ApiResponse;
import com.debuglife.mbti.common.audit.AuditAction;
import com.debuglife.mbti.common.exception.BusinessException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    public ApiResponse<TestAttempt> startAttempt() {
        return ApiResponse.success(assessmentService.startAttempt(currentUserId()));
    }

    @GetMapping("/attempts/{id}/questions")
    public ApiResponse<List<MbtiQuestion>> getQuestions(@PathVariable Long id) {
        return ApiResponse.success(assessmentService.getQuestions(id, currentUserId()));
    }

    @PostMapping("/attempts/{id}/answers")
    public ApiResponse<Void> submitAnswers(@PathVariable Long id,
                                           @Valid @RequestBody SubmitAnswersRequest request) {
        assessmentService.submitAnswers(id, currentUserId(), request);
        return ApiResponse.success(null);
    }

    @AuditAction(value = "ASSESSMENT_COMPLETE", description = "提交并完成测评")
    @PostMapping("/attempts/{id}/complete")
    public ApiResponse<TestResult> completeAttempt(@PathVariable Long id) {
        return ApiResponse.success(assessmentService.completeAttempt(id, currentUserId()));
    }

    @GetMapping("/results/{id}")
    public ApiResponse<TestResult> getResult(@PathVariable Long id) {
        return ApiResponse.success(assessmentService.getResult(id, currentUserId()));
    }

    @GetMapping("/results/my")
    public ApiResponse<Page<TestResult>> getMyResults(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(assessmentService.getMyResults(currentUserId(), page, size));
    }

    private Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            throw new BusinessException("UNAUTHORIZED", "请先登录");
        }
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
        return user.getId();
    }
}
