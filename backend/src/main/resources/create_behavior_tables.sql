-- 用户行为日志表
CREATE TABLE IF NOT EXISTS user_behavior_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    behavior_type VARCHAR(20) NOT NULL COMMENT 'CLICK,VIEW,RATING,COLLECTION,SEARCH,NOTE',
    target_id BIGINT NOT NULL,
    target_info VARCHAR(500) COMMENT '额外信息',
    session_id VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_behavior_type (behavior_type),
    INDEX idx_created_at (created_at)
) COMMENT='用户行为日志表';

-- 用户偏好表
CREATE TABLE IF NOT EXISTS user_preferences (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    favorite_genres VARCHAR(500) COMMENT '喜欢的类型',
    favorite_directors VARCHAR(500) COMMENT '喜欢的导演',
    favorite_actors VARCHAR(500) COMMENT '喜欢的演员',
    INDEX idx_user_id (user_id)
) COMMENT='用户偏好表';
