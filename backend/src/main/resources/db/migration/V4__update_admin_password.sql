-- 更新 admin 用户密码为 BCrypt 加密
-- 原始密码: admin123
-- BCrypt 加密后: $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lKObHKyIG6BUJcwva
UPDATE sys_user 
SET password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lKObHKyIG6BUJcwva'
WHERE username = 'admin';
