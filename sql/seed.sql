USE catering;

-- 管理员 admin / admin123 (BCrypt)
INSERT INTO `user` (`username`, `password`, `nickname`, `role`, `status`) VALUES
('admin', '$2a$10$tZkB.MW5C/gqiujshTxLOuDQoOEkgxdAKZkGHSj58y2N.zZCHHAUi', '系统管理员', 'admin', 1);

-- 测试学生用户
INSERT INTO `user` (`open_id`, `nickname`, `avatar`, `role`, `status`) VALUES
('mock_openid_001', '测试同学', 'https://thirdwx.qlogo.cn/mmopen/vi_32/default.png', 'student', 1),
('mock_openid_002', '美食达人', 'https://thirdwx.qlogo.cn/mmopen/vi_32/default.png', 'student', 1);

-- 食堂
INSERT INTO `shop` (`name`, `type`, `address`, `lng`, `lat`, `avg_price`, `avg_score`, `business_hours`, `status`) VALUES
('第一食堂', 0, '校园东区第一食堂', 116.397128, 39.916527, 15.00, 4.2, '06:30-21:00', 1),
('第二食堂', 0, '校园西区第二食堂', 116.395000, 39.914000, 18.00, 4.0, '07:00-20:30', 1),
('学府快餐', 1, '南门商业街12号', 116.398500, 39.913000, 22.00, 3.8, '10:00-22:00', 1);

-- 档口
INSERT INTO `stall` (`shop_id`, `name`, `category`, `status`, `sort_order`) VALUES
(1, '川味窗口', '特色小炒', 1, 1),
(1, '面食坊', '面食粥粉', 1, 2),
(1, '快餐档', '快餐便当', 1, 3),
(2, '清真餐厅', '特色小炒', 1, 1),
(2, '奶茶饮品', '奶茶饮品', 1, 2),
(3, '汉堡炸鸡', '小吃炸串', 1, 1);

-- 菜品
INSERT INTO `dish` (`stall_id`, `name`, `price`, `cover_image`, `description`, `category`, `avg_score`, `review_count`, `sale_count`, `status`) VALUES
(1, '宫保鸡丁盖饭', 12.00, '/uploads/demo/gongbao.jpg', '经典川味，微辣下饭', '特色小炒', 4.5, 28, 320, 1),
(1, '鱼香肉丝', 11.00, '/uploads/demo/yuxiang.jpg', '酸甜可口', '特色小炒', 4.3, 15, 210, 1),
(1, '麻婆豆腐', 8.00, '/uploads/demo/mapo.jpg', '麻辣鲜香', '特色小炒', 4.0, 12, 180, 1),
(2, '牛肉拉面', 14.00, '/uploads/demo/lamian.jpg', '汤浓面劲', '面食粥粉', 4.6, 35, 450, 1),
(2, '酸辣粉', 9.00, '/uploads/demo/suanla.jpg', '酸辣开胃', '面食粥粉', 4.2, 20, 280, 1),
(3, '鸡腿饭套餐', 15.00, '/uploads/demo/jitui.jpg', '荤素搭配', '快餐便当', 3.8, 18, 200, 1),
(3, '红烧肉饭', 13.00, '/uploads/demo/hongshao.jpg', '肥而不腻', '快餐便当', 4.1, 10, 150, 1),
(4, '大盘鸡', 18.00, '/uploads/demo/dapanji.jpg', '份量足', '特色小炒', 4.7, 22, 190, 1),
(5, '珍珠奶茶', 8.00, '/uploads/demo/naicha.jpg', '经典口味', '奶茶饮品', 4.4, 40, 520, 1),
(5, '杨枝甘露', 12.00, '/uploads/demo/yangzhi.jpg', '夏日首选', '奶茶饮品', 4.5, 25, 300, 1),
(6, '香辣鸡腿堡', 16.00, '/uploads/demo/hanbao.jpg', '外酥里嫩', '小吃炸串', 3.5, 8, 120, 1),
(6, '鸡米花', 10.00, '/uploads/demo/jimihua.jpg', '小食分享', '小吃炸串', 3.2, 5, 80, 1);

-- 已通过评价
INSERT INTO `review` (`user_id`, `dish_id`, `shop_id`, `score`, `content`, `is_anonymous`, `audit_status`, `create_time`) VALUES
('enc_1', 1, 1, 5, '宫保鸡丁很下饭，分量足，推荐！', 1, 'approved', DATE_SUB(NOW(), INTERVAL 2 DAY)),
('enc_2', 1, 1, 4, '味道不错，略咸', 1, 'approved', DATE_SUB(NOW(), INTERVAL 1 DAY)),
('enc_1', 4, 1, 5, '牛肉面汤头很鲜，面条劲道', 1, 'approved', DATE_SUB(NOW(), INTERVAL 3 DAY)),
('enc_2', 4, 1, 5, '食堂最好吃的面！', 1, 'approved', DATE_SUB(NOW(), INTERVAL 1 DAY)),
('enc_1', 8, 2, 5, '大盘鸡份量超大，性价比高', 1, 'approved', DATE_SUB(NOW(), INTERVAL 2 DAY)),
('enc_2', 9, 2, 4, '奶茶甜度刚好', 1, 'approved', NOW()),
('enc_1', 11, 3, 2, '汉堡偏干，不太推荐', 1, 'approved', DATE_SUB(NOW(), INTERVAL 1 DAY)),
('enc_2', 12, 3, 2, '鸡米花有点油腻', 1, 'approved', DATE_SUB(NOW(), INTERVAL 2 DAY));

-- 待审核评价
INSERT INTO `review` (`user_id`, `dish_id`, `shop_id`, `score`, `content`, `is_anonymous`, `audit_status`, `create_time`) VALUES
('enc_1', 2, 1, 4, '鱼香肉丝味道正宗，下次还来', 1, 'pending', NOW());

-- 用户行为
INSERT INTO `user_behavior` (`user_id`, `dish_id`, `action_type`) VALUES
(2, 1, 'view'), (2, 1, 'favorite'), (2, 4, 'view'), (2, 4, 'review'),
(3, 4, 'view'), (3, 8, 'favorite'), (3, 9, 'view');

-- 收藏
INSERT INTO `user_favorite` (`user_id`, `dish_id`) VALUES (2, 1), (2, 4), (3, 8);

-- 反馈
INSERT INTO `feedback` (`user_id`, `type`, `description`, `status`) VALUES
(2, 'hygiene', '第一食堂二楼餐具清洗不够干净', 'pending'),
(2, 'price', '第二食堂部分菜品涨价未公示', 'processing');

-- 敏感词
INSERT INTO `sensitive_word` (`content`, `category`) VALUES
('广告', 'spam'), ('刷单', 'spam'), ('违法', 'illegal');

-- 论坛帖子
INSERT INTO `post` (`user_id`, `title`, `content`, `zone`, `audit_status`, `like_count`) VALUES
(2, '第一食堂必吃榜', '强烈推荐牛肉拉面和宫保鸡丁！', 'recommend', 'approved', 15),
(3, '避雷指南', '南门炸鸡店最近品质下降', 'warning', 'approved', 8);
