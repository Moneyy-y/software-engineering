-- 红黑榜人工管理：置顶排序、隐藏
ALTER TABLE `dish`
  ADD COLUMN `board_sort` INT DEFAULT NULL COMMENT '榜单内排序，数值越小越靠前' AFTER `board_status`,
  ADD COLUMN `board_hidden` TINYINT DEFAULT 0 COMMENT '1=在榜单中隐藏' AFTER `board_sort`;
