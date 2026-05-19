-- ========================================
-- 高校餐饮服务质量感知与推荐系统
-- 数据库重置脚本
-- ========================================
-- 说明：此脚本会完全清空并重建数据库
-- 使用方法：在 MySQL 客户端或 Workbench 中执行
-- ========================================

-- 1. 删除并重建数据库
DROP DATABASE IF EXISTS catering;
CREATE DATABASE catering CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE catering;

-- 2. 执行建表脚本
SOURCE d:/21425/xiaozhong/software-engineering/sql/schema.sql;

-- 3. 执行种子数据脚本
SOURCE d:/21425/xiaozhong/software-engineering/sql/seed.sql;

-- 4. 验证数据
SELECT '============== 数据验证 ==============' AS '验证信息';

-- 验证用户
SELECT '用户数据：' AS '信息';
SELECT user_id, username, nickname, role, status FROM user ORDER BY user_id;

-- 验证菜单
SELECT '菜单数据：' AS '信息';
SELECT menu_id, name, path, sort_order, status FROM menu ORDER BY menu_id;

-- 验证角色-菜单分配
SELECT '角色-菜单分配：' AS '信息';
SELECT role, GROUP_CONCAT(menu_id ORDER BY menu_id) AS menu_ids 
FROM role_menu 
GROUP BY role 
ORDER BY role;

-- 验证其他关键表
SELECT '菜品数量：' AS '统计', COUNT(*) AS '数量' FROM dish;
SELECT '食堂数量：' AS '统计', COUNT(*) AS '数量' FROM shop;
SELECT '评价数量：' AS '统计', COUNT(*) AS '数量' FROM review;

SELECT '============== 重置完成 ==============' AS '完成信息';
SELECT '请使用以下账号登录：' AS '提示';
SELECT '管理端 - admin / admin123' AS '管理员';
SELECT '管理端 - auditor / auditor123' AS '审核员';
