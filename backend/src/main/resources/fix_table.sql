-- 删除重复的字段
ALTER TABLE ratings DROP COLUMN create_time;
ALTER TABLE ratings DROP COLUMN update_time;

-- 确保 created_at 和 updated_at 有默认值
ALTER TABLE ratings MODIFY COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE ratings MODIFY COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- 为已有数据设置默认值
UPDATE ratings SET created_at = CURRENT_TIMESTAMP WHERE created_at IS NULL;
UPDATE ratings SET updated_at = CURRENT_TIMESTAMP WHERE updated_at IS NULL;
