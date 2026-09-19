-- ============================================================
-- V4: 系统用户操作审计日志表
-- 记录敏感操作（登录、登出、测评提交、管理端变更、AI 调用等）
-- ============================================================
CREATE TABLE IF NOT EXISTS sys_audit_log (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id       BIGINT       NULL COMMENT '操作用户 ID（未登录场景为 NULL）',
    username      VARCHAR(64)  NULL COMMENT '操作人用户名快照',
    action        VARCHAR(64)  NOT NULL COMMENT '操作类型，如 AUTH_LOGIN / ADMIN_UPDATE_USER',
    detail        VARCHAR(1024) NULL COMMENT '操作详情（脱敏后的参数摘要）',
    http_method   VARCHAR(8)   NOT NULL COMMENT 'HTTP 方法',
    path          VARCHAR(255) NOT NULL COMMENT '请求路径',
    ip            VARCHAR(64)  NULL COMMENT '客户端 IP',
    success       BIT          NOT NULL DEFAULT 1 COMMENT '是否成功',
    error_message VARCHAR(512) NULL COMMENT '失败原因（成功时为 NULL）',
    cost_ms       BIGINT       NULL COMMENT '处理耗时（毫秒）',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发生时间',
    PRIMARY KEY (id),
    KEY idx_audit_user (user_id),
    KEY idx_audit_action (action),
    KEY idx_audit_created (created_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户操作审计日志';
