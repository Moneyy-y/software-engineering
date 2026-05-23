-- 用户表增加协议同意字段（已有库执行一次即可）
USE `catering`;

ALTER TABLE `user`
  ADD COLUMN `protocol_agreed` TINYINT(1) DEFAULT 0 COMMENT '是否同意用户协议' AFTER `status`,
  ADD COLUMN `protocol_agreed_at` DATETIME DEFAULT NULL COMMENT '协议同意时间' AFTER `protocol_agreed`;
