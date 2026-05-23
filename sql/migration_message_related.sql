-- 消息表增加跳转关联字段（已有库执行一次即可）
-- 若报 Error 1046，请先选中数据库：左侧双击 catering，或取消下行注释
USE `catering`;

ALTER TABLE `message`
  ADD COLUMN `related_type` VARCHAR(20) DEFAULT NULL COMMENT 'review/post/feedback' AFTER `type`,
  ADD COLUMN `related_id` BIGINT DEFAULT NULL AFTER `related_type`,
  ADD COLUMN `dish_id` BIGINT DEFAULT NULL COMMENT '评价关联菜品' AFTER `related_id`;
