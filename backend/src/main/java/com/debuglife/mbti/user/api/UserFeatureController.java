package com.debuglife.mbti.user.api;

import com.debuglife.mbti.common.api.ApiResponse;
import com.debuglife.mbti.common.exception.BusinessException;
import com.debuglife.mbti.user.dto.CareerAdviceResponse;
import com.debuglife.mbti.user.dto.CompatibilityRequest;
import com.debuglife.mbti.user.dto.CompatibilityResponse;
import com.debuglife.mbti.user.dto.GrowthPoint;
import com.debuglife.mbti.user.service.UserFeatureService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
public class UserFeatureController {

    private final UserFeatureService userFeatureService;

    public UserFeatureController(UserFeatureService userFeatureService) {
        this.userFeatureService = userFeatureService;
    }

    @GetMapping("/growth")
    public ApiResponse<List<GrowthPoint>> growth(@RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.success(userFeatureService.getGrowthTrack(currentUsername(), limit));
    }

    @PostMapping("/compatibility")
    public ApiResponse<CompatibilityResponse> compatibility(@Valid @RequestBody CompatibilityRequest request) {
        return ApiResponse.success(
                userFeatureService.getCompatibility(currentUsername(), request.getOtherUsername()));
    }

    @GetMapping("/career-advice")
    public ApiResponse<CareerAdviceResponse> careerAdvice() {
        return ApiResponse.success(userFeatureService.getCareerAdvice(currentUsername()));
    }

    @GetMapping(value = "/export", produces = "text/csv;charset=UTF-8")
    public ResponseEntity<byte[]> export() {
        String csv = userFeatureService.exportHistoryCsv(currentUsername());
        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"mbti-history.csv\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(bytes);
    }

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            throw new BusinessException("UNAUTHORIZED", "请先登录");
        }
        return authentication.getName();
    }
}
