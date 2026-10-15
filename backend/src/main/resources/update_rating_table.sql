-- 更新评分表结构，添加时间戳字段

-- 添加 created_at 字段
ALTER TABLE ratings ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- 添加 updated_at 字段
ALTER TABLE ratings ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- 为已有数据设置默认值
UPDATE ratings SET created_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP;
