-- Migrated from the legacy project's sql/schema.sql.
-- Flyway manages the connected database; CREATE DATABASE and USE statements are intentionally removed.

-- ============================================================
-- MBTI 职业性格测评系统 - 数据库建表脚本
-- 数据库: mbti_db (UTF-8)
-- ============================================================

-- ============================================================
-- 1. 用户表
--    角色: STUDENT(学生) / TEACHER(教师) / ADMIN(管理员)
-- ============================================================
CREATE TABLE IF NOT EXISTS sys_user (
    id          INT             PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username    VARCHAR(50)     NOT NULL UNIQUE            COMMENT '登录账号',
    password    VARCHAR(100)    NOT NULL                   COMMENT '登录密码',
    real_name   VARCHAR(50)     DEFAULT NULL               COMMENT '真实姓名',
    role        VARCHAR(20)     NOT NULL DEFAULT 'STUDENT' COMMENT '角色：STUDENT/TEACHER/ADMIN',
    avatar      VARCHAR(255)    DEFAULT NULL               COMMENT '头像路径',
    create_time DATETIME        DEFAULT CURRENT_TIMESTAMP  COMMENT '注册时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================================
-- 2. 测试记录表
--    存储每次测评的 8 维度得分与最终性格类型
-- ============================================================
CREATE TABLE IF NOT EXISTS test_record (
    id          INT         PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    user_id     INT         NOT NULL                   COMMENT '用户ID',
    result_type VARCHAR(4)  NOT NULL                   COMMENT '性格类型(如 INTJ)',
    e_score     INT         DEFAULT 0                  COMMENT '外倾得分',
    i_score     INT         DEFAULT 0                  COMMENT '内倾得分',
    s_score     INT         DEFAULT 0                  COMMENT '实感得分',
    n_score     INT         DEFAULT 0                  COMMENT '直觉得分',
    t_score     INT         DEFAULT 0                  COMMENT '理性得分',
    f_score     INT         DEFAULT 0                  COMMENT '情感得分',
    j_score     INT         DEFAULT 0                  COMMENT '判断得分',
    p_score     INT         DEFAULT 0                  COMMENT '感知得分',
    test_time   DATETIME    DEFAULT CURRENT_TIMESTAMP  COMMENT '测试时间',

    INDEX idx_user_id (user_id),
    INDEX idx_test_time (test_time),
    CONSTRAINT fk_record_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试记录表';

-- ============================================================
-- 3. 题目表
--    每道题有两个选项，各对应一个 MBTI 维度
-- ============================================================
CREATE TABLE IF NOT EXISTS mbti_question (
    id              INT          PRIMARY KEY AUTO_INCREMENT COMMENT '题目ID',
    question_text   VARCHAR(500) NOT NULL                   COMMENT '题目内容',
    option_a        VARCHAR(200) NOT NULL                   COMMENT '选项A内容',
    option_a_type   VARCHAR(1)   NOT NULL                   COMMENT '选项A对应维度(E/I/S/N/T/F/J/P)',
    option_b        VARCHAR(200) NOT NULL                   COMMENT '选项B内容',
    option_b_type   VARCHAR(1)   NOT NULL                   COMMENT '选项B对应维度(E/I/S/N/T/F/J/P)',
    status          INT          DEFAULT 1                  COMMENT '状态：1-启用 0-禁用',

    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MBTI题目表';

-- ============================================================
-- 4. 性格类型解析表
--    16 种 MBTI 人格类型的详细描述与职业建议
-- ============================================================
CREATE TABLE IF NOT EXISTS mbti_personality (
    id            INT          PRIMARY KEY AUTO_INCREMENT COMMENT '类型ID',
    type_code     VARCHAR(4)   NOT NULL UNIQUE            COMMENT '性格代码(如 INTJ)',
    title         VARCHAR(50)  NOT NULL                   COMMENT '性格称号(如 建筑师)',
    description   TEXT         DEFAULT NULL               COMMENT '性格详细描述',
    career_advice TEXT         DEFAULT NULL               COMMENT '职业发展建议'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='16种人格类型解析表';

-- ============================================================
-- 5. 性格维度定义表
--    MBTI 四对维度(E-I/S-N/T-F/J-P)的说明
-- ============================================================
CREATE TABLE IF NOT EXISTS mbti_dimension (
    id          INT          PRIMARY KEY AUTO_INCREMENT COMMENT '维度ID',
    dim_name    VARCHAR(50)  NOT NULL                   COMMENT '维度名称(如 外倾E-内倾I)',
    description TEXT         DEFAULT NULL               COMMENT '维度详细说明',
    status      INT          DEFAULT 1                  COMMENT '状态：1-启用 0-禁用'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MBTI维度定义表';

-- ============================================================
-- 6. 考核类型表
--    测评的分类与定价
-- ============================================================
CREATE TABLE IF NOT EXISTS mbti_assessment_type (
    id        INT          PRIMARY KEY AUTO_INCREMENT COMMENT '类型ID',
    type_name VARCHAR(100) NOT NULL                   COMMENT '类型名称',
    status    INT          DEFAULT 1                  COMMENT '状态：1-在用 0-作废',
    price     DOUBLE       DEFAULT 0.0                COMMENT '价格'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考核类型表';

-- ============================================================
-- 7. 测试安排表
--    管理员创建的在线/线下测评计划
-- ============================================================
CREATE TABLE IF NOT EXISTS assessment_info (
    id          INT          PRIMARY KEY AUTO_INCREMENT COMMENT '安排ID',
    title       VARCHAR(200) NOT NULL                   COMMENT '测试标题',
    purpose     VARCHAR(500) DEFAULT NULL               COMMENT '测试目的',
    content     TEXT         DEFAULT NULL               COMMENT '测试内容说明',
    format      VARCHAR(20)  NOT NULL                   COMMENT '形式：ONLINE/OFFLINE',
    test_time   DATETIME     DEFAULT NULL               COMMENT '预定测试时间',
    location    VARCHAR(200) DEFAULT NULL               COMMENT '线下地点',
    notes       TEXT         DEFAULT NULL               COMMENT '备注说明',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',

    INDEX idx_test_time (test_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试安排表';

-- ============================================================
-- 8. 报名记录表
--    用户与测试安排的报名关系
-- ============================================================
CREATE TABLE IF NOT EXISTS assessment_registration (
    assessment_id INT      NOT NULL  COMMENT '测试安排ID',
    user_id       INT      NOT NULL  COMMENT '用户ID',
    register_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',

    PRIMARY KEY (assessment_id, user_id),
    INDEX idx_user_id (user_id),
    CONSTRAINT fk_reg_assessment FOREIGN KEY (assessment_id) REFERENCES assessment_info(id) ON DELETE CASCADE,
    CONSTRAINT fk_reg_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报名记录表';
