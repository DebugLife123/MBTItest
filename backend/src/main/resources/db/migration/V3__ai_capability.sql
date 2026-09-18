-- ============================================================
-- AI 能力模块：用户咨询会话、消息与管理员团队分析记录
-- ============================================================

CREATE TABLE IF NOT EXISTS ai_chat_session (
    id               BIGINT       PRIMARY KEY AUTO_INCREMENT COMMENT 'AI 会话ID',
    user_id          BIGINT       NOT NULL                   COMMENT '所属用户ID',
    title            VARCHAR(120) NOT NULL                   COMMENT '会话标题',
    personality_type VARCHAR(4)   DEFAULT NULL               COMMENT '会话关联的MBTI类型',
    provider         VARCHAR(40)  NOT NULL                   COMMENT '模型提供方',
    model            VARCHAR(120) NOT NULL                   COMMENT '模型名称',
    status           VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE'  COMMENT '状态：ACTIVE/ARCHIVED',
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_ai_session_user_updated (user_id, updated_at),
    CONSTRAINT fk_ai_session_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 咨询会话表';

CREATE TABLE IF NOT EXISTS ai_chat_message (
    id         BIGINT      PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
    session_id BIGINT      NOT NULL                   COMMENT '会话ID',
    role       VARCHAR(20) NOT NULL                   COMMENT '角色：USER/ASSISTANT/SYSTEM',
    content    LONGTEXT    NOT NULL                   COMMENT '消息内容',
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_ai_message_session_created (session_id, created_at),
    CONSTRAINT fk_ai_message_session FOREIGN KEY (session_id) REFERENCES ai_chat_session(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 咨询消息表';

CREATE TABLE IF NOT EXISTS ai_team_analysis (
    id            BIGINT       PRIMARY KEY AUTO_INCREMENT COMMENT '团队分析ID',
    admin_user_id BIGINT       NOT NULL                   COMMENT '发起分析的管理员ID',
    total_members INT          NOT NULL                   COMMENT '参与分析人数',
    type_counts   TEXT         NOT NULL                   COMMENT '纳入分析的类型分布JSON',
    focus         VARCHAR(500) DEFAULT NULL               COMMENT '分析关注点',
    result        LONGTEXT     NOT NULL                   COMMENT 'AI团队分析报告',
    provider      VARCHAR(40)  NOT NULL                   COMMENT '模型提供方',
    model         VARCHAR(120) NOT NULL                   COMMENT '模型名称',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_ai_team_admin_created (admin_user_id, created_at),
    CONSTRAINT fk_ai_team_admin FOREIGN KEY (admin_user_id) REFERENCES sys_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI团队分析记录表';