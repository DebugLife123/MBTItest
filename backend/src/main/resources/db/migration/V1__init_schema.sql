-- ============================================================
-- MBTI 职业性格测评系统 - 初始化表结构
-- 说明：本脚本与 JPA 实体严格对齐，spring.jpa.hibernate.ddl-auto=validate 可直接校验通过。
-- ============================================================

-- 1. 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id            BIGINT       PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username      VARCHAR(50)  NOT NULL UNIQUE            COMMENT '登录账号',
    password      VARCHAR(255) NOT NULL                   COMMENT 'BCrypt 密码散列',
    real_name     VARCHAR(50)  DEFAULT NULL               COMMENT '昵称/真实姓名',
    email         VARCHAR(100) DEFAULT NULL               COMMENT '邮箱',
    avatar        VARCHAR(255) DEFAULT NULL               COMMENT '头像地址',
    role          VARCHAR(20)  NOT NULL DEFAULT 'USER'    COMMENT '角色：STUDENT/TEACHER/USER/ADMIN',
    enabled       BIT(1)       NOT NULL DEFAULT b'1'      COMMENT '是否启用',
    last_login_at DATETIME     DEFAULT NULL               COMMENT '最后登录时间',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    updated_at    DATETIME     DEFAULT CURRENT_TIMESTAMP  COMMENT '更新时间',
    UNIQUE KEY uk_user_email (email),
    INDEX idx_user_create_time (create_time),
    INDEX idx_user_last_login (last_login_at),
    INDEX idx_user_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 性格维度定义表
CREATE TABLE IF NOT EXISTS mbti_dimension (
    id          BIGINT       PRIMARY KEY AUTO_INCREMENT COMMENT '维度ID',
    code        VARCHAR(4)   NOT NULL UNIQUE            COMMENT '维度代码：E/I,S/N,T/F,J/P',
    dim_name    VARCHAR(50)  NOT NULL                   COMMENT '维度名称',
    description TEXT         DEFAULT NULL               COMMENT '维度说明',
    status      INT          NOT NULL DEFAULT 1         COMMENT '状态：1-启用 0-禁用'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MBTI维度定义表';

-- 3. 16 种人格类型解析表
CREATE TABLE IF NOT EXISTS mbti_personality (
    id                 BIGINT       PRIMARY KEY AUTO_INCREMENT COMMENT '类型ID',
    type_code          VARCHAR(4)   NOT NULL UNIQUE            COMMENT '性格代码，如 INTJ',
    type_name          VARCHAR(100) NOT NULL                   COMMENT '性格称号，如 建筑师',
    description        TEXT         DEFAULT NULL               COMMENT '性格详细描述',
    strengths          TEXT         DEFAULT NULL               COMMENT '优势特征',
    weaknesses         TEXT         DEFAULT NULL               COMMENT '劣势/成长点',
    career_suggestions TEXT         DEFAULT NULL               COMMENT '职业发展建议'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='16种人格类型解析表';

-- 4. 题目表
CREATE TABLE IF NOT EXISTS mbti_question (
    id           BIGINT       PRIMARY KEY AUTO_INCREMENT COMMENT '题目ID',
    dimension_id BIGINT       NOT NULL                   COMMENT '所属维度ID',
    content      VARCHAR(500) NOT NULL                   COMMENT '题目内容',
    option_a     VARCHAR(200) NOT NULL                   COMMENT '选项A内容',
    option_b     VARCHAR(200) NOT NULL                   COMMENT '选项B内容',
    answer_type  VARCHAR(1)   NOT NULL                   COMMENT '选项A对应维度字母(E/I/S/N/T/F/J/P)',
    sort_order   INT          NOT NULL DEFAULT 0         COMMENT '排序序号',
    created_at   DATETIME     DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    updated_at   DATETIME     DEFAULT CURRENT_TIMESTAMP  COMMENT '更新时间',
    INDEX idx_question_dimension (dimension_id),
    INDEX idx_question_sort (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MBTI题目表';

-- 5. 测评会话表
CREATE TABLE IF NOT EXISTS test_attempt (
    id           BIGINT      PRIMARY KEY AUTO_INCREMENT COMMENT '会话ID',
    user_id      BIGINT      NOT NULL                   COMMENT '用户ID',
    status       VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS' COMMENT '状态：IN_PROGRESS/COMPLETED/ABANDONED',
    started_at   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
    completed_at DATETIME    DEFAULT NULL               COMMENT '完成时间',
    result_type  VARCHAR(4)  DEFAULT NULL               COMMENT '结果性格类型',
    INDEX idx_attempt_user_status (user_id, status),
    INDEX idx_attempt_started (started_at),
    CONSTRAINT fk_attempt_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测评会话表';

-- 6. 答题记录表
CREATE TABLE IF NOT EXISTS test_answer (
    id          BIGINT      PRIMARY KEY AUTO_INCREMENT COMMENT '答案ID',
    attempt_id  BIGINT      NOT NULL                   COMMENT '会话ID',
    question_id BIGINT      NOT NULL                   COMMENT '题目ID',
    answer      VARCHAR(1)  NOT NULL                   COMMENT '用户选择：A/B',
    created_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '作答时间',
    INDEX idx_answer_attempt (attempt_id),
    CONSTRAINT fk_answer_attempt FOREIGN KEY (attempt_id) REFERENCES test_attempt(id) ON DELETE CASCADE,
    CONSTRAINT fk_answer_question FOREIGN KEY (question_id) REFERENCES mbti_question(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='答题记录表';

-- 7. 测评结果表
CREATE TABLE IF NOT EXISTS test_record (
    id             BIGINT   PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    attempt_id     BIGINT   NOT NULL                   COMMENT '会话ID',
    user_id        BIGINT   NOT NULL                   COMMENT '用户ID',
    personality_id BIGINT   NOT NULL                   COMMENT '性格类型ID',
    result_type    VARCHAR(4) NOT NULL                 COMMENT '结果性格类型',
    e_score        INT      NOT NULL DEFAULT 0         COMMENT '外倾得分',
    i_score        INT      NOT NULL DEFAULT 0         COMMENT '内倾得分',
    s_score        INT      NOT NULL DEFAULT 0         COMMENT '实感得分',
    n_score        INT      NOT NULL DEFAULT 0         COMMENT '直觉得分',
    t_score        INT      NOT NULL DEFAULT 0         COMMENT '理性得分',
    f_score        INT      NOT NULL DEFAULT 0         COMMENT '情感得分',
    j_score        INT      NOT NULL DEFAULT 0         COMMENT '判断得分',
    p_score        INT      NOT NULL DEFAULT 0         COMMENT '感知得分',
    created_at     DATETIME DEFAULT CURRENT_TIMESTAMP  COMMENT '测试时间',
    UNIQUE KEY uk_record_attempt (attempt_id),
    INDEX idx_record_user_created (user_id, created_at),
    INDEX idx_record_type_created (result_type, created_at),
    CONSTRAINT fk_record_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_record_attempt FOREIGN KEY (attempt_id) REFERENCES test_attempt(id) ON DELETE CASCADE,
    CONSTRAINT fk_record_personality FOREIGN KEY (personality_id) REFERENCES mbti_personality(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测评结果表';

-- 8. 考核类型表
CREATE TABLE IF NOT EXISTS mbti_assessment_type (
    id        BIGINT       PRIMARY KEY AUTO_INCREMENT COMMENT '类型ID',
    type_name VARCHAR(100) NOT NULL                   COMMENT '类型名称',
    status    INT          NOT NULL DEFAULT 1         COMMENT '状态：1-在用 0-作废',
    price     DOUBLE       NOT NULL DEFAULT 0.0       COMMENT '价格'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考核类型表';

-- 9. 测试安排表
CREATE TABLE IF NOT EXISTS assessment_info (
    id          BIGINT       PRIMARY KEY AUTO_INCREMENT COMMENT '安排ID',
    title       VARCHAR(200) NOT NULL                   COMMENT '测试标题',
    purpose     VARCHAR(500) DEFAULT NULL               COMMENT '测试目的',
    content     TEXT         DEFAULT NULL               COMMENT '测试内容说明',
    format      VARCHAR(20)  NOT NULL                   COMMENT '形式：ONLINE/OFFLINE',
    test_time   DATETIME     DEFAULT NULL               COMMENT '预定测试时间',
    location    VARCHAR(200) DEFAULT NULL               COMMENT '线下地点',
    notes       TEXT         DEFAULT NULL               COMMENT '备注说明',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_assessment_test_time (test_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试安排表';

-- 10. 报名记录表
CREATE TABLE IF NOT EXISTS assessment_registration (
    assessment_id BIGINT   NOT NULL COMMENT '测试安排ID',
    user_id       BIGINT   NOT NULL COMMENT '用户ID',
    register_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
    PRIMARY KEY (assessment_id, user_id),
    INDEX idx_registration_user (user_id),
    CONSTRAINT fk_reg_assessment FOREIGN KEY (assessment_id) REFERENCES assessment_info(id) ON DELETE CASCADE,
    CONSTRAINT fk_reg_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报名记录表';
