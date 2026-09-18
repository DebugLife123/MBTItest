package com.debuglife.mbti.ai.api;

import com.debuglife.mbti.ai.dto.TeamAnalysisRequest;
import com.debuglife.mbti.ai.dto.TeamAnalysisResponse;
import com.debuglife.mbti.ai.service.AiTeamAnalysisService;
import com.debuglife.mbti.common.api.ApiResponse;
import com.debuglife.mbti.common.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/ai")
@Tag(name = "AI 团队分析", description = "管理员基于真实测评类型分布生成团队画像")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAiController {

    private final AiTeamAnalysisService teamAnalysisService;

    public AdminAiController(AiTeamAnalysisService teamAnalysisService) {
        this.teamAnalysisService = teamAnalysisService;
    }

    @PostMapping("/team-analysis")
    @Operation(summary = "生成团队画像分析")
    public ApiResponse<TeamAnalysisResponse> analyze(@Valid @RequestBody TeamAnalysisRequest request) {
        return ApiResponse.success(teamAnalysisService.analyze(currentUsername(), request));
    }

    @GetMapping("/team-analysis/history")
    @Operation(summary = "获取团队分析历史")
    public ApiResponse<List<TeamAnalysisResponse>> history() {
        return ApiResponse.success(teamAnalysisService.history());
    }

    @GetMapping("/team-analysis/latest")
    @Operation(summary = "获取最近一次团队分析")
    public ApiResponse<TeamAnalysisResponse> latest() {
        return ApiResponse.success(teamAnalysisService.latest());
    }

    @GetMapping("/team-analysis/available-types")
    @Operation(summary = "获取可用于演示的真实测评类型")
    public ApiResponse<List<String>> availableTypes() {
        return ApiResponse.success(teamAnalysisService.availableTypes());
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
