package com.debuglife.mbti.admin.api;

import com.debuglife.mbti.common.api.ApiResponse;
import com.debuglife.mbti.common.audit.AuditLog;
import com.debuglife.mbti.common.audit.AuditLogRepository;
import com.debuglife.mbti.common.audit.AuditLogView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端操作审计查询：仅 ADMIN 可访问。
 */
@RestController
@RequestMapping("/api/v1/admin/audit-logs")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "管理后台-审计日志", description = "用户操作审计记录查询（仅管理员）")
public class AdminAuditController {

    private final AuditLogRepository repository;

    public AdminAuditController(AuditLogRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @Operation(summary = "分页查询审计日志", description = "支持按操作类型或操作人模糊筛选，默认按时间倒序")
    public ApiResponse<Page<AuditLogView>> list(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AuditLog> result;
        if (action != null && !action.isBlank()) {
            result = repository.findByActionContainingIgnoreCase(action.trim(), pageable);
        } else if (username != null && !username.isBlank()) {
            result = repository.findByUsernameContainingIgnoreCase(username.trim(), pageable);
        } else {
            result = repository.findAll(pageable);
        }
        return ApiResponse.success(result.map(AuditLogView::from));
    }
}
