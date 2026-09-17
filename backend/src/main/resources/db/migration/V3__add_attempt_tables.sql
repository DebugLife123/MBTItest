-- 创建测评会话表
CREATE TABLE IF NOT EXISTS test_attempt (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    started_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at DATETIME NULL,
    result_type VARCHAR(4) NULL,
    FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    INDEX idx_user_status (user_id, status),
    INDEX idx_started_at (started_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建答案记录表
CREATE TABLE IF NOT EXISTS test_answer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    attempt_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    answer VARCHAR(1) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (attempt_id) REFERENCES test_attempt(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES mbti_question(id),
    INDEX idx_attempt (attempt_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 更新测评结果表增加会话ID
ALTER TABLE test_record 
ADD COLUMN IF NOT EXISTS attempt_id BIGINT NULL AFTER id,
ADD INDEX idx_attempt (attempt_id);
