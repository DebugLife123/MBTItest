package com.debuglife.mbti.admin.api;

import com.debuglife.mbti.admin.dto.*;
import com.debuglife.mbti.admin.service.AdminService;
import com.debuglife.mbti.assessment.entity.MbtiPersonality;
import com.debuglife.mbti.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "管理后台", description = "管理员功能接口，需要 ADMIN 角色")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    @Operation(summary = "获取用户列表", description = "分页查询用户，支持用户名/邮箱关键词搜索")
    public ApiResponse<Page<UserListItem>> getUserList(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        Sort.Direction sortDirection = "ASC".equalsIgnoreCase(direction)
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by(sortDirection, sortBy));
        return ApiResponse.success(adminService.getUserList(keyword, pageable));
    }

    @PutMapping("/users/{id}/status")
    @Operation(summary = "更新用户状态", description = "启用或禁用指定用户")
    public ApiResponse<Void> updateUserStatus(@PathVariable Long id, @RequestParam Boolean enabled) {
        adminService.updateUserStatus(id, enabled);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "删除用户", description = "删除用户及其测评记录，不可恢复")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ApiResponse.success(null);
    }

    @GetMapping("/statistics")
    @Operation(summary = "获取统计数据", description = "用户总数、活跃用户、测评总数与完成率")
    public ApiResponse<UserStatistics> getStatistics() {
        return ApiResponse.success(adminService.getStatistics());
    }

    @GetMapping("/personality-distribution")
    @Operation(summary = "获取性格分布", description = "各性格类型的人数与占比")
    public ApiResponse<List<PersonalityDistribution>> getPersonalityDistribution() {
        return ApiResponse.success(adminService.getPersonalityDistribution());
    }

    @GetMapping("/analytics/personality-distribution")
    @Operation(summary = "获取性格分布（分析路径）")
    public ApiResponse<List<PersonalityDistribution>> getPersonalityDistributionAnalytics() {
        return ApiResponse.success(adminService.getPersonalityDistribution());
    }

    @GetMapping("/analytics/completion-rate")
    @Operation(summary = "获取完成率趋势", description = "按天统计最近 N 天的会话完成率")
    public ApiResponse<List<CompletionRatePoint>> getCompletionRateTrend(
            @RequestParam(defaultValue = "14") int days) {
        return ApiResponse.success(adminService.getCompletionRateTrend(days));
    }

    @GetMapping("/questions")
    @Operation(summary = "获取题目列表", description = "按排序号返回全部题目")
    public ApiResponse<List<QuestionView>> getQuestions() {
        return ApiResponse.success(adminService.getQuestions());
    }

    @PostMapping("/questions")
    @Operation(summary = "创建题目")
    public ApiResponse<QuestionView> createQuestion(@Valid @RequestBody QuestionRequest request) {
        return ApiResponse.success(adminService.createQuestion(request));
    }

    @PutMapping("/questions/{id}")
    @Operation(summary = "更新题目")
    public ApiResponse<QuestionView> updateQuestion(@PathVariable Long id,
                                                    @Valid @RequestBody QuestionRequest request) {
        return ApiResponse.success(adminService.updateQuestion(id, request));
    }

    @DeleteMapping("/questions/{id}")
    @Operation(summary = "删除题目")
    public ApiResponse<Void> deleteQuestion(@PathVariable Long id) {
        adminService.deleteQuestion(id);
        return ApiResponse.success(null);
    }

    @GetMapping("/personalities")
    @Operation(summary = "获取性格类型列表")
    public ApiResponse<List<MbtiPersonality>> getPersonalities() {
        return ApiResponse.success(adminService.getPersonalities());
    }

    @PutMapping("/personalities/{typeCode}")
    @Operation(summary = "更新性格类型解析")
    public ApiResponse<MbtiPersonality> updatePersonality(@PathVariable String typeCode,
                                                          @Valid @RequestBody PersonalityRequest request) {
        return ApiResponse.success(adminService.updatePersonality(typeCode, request));
    }
}
