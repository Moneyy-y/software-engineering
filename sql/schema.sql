SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS catering DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE catering;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
  `user_id` BIGINT NOT NULL AUTO_INCREMENT,
  `open_id` VARCHAR(64) DEFAULT NULL,
  `username` VARCHAR(50) DEFAULT NULL,
  `password` VARCHAR(100) DEFAULT NULL,
  `nickname` VARCHAR(50) DEFAULT '',
  `avatar` VARCHAR(255) DEFAULT '',
  `mobile` VARCHAR(64) DEFAULT NULL,
  `role` VARCHAR(20) NOT NULL DEFAULT 'student',
  `status` TINYINT DEFAULT 1,
  `protocol_agreed` TINYINT(1) DEFAULT 0 COMMENT '是否同意用户协议',
  `protocol_agreed_at` DATETIME DEFAULT NULL COMMENT '协议同意时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_open_id` (`open_id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 食堂/商铺
CREATE TABLE IF NOT EXISTS `shop` (
  `shop_id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `type` TINYINT NOT NULL DEFAULT 0 COMMENT '0食堂 1周边',
  `logo` VARCHAR(255) DEFAULT '',
  `address` VARCHAR(255) DEFAULT '',
  `lng` DECIMAL(10,6) DEFAULT NULL,
  `lat` DECIMAL(10,6) DEFAULT NULL,
  `phone` VARCHAR(20) DEFAULT '',
  `business_hours` VARCHAR(100) DEFAULT '',
  `avg_price` DECIMAL(10,2) DEFAULT 0.00,
  `avg_score` DECIMAL(2,1) DEFAULT 0.0,
  `status` TINYINT DEFAULT 1,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 档口
CREATE TABLE IF NOT EXISTS `stall` (
  `stall_id` BIGINT NOT NULL AUTO_INCREMENT,
  `shop_id` BIGINT NOT NULL,
  `name` VARCHAR(100) NOT NULL,
  `category` VARCHAR(50) DEFAULT '',
  `status` TINYINT DEFAULT 1,
  `sort_order` INT DEFAULT 0,
  PRIMARY KEY (`stall_id`),
  KEY `idx_shop_id` (`shop_id`),
  CONSTRAINT `fk_stall_shop` FOREIGN KEY (`shop_id`) REFERENCES `shop` (`shop_id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 菜品
CREATE TABLE IF NOT EXISTS `dish` (
  `dish_id` BIGINT NOT NULL AUTO_INCREMENT,
  `stall_id` BIGINT NOT NULL,
  `name` VARCHAR(100) NOT NULL,
  `price` DECIMAL(10,2) NOT NULL,
  `original_price` DECIMAL(10,2) DEFAULT NULL,
  `cover_image` VARCHAR(255) DEFAULT '',
  `images` VARCHAR(2000) DEFAULT '',
  `description` VARCHAR(500) DEFAULT '',
  `category` VARCHAR(50) DEFAULT '',
  `tags` VARCHAR(200) DEFAULT '',
  `avg_score` DECIMAL(2,1) DEFAULT 0.0,
  `review_count` INT DEFAULT 0,
  `sale_count` INT DEFAULT 0,
  `status` TINYINT DEFAULT 1,
  `board_status` VARCHAR(20) DEFAULT NULL COMMENT 'red上榜/red_remove黑榜移除/black上榜/black_remove红榜移除',
  `board_sort` INT DEFAULT NULL COMMENT '榜单内排序，数值越小越靠前',
  `board_hidden` TINYINT DEFAULT 0 COMMENT '1=在榜单中隐藏',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`dish_id`),
  KEY `idx_stall_id` (`stall_id`),
  KEY `idx_status_score` (`status`, `avg_score`),
  CONSTRAINT `fk_dish_stall` FOREIGN KEY (`stall_id`) REFERENCES `stall` (`stall_id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 评价
CREATE TABLE IF NOT EXISTS `review` (
  `review_id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` VARCHAR(128) NOT NULL,
  `dish_id` BIGINT NOT NULL,
  `shop_id` BIGINT DEFAULT NULL,
  `score` INT NOT NULL,
  `content` TEXT,
  `images` VARCHAR(2000) DEFAULT '',
  `is_anonymous` TINYINT(1) DEFAULT 1,
  `audit_status` VARCHAR(20) DEFAULT 'pending',
  `reject_reason` VARCHAR(255) DEFAULT NULL,
  `auditor_id` BIGINT DEFAULT NULL,
  `audit_time` DATETIME DEFAULT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`review_id`),
  KEY `idx_dish_status` (`dish_id`, `audit_status`),
  KEY `idx_audit_status` (`audit_status`),
  CONSTRAINT `fk_review_dish` FOREIGN KEY (`dish_id`) REFERENCES `dish` (`dish_id`) ON DELETE RESTRICT,
  CONSTRAINT `chk_score` CHECK (`score` BETWEEN 1 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 用户行为
CREATE TABLE IF NOT EXISTS `user_behavior` (
  `behavior_id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `dish_id` BIGINT NOT NULL,
  `action_type` VARCHAR(20) NOT NULL COMMENT 'view/favorite/review',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`behavior_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_dish_id` (`dish_id`),
  CONSTRAINT `fk_behavior_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`),
  CONSTRAINT `fk_behavior_dish` FOREIGN KEY (`dish_id`) REFERENCES `dish` (`dish_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 收藏
CREATE TABLE IF NOT EXISTS `user_favorite` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `dish_id` BIGINT NOT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_dish` (`user_id`, `dish_id`),
  CONSTRAINT `fk_fav_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`),
  CONSTRAINT `fk_fav_dish` FOREIGN KEY (`dish_id`) REFERENCES `dish` (`dish_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 反馈
CREATE TABLE IF NOT EXISTS `feedback` (
  `feedback_id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `type` VARCHAR(50) DEFAULT 'other',
  `description` VARCHAR(500) NOT NULL,
  `images` VARCHAR(2000) DEFAULT '',
  `status` VARCHAR(20) DEFAULT 'pending',
  `handler_id` BIGINT DEFAULT NULL,
  `reply` VARCHAR(500) DEFAULT NULL,
  `accept_time` DATETIME DEFAULT NULL,
  `resolve_time` DATETIME DEFAULT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`feedback_id`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_feedback_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 论坛帖子
CREATE TABLE IF NOT EXISTS `post` (
  `post_id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `title` VARCHAR(100) NOT NULL,
  `content` TEXT NOT NULL,
  `images` VARCHAR(2000) DEFAULT '',
  `zone` VARCHAR(50) DEFAULT 'general',
  `like_count` INT DEFAULT 0,
  `comment_count` INT DEFAULT 0,
  `audit_status` VARCHAR(20) DEFAULT 'pending',
  `reject_reason` VARCHAR(500) DEFAULT NULL COMMENT '拒绝原因',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`post_id`),
  KEY `idx_audit_status` (`audit_status`),
  CONSTRAINT `fk_post_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 评论
CREATE TABLE IF NOT EXISTS `comment` (
  `comment_id` BIGINT NOT NULL AUTO_INCREMENT,
  `post_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `content` VARCHAR(500) NOT NULL,
  `like_count` INT DEFAULT 0,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`comment_id`),
  KEY `idx_post_id` (`post_id`),
  CONSTRAINT `fk_comment_post` FOREIGN KEY (`post_id`) REFERENCES `post` (`post_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_comment_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 敏感词
CREATE TABLE IF NOT EXISTS `sensitive_word` (
  `word_id` BIGINT NOT NULL AUTO_INCREMENT,
  `content` VARCHAR(100) NOT NULL,
  `category` VARCHAR(50) DEFAULT 'default',
  `status` TINYINT DEFAULT 1,
  PRIMARY KEY (`word_id`),
  UNIQUE KEY `uk_content` (`content`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 浏览记录
CREATE TABLE IF NOT EXISTS `user_browse` (
  `browse_id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `dish_id` BIGINT,
  `post_id` BIGINT,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`browse_id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `fk_browse_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 消息
CREATE TABLE IF NOT EXISTS `message` (
  `message_id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `title` VARCHAR(100) NOT NULL,
  `content` TEXT NOT NULL,
  `type` VARCHAR(20) DEFAULT 'system',
  `related_type` VARCHAR(20) DEFAULT NULL COMMENT 'review/post/feedback',
  `related_id` BIGINT DEFAULT NULL,
  `dish_id` BIGINT DEFAULT NULL COMMENT '评价关联菜品',
  `is_read` BOOLEAN DEFAULT FALSE,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`message_id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `fk_message_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 举报表
CREATE TABLE IF NOT EXISTS `report` (
  `report_id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `target_type` VARCHAR(20) NOT NULL COMMENT 'post/review/comment',
  `target_id` BIGINT NOT NULL,
  `reason` VARCHAR(100) NOT NULL,
  `description` VARCHAR(500) DEFAULT '',
  `status` VARCHAR(20) DEFAULT 'pending',
  `handler_id` BIGINT DEFAULT NULL,
  `handle_result` VARCHAR(500) DEFAULT NULL,
  `handle_time` DATETIME DEFAULT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`report_id`),
  KEY `idx_status` (`status`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `fk_report_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 菜单表
CREATE TABLE IF NOT EXISTS `menu` (
  `menu_id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL,
  `path` VARCHAR(100) NOT NULL,
  `icon` VARCHAR(50) DEFAULT '',
  `parent_id` BIGINT DEFAULT 0,
  `sort_order` INT DEFAULT 0,
  `roles` VARCHAR(200) DEFAULT '',
  `status` TINYINT DEFAULT 1,
  PRIMARY KEY (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 角色菜单关联表
CREATE TABLE IF NOT EXISTS `role_menu` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `role` VARCHAR(20) NOT NULL,
  `menu_id` BIGINT NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_menu` (`role`, `menu_id`),
  CONSTRAINT `fk_role_menu_menu` FOREIGN KEY (`menu_id`) REFERENCES `menu` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 审计日志表
CREATE TABLE IF NOT EXISTS `audit_log` (
  `log_id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT,
  `username` VARCHAR(50),
  `operation` VARCHAR(100),
  `method` VARCHAR(200),
  `params` TEXT,
  `ip` VARCHAR(50),
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`log_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET FOREIGN_KEY_CHECKS = 1;
