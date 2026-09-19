package com.debuglife.mbti.common.audit;

import java.time.LocalDateTime;

/** 管理端审计日志视图对象 */
public record AuditLogView(
        Long id,
        Long userId,
        String username,
        String action,
        String detail,
        String httpMethod,
        String path,
        String ip,
        Boolean success,
        String errorMessage,
        Long costMs,
        LocalDateTime createdAt) {

    public static AuditLogView from(AuditLog log) {
        return new AuditLogView(log.getId(), log.getUserId(), log.getUsername(), log.getAction(),
                log.getDetail(), log.getHttpMethod(), log.getPath(), log.getIp(), log.getSuccess(),
                log.getErrorMessage(), log.getCostMs(), log.getCreatedAt());
    }
}
